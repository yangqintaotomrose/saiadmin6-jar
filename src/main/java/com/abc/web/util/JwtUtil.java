package com.abc.web.util;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 简化的JWT工具类（使用Base64编码模拟JWT）
 */
@Component
public class JwtUtil {

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username) {
        // 模拟JWT payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", "webman.tinywan.cn");
        payload.put("aud", "webman.tinywan.cn");
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("nbf", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() / 1000) + 28800); // 8小时过期
        payload.put("type", "access");
        
        Map<String, Object> extend = new HashMap<>();
        extend.put("access_exp", 28800);
        extend.put("id", userId);
        extend.put("username", username);
        extend.put("plat", "saiadmin");
        
        payload.put("extend", extend);
        
        // 简单的Base64编码模拟JWT
        String header = Base64.getEncoder().encodeToString("{\"typ\":\"JWT\",\"alg\":\"HS256\"}".getBytes());
        String payloadStr = Base64.getEncoder().encodeToString(payload.toString().getBytes());
        String signature = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        
        return header + "." + payloadStr + "." + signature;
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        // 模拟JWT payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("iss", "webman.tinywan.cn");
        payload.put("aud", "webman.tinywan.cn");
        payload.put("iat", System.currentTimeMillis() / 1000);
        payload.put("nbf", System.currentTimeMillis() / 1000);
        payload.put("exp", (System.currentTimeMillis() / 1000) + 604800); // 7天过期
        payload.put("type", "refresh");
        
        Map<String, Object> extend = new HashMap<>();
        extend.put("access_exp", 28800);
        extend.put("id", userId);
        extend.put("username", username);
        extend.put("plat", "saiadmin");
        
        payload.put("extend", extend);
        
        // 简单的Base64编码模拟JWT
        String header = Base64.getEncoder().encodeToString("{\"typ\":\"JWT\",\"alg\":\"HS256\"}".getBytes());
        String payloadStr = Base64.getEncoder().encodeToString(payload.toString().getBytes());
        String signature = UUID.randomUUID().toString().replace("-", "").substring(0, 32);
        
        return header + "." + payloadStr + "." + signature;
    }

    /**
     * 验证令牌（简化版）
     */
    public boolean validateToken(String token) {
        try {
            if (token == null || token.split("\\.").length != 3) {
                return false;
            }
            // 简单验证，实际项目中应该验证签名和过期时间
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            // 简化解析，实际应该使用JSON解析库
            if (payloadStr.contains("\"id\":")) {
                int startIndex = payloadStr.indexOf("\"id\":") + 5;
                int endIndex = payloadStr.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = payloadStr.indexOf("}", startIndex);
                }
                String idStr = payloadStr.substring(startIndex, endIndex).trim();
                return Long.parseLong(idStr.replaceAll("[^0-9]", ""));
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            // 简化解析
            if (payloadStr.contains("\"username\":")) {
                int startIndex = payloadStr.indexOf("\"username\":\"") + 12;
                int endIndex = payloadStr.indexOf("\"", startIndex);
                return payloadStr.substring(startIndex, endIndex);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从令牌中获取令牌类型
     */
    public String getTokenTypeFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            // 简化解析
            if (payloadStr.contains("\"type\":")) {
                int startIndex = payloadStr.indexOf("\"type\":\"") + 8;
                int endIndex = payloadStr.indexOf("\"", startIndex);
                return payloadStr.substring(startIndex, endIndex);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return true;
            
            String payloadStr = new String(Base64.getDecoder().decode(parts[1]));
            // 简化解析过期时间
            if (payloadStr.contains("\"exp\":")) {
                int startIndex = payloadStr.indexOf("\"exp\":") + 6;
                int endIndex = payloadStr.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = payloadStr.indexOf("}", startIndex);
                }
                String expStr = payloadStr.substring(startIndex, endIndex).trim();
                long expTime = Long.parseLong(expStr);
                long currentTime = System.currentTimeMillis() / 1000;
                return currentTime > expTime;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}