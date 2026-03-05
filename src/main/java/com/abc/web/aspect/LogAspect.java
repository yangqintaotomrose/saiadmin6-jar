package com.abc.web.aspect;

import com.abc.web.annotation.Log;
import com.abc.web.controller.WebController;
import com.abc.web.util.Ip2regionUtil;
import com.abc.web.util.LoginHelper;
import com.abc.web.util.OperationLogUtil;
import com.alibaba.fastjson.JSON;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.IData;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志切面类
 * 自动拦截带有@Log注解的方法，记录操作日志到数据库
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    @Around("@annotation(com.abc.web.annotation.Log)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        // 获取请求信息
        HttpServletRequest request = getCurrentRequest();
        String username = LoginHelper.getUsername();
        if (username == null) {
            username = "anonymous";
        }

        String ip = getClientIpAddress(request);
        String ipLocation = Ip2regionUtil.getIpLocation(ip);

        // 记录请求参数
        String requestData = "";
        if (logAnnotation.recordRequest()) {
            // Object[] args = joinPoint.getArgs();
            // try {
            //     requestData = JSON.toJSONString(args);
            // } catch (Exception e) {
            //     requestData = "参数序列化失败";
            //     logger.warn("请求参数序列化失败", e);
            // }
            IData param =WebController.getIDataFromStreamExt();
            requestData = JSON.toJSONString(param);
        }

        Object result = null;
        String responseData = "";
        boolean success = true;
        String errorMessage = "";

        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录响应数据
            if (logAnnotation.recordResponse()) {
                try {
                    responseData = JSON.toJSONString(result);
                } catch (Exception e) {
                    responseData = "响应数据序列化失败";
                    logger.warn("响应数据序列化失败", e);
                }
            }

        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // 记录操作日志到数据库
            try {
                recordOperationLog(
                    username,
                    request,
                    logAnnotation,
                    ip,
                    ipLocation,
                    requestData,
                    responseData,
                    success,
                    errorMessage,
                    System.currentTimeMillis() - startTime
                );
            } catch (Exception e) {
                logger.error("记录操作日志失败", e);
            }
        }

        return result;
    }

    /**
     * 记录操作日志到数据库
     */
    private void recordOperationLog(String username, HttpServletRequest request, Log logAnnotation,
                                  String ip, String ipLocation, String requestData,
                                  String responseData, boolean success, String errorMessage,
                                  long executeTime) {
        try {
            // 构建日志数据
            IData logData = new IData();
            logData.set("username", username);
            logData.set("app", "系统管理平台");
            logData.set("method", request != null ? request.getMethod() : "UNKNOWN");
            logData.set("router", request != null ? request.getRequestURI() : "UNKNOWN");
            logData.set("service_name", logAnnotation.serviceName());
            logData.set("ip", ip);
            logData.set("ip_location", ipLocation);
            logData.set("request_data", requestData);
            logData.set("resp_data", responseData);

            // 设置备注信息
            String remark = logAnnotation.remark();
            if (!success) {
                remark += " [失败] " + errorMessage;
            }
            remark += " [耗时: " + executeTime + "ms]";
            logData.set("remark", remark);

            logData.set("created_by", LoginHelper.getUserId());
            logData.set("create_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 插入数据库
            BaseDao dao = BaseDao.getDao("");
            dao.insert("sa_system_oper_log", logData);

            logger.info("操作日志记录成功: 用户={}, 业务={}, IP={}, 耗时={}ms",
                       username, logAnnotation.serviceName(), ip, executeTime);

        } catch (Exception e) {
            logger.error("记录操作日志到数据库失败", e);
        }
    }

    /**
     * 获取当前请求对象
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return attributes.getRequest();
        } catch (IllegalStateException e) {
            logger.debug("无法获取当前请求上下文", e);
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

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
}
