package com.abc.config;

import com.xtr.framework.hutool.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * dao数据源拦截器子，自动对request对象注入dao对象
 */
@Component
public class DaoInterceptor implements HandlerInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(DaoInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o)
			throws Exception {
		//System.out.println(this);
		System.out.println("请求路径:"+request.getRequestURI() + "  " + request.getMethod());
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o,
                           ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse httpServletResponse, Object o,
                                Exception e) throws Exception {
		logger.info("数据源请求后拦截-----");
		// 提交数据库连接
		HashMap<String, BaseDao> daos = BaseDao.getThreadLocal().get();
		if(daos == null){
			//没有读取数据库
			return;
		}
		for(String key:daos.keySet())
		{
			boolean hasException = false;
			if(null != e)
			{
				hasException = true;
			}
			if (null != request.getAttribute("_exception")) {
				hasException = true;
			}
			if(hasException){
				//有异常回滚数据
				daos.get(key).rollback();
			}else{
				//无异常提交数据
				daos.get(key).commit();
			}

		}
	}
}
