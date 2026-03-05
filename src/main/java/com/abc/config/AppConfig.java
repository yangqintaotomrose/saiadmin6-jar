package com.abc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 应用配置类
 * 管理应用的基础URL和其他配置信息
 */
@Component
public class AppConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    private String resolvedBaseUrl;

    @PostConstruct
    public void init() {
        // 如果baseUrl是默认值，尝试构建完整的URL
        // if ("http://localhost:8080".equals(baseUrl)) {
        //     try {
        //         String host = InetAddress.getLocalHost().getHostAddress();
        //         resolvedBaseUrl = "http://" + host + ":" + serverPort;
        //         if (!"/".equals(contextPath)) {
        //             resolvedBaseUrl += contextPath;
        //         }
        //     } catch (UnknownHostException e) {
        //         resolvedBaseUrl = baseUrl;
        //     }
        // } else {
        //     resolvedBaseUrl = baseUrl;
        // }
        resolvedBaseUrl = baseUrl;
    }

    /**
     * 获取应用基础URL
     * @return 基础URL
     */
    public String getBaseUrl() {
        return resolvedBaseUrl;
    }

    /**
     * 构建完整URL
     * @param relativePath 相对路径
     * @return 完整URL
     */
    public String buildFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return resolvedBaseUrl;
        }

        // 确保路径以/开头
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }

        return resolvedBaseUrl + relativePath;
    }

    /**
     * 获取相对URL
     * @param fullPath 完整路径
     * @return 相对URL
     */
    public String getRelativeUrl(String fullPath) {
        if (fullPath == null || fullPath.isEmpty()) {
            return "";
        }

        // 移除基础URL部分
        if (fullPath.startsWith(resolvedBaseUrl)) {
            return fullPath.substring(resolvedBaseUrl.length());
        }

        return fullPath;
    }
}
