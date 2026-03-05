package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.abc.web.util.FileUploadUtil;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.IData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 文件管理控制器
 * 提供文件上传、下载、删除等完整功能
 */
@RestController
@RequestMapping("/api/file")
public class FileController extends WebController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * 通用文件上传接口
     */
    @PostMapping("/upload")
    public Object uploadFile(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "type", defaultValue = "image") String type,
                            @RequestParam(value = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                            HttpServletRequest request, HttpServletResponse response) {

        try {
            FileUploadUtil.FileType fileType = getFileType(type);

            // 使用工具类处理文件上传
            FileUploadUtil.UploadResult uploadResult = fileUploadUtil.uploadFile(file, categoryId, fileType);

            // 写入数据库
            BaseDao dao = BaseDao.getDao("");
            IData attachmentInfo = new IData();
            attachmentInfo.set("category_id", uploadResult.getCategoryId());
            attachmentInfo.set("storage_mode", 1); // 本地存储
            attachmentInfo.set("origin_name", uploadResult.getOriginalName());
            attachmentInfo.set("object_name", uploadResult.getNewFileName());
            attachmentInfo.set("hash", uploadResult.getHash());
            attachmentInfo.set("mime_type", uploadResult.getMimeType());
            attachmentInfo.set("storage_path", uploadResult.getFilePath());
            attachmentInfo.set("suffix", uploadResult.getExtension());
            attachmentInfo.set("size_byte", uploadResult.getSizeByte());
            attachmentInfo.set("size_info", uploadResult.getSizeInfo());
            attachmentInfo.set("url", uploadResult.getUrl());
            attachmentInfo.set("create_time", dao.getSysTimeLocal());
            attachmentInfo.set("update_time", dao.getSysTimeLocal());
            attachmentInfo.setTableName("sa_system_attachment");

            dao.insert(attachmentInfo);

            // 返回成功信息
            IData result = new IData();
            result.set("id", attachmentInfo.get("id"));
            result.set("url", uploadResult.getUrl());
            result.set("relativeUrl", "/imgs/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + uploadResult.getNewFileName());
            result.set("fileName", uploadResult.getNewFileName());
            result.set("originalName", uploadResult.getOriginalName());
            result.set("size", uploadResult.getSizeInfo());
            result.set("hash", uploadResult.getHash());
            result.set("fileType", type);

            return R.ok("文件上传成功", result);

        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 批量文件上传
     */
    @PostMapping("/upload/batch")
    public Object batchUpload(@RequestParam("files") MultipartFile[] files,
                             @RequestParam(value = "type", defaultValue = "image") String type,
                             @RequestParam(value = "categoryId", required = false, defaultValue = "0") Integer categoryId) {
        List<Object> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            try {
                MultipartFile file = files[i];
                Object result = uploadFile(file, type, categoryId, null, null);
                results.add(result);
            } catch (Exception e) {
                errors.add("第" + (i + 1) + "个文件上传失败: " + e.getMessage());
            }
        }

        IData response = new IData();
        response.set("successCount", results.size());
        response.set("errorCount", errors.size());
        response.set("results", results);
        response.set("errors", errors);

        if (errors.isEmpty()) {
            return R.ok("批量上传完成", response);
        } else {
            return R.fail("部分文件上传失败", response);
        }
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable Long id, HttpServletResponse response) {
        try {
            BaseDao dao = BaseDao.getDao("");
            IData fileInfo = dao.queryByFirst("SELECT * FROM sa_system_attachment WHERE id = ?", id);

            if (fileInfo == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String filePath = fileInfo.getString("storage_path") + fileInfo.getString("object_name");
            File file = new File(filePath);

            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // 设置响应头
            response.setContentType(fileInfo.getString("mime_type"));
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileInfo.getString("origin_name") + "\"");
            response.setContentLength((int) file.length());

            // 写入文件内容
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{id}")
    public Object getFileInfo(@PathVariable Long id) {
        try {
            BaseDao dao = BaseDao.getDao("");
            IData fileInfo = dao.queryByFirst("SELECT * FROM sa_system_attachment WHERE id = ?", id);

            if (fileInfo == null) {
                return R.fail("文件不存在");
            }

            return R.ok(fileInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{id}")
    public Object deleteFile(@PathVariable Long id) {
        try {
            BaseDao dao = BaseDao.getDao("");
            IData fileInfo = dao.queryByFirst("SELECT * FROM sa_system_attachment WHERE id = ?", id);

            if (fileInfo == null) {
                return R.fail("文件不存在");
            }

            // 删除物理文件
            String filePath = fileInfo.getString("storage_path") + fileInfo.getString("object_name");
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }

            // 删除数据库记录
            dao.delete("sa_system_attachment", "id", id);

            return R.ok("文件删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public Object getFileList(@RequestParam(value = "categoryId", required = false) Integer categoryId,
                             @RequestParam(value = "type", required = false) String type,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "size", defaultValue = "20") Integer size) {
        try {
            BaseDao dao = BaseDao.getDao("");
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM sa_system_attachment WHERE 1=1");

            List<Object> params = new ArrayList<>();

            if (categoryId != null) {
                sql.append(" AND category_id = ?");
                params.add(categoryId);
            }

            if (type != null && !type.isEmpty()) {
                sql.append(" AND suffix IN (");
                List<String> extensions = getAllowedExtensions(type);
                for (int i = 0; i < extensions.size(); i++) {
                    sql.append(i > 0 ? ",?" : "?");
                    params.add(extensions.get(i));
                }
                sql.append(")");
            }

            sql.append(" ORDER BY create_time DESC");

            // 简单分页实现
            int offset = (page - 1) * size;
            sql.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);

            Object[] paramArray = params.toArray();
            // 这里需要根据您的DAO框架调整查询方法
            // IDataset list = dao.queryList(sql.toString(), paramArray);

            IData result = new IData();
            // result.set("list", list);
            result.set("page", page);
            result.set("size", size);
            // result.set("total", getTotalCount(dao, categoryId, type));

            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据类型获取文件类型枚举
     */
    private FileUploadUtil.FileType getFileType(String type) {
        switch (type.toLowerCase()) {
            case "image":
                return FileUploadUtil.FileType.IMAGE;
            case "document":
                return FileUploadUtil.FileType.DOCUMENT;
            case "audio":
                return FileUploadUtil.FileType.AUDIO;
            case "video":
                return FileUploadUtil.FileType.VIDEO;
            default:
                return FileUploadUtil.FileType.IMAGE;
        }
    }

    /**
     * 根据类型获取允许的扩展名
     */
    private List<String> getAllowedExtensions(String type) {
        switch (type.toLowerCase()) {
            case "image":
                return java.util.Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg");
            case "document":
                return java.util.Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
            case "audio":
                return java.util.Arrays.asList("mp3", "wav", "flac", "aac", "ogg");
            case "video":
                return java.util.Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv");
            default:
                return new ArrayList<>();
        }
    }
}
