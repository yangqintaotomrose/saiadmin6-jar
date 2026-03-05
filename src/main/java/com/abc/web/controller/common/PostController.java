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
 * 岗位信息控制器 - 根据岗位信息JSON Schema生成，保持与RoleController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/post/")
public class PostController extends WebController {

    /**
     * 查询岗位信息列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_post t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
        parser.addSQL(" AND t.code LIKE CONCAT('%', :code, '%') ");
        parser.addSQL(" AND t.status = :status ");
        parser.addSQL(" AND t.delete_time IS NULL ");
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

    @RequestMapping(value = "accessPost")
    public Object accessPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_post t ");
        parser.addSQL(" WHERE 1=1 and status=1");
        IDataset list = dao.queryPage(parser, param,this.getSinglePage(param));
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });

        return successResponse(list);
    }

    /**
     * 获取岗位信息详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_system_post WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("岗位信息不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加岗位信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_post");
        dao.insert(info);
        return R.ok("岗位信息添加成功");
    }

    /**
     * 编辑岗位信息
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_post");
        dao.updateById(info);
        return R.ok("岗位信息修改成功");
    }

    /**
     * 删除岗位信息（软删除）
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
            updateData.setTableName("sa_system_post");
            dao.updateById(updateData);
        }
        return R.ok("岗位信息删除成功");
    }

    /**
     * 批量删除岗位信息（软删除）
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
            updateData.setTableName("sa_system_post");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的岗位信息
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
            updateData.setTableName("sa_system_post");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }
}
