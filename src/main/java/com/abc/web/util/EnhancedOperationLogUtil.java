package com.abc.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 增强版操作日志工具类
 * 集成真实的IP地理位置API服务
 * 提供更精确的位置解析和更好的性能优化
 */
public class EnhancedOperationLogUtil extends OperationLogUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedOperationLogUtil.class);
    
    // 使用ConcurrentHashMap保证线程安全
    private static final ConcurrentHashMap<String, LocationCacheEntry> ipLocationCache = new ConcurrentHashMap<>();
    
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_TIME = TimeUnit.HOURS.toMillis(24);
    
    // 使用本地ip2region数据库
    private static final String LOCAL_DB_PROVIDER = "ip2region";
    
    /**
     * 缓存条目内部类
     */
    private static class LocationCacheEntry {
        private final String location;
        private final long createTime;
        
        public LocationCacheEntry(String location) {
            this.location = location;
            this.createTime = System.currentTimeMillis();
        }
        
        public String getLocation() {
            return location;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() - createTime > CACHE_EXPIRE_TIME;
        }
    }
    
    /**
     * 获取IP地理位置（增强版）
     * @param ipAddress IP地址
     * @return 地理位置信息
     */
    public static String getEnhancedIpLocation(String ipAddress) {
        // 参数验证
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return "未知IP";
        }
        
        // 检查缓存
        LocationCacheEntry cachedEntry = ipLocationCache.get(ipAddress);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return cachedEntry.getLocation();
        }
        
        // 本地地址直接返回
        if (isLocalOrPrivateAddress(ipAddress)) {
            String location = parseLocalAddress(ipAddress);
            cacheLocation(ipAddress, location);
            return location;
        }
        
        // 调用API获取位置信息
        String location = fetchLocationFromApis(ipAddress);
        cacheLocation(ipAddress, location);
        return location;
    }
    
    /**
     * 通过本地数据库获取位置信息
     * @param ipAddress IP地址
     * @return 地理位置信息
     */
    private static String fetchLocationFromApis(String ipAddress) {
        try {
            // 直接使用本地ip2region数据库查询
            String location = Ip2regionUtil.getIpLocation(ipAddress);
            if (isValidLocation(location)) {
                return location;
            }
        } catch (Exception e) {
            logger.warn("查询IP地理位置失败: {}", ipAddress, e);
        }
        
        return "未知地区";
    }
    
    // 已移除网络API调用方法，全部使用本地ip2region数据库
    
    /**
     * 判断是否为本地或私有地址
     * @param ipAddress IP地址
     * @return 是否为本地地址
     */
    private static boolean isLocalOrPrivateAddress(String ipAddress) {
        return ipAddress == null || 
               ipAddress.equals("127.0.0.1") || 
               ipAddress.equals("0:0:0:0:0:0:0:1") ||
               ipAddress.startsWith("192.168.") ||
               ipAddress.startsWith("10.") ||
               ipAddress.startsWith("172.");
    }
    
    /**
     * 解析本地地址
     * @param ipAddress IP地址
     * @return 地址描述
     */
    private static String parseLocalAddress(String ipAddress) {
        if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
            return "本地回环地址";
        } else if (ipAddress.startsWith("192.168.")) {
            return "局域网(C类)";
        } else if (ipAddress.startsWith("10.")) {
            return "局域网(A类)";
        } else if (ipAddress.startsWith("172.")) {
            return "局域网(B类)";
        }
        return "内网地址";
    }
    
    /**
     * 验证位置信息是否有效
     * @param location 位置信息
     * @return 是否有效
     */
    private static boolean isValidLocation(String location) {
        return location != null && 
               !location.isEmpty() && 
               !location.contains("null") && 
               !location.equals("未知地区");
    }
    
    /**
     * 缓存位置信息
     * @param ipAddress IP地址
     * @param location 位置信息
     */
    private static void cacheLocation(String ipAddress, String location) {
        ipLocationCache.put(ipAddress, new LocationCacheEntry(location));
    }
    
    /**
     * 清理过期缓存
     */
    public static void cleanupExpiredCache() {
        ipLocationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        logger.info("已清理过期的IP位置缓存，剩余条目数: {}", ipLocationCache.size());
    }
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public static JSONObject getCacheStats() {
        long totalEntries = ipLocationCache.size();
        long expiredEntries = ipLocationCache.values().stream()
                .filter(LocationCacheEntry::isExpired)
                .count();
        
        JSONObject stats = new JSONObject();
        stats.put("total_entries", totalEntries);
        stats.put("expired_entries", expiredEntries);
        stats.put("active_entries", totalEntries - expiredEntries);
        stats.put("cache_expire_time_hours", TimeUnit.MILLISECONDS.toHours(CACHE_EXPIRE_TIME));
        
        return stats;
    }
    
    /**
     * 手动刷新特定IP的缓存
     * @param ipAddress IP地址
     */
    public static void refreshIpLocation(String ipAddress) {
        ipLocationCache.remove(ipAddress);
        getEnhancedIpLocation(ipAddress); // 重新获取并缓存
    }
}