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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录日志工具类
 * 专门用于记录用户登录日志到sa_system_login_log表
 * 包含IP地址解析、地理位置转换和用户代理信息解析功能
 */
public class LoginLogUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginLogUtil.class);
    
    // 缓存IP位置信息，避免频繁查询
    private static final Map<String, String> ipLocationCache = new HashMap<>();
    
    // 浏览器和操作系统识别的关键词
    private static final Map<String, String> browserKeywords = new HashMap<>();
    private static final Map<String, String> osKeywords = new HashMap<>();
    
    static {
        // 浏览器关键词映射
        browserKeywords.put("chrome", "Chrome");
        browserKeywords.put("firefox", "Firefox");
        browserKeywords.put("safari", "Safari");
        browserKeywords.put("edge", "Edge");
        browserKeywords.put("opera", "Opera");
        browserKeywords.put("msie", "IE");
        browserKeywords.put("trident", "IE");
        
        // 操作系统关键词映射
        osKeywords.put("windows", "Windows");
        osKeywords.put("mac", "MacOS");
        osKeywords.put("linux", "Linux");
        osKeywords.put("android", "Android");
        osKeywords.put("iphone", "iOS");
        osKeywords.put("ipad", "iOS");
    }
    
    /**
     * 记录登录日志
     * @param username 用户名
     * @param isSuccess 登录是否成功
     * @param message 登录消息
     * @param remark 备注信息
     */
    public static void logLogin(String username, boolean isSuccess, String message, String remark) {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                logger.warn("无法获取当前请求上下文");
                return;
            }
            
            // 构建登录日志数据
            IData logData = new IData();
            logData.set("username", username);
            logData.set("ip", getClientIpAddress(request));
            logData.set("ip_location", getIpLocation(getClientIpAddress(request)));
            logData.set("os", getOperatingSystem(request));
            logData.set("browser", getBrowser(request));
            logData.set("status", isSuccess ? 1 : 2); // 1成功 2失败
            logData.set("message", message);
            logData.set("login_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            logData.set("remark", remark);
            logData.set("created_by", getLoginUserId());
            logData.set("create_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            // 插入到登录日志表
            BaseDao dao = BaseDao.getDao("");
            dao.insert("sa_system_login_log", logData);
            
            logger.info("登录日志记录成功: 用户={}, IP={}, 状态={}", username, getClientIpAddress(request), 
                       isSuccess ? "成功" : "失败");
                       
        } catch (Exception e) {
            logger.error("记录登录日志失败", e);
        }
    }
    
    /**
     * 记录登录成功日志
     * @param username 用户名
     * @param remark 备注信息
     */
    public static void logLoginSuccess(String username, String remark) {
        logLogin(username, true, "登录成功", remark);
    }
    
    /**
     * 记录登录失败日志
     * @param username 用户名
     * @param errorMessage 错误消息
     * @param remark 备注信息
     */
    public static void logLoginFailure(String username, String errorMessage, String remark) {
        logLogin(username, false, errorMessage, remark);
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
            // 使用本地ip2region数据库查询
            String location = Ip2regionUtil.getIpLocation(ipAddress);
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
     * 获取操作系统信息
     * @param request HttpServletRequest对象
     * @return 操作系统名称
     */
    public static String getOperatingSystem(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }
        
        userAgent = userAgent.toLowerCase();
        
        for (Map.Entry<String, String> entry : osKeywords.entrySet()) {
            if (userAgent.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "未知";
    }
    
    /**
     * 获取浏览器信息
     * @param request HttpServletRequest对象
     * @return 浏览器名称
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            return "未知";
        }
        
        userAgent = userAgent.toLowerCase();
        
        for (Map.Entry<String, String> entry : browserKeywords.entrySet()) {
            if (userAgent.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "未知";
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