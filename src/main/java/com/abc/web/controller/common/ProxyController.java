package com.abc.web.controller.common;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.Method;
import cn.hutool.json.JSONObject;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.BaseWebController;
import com.xtr.framework.hutool.IData;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理SaiAdmin官网的api请求
 */
@Controller
@RequestMapping("/proxy")
public class ProxyController extends BaseWebController {

    // 目标服务器基础URL
    private static final String TARGET_BASE_URL = "http://v6.saithink.top/prod";

    /**
     * 处理GET请求
     */
    @GetMapping(value = "/**")
    @ResponseBody
    public Object handleGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleRequest(request, response);
    }

    /**
     * 处理POST请求
     */
    @PostMapping(value = "/**")
    @ResponseBody
    public Object handlePost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleRequest(request, response);
    }

    /**
     * 处理PUT请求
     */
    @PutMapping(value = "/**")
    @ResponseBody
    public Object handlePut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleRequest(request, response);
    }

    /**
     * 处理DELETE请求
     */
    @DeleteMapping(value = "/**")
    @ResponseBody
    public Object handleDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleRequest(request, response);
    }

    /**
     * 处理PATCH请求
     */
    @PatchMapping(value = "/**")
    @ResponseBody
    public Object handlePatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleRequest(request, response);
    }

    /**
     * 处理OPTIONS请求
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    @ResponseBody
    public Object handleOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return handleRequest(request, response);
    }

    /**
     * 统一处理请求的方法
     */
    private Object handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 获取请求路径
            String requestUri = request.getRequestURI();
            String proxyPath = requestUri.replace("/proxy", "");
            String requestBody = "";
            // 构建目标URL
            String targetUrl = TARGET_BASE_URL + proxyPath;

            // 获取查询参数
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                targetUrl += "?" + queryString;
            }

            // 创建HTTP请求
            HttpRequest httpRequest;
            String method = request.getMethod().toUpperCase();

            switch (method) {
                case "GET":
                    httpRequest = HttpRequest.get(targetUrl);
                    break;
                case "POST":
                    httpRequest = HttpRequest.post(targetUrl);
                    break;
                case "PUT":
                    httpRequest = HttpRequest.put(targetUrl);
                    break;
                case "DELETE":
                    httpRequest = HttpRequest.delete(targetUrl);
                    break;
                case "PATCH":
                    httpRequest = HttpRequest.patch(targetUrl);
                    break;
                case "HEAD":
                    httpRequest = HttpRequest.head(targetUrl);
                    break;
                case "OPTIONS":
                    httpRequest = HttpRequest.options(targetUrl);
                    break;
                default:
                    httpRequest = HttpRequest.trace(targetUrl).method(Method.POST);
                    break;
            }

            // 复制请求头
            Map<String, String> headers = new HashMap<>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();

                // 过滤掉一些不需要转发的头部
                if (!headerName.toLowerCase().equals("host") &&
                        !headerName.toLowerCase().equals("cookie") &&
                        !headerName.toLowerCase().equals("connection")) {
                    headers.put(headerName, request.getHeader(headerName));
                }
            }

            // 添加转发相关的头部
            // headers.put("X-Forwarded-Host", request.getRemoteHost());
            // headers.put("X-Forwarded-For", request.getRemoteAddr());
            // headers.put("X-Real-IP", request.getRemoteAddr());

            httpRequest.addHeaders(headers);

            // 如果是POST、PUT、PATCH请求，复制请求体
            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                requestBody = getBodyString(request);
                if (requestBody != null) {
                    httpRequest.body(requestBody);
                }
            }

            // 执行请求
            HttpResponse httpResponse = httpRequest.execute();

            // 设置响应状态码
            response.setStatus(httpResponse.getStatus());

            // 复制响应头
            for (Map.Entry<String, List<String>> entry : httpResponse.headers().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
                //response.setHeader(entry.getKey(), entry.getValue().get(0));
            }

            // 获取响应体并处理编码
            String responseBody = httpResponse.body();

            // 返回结果
            IData result = new IData();
            result.set("code", HttpStatus.HTTP_OK);
            result.set("msg", "请求成功");
            result.set("method", method);
            result.set("uri", targetUrl);
            result.set("response_status", httpResponse.getStatus());
            result.set("resp_data", responseBody);
            result.set("req_data",requestBody);

            result.set("id",System.currentTimeMillis());
            result.set("create_date", LocalDateTime.now());

            BaseDao dao = BaseDao.getDao("");
            result.setTableName("proxy_log");
            dao.insert(result);
            return responseBody;

        } catch (Exception e) {
            e.printStackTrace();

            JSONObject errorResult = new JSONObject();
            errorResult.set("code", 500);
            errorResult.set("msg", "请求转发失败: " + e.getMessage());
            errorResult.set("error", e.getClass().getSimpleName());
            errorResult.set("req_param", getRequestParam());

            return errorResult;
        }
    }

    /**
     * 从请求中获取body字符串
     */
    private String getBodyString(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try (java.io.BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.length() > 0 ? stringBuilder.toString() : null;
    }
}
