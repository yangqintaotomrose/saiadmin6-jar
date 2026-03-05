package com.abc.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志工具类
 * 提供记录系统操作日志的通用静态方法
 * 包含IP地址解析和地理位置转换功能
 */
public class OperationLogUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(OperationLogUtil.class);
    
    // IP地理位置API服务地址（免费API示例）
    private static final String IP_LOCATION_API = "http://ip-api.com/json/";
    
    // 缓存IP位置信息，避免频繁请求API
    private static final Map<String, String> ipLocationCache = new HashMap<>();
    

    
    /**
     * 记录通用操作日志
     * @param username 用户名
     * @param serviceName 业务名称
     * @param router 请求路由
     * @param remark 备注信息
     * @param requestData 请求数据（可选）
     */
    public static void logOperation(String username, String serviceName, String router, String remark, Object requestData) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                logger.warn("无法获取当前请求上下文");
                return;
            }
            
            // 构建日志数据
            IData logData = new IData();
            logData.set("username", username);
            logData.set("app", "系统管理平台");
            logData.set("method", request.getMethod());
            logData.set("router", router);
            logData.set("service_name", serviceName);
            logData.set("ip", getClientIpAddress(request));
            logData.set("ip_location", getIpLocation(getClientIpAddress(request)));
            logData.set("request_data", requestData != null ? JSON.toJSONString(requestData) : "");
            logData.set("remark", remark);
            logData.set("created_by", getLoginUserId());
            logData.set("create_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 插入数据库
            BaseDao dao = BaseDao.getDao("");
            dao.insert("sa_system_oper_log", logData);
            
            logger.info("操作日志记录成功: 用户={}, 业务={}, IP={}", username, serviceName, getClientIpAddress(request));
                       
        } catch (Exception e) {
            logger.error("记录操作日志失败", e);
        }
    }
    
    /**
     * 获取客户端真实IP地址
     * @param request HttpServletRequest对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 处理多个IP的情况，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        // 如果都没有获取到，使用远程地址
        return request.getRemoteAddr();
    }
    
    /**
     * 获取IP地址对应的地理位置
     * @param ipAddress IP地址
     * @return 地理位置信息
     */
    public static String getIpLocation(String ipAddress) {
        // 先从缓存获取
        if (ipLocationCache.containsKey(ipAddress)) {
            return ipLocationCache.get(ipAddress);
        }
        
        // 本地IP直接返回内网
        if (isLocalAddress(ipAddress)) {
            String location = "内网地址";
            ipLocationCache.put(ipAddress, location);
            return location;
        }
        
        try {
            // 调用IP地理位置API（这里使用简单的实现，实际项目中可能需要更复杂的处理）
            String location = getLocationFromApi(ipAddress);
            ipLocationCache.put(ipAddress, location);
            return location;
        } catch (Exception e) {
            logger.warn("获取IP地理位置失败: {}", ipAddress, e);
            String defaultLocation = "未知地区";
            ipLocationCache.put(ipAddress, defaultLocation);
            return defaultLocation;
        }
    }
    
    /**
     * 从本地数据库获取地理位置信息
     * 使用ip2region本地数据库进行IP地址解析
     * @param ipAddress IP地址
     * @return 地理位置描述
     */
    private static String getLocationFromApi(String ipAddress) {
        try {
            // 使用本地ip2region数据库查询
            return Ip2regionUtil.getIpLocation(ipAddress);
        } catch (Exception e) {
            logger.warn("查询IP地理位置失败，使用默认值: {}", ipAddress, e);
            return "未知地区";
        }
    }
    
    /**
     * 判断是否为本地地址
     * @param ipAddress IP地址
     * @return 是否为本地地址
     */
    private static boolean isLocalAddress(String ipAddress) {
        return ipAddress == null || 
               ipAddress.equals("127.0.0.1") || 
               ipAddress.equals("0:0:0:0:0:0:0:1") || // IPv6 localhost
               ipAddress.startsWith("192.168.") ||
               ipAddress.startsWith("10.") ||
               ipAddress.startsWith("172.");
    }
    
    /**
     * 获取当前请求对象
     * @return HttpServletRequest对象
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            logger.debug("无法获取当前请求上下文", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    private static Long getLoginUserId() {
        try {
            return LoginHelper.getUserId();
        } catch (Exception e) {
            logger.debug("获取登录用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 清理IP位置缓存
     */
    public static void clearIpLocationCache() {
        ipLocationCache.clear();
        logger.info("IP位置缓存已清理");
    }
    
    /**
     * 获取缓存中的IP位置信息
     * @param ipAddress IP地址
     * @return 位置信息
     */
    public static String getCachedIpLocation(String ipAddress) {
        return ipLocationCache.get(ipAddress);
    }
    
    /**
     * 手动添加IP位置缓存
     * @param ipAddress IP地址
     * @param location 位置信息
     */
    public static void putIpLocationCache(String ipAddress, String location) {
        ipLocationCache.put(ipAddress, location);
    }
}