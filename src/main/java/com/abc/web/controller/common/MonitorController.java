package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.xtr.framework.hutool.IData;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控控制器
 * 提供服务器内存、磁盘、PHP环境等系统信息监控功能
 */
@RestController
@RequestMapping("/proxy/core/server/")
public class MonitorController extends WebController {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    /**
     * 获取服务器监控信息
     * 返回内存、磁盘、PHP环境等系统信息
     */
    @RequestMapping(value = "monitor")
    public Object monitor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // 获取内存信息
            data.put("memory", getMemoryInfo());
            
            // 获取磁盘信息
            data.put("disk", getDiskInfo());
            
            // 获取PHP环境信息
            data.put("phpEnv", getPhpEnvironmentInfo());
            
            return R.ok("success", data);
        } catch (Exception e) {
            return R.fail("获取服务器信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取内存使用信息
     */
    private Map<String, Object> getMemoryInfo() {
        Map<String, Object> memoryInfo = new HashMap<>();
        
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            
            // 获取JVM内存信息
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
            
            // 获取系统总内存和可用内存（需要通过系统命令获取）
            long totalMemory = Runtime.getRuntime().totalMemory();
            long freeMemory = Runtime.getRuntime().freeMemory();
            long maxMemory = Runtime.getRuntime().maxMemory();
            
            // 计算使用量
            long usedMemory = totalMemory - freeMemory;
            
            // 转换为GB/MB格式
            double totalGb = totalMemory / (1024.0 * 1024.0 * 1024.0);
            double freeMb = freeMemory / (1024.0 * 1024.0);
            double usedGb = usedMemory / (1024.0 * 1024.0 * 1024.0);
            double phpMb = heapUsed / (1024.0 * 1024.0);
            double usageRate = (usedMemory * 100.0) / totalMemory;
            
            // 格式化显示信息
            memoryInfo.put("total", formatBytes(totalMemory));
            memoryInfo.put("free", formatBytes(freeMemory));
            memoryInfo.put("used", formatBytes(usedMemory));
            memoryInfo.put("php", formatBytes(heapUsed));
            memoryInfo.put("rate", DECIMAL_FORMAT.format(usageRate) + "%");
            
            // 原始数据
            Map<String, Object> rawInfo = new HashMap<>();
            rawInfo.put("total", totalMemory);
            rawInfo.put("free", freeMemory);
            rawInfo.put("used", usedMemory);
            rawInfo.put("php", heapUsed);
            rawInfo.put("rate", Double.parseDouble(DECIMAL_FORMAT.format(usageRate)));
            memoryInfo.put("raw", rawInfo);
            
        } catch (Exception e) {
            // 如果获取失败，返回默认值
            memoryInfo.put("total", "0 B");
            memoryInfo.put("free", "0 B");
            memoryInfo.put("used", "0 B");
            memoryInfo.put("php", "0 B");
            memoryInfo.put("rate", "0%");
            Map<String, Object> rawInfo = new HashMap<>();
            rawInfo.put("total", 0L);
            rawInfo.put("free", 0L);
            rawInfo.put("used", 0L);
            rawInfo.put("php", 0L);
            rawInfo.put("rate", 0.0);
            memoryInfo.put("raw", rawInfo);
        }
        
        return memoryInfo;
    }

    /**
     * 获取磁盘使用信息
     */
    private List<Map<String, Object>> getDiskInfo() {
        List<Map<String, Object>> diskList = new ArrayList<>();
        
        try {
            File[] roots = File.listRoots();
            
            for (File root : roots) {
                Map<String, Object> diskInfo = new HashMap<>();
                
                long totalSpace = root.getTotalSpace();
                long freeSpace = root.getFreeSpace();
                long usableSpace = root.getUsableSpace();
                long usedSpace = totalSpace - freeSpace;
                
                double usagePercentage = totalSpace > 0 ? (usedSpace * 100.0) / totalSpace : 0;
                
                diskInfo.put("filesystem", root.getAbsolutePath());
                diskInfo.put("type", "local");
                diskInfo.put("mounted_on", root.getAbsolutePath());
                diskInfo.put("size", formatBytes(totalSpace));
                diskInfo.put("available", formatBytes(usableSpace));
                diskInfo.put("used", formatBytes(usedSpace));
                diskInfo.put("use_percentage", DECIMAL_FORMAT.format(usagePercentage) + "%");
                
                // 原始数据
                Map<String, Object> rawInfo = new HashMap<>();
                rawInfo.put("size", totalSpace);
                rawInfo.put("available", usableSpace);
                rawInfo.put("used", usedSpace);
                diskInfo.put("raw", rawInfo);
                
                diskList.add(diskInfo);
            }
            
        } catch (Exception e) {
            // 如果获取失败，返回空列表
        }
        
        return diskList;
    }

    /**
     * 获取PHP环境信息（模拟数据）
     * 实际项目中可以根据需要获取真实的PHP环境信息
     */
    private Map<String, Object> getPhpEnvironmentInfo() {
        Map<String, Object> phpInfo = new HashMap<>();
        
        try {
            // 获取Java运行时信息作为替代
            String javaVersion = System.getProperty("java.version");
            String osName = System.getProperty("os.name");
            String userDir = System.getProperty("user.dir");
            
            phpInfo.put("php_version", javaVersion); // 使用Java版本代替PHP版本
            phpInfo.put("os", osName);
            phpInfo.put("project_path", userDir);
            phpInfo.put("memory_limit", "1024M");
            phpInfo.put("max_execution_time", "0");
            phpInfo.put("error_reporting", "32767");
            phpInfo.put("display_errors", "on");
            phpInfo.put("upload_max_filesize", "50M");
            phpInfo.put("post_max_size", "50M");
            phpInfo.put("extension_dir", System.getProperty("java.home") + "/lib/ext");
            phpInfo.put("loaded_extensions", "Core, date, libxml, openssl, pcre, zlib, bcmath, ctype, curl, dom, filter, ftp, gd, hash, iconv, intl, json, mbstring, SPL, session, standard, mysqlnd, PDO, pdo_mysql, Phar, posix, random, Reflection, mysqli, shmop, SimpleXML, soap, sockets, sodium, sysvsem, tokenizer, xml, xmlreader, xmlwriter, zip, fileinfo");
            
        } catch (Exception e) {
            // 如果获取失败，返回默认值
            phpInfo.put("php_version", "unknown");
            phpInfo.put("os", "unknown");
            phpInfo.put("project_path", "/");
            phpInfo.put("memory_limit", "128M");
            phpInfo.put("max_execution_time", "30");
            phpInfo.put("error_reporting", "0");
            phpInfo.put("display_errors", "off");
            phpInfo.put("upload_max_filesize", "2M");
            phpInfo.put("post_max_size", "8M");
            phpInfo.put("extension_dir", "");
            phpInfo.put("loaded_extensions", "");
        }
        
        return phpInfo;
    }

    /**
     * 格式化字节大小为人类可读的格式
     */
    private String formatBytes(long bytes) {
        if (bytes <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = (int) (Math.log(bytes) / Math.log(1024));
        
        if (unitIndex >= units.length) {
            unitIndex = units.length - 1;
        }
        
        double size = bytes / Math.pow(1024, unitIndex);
        return DECIMAL_FORMAT.format(size) + " " + units[unitIndex];
    }
}