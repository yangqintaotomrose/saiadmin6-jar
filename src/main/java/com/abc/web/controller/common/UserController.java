package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.HttpStatus;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户信息控制器 - 根据用户信息表结构生成，保持与PostController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/user/")
public class UserController extends WebController {

    /**
     * 查询用户信息列表
     */
    @RequestMapping(value = "index")
    public Object index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*, d.name as dept_name");
        parser.addSQL(" FROM sa_system_user t ");
        parser.addSQL(" LEFT JOIN sa_system_dept d ON t.dept_id = d.id ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.username LIKE CONCAT('%', :username, '%') ");
        parser.addSQL(" AND t.realname LIKE CONCAT('%', :realname, '%') ");
        parser.addSQL(" AND t.gender = :gender ");
        parser.addSQL(" AND t.dept_id = :dept_id ");
        parser.addSQL(" AND t.is_super = :isSuper ");
        parser.addSQL(" AND t.status = :status ");
        parser.addSQL(" AND t.delete_time IS NULL ");
        parser.addSQL(" ORDER BY t.id DESC ");
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time", obj.getString("create_time"));
            obj.set("update_time", obj.getString("update_time"));
            // 日期格式化去除毫秒数
            obj.set("login_time", obj.getString("login_time") == null ?null:obj.getString("login_time").replaceAll("\\.\\d+$", ""));
            // 设置部门名称，如果为空则显示"未分配"
            obj.set("dept_name", obj.getString("dept_name") != null ? obj.getString("dept_name") : "未分配");
        });

        return wrapPageQueryList(list, page);
    }

    /**
     * 获取启用的用户列表（用于下拉选择等场景）
     */
    @RequestMapping(value = "accessUser")
    public Object accessUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.id, t.username, t.realname, t.avatar");
        parser.addSQL(" FROM sa_system_user t ");
        parser.addSQL(" WHERE 1=1 and t.status=1 and t.delete_time IS NULL");
        parser.addSQL(" ORDER BY t.id DESC ");
        IDataset list = dao.queryPage(parser, param, this.getSinglePage(param));
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time", obj.getString("create_time"));
        });

        return successResponse(list);
    }

    /**
     * 获取用户信息详情
     */
    @RequestMapping(value = "read")
    public Object read(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_system_user WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("用户信息不存在");
        }
        IData deptList = dao.queryByFirst("SELECT * FROM sa_system_dept WHERE id=? AND delete_time IS NULL", one.getLong("dept_id"));
        one.set("deptList",deptList);
        IDataset roleList = dao.queryList("SELECT * FROM sa_system_role WHERE id in (select role_id from sa_system_user_role where user_id=?)", one.getLong("id"));
        one.set("roleList",roleList);
        IDataset postList = dao.queryList("SELECT * FROM sa_system_post WHERE id in (select post_id from sa_system_user_post where user_id=?)", one.getLong("id"));
        one.set("postList",postList);
        return R.ok(one);
    }

    /**
     * 添加用户信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        JSONArray post_ids = (JSONArray) param.getObj("post_ids");
        JSONArray role_ids = (JSONArray) param.getObj("role_ids");
        BaseDao dao = BaseDao.getDao("");

        // 检查用户名唯一性（排除当前用户）
        String username = param.getString("username");
        IData existUser = dao.queryByFirst("SELECT id FROM sa_system_user WHERE username=?  AND delete_time IS NULL", username);
        if (existUser != null) {
            return R.fail("用户名已存在");
        }

        param.remove("post_ids");
        param.remove("role_ids");
        param.remove("password_confirm");
        param.set("update_time", dao.getSysTimeLocal());
        param.setTableName("sa_system_user");
        long id = dao.insertExt(param);

        //新增岗位
        for (int i = 0; i < post_ids.size(); i++) {
            Integer a = post_ids.getInteger(i);
            IData updateData = new IData();
            updateData.set("user_id", id);
            updateData.set("post_id", a);
            updateData.setTableName("sa_system_user_post");
            dao.insertExt(updateData);
        }
        //新增角色
        for (int i = 0; i < role_ids.size(); i++) {
            Integer a = role_ids.getInteger(i);
            IData updateData = new IData();
            updateData.set("user_id", id);
            updateData.set("role_id", a);
            updateData.setTableName("sa_system_user_role");
            dao.insertExt(updateData);
        }

        return R.ok("新增用户信息成功");
    }

    /**
     * 编辑用户信息
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");

        // 检查用户名唯一性（排除当前用户）
        String username = param.getString("username");
        Long id = param.getLong("id");
        IData existUser = dao.queryByFirst("SELECT id FROM sa_system_user WHERE username=? AND id!=? AND delete_time IS NULL", username, id);
        if (existUser != null) {
            return R.fail("用户名已存在");
        }
        dao.execSql("DELETE FROM sa_system_user_post WHERE user_id=?", id);
        //新增岗位
        JSONArray post_ids = (JSONArray) param.getObj("post_ids");
        for (int i = 0; i < post_ids.size(); i++) {
            Integer a = post_ids.getInteger(i);
            IData updateData = new IData();
            updateData.set("user_id", id);
            updateData.set("post_id", a);
            updateData.setTableName("sa_system_user_post");
            dao.insertExt(updateData);
        }
        dao.execSql("DELETE FROM sa_system_user_role WHERE user_id=?", id);
        //新增角色
        JSONArray role_ids = (JSONArray) param.getObj("role_ids");
        for (int i = 0; i < role_ids.size(); i++) {
            Integer a = role_ids.getInteger(i);
            IData updateData = new IData();
            updateData.set("user_id", id);
            updateData.set("role_id", a);
            updateData.setTableName("sa_system_user_role");
            dao.insertExt(updateData);
        }
        param.remove("post_ids");
        param.remove("role_ids");
        param.remove("password");
        param.remove("password_confirm");
        param.set("update_time", dao.getSysTimeLocal());
        param.setTableName("sa_system_user");
        dao.updateById(param);


        return R.ok("用户信息修改成功");
    }

    /**
     * 删除用户信息（软删除）
     */
    @RequestMapping(value = "destroy")
    public Object delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids = (JSONArray) param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger(i);
            IData updateData = new IData();
            updateData.set("id", a);
            updateData.set("delete_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_system_user");
            dao.updateById(updateData);
        }
        return R.ok("用户信息删除成功");
    }

    /**
     * 批量删除用户信息（软删除）
     */
    @RequestMapping(value = "deletes")
    public Object deletes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            IData updateData = new IData();
            updateData.set("id", Integer.parseInt(id));
            updateData.set("delete_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_system_user");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的用户信息
     */
    @RequestMapping(value = "restore")
    public Object restore(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            IData updateData = new IData();
            updateData.set("id", Integer.parseInt(id));
            updateData.set("delete_time", null);
            updateData.setTableName("sa_system_user");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }

    /**
     * 重置用户密码
     */
    @RequestMapping(value = "resetPassword")
    public Object resetPassword(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        Long userId = param.getLong("id");
        String newPassword = param.getString("newPassword");

        if (userId == null) {
            return R.fail("用户ID不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return R.fail("新密码不能为空");
        }

        IData updateData = new IData();
        updateData.set("id", userId);
        updateData.set("password", newPassword); // 假设前端已经加密处理
        updateData.set("update_time", dao.getSysTimeLocal());
        updateData.setTableName("sa_system_user");
        dao.updateById(updateData);

        return R.ok("密码重置成功");
    }

    /**
     * 更新用户状态
     */
    @RequestMapping(value = "changeStatus")
    public Object changeStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        Long userId = param.getLong("id");
        Integer status = param.getInt("status");

        if (userId == null) {
            return R.fail("用户ID不能为空");
        }
        if (status == null) {
            return R.fail("状态值不能为空");
        }

        IData updateData = new IData();
        updateData.set("id", userId);
        updateData.set("status", status);
        updateData.set("update_time", dao.getSysTimeLocal());
        updateData.setTableName("sa_system_user");
        dao.updateById(updateData);

        return R.ok("状态更新成功");
    }
}
