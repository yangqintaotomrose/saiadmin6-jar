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
 * 系统菜单控制器 - 根据sys_menu表结构生成，保持与BladeUserController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/menu/")
public class MenuController extends WebController {


    /**
     * 查询系统菜单列表（构建树形结构）
     */
    @RequestMapping(value = "index")
    public Object index(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        param.set("limit",1000);
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_menu t ");
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


    @RequestMapping(value = "accessMenu")
    public Object accessMenu(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        param.set("limit", 1000);
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_menu t ");
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
            obj.set("label", obj.getString("name"));
            obj.set("value", obj.getInt("id"));

            obj.set("create_time", obj.getString("create_time"));
            obj.set("update_time", obj.getString("update_time"));
        });
        List<IData> root = TreeBuilder.buildTreeStructure(list, "id", "parent_id");
        return successResponse(root);
    }

    /**
     * 查询系统菜单列表
     */
    @RequestMapping(value = "list")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sys_menu t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.parent_id = :parentId ");
        parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
        parser.addSQL(" AND t.path LIKE CONCAT('%', :path, '%') ");
        parser.addSQL(" AND t.label LIKE CONCAT('%', :label, '%') ");
        parser.addSQL(" AND t.component LIKE CONCAT('%', :component, '%') ");
        parser.addSQL(" AND t.link LIKE CONCAT('%', :link, '%') ");
        parser.addSQL(" AND t.order_num = :orderNum ");
        parser.addSQL(" AND t.query_param LIKE CONCAT('%', :queryParam, '%') ");
        parser.addSQL(" AND t.is_frame = :isFrame ");
        parser.addSQL(" AND t.is_cache = :isCache ");
        parser.addSQL(" AND t.menu_type = :menuType ");
        parser.addSQL(" AND t.visible = :visible ");
        parser.addSQL(" AND t.status = :status ");
        parser.addSQL(" AND t.icon LIKE CONCAT('%', :icon, '%') ");
        parser.addSQL(" AND t.create_by LIKE CONCAT('%', :createBy, '%') ");
        parser.addSQL(" AND t.remark LIKE CONCAT('%', :remark, '%') ");
        parser.addSQL(" AND t.is_enable = :isEnable ");
        parser.addSQL(" AND t.keep_alive = :keepAlive ");
        parser.addSQL(" AND t.is_hide = :isHide ");
        parser.addSQL(" AND t.is_iframe = :isIframe ");
        parser.addSQL(" AND t.show_badge = :showBadge ");
        parser.addSQL(" AND t.fixed_tab = :fixedTab ");
        parser.addSQL(" AND t.is_hide_tab = :isHideTab ");
        parser.addSQL(" AND t.is_full_page = :isFullPage ");
        parser.addSQL(" ORDER BY t.id DESC ");
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        IDataset two = ChangeBean.dbs_vos(list);
        two.setTotal(list.getTotal());
        return wrapPageQueryList(two, page);
    }

    /**
     * 获取系统菜单详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sys_menu WHERE id=?", param.getLong("id"));
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加系统菜单
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.setTableName("sa_system_menu");
        dao.insert(info);
        return R.ok("添加菜单成功");
    }

    /**
     * 编辑系统菜单
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.setTableName("sa_system_menu");
        dao.updateById(info);
        return R.ok("修改菜单成功");
    }

    /**
     * 删除系统菜单
     */
    @RequestMapping(value = "destroy")
    public Object delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids =(JSONArray)param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger( i);
            dao.delete("sa_system_menu", "id", a);
        }
        return R.ok("删除成功");
    }

}
