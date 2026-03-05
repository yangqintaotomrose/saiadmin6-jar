package com.abc.web.controller.common;

import com.abc.bean.TreeBuilder;
import com.abc.web.controller.WebController;
import com.abc.web.domain.HttpStatus;
import com.abc.web.domain.R;
import com.abc.web.util.FileUploadUtil;
import com.abc.web.util.SaTokenUtil;
import com.abc.web.util.LoginHelper;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 系统控制器 - 处理系统相关功能，包括文件上传等
 */
@RestController
@RequestMapping("/proxy/core/system/")
public class SystemController extends WebController {

    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Autowired
    private SaTokenUtil saTokenUtil;

    /**
     * 通用方法：获取当前登录用户对象
     * @return 当前登录用户信息，未登录时返回null
     */
    private IData getCurrentUser() {
        return LoginHelper.getCurrentUser();
    }

    /**
     * 通用方法：获取当前登录用户ID
     * @return 当前登录用户ID，未登录时返回null
     */
    private Long getCurrentUserId() {
        return LoginHelper.getUserId();
    }

    /**
     * 通用方法：检查用户是否已登录
     * @return true表示已登录，false表示未登录
     */
    private boolean isUserLoggedIn() {
        return LoginHelper.isLogin();
    }
    @RequestMapping(value = "getResourceCategory")
    public Object getResourceCategory() throws IOException {
        IData param = getRequestParam();
        param.set("limit", 1000);
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_category t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.category_name LIKE CONCAT('%', :categoryName, '%') ");
        parser.addSQL(" AND t.parent_id = :parentId ");
        parser.addSQL(" AND t.status = :status ");
        parser.addSQL(" AND t.delete_time IS NULL ");
        Pagination page =  this.getSinglePage(param);
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("label", obj.getString("category_name"));
            obj.set("value", obj.getInt("id"));

            obj.set("create_time", obj.getString("create_time"));
            obj.set("update_time", obj.getString("update_time"));
        });
        List<IData> root = TreeBuilder.buildTreeStructure(list, "id", "parent_id");

        return successResponse(root);
    }
    @RequestMapping(value = "getResourceList")
    public Object getResourceList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*, c.category_name");
        parser.addSQL(" FROM sa_system_attachment t ");
        parser.addSQL(" LEFT JOIN sa_system_category c ON t.category_id = c.id ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.origin_name LIKE CONCAT('%', :origin_name, '%') ");
        parser.addSQL(" AND t.object_name LIKE CONCAT('%', :object_name, '%') ");
        parser.addSQL(" AND t.hash = :hash ");
        parser.addSQL(" AND t.mime_type = :mimeType ");
        parser.addSQL(" AND t.category_id = :category_id ");
        parser.addSQL(" AND t.storage_mode = :storage_mode ");
        parser.addSQL(" AND t.delete_time IS NULL ");
        parser.addSQL(" ORDER BY t.create_time DESC ");
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time", obj.getString("create_time"));
            obj.set("update_time", obj.getString("update_time"));
        });

        return wrapPageQueryList(list, page);
    }



    @RequestMapping(value = "accessRole")
    public Object accessRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_role t ");
        parser.addSQL(" WHERE 1=1 and status=1");
        IDataset list = dao.queryPage(parser, param,this.getSinglePage(param));
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });

        return successResponse(list);
    }

    //查询用户信息
    @RequestMapping(value = "user")
    public Object user(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");

        // 使用通用方法获取当前用户
        IData currentUser = getCurrentUser();
        if (currentUser == null) {
            return R.fail("用户未登录");
        }

        Long userId = getCurrentUserId();
        System.out.println("当前登录用户：" + userId);
        IData user = dao.queryByFirst("SELECT * FROM sa_system_user WHERE id=? AND delete_time IS NULL", userId);
        // 根据部门ID查询部门
        IData department = dao.queryByFirst("SELECT * FROM sa_system_dept WHERE id=?", user.getInt("dept_id"));
        // 设置部门
        user.set("department", department);
        //设置按钮权限
        IDataset buttons = new IDataset();
        buttons.add("*");
        user.set("buttons",buttons);
        IDataset roles = new IDataset();
        roles.add("super_admin");
        user.set("roles",roles);

        return successResponse(user);
    }

    //查询字典
    @RequestMapping(value = "dictAll")
    public Object dictAll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");

        // 1. 查询字典类型表 sa_system_dict_type
        String typeSql = "SELECT t.* FROM sa_system_dict_type t " +
                        "WHERE t.status = 1 AND t.delete_time IS NULL " +
                        "ORDER BY t.id ASC";
        IDataset typeList = dao.queryList(typeSql);

        // 2. 查询字典数据表 sa_system_dict_data
        String dataSql = "SELECT d.*, t.code as type_code, t.name as type_name " +
                        "FROM sa_system_dict_data d " +
                        "LEFT JOIN sa_system_dict_type t ON d.type_id = t.id " +
                        "WHERE d.status = 1 AND d.delete_time IS NULL " +
                        "AND t.status = 1 AND t.delete_time IS NULL " +
                        "ORDER BY d.sort ASC, d.id ASC";
        IDataset dataList = dao.queryList(dataSql);

        // 3. 构建返回数据结构
        JSONArray result = new JSONArray();
        IData dict = new IData();
        // 按字典类型分组字典数据
        for (int i = 0; i < typeList.size(); i++) {
            IData type = typeList.getData(i);
            dict.set(type.getString("code"), IDataHepler.getSubDataset(dataList,"type_id",type.getString("id")));
        }
        return successResponse(dict);
    }

    @RequestMapping(value = "menu")
    public Object menu(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        param.set("limit",1000);
        BaseDao dao = BaseDao.getDao("");

        // 检查是否为超级管理员
        boolean isSuperAdmin = LoginHelper.isSuperAdmin();

        IDataset list;

        if (isSuperAdmin) {
            // 超级管理员返回所有菜单
            SQLParser parser = new SQLParser(param);
            parser.addSQL(" SELECT t.*");
            parser.addSQL(" FROM sa_system_menu t ");
            parser.addSQL(" WHERE 1=1 and type in(1,2,4)");
            parser.addSQL(" AND t.is_del = :isDel ");
            parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
            parser.addSQL(" AND t.parent_id LIKE CONCAT('%', :parentId, '%') ");
            parser.addSQL(" AND t.principal LIKE CONCAT('%', :principal, '%') ");
            parser.addSQL(" AND t.phone LIKE CONCAT('%', :phone, '%') ");
            parser.addSQL(" AND t.email LIKE CONCAT('%', :email, '%') ");
            parser.addSQL(" AND t.status = :status ");
            parser.addSQL(" AND t.remark LIKE CONCAT('%', :remark, '%') ");
            Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
            list = dao.queryPage(parser, param,page);
        } else {
            // 普通用户按角色权限过滤菜单
            Long[] roleIds = LoginHelper.getUserRoleIds();
            if (roleIds == null || roleIds.length == 0) {
                // 无角色用户返回空菜单
                list = new IDataset();
            } else {
                SQLParser parser = new SQLParser(param);
                parser.addSQL(" SELECT DISTINCT t.*");
                parser.addSQL(" FROM sa_system_menu t ");
                parser.addSQL(" INNER JOIN sa_system_role_menu rm ON t.id = rm.menu_id ");
                parser.addSQL(" WHERE 1=1 and type in(1,2,4)");
                parser.addSQL(" AND rm.role_id IN (" + String.join(",", java.util.Arrays.stream(roleIds).map(String::valueOf).toArray(String[]::new)) + ")");
                parser.addSQL(" AND t.is_del = :isDel ");
                parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
                parser.addSQL(" AND t.parent_id LIKE CONCAT('%', :parentId, '%') ");
                parser.addSQL(" AND t.principal LIKE CONCAT('%', :principal, '%') ");
                parser.addSQL(" AND t.phone LIKE CONCAT('%', :phone, '%') ");
                parser.addSQL(" AND t.email LIKE CONCAT('%', :email, '%') ");
                parser.addSQL(" AND t.status = :status ");
                parser.addSQL(" AND t.remark LIKE CONCAT('%', :remark, '%') ");
                Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
                list = dao.queryPage(parser, param,page);
            }
        }
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            if("1".equals(obj.getString("type"))){
                obj.set("component","/index/index");
            }
            IData meta = new IData();
            obj.set("meta",meta);
            meta.set("title",obj.getString("name"));
            meta.set("icon",obj.getString("icon"));
            meta.set("isIframe","1".equals(obj.getString("is_iframe")));
            meta.set("keepAlive","1".equals(obj.getString("is_keep_alive")));
            meta.set("isHide","1".equals(obj.getString("is_hidden")));
            meta.set("fixedTab","1".equals(obj.getString("is_fixed_tab")));
            meta.set("isFullPage","1".equals(obj.getString("is_full_page")));
            if("4".equals(obj.getString("type"))){
                obj.set("path","/outside/Iframe");
            //    meta.set("link",obj.getString("link_url"));
            }

            obj.set("create_time",obj.getString("create_time"));
            //obj.set("status","1".equals(obj.getString("status"))?1:0);

        });
        List<IData> root = TreeBuilder.buildTreeStructure(list,"id","parent_id");

        return successResponse(root);

    }


    @RequestMapping(value = "loginChart")
    public Object loginChart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        //todo 待实现

        return successResponse(null);

    }
    @RequestMapping(value = "loginBarChart")
    public Object loginBarChart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        //todo 待实现

        return successResponse(null);

    }
    @RequestMapping(value = "statistics")
    public Object statistics(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        //todo 待实现

        return successResponse(null);

    }




    /**
     * 图片上传
     */
    @RequestMapping(value = "uploadImage")
    public Object uploadImage(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "category_id", required = false, defaultValue = "0") Integer categoryId,
                             HttpServletRequest request, HttpServletResponse response) {
        return uploadFile(file, categoryId, FileUploadUtil.FileType.IMAGE);
    }

    /**
     * 文档上传
     */
    @RequestMapping(value = "uploadDocument")
    public Object uploadDocument(@RequestParam("file") MultipartFile file,
                                @RequestParam(value = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                                HttpServletRequest request, HttpServletResponse response) {
        return uploadFile(file, categoryId, FileUploadUtil.FileType.DOCUMENT);
    }

    /**
     * 音频上传
     */
    @RequestMapping(value = "uploadAudio")
    public Object uploadAudio(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                             HttpServletRequest request, HttpServletResponse response) {
        return uploadFile(file, categoryId, FileUploadUtil.FileType.AUDIO);
    }

    /**
     * 视频上传
     */
    @RequestMapping(value = "uploadVideo")
    public Object uploadVideo(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "categoryId", required = false, defaultValue = "0") Integer categoryId,
                             HttpServletRequest request, HttpServletResponse response) {
        return uploadFile(file, categoryId, FileUploadUtil.FileType.VIDEO);
    }

    /**
     * 通用文件上传方法
     */
    private Object uploadFile(MultipartFile file, Integer categoryId, FileUploadUtil.FileType fileType) {
        try {
            // 使用工具类处理文件上传
            FileUploadUtil.UploadResult uploadResult = null;

            switch (fileType) {
                case IMAGE:
                    uploadResult = fileUploadUtil.uploadImage(file, categoryId);
                    break;
                case DOCUMENT:
                    uploadResult = fileUploadUtil.uploadDocument(file, categoryId);
                    break;
                case AUDIO:
                    uploadResult = fileUploadUtil.uploadAudio(file, categoryId);
                    break;
                case VIDEO:
                    uploadResult = fileUploadUtil.uploadVideo(file, categoryId);
                    break;
            }

            if (uploadResult == null) {
                return R.fail("不支持的文件类型");
            }

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
            result.set("fileType", fileType.name().toLowerCase());

            return R.ok(getSuccessMessage(fileType) + "上传成功", result);

        } catch (IllegalArgumentException e) {
            return R.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取成功消息
     */
    private String getSuccessMessage(FileUploadUtil.FileType fileType) {
        switch (fileType) {
            case IMAGE: return "图片";
            case DOCUMENT: return "文档";
            case AUDIO: return "音频";
            case VIDEO: return "视频";
            default: return "文件";
        }
    }


}
