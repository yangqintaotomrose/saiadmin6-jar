package com.abc.web.util;

import com.abc.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Component
public class FileUploadUtil {

    // 从配置文件读取基础路径
    @Value("${file.upload.path:/data/imgs/}")
    private String basePath;
    
    @Autowired
    private AppConfig appConfig;
    
    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = 
        Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
    
    // 允许的文档格式
    private static final List<String> ALLOWED_DOCUMENT_EXTENSIONS = 
        Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    
    // 允许的音频格式
    private static final List<String> ALLOWED_AUDIO_EXTENSIONS = 
        Arrays.asList("mp3", "wav", "flac", "aac", "ogg");
    
    // 允许的视频格式
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = 
        Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");
    
    // 文件大小限制（50MB）
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    @PostConstruct
    public void init() {
        // 确保基础目录存在
        File baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    /**
     * 上传图片文件
     */
    public UploadResult uploadImage(MultipartFile file, Integer categoryId) throws Exception {
        return uploadFile(file, categoryId, FileType.IMAGE);
    }
    
    /**
     * 上传文档文件
     */
    public UploadResult uploadDocument(MultipartFile file, Integer categoryId) throws Exception {
        return uploadFile(file, categoryId, FileType.DOCUMENT);
    }
    
    /**
     * 上传音频文件
     */
    public UploadResult uploadAudio(MultipartFile file, Integer categoryId) throws Exception {
        return uploadFile(file, categoryId, FileType.AUDIO);
    }
    
    /**
     * 上传视频文件
     */
    public UploadResult uploadVideo(MultipartFile file, Integer categoryId) throws Exception {
        return uploadFile(file, categoryId, FileType.VIDEO);
    }
    
    /**
     * 通用文件上传方法
     */
    public UploadResult uploadFile(MultipartFile file, Integer categoryId, FileType fileType) throws Exception {
        // 检查文件是否为空
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过" + formatFileSize(MAX_FILE_SIZE));
        }

        // 获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        // 检查文件扩展名
        String extension = getFileExtension(originalFilename);
        if (!isAllowedExtension(extension, fileType)) {
            throw new IllegalArgumentException("不支持的文件格式，该类型仅支持: " + getAllowedExtensions(fileType));
        }

        // 生成新的文件名
        String newFileName = generateUniqueFileName(originalFilename);
        
        // 创建日期目录
        String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String fullPath = basePath + dateDir + "/";
        
        // 创建目录
        File directory = new File(fullPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 保存文件
        Path filePath = Paths.get(fullPath + newFileName);
        Files.write(filePath, file.getBytes());

        // 计算文件hash
        String hash = calculateFileHash(file.getBytes());

        // 获取文件信息
        String mimeType = file.getContentType();
        long sizeByte = file.getSize();
        String sizeInfo = formatFileSize(sizeByte);
        String relativeUrl = "/imgs/" + dateDir + "/" + newFileName;
        String url = appConfig.buildFullUrl(relativeUrl);

        // 构建返回结果
        UploadResult result = new UploadResult();
        result.setOriginalName(originalFilename);
        result.setNewFileName(newFileName);
        result.setFilePath(fullPath);
        result.setUrl(url);
        result.setRelativeUrl(relativeUrl);
        result.setHash(hash);
        result.setMimeType(mimeType);
        result.setSizeByte(sizeByte);
        result.setSizeInfo(sizeInfo);
        result.setExtension(extension);
        result.setCategoryId(categoryId);

        return result;
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 检查是否为允许的文件扩展名
     */
    public boolean isAllowedExtension(String extension, FileType fileType) {
        List<String> allowedExtensions = getAllowedExtensionsList(fileType);
        return allowedExtensions.stream()
                .anyMatch(allowed -> allowed.equalsIgnoreCase(extension));
    }
    
    /**
     * 获取指定类型的允许扩展名列表
     */
    private List<String> getAllowedExtensionsList(FileType fileType) {
        switch (fileType) {
            case IMAGE:
                return ALLOWED_IMAGE_EXTENSIONS;
            case DOCUMENT:
                return ALLOWED_DOCUMENT_EXTENSIONS;
            case AUDIO:
                return ALLOWED_AUDIO_EXTENSIONS;
            case VIDEO:
                return ALLOWED_VIDEO_EXTENSIONS;
            default:
                return ALLOWED_IMAGE_EXTENSIONS;
        }
    }
    
    /**
     * 获取指定类型的允许扩展名字符串
     */
    private String getAllowedExtensions(FileType fileType) {
        return String.join(", ", getAllowedExtensionsList(fileType));
    }

    /**
     * 生成唯一的文件名
     */
    public String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String timestamp = String.valueOf(System.currentTimeMillis());
        return uuid + "_" + timestamp + "." + extension;
    }

    /**
     * 计算文件hash
     */
    public String calculateFileHash(byte[] fileBytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(fileBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 格式化文件大小
     */
    public String formatFileSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2fKB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2fMB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2fGB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 文件类型枚举
     */
    public enum FileType {
        IMAGE, DOCUMENT, AUDIO, VIDEO
    }
    
    /**
     * 上传结果封装类
     */
    public static class UploadResult {
        private String originalName;
        private String newFileName;
        private String filePath;
        private String url;
        private String relativeUrl;
        private String hash;
        private String mimeType;
        private long sizeByte;
        private String sizeInfo;
        private String extension;
        private Integer categoryId;

        // getters and setters
        public String getOriginalName() { return originalName; }
        public void setOriginalName(String originalName) { this.originalName = originalName; }
        
        public String getNewFileName() { return newFileName; }
        public void setNewFileName(String newFileName) { this.newFileName = newFileName; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getHash() { return hash; }
        public void setHash(String hash) { this.hash = hash; }
        
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        
        public long getSizeByte() { return sizeByte; }
        public void setSizeByte(long sizeByte) { this.sizeByte = sizeByte; }
        
        public String getSizeInfo() { return sizeInfo; }
        public void setSizeInfo(String sizeInfo) { this.sizeInfo = sizeInfo; }
        
        public String getExtension() { return extension; }
        public void setExtension(String extension) { this.extension = extension; }
        
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        
        public String getRelativeUrl() { return relativeUrl; }
        public void setRelativeUrl(String relativeUrl) { this.relativeUrl = relativeUrl; }
    }
}