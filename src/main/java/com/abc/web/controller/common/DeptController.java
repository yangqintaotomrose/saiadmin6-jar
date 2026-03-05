package com.abc.web.controller.common;

import com.abc.bean.TreeBuilder;
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
import java.util.List;

/**
 * 部门信息控制器 - 根据sys_dept表结构生成，保持与CpuInfoController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/dept/")
public class DeptController extends WebController {

    /**
     * 查询部门信息列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        param.set("limit",1000);
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_dept t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.is_del = :isDel ");
        parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
        parser.addSQL(" AND t.parent_id LIKE CONCAT('%', :parentId, '%') ");
        parser.addSQL(" AND t.principal LIKE CONCAT('%', :principal, '%') ");
        parser.addSQL(" AND t.phone LIKE CONCAT('%', :phone, '%') ");
        parser.addSQL(" AND t.email LIKE CONCAT('%', :email, '%') ");
        parser.addSQL(" AND t.status = :status ");
        parser.addSQL(" AND t.remark LIKE CONCAT('%', :remark, '%') ");
        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param,page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));
            //obj.set("status","1".equals(obj.getString("status"))?1:0);

        });
        List<IData> root = TreeBuilder.buildTreeStructure(list,"id","parent_id");
        return wrapPageQueryListExt(root,page);
    }


    @RequestMapping(value = "accessDept")
    public Object accessDept(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        param.set("limit",1000);
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_dept t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.status = 1 ");
        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param,page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));
            obj.set("value",obj.getInt("id"));
            obj.set("label",obj.getString("name"));

        });
        List<IData> root = TreeBuilder.buildTreeStructure(list,"id","parent_id");

        return successResponse(root);
    }
    /**
     * 获取部门信息详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sys_dept WHERE id=?", param.getLong("id"));
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加部门信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.setTableName("sa_system_dept");
        dao.insert(info);
        return R.ok("部门信息添加成功");
    }

    /**
     * 编辑部门信息
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.setTableName("sa_system_dept");
        dao.updateById(info);
        return R.ok("部门信息修改成功");
    }

    /**
     * 删除部门信息
     */
    @RequestMapping(value = "destroy")
    public Object delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids =(JSONArray)param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger( i);
            dao.delete("sa_system_dept", "id", a);
        }


        return R.ok("删除成功");
    }

    /**
     * 批量删除部门信息
     */
    @RequestMapping(value = "deletes")
    public Object deletes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            dao.delete("sys_dept", "id", Integer.parseInt(id));
        }
        return R.ok("批量删除成功");
    }
}
