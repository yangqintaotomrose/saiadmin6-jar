package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.HttpStatus;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 数据库管理控制器
 * 提供数据库表维护、查询、优化等功能
 */
@RestController
@RequestMapping("/proxy/core/database")
public class DatabaseController extends WebController {

    /**
     * 获取数据表列表
     */
    @RequestMapping(value = "index")
    public Object getTableList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" select " +
                "    table_schema as 'database'," +
                "    table_name as 'name'," +
                "    engine as 'engine'," +
                "    version as 'version'," +
                "    row_format as 'row_format'," +
                "    table_rows as 'rows'," +
                "    avg_row_length as 'avg_row_length'," +
                "    data_length as 'data_length'," +
                "    max_data_length as 'max_data_length'," +
                "    index_length as 'index_length'," +
                "    data_free as 'data_free'," +
                "    auto_increment as 'auto_increment'," +
                "    create_time as 'create_time'," +
                "    update_time as 'update_time'," +
                "    check_time as 'check_time'," +
                "    table_collation as 'collation'," +
                "    checksum as 'checksum'," +
                "    create_options as 'create_options'," +
                "    table_comment as 'comment'" +
                "from information_schema.tables t  ");
        parser.addSQL(" WHERE 1=1   ");
        parser.addSQL(" and t.table_schema='saiadmin6' ");
        parser.addSQL(" AND t.is_del = :isDel ");
        parser.addSQL(" AND (t.table_name LIKE CONCAT('%', :name, '%') or t.table_comment LIKE CONCAT('%', :name, '%') ) ");
        if(param.containsKey("orderField"))
        {
            parser.addSQL(" order by  "+param.getString("orderField") +" "+param.getString("orderType"));
        }
        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));
            obj.set("update_time",obj.getString("update_time"));
        });
        return wrapPageQueryList(list,page);
    }

    @RequestMapping(value = "detailed")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IDataset list = dao.queryList("" +
                "SELECT *" +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = 'saiadmin6' " +
                "  AND TABLE_NAME = '"+param.getString("table")+"'" +
                "ORDER BY ORDINAL_POSITION;" +
                "");

        return successResponse(list);
    }


    @RequestMapping(value = "optimize")
    public Object optimize(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray tables = (JSONArray) info.getObj("tables");
        for (int i = 0; i < tables.size(); i++) {
            String table = tables.getString(i);
            IDataset list = dao.queryList(" OPTIMIZE TABLE " + table);
            System.out.println(list);
        }

        return R.ok("优化表成功");
    }

    @RequestMapping(value = "fragment")
    public Object fragment(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray tables = (JSONArray) info.getObj("tables");
        for (int i = 0; i < tables.size(); i++) {
            String table = tables.getString(i);
            IDataset list = dao.queryList(" ANALYZE TABLE " + table);
            System.out.println(list);
        }

        return R.ok("整理碎片成功");
    }

    @RequestMapping(value = "recycle")
    public Object recycle(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData hasCol = dao.queryByFirst("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'saiadmin6' AND TABLE_NAME = '"+param.getString("table")+"' and column_name='delete_time'");
        if(hasCol == null)
        {
            throw new RuntimeException("当前表不支持回收站功能");
        }

        SQLParser parser = new SQLParser(param);
        parser.addSQL(" select * from "+param.getString("table"));
        parser.addSQL(" WHERE 1=1  and delete_time is not null  ");
        parser.addSQL(" order by  delete_time desc");
        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("delete_time",obj.getString("delete_time"));
            obj.set("create_time",obj.getString("create_time"));
            obj.set("update_time",obj.getString("update_time"));
        });
        return wrapPageQueryList(list,page);
    }

    /**
     * 删除数据（物理删除）
     */
    @RequestMapping(value = "delete")
    public Object delete(HttpServletRequest request, HttpServletResponse response) {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids = (JSONArray) param.getObj("ids");
        String idsStr = ids.stream().map(Object::toString).collect(Collectors.joining(","));// 转换为字符串
        dao.execSql(" DELETE FROM " + param.getString("table") + " WHERE delete_time is not null and  id IN (" + idsStr + ") ");
        return R.ok("回收站销毁成功");
    }

    /**
     * 恢复数据（将delete_time设为null）
     */
    @RequestMapping(value = "recovery")
    public Object recovery(HttpServletRequest request, HttpServletResponse response) {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids = (JSONArray) param.getObj("ids");
        String idsStr = ids.stream().map(Object::toString).collect(Collectors.joining(","));// 转换为字符串
        dao.execSql(" update  " + param.getString("table") + " set delete_time = null WHERE delete_time is not null and  id IN (" + idsStr + ") ");
        return R.ok("回收站恢复销毁成功");
    }

    /**
     * 获取数据源列表
     */
    @RequestMapping(value = "dataSource")
    public Object dataSource(HttpServletRequest request, HttpServletResponse response) {
        JSONArray dataSources = new JSONArray();
        dataSources.add("mysql");

        return successResponse(dataSources);
    }

    /**
     * 加载数据库表和列信息到代码生成表中
     */

}
