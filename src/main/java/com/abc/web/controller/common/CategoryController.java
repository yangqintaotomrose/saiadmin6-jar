package com.abc.web.controller.common;

import com.abc.bean.TreeBuilder;
import com.abc.web.controller.WebController;
import com.abc.web.domain.HttpStatus;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 附件分类控制器 - 根据附件分类JSON Schema生成，保持与DeptController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/category/")
public class CategoryController extends WebController {

    /**
     * 查询附件分类列表（树形结构）
     */
    @RequestMapping(value = "index")
    public Object index(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
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

    /**
     * 获取附件分类详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_system_category WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("附件分类不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加附件分类
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_category");
        dao.insert(info);
        return R.ok("附件分类添加成功");
    }

    /**
     * 编辑附件分类
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_category");
        dao.updateById(info);
        return R.ok("附件分类修改成功");
    }

    /**
     * 删除附件分类（软删除）
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
            updateData.setTableName("sa_system_category");
            dao.updateById(updateData);
        }
        return R.ok("附件分类删除成功");
    }

    /**
     * 批量删除附件分类（软删除）
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
            updateData.setTableName("sa_system_category");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的附件分类
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
            updateData.setTableName("sa_system_category");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }

    /**
     * 获取分类树形结构（用于前端下拉选择）
     */
    // @RequestMapping(value = "tree")
    // public Object tree(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //     IData param = getRequestParam();
    //     BaseDao dao = BaseDao.getDao("");
    //     SQLParser parser = new SQLParser(param);
    //     parser.addSQL(" SELECT t.id, t.parent_id, t.category_name as name, t.sort, t.status");
    //     parser.addSQL(" FROM sa_system_category t ");
    //     parser.addSQL(" WHERE t.status = 1 ");
    //     parser.addSQL(" AND t.delete_time IS NULL ");
    //     parser.addSQL(" ORDER BY t.sort ASC, t.create_time ASC ");
    //
    //     IDataset list = dao.query(parser, param);
    //     List<IData> root = TreeBuilder.buildTreeStructure(list, "id", "parent_id");
    //     return R.ok(root);
    // }
}
