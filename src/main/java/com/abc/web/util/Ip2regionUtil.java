package com.abc.web.util;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于ip2region的IP地理位置查询工具类
 * 使用本地数据库文件进行IP地址解析，无需网络请求
 */
@Slf4j
@Component
public class Ip2regionUtil {
    
    // ip2region数据库搜索器
    private static Searcher searcher;
    
    // 缓存IP查询结果
    private static final ConcurrentHashMap<String, IpLocationCacheEntry> ipLocationCache = new ConcurrentHashMap<>();
    
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_TIME = TimeUnit.HOURS.toMillis(24);
    
    /**
     * 缓存条目内部类
     */
    private static class IpLocationCacheEntry {
        private final String location;
        private final long createTime;
        
        public IpLocationCacheEntry(String location) {
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
     * 初始化ip2region搜索器
     */
    @PostConstruct
    public void init() {
        try {
            // 从classpath加载ip2region.xdb文件
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            if (!resource.exists()) {
                log.warn("未找到ip2region.xdb文件，请将该文件放置在resources目录下");
                return;
            }
            
            String dbPath = resource.getFile().getAbsolutePath();
            searcher = Searcher.newWithFileOnly(dbPath);
            log.info("ip2region数据库加载成功: {}", dbPath);
            
        } catch (Exception e) {
            log.error("初始化ip2region搜索器失败", e);
        }
    }
    
    /**
     * 根据IP地址获取地理位置信息
     * @param ip IP地址
     * @return 地理位置信息（格式：国家|区域|省份|城市|ISP）
     */
    public static String getIpLocation(String ip) {
        // 参数验证
        if (ip == null || ip.trim().isEmpty()) {
            return "未知IP";
        }
        
        // 检查缓存
        IpLocationCacheEntry cachedEntry = ipLocationCache.get(ip);
        if (cachedEntry != null && !cachedEntry.isExpired()) {
            return cachedEntry.getLocation();
        }
        
        // 本地地址直接返回
        if (isLocalOrPrivateAddress(ip)) {
            String location = parseLocalAddress(ip);
            cacheLocation(ip, location);
            return location;
        }
        
        // 使用ip2region查询
        String location = searchIpLocation(ip);
        cacheLocation(ip, location);
        return location;
    }
    
    /**
     * 使用ip2region搜索器查询IP位置
     * @param ip IP地址
     * @return 格式化的位置信息
     */
    private static String searchIpLocation(String ip) {
        if (searcher == null) {
            log.warn("ip2region搜索器未初始化，返回默认值");
            return "未知地区";
        }
        
        try {
            // 执行搜索
            String region = searcher.search(ip);
            
            if (region != null && !region.isEmpty()) {
                // 解析ip2region返回的数据格式：国家|区域|省份|城市|ISP
                String[] parts = region.split("\\|");
                if (parts.length >= 4) {
                    String country = parts[0];
                    String province = parts[2];
                    String city = parts[3];
                    
                    // 格式化输出
                    if ("中国".equals(country)) {
                        // 处理省份和城市
                        if (!"0".equals(province) && !"0".equals(city)) {
                            return province + city;
                        } else if (!"0".equals(province)) {
                            return province;
                        } else {
                            return "中国";
                        }
                    } else if (!"0".equals(country)) {
                        return country;
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("查询IP地址失败: {}", ip, e);
        }
        
        return "未知地区";
    }
    
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
     * 缓存位置信息
     * @param ipAddress IP地址
     * @param location 位置信息
     */
    private static void cacheLocation(String ipAddress, String location) {
        ipLocationCache.put(ipAddress, new IpLocationCacheEntry(location));
    }
    
    /**
     * 清理过期缓存
     */
    public static void cleanupExpiredCache() {
        ipLocationCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        log.info("已清理过期的IP位置缓存，剩余条目数: {}", ipLocationCache.size());
    }
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计信息
     */
    public static java.util.Map<String, Object> getCacheStats() {
        long totalEntries = ipLocationCache.size();
        long expiredEntries = ipLocationCache.values().stream()
                .filter(IpLocationCacheEntry::isExpired)
                .count();
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
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
        getIpLocation(ipAddress); // 重新获取并缓存
    }
    
    /**
     * 关闭搜索器资源
     */
    public static void close() {
        if (searcher != null) {
            try {
                searcher.close();
                log.info("ip2region搜索器已关闭");
            } catch (IOException e) {
                log.error("关闭ip2region搜索器失败", e);
            }
        }
    }
}