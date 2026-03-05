package com.abc.web.service;

import com.abc.web.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * JWT Redis服务类
 * 用于管理JWT令牌在Redis中的存储和验证
 */
@Service
public class JwtRedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    // Redis key前缀
    private static final String ACCESS_TOKEN_PREFIX = "jwt:access:";
    private static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";
    private static final String USER_TOKEN_PREFIX = "jwt:user:";

    /**
     * 存储访问令牌
     */
    public void storeAccessToken(String token, Long userId) {
        String key = ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, 8, TimeUnit.HOURS);
        
        // 同时存储用户到令牌的映射
        String userKey = USER_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(userKey, token, 8, TimeUnit.HOURS);
    }

    /**
     * 存储刷新令牌
     */
    public void storeRefreshToken(String token, Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, 7, TimeUnit.DAYS);
    }

    /**
     * 验证访问令牌
     */
    public boolean validateAccessToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }

        String storedToken = (String) redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
        
        return token.equals(storedToken) && !jwtUtil.isTokenExpired(token);
    }

    /**
     * 验证刷新令牌
     */
    public boolean validateRefreshToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return false;
        }

        String storedToken = (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        
        return token.equals(storedToken) && !jwtUtil.isTokenExpired(token);
    }

    /**
     * 通过用户ID获取访问令牌
     */
    public String getAccessTokenByUserId(Long userId) {
        return (String) redisTemplate.opsForValue().get(ACCESS_TOKEN_PREFIX + userId);
    }

    /**
     * 通过用户ID获取刷新令牌
     */
    public String getRefreshTokenByUserId(Long userId) {
        return (String) redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
    }

    /**
     * 通过访问令牌获取用户ID
     */
    public Long getUserIdByAccessToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 刷新令牌
     */
    public String refreshToken(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        
        // 生成新的访问令牌
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        
        // 更新Redis中的访问令牌
        storeAccessToken(newAccessToken, userId);
        
        return newAccessToken;
    }

    /**
     * 删除用户的令牌
     */
    public void removeUserTokens(Long userId) {
        redisTemplate.delete(ACCESS_TOKEN_PREFIX + userId);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        redisTemplate.delete(USER_TOKEN_PREFIX + userId);
    }

    /**
     * 黑名单令牌（用于强制登出）
     */
    public void blacklistToken(String token, Long userId) {
        String blacklistKey = "jwt:blacklist:" + token;
        // 设置较短的过期时间，通常是令牌剩余的有效期
        long expireTime = getRemainingTime(token);
        if (expireTime > 0) {
            redisTemplate.opsForValue().set(blacklistKey, "blacklisted", expireTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查令牌是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = "jwt:blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    /**
     * 获取令牌剩余时间
     */
    private long getRemainingTime(String token) {
        try {
            // 简化实现，返回固定的过期时间
            // 实际项目中应该解析JWT获取准确的过期时间
            return 28800000L; // 8小时
        } catch (Exception e) {
            return 0;
        }
    }
}