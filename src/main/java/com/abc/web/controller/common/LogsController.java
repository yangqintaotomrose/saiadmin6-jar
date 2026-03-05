package com.abc.web.controller.common;

import com.abc.web.annotation.Log;
import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/proxy/core/logs/")
public class LogsController extends WebController {

    /**
     * 登录日志查询
     */
    @Log(serviceName = "日志查询", remark = "查询日志信息")
    @RequestMapping(value = "getLoginLogPageList")
    public Object getLoginLogPageList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_login_log t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.username LIKE CONCAT('%', :username, '%') ");
        parser.addSQL(" AND t.ip LIKE CONCAT('%', :ip, '%') ");
        parser.addSQL(" AND t.status = :status ");
        if(param.containsKey("orderField"))
        {
            parser.addSQL(" order by "+param.getString("orderField") + " " + param.getString("orderType"));
        }

        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param,page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("login_time",obj.getString("login_time"));

        });

        return wrapPageQueryList(list,page);
    }

    /**
     * 删除登录日志
     */
    @Log(serviceName = "删除登录日志", remark = "删除登录日志信息")
    @RequestMapping(value = "deleteLoginLog")
    public Object deleteLoginLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids =(JSONArray)param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger( i);
            dao.delete("sa_system_login_log", "id", a);
        }
        return R.ok("删除成功");
    }

    /**
     * 登录日志查询
     */
    @RequestMapping(value = "getOperLogPageList")
    public Object getOperLogPageList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_oper_log t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.username LIKE CONCAT('%', :username, '%') ");
        parser.addSQL(" AND t.ip LIKE CONCAT('%', :ip, '%') ");
        parser.addSQL(" AND t.status = :status ");
        if(param.containsKey("orderField"))
        {
            parser.addSQL(" order by "+param.getString("orderField") + " " + param.getString("orderType"));
        }

        Pagination page = request.getAttribute("is_export") == null ?this.getSinglePage(param):this.getExportPage();
        IDataset list = dao.queryPage(parser, param,page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });

        return wrapPageQueryList(list,page);
    }

    /**
     * 删除登录日志
     */
    @RequestMapping(value = "deleteOperLog")
    public Object deleteOperLog(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids =(JSONArray)param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger( i);
            dao.delete("sa_system_oper_log", "id", a);
        }
        return R.ok("删除成功");
    }


}
