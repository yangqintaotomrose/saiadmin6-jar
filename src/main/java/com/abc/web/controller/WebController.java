package com.abc.web.controller;

import com.abc.web.domain.HttpStatus;
import com.xtr.framework.hutool.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;

public class WebController extends BaseWebController {

    public Pagination getSinglePage(IData idata) {
        int size = idata.getInt("limit", 20);
        int currPage = idata.getInt("page", 1);
        Pagination pagination = new Pagination();
        pagination.setCurrPage(currPage);
        pagination.setSize(size);
        return pagination;
    }

    public Pagination getExportPage() {
        int size = '\uea60';
        int currPage = 0;
        Pagination pagination = new Pagination();
        pagination.setCurrPage(currPage);
        return pagination;
    }

    //包装方法返回
    public static IData wrapPageQueryList(IDataset two,Pagination page) {
        IData rspData = new IData();
        IData data = new IData();
        rspData.set("code", HttpStatus.SUCCESS);
        rspData.set("msg", "请求成功");
        rspData.set("data", data);

        data.set("data", two);
        data.set("current", page.getCurrPage());
        data.set("size", page.getSize());
        data.set("total", two.getTotal());
        return rspData;
    }
    public static IData wrapPageQueryListExt(List<IData> two, Pagination page) {
        IData rspData = new IData();
        IData data = new IData();
        rspData.set("code", HttpStatus.SUCCESS);
        rspData.set("msg", "请求成功");
        rspData.set("data", data);

        data.set("records", two);
        data.set("current", page.getCurrPage());
        data.set("size", page.getSize());
        data.set("total", two.size());
        return rspData;
    }

    public static IData getIDataFromStreamExt() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if (null != request.getAttribute("request_param")) {
            return (IData)request.getAttribute("request_param");
        } else {
            IData param = new IData();
            Enumeration paramNames = request.getParameterNames();

            String json;
            while(paramNames.hasMoreElements()) {
                json = (String)paramNames.nextElement();
                String[] values = request.getParameterValues(json);
                String value = values[0];
                param.put(json.trim(), value.trim());
            }

            request.setAttribute("request_param", param);
            if (request.getRequestURI().indexOf("upload") < 0) {
                System.out.println(param);
            }

            json = readAsChars(request);
            if (json != null && json.length() != 0) {
                param.putAll(new IData(json));
                return param;
            } else {
                return param;
            }
        }
    }

    /**
     * 包装通用成功响应数据
     * @param data 响应数据
     * @return 成功响应IData对象
     */
    public static IData successResponse(Object data) {
        IData rspData = new IData();
        rspData.set("code", HttpStatus.SUCCESS);
        rspData.set("msg", "请求成功");
        rspData.set("data", data);
        return rspData;
    }
}
