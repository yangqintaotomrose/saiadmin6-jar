package com.abc.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的Controller方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    
    /**
     * 业务名称
     */
    String serviceName() default "";
    
    /**
     * 备注信息
     */
    String remark() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean recordRequest() default true;
    
    /**
     * 是否记录响应数据
     */
    boolean recordResponse() default true;
}