package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理控制器
 * 提供Redis缓存配置信息查询功能
 */
@RestController
@RequestMapping("/proxy/core/server/")
public class CacheController extends WebController {

    /**
     * 获取缓存配置信息
     * 返回系统中各模块的Redis缓存配置详情
     */
    @RequestMapping(value = "cache")
    public Object cache(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 构建缓存配置数据
        Map<String, Object> cacheConfig = buildCacheConfig();

        return R.ok("success", cacheConfig);
    }

    /**
     * 构建缓存配置信息
     * @return 缓存配置Map
     */
    private Map<String, Object> buildCacheConfig() {
        Map<String, Object> data = new HashMap<>();

        // 菜单缓存配置
        Map<String, Object> menuCache = new HashMap<>();
        menuCache.put("prefix", "saiadmin:menu_cache:user_");
        menuCache.put("expire", 604800); // 7天
        menuCache.put("tag", "saiadmin:menu_cache");
        data.put("menu_cache", menuCache);

        // 按钮缓存配置
        Map<String, Object> buttonCache = new HashMap<>();
        buttonCache.put("prefix", "saiadmin:button_cache:user_");
        buttonCache.put("expire", 7200); // 2小时
        buttonCache.put("all", "saiadmin:button_cache:all");
        buttonCache.put("role", "saiadmin:button_cache:role_");
        buttonCache.put("tag", "saiadmin:button_cache");
        data.put("button_cache", buttonCache);

        // 配置缓存配置
        Map<String, Object> configCache = new HashMap<>();
        configCache.put("expire", 31536000); // 1年
        configCache.put("prefix", "saiadmin:config_cache:config_");
        configCache.put("tag", "saiadmin:config_cache");
        data.put("config_cache", configCache);

        // 字典缓存配置
        Map<String, Object> dictCache = new HashMap<>();
        dictCache.put("expire", 31536000); // 1年
        dictCache.put("tag", "saiadmin:dict_cache");
        data.put("dict_cache", dictCache);

        // 反射缓存配置
        Map<String, Object> reflectionCache = new HashMap<>();
        reflectionCache.put("tag", "saiadmin:reflection");
        reflectionCache.put("expire", 31536000); // 1年
        reflectionCache.put("no_need", "saiadmin:reflection_cache:no_need_");
        reflectionCache.put("attr", "saiadmin:reflection_cache:attr_");
        data.put("reflection_cache", reflectionCache);

        return data;
    }

    /**
     * 获取菜单缓存详情
     */
    @RequestMapping(value = "menu")
    public Object getMenuCacheInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> menuCache = new HashMap<>();
        menuCache.put("prefix", "saiadmin:menu_cache:user_");
        menuCache.put("expire", 604800);
        menuCache.put("tag", "saiadmin:menu_cache");

        return R.ok("菜单缓存信息获取成功", menuCache);
    }

    /**
     * 获取按钮缓存详情
     */
    @RequestMapping(value = "button")
    public Object getButtonCacheInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> buttonCache = new HashMap<>();
        buttonCache.put("prefix", "saiadmin:button_cache:user_");
        buttonCache.put("expire", 7200);
        buttonCache.put("all", "saiadmin:button_cache:all");
        buttonCache.put("role", "saiadmin:button_cache:role_");
        buttonCache.put("tag", "saiadmin:button_cache");

        return R.ok("按钮缓存信息获取成功", buttonCache);
    }

    /**
     * 获取配置缓存详情
     */
    @RequestMapping(value = "config")
    public Object getConfigCacheInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> configCache = new HashMap<>();
        configCache.put("expire", 31536000);
        configCache.put("prefix", "saiadmin:config_cache:config_");
        configCache.put("tag", "saiadmin:config_cache");

        return R.ok("配置缓存信息获取成功", configCache);
    }

    /**
     * 获取字典缓存详情
     */
    @RequestMapping(value = "dict")
    public Object getDictCacheInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> dictCache = new HashMap<>();
        dictCache.put("expire", 31536000);
        dictCache.put("tag", "saiadmin:dict_cache");

        return R.ok("字典缓存信息获取成功", dictCache);
    }

    /**
     * 获取反射缓存详情
     */
    @RequestMapping(value = "reflection")
    public Object getReflectionCacheInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> reflectionCache = new HashMap<>();
        reflectionCache.put("tag", "saiadmin:reflection");
        reflectionCache.put("expire", 31536000);
        reflectionCache.put("no_need", "saiadmin:reflection_cache:no_need_");
        reflectionCache.put("attr", "saiadmin:reflection_cache:attr_");

        return R.ok("反射缓存信息获取成功", reflectionCache);
    }
}
