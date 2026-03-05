package com.abc.config;


import com.abc.web.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 *
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常:", requestURI, e);
        //设置异常信息，用于dao拦截时的回滚
        request.setAttribute("_exception",e);
        return R.fail(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常:", requestURI, e);
        //设置异常信息，用于dao拦截时的回滚
        request.setAttribute("_exception",e);
        return R.fail(e.getMessage());
    }

    // /**
    //  * 演示模式异常
    //  */
    // @ExceptionHandler(DemoModeException.class)
    // public R<Void> handleDemoModeException(DemoModeException e) {
    //     return R.fail("演示模式，不允许操作");
    // }

}
