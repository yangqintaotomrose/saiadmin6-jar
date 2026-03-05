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
 * 角色信息控制器 - 根据角色信息JSON Schema生成，保持与CpuInfoController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/role/")
public class RoleController extends WebController {

    /**
     * 查询角色信息列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_role t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
        parser.addSQL(" AND t.code LIKE CONCAT('%', :code, '%') ");
        parser.addSQL(" AND t.status = :status ");
        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param,page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });

        return wrapPageQueryList(list,page);
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
        IData rspData = new IData();
        rspData.set("code", HttpStatus.SUCCESS);
        rspData.set("msg", "请求成功");
        rspData.set("data", list);
        return rspData;
    }


    @RequestMapping(value = "getMenuByRole")
    public Object getMenuByRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        // 根据角色获取菜单列表
        IDataset  menus = dao.queryList("SELECT * FROM sa_system_menu " +
                "where status=1 " +
                "and id in ( select menu_id from sa_system_role_menu where role_id='"+param.getString("id")+"' ) ");

        //处理数据
        menus.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });
        IData role = new IData();
        role.set("id", param.getString("id"));
        role.set("menus", menus);

        return successResponse(role);
    }


    @RequestMapping(value = "menuPermission")
    public Object menuPermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray menu_ids =(JSONArray)param.getObj("menu_ids");
        // 删除现有菜单
        dao.delete("sa_system_role_menu", "role_id", param.getString("id"));
        // 批量插入菜单
        for (int i = 0; i < menu_ids.size(); i++) {
            Integer a = menu_ids.getInteger( i);
            IData item = new IData();
            item.set("menu_id", a);
            item.set("role_id", param.getString("id"));
            item.setTableName("sa_system_role_menu");
            dao.insertExt(item);
        }
        return R.ok("授权成功");
    }

    /**
     * 获取角色信息详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM role_info WHERE id=?", param.getLong("id"));
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加角色信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time",dao.getSysTimeLocal());
        info.setTableName("sa_system_role");
        dao.insert(info);
        return R.ok("角色信息添加成功");
    }

    /**
     * 编辑角色信息
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.setTableName("sa_system_role");
        dao.updateById(info);
        return R.ok("角色信息修改成功");
    }

    /**
     * 删除角色信息
     */
    @RequestMapping(value = "destroy")
    public Object delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids =(JSONArray)param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger( i);
            dao.delete("sa_system_role", "id", a);
        }
        return R.ok("删除成功");
    }

    /**
     * 批量删除角色信息
     */
    @RequestMapping(value = "deletes")
    public Object deletes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            dao.delete("role_info", "id", Integer.parseInt(id));
        }
        return R.ok("批量删除成功");
    }
}
