package com.abc.web.util;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

/**
 * Sa-Token 工具类
 */
@Component
public class SaTokenUtil {

    /**
     * 用户登录
     * @param userId 用户ID
     * @param username 用户名
     * @return 登录结果
     */
    public SaResult login(Long userId, String username) {
        // 登录授权，将用户ID作为登录标识
        StpUtil.login(userId);
        
        // 获取token
        String token = StpUtil.getTokenValue();
        
        // 构造返回结果
        JSONObject data = new JSONObject();
        data.put("access_token", token);
        data.put("token_type", "Bearer");
        data.put("expires_in", StpUtil.getTokenTimeout());
        data.put("user_id", userId);
        data.put("username", username);
        
        return SaResult.data(data);
    }

    /**
     * 用户登出
     * @return 登出结果
     */
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok("登出成功");
    }

    /**
     * 检查是否登录
     * @return 是否登录
     */
    public boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    public Object getLoginId() {
        return StpUtil.getLoginId();
    }

    /**
     * 获取当前登录用户ID（Long类型）
     * @return 用户ID
     */
    public Long getLoginIdAsLong() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 获取当前登录用户名
     * @return 用户名
     */
    public String getLoginName() {
        // 这里可以根据业务需要从数据库查询用户名
        return "user_" + StpUtil.getLoginIdAsString();
    }

    /**
     * 刷新token
     * @return 刷新结果
     */
    public SaResult refreshToken() {
        if (!StpUtil.isLogin()) {
            return SaResult.error("用户未登录");
        }
        
        // 重新登录以刷新token
        Long userId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        StpUtil.login(userId);
        
        String newToken = StpUtil.getTokenValue();
        
        JSONObject data = new JSONObject();
        data.put("access_token", newToken);
        data.put("token_type", "Bearer");
        data.put("expires_in", StpUtil.getTokenTimeout());
        
        return SaResult.data(data);
    }

    /**
     * 验证token
     * @param token token值
     * @return 验证结果
     */
    public SaResult verifyToken(String token) {
        try {
            // 设置临时token进行验证
            StpUtil.setTokenValue(token);
            if (StpUtil.isLogin()) {
                JSONObject data = new JSONObject();
                data.put("user_id", StpUtil.getLoginId());
                data.put("valid", true);
                return SaResult.data(data);
            } else {
                return SaResult.error("token无效");
            }
        } catch (Exception e) {
            return SaResult.error("token验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取token剩余有效时间
     * @return 剩余时间（秒）
     */
    public long getTokenTimeout() {
        return StpUtil.getTokenTimeout();
    }

    /**
     * 获取会话剩余有效时间
     * @return 剩余时间（秒）
     */
    public long getSessionTimeout() {
        return StpUtil.getSessionTimeout();
    }
}