package com.abc.config;

import com.abc.web.domain.R;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.IData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * Session认证拦截器
 */
@Component
public class SessionAuthenticationInterceptor implements HandlerInterceptor {

    // Sa-Token 自动处理认证，无需手动注入

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        
        // 放行不需要认证的接口
        if (shouldNotFilter(requestURI)) {
            return true;
        }
        
        // 使用Sa-Token进行认证
        if (!StpUtil.isLogin()) {
            sendErrorResponse(response, "用户未登录");
            return false;
        }
        
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户信息
        BaseDao dao = BaseDao.getDao("");
        IData user = dao.queryByFirst(
            "SELECT * FROM sa_system_user WHERE id=? AND delete_time IS NULL",
            userId
        );
        
        if (user == null) {
            sendErrorResponse(response, "用户不存在");
            return false;
        }
        
        // 检查用户状态
        Integer status = user.getInt("status");
        if (status != null && status == 0) {
            sendErrorResponse(response, "账户已被禁用");
            return false;
        }
        
        // 将用户信息存入request属性中
        request.setAttribute("currentUser", user);
        request.setAttribute("userId", userId);
        request.setAttribute("username", user.getString("username"));
        
        return true;
    }

    /**
     * 判断是否不需要过滤的请求
     */
    private boolean shouldNotFilter(String requestURI) {
        // 登录、验证码等公开接口不需要认证
        return requestURI.endsWith("/proxy/core/login") ||
               requestURI.endsWith("/proxy/core/captcha") ||
               requestURI.endsWith("/proxy/core/verifyCaptcha") ||
               requestURI.contains("/static/") ||
               requestURI.contains("/favicon.ico");
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        R<Void> result = R.fail(401, message);
        PrintWriter writer = response.getWriter();
        writer.write(JSON.toJSONString(result));
        writer.flush();
        writer.close();
    }
}