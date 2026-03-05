package com.abc.config;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private DaoInterceptor daoInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(daoInterceptor).addPathPatterns("/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置本地文件访问路径映射
        String imgsPath = "/data/imgs/";
        File imgDir = new File(imgsPath);
        if (!imgDir.exists()) {
            imgDir.mkdirs();
        }
        
        // 映射 /imgs/** 到本地文件系统
        registry.addResourceHandler("/imgs/**")
                .addResourceLocations("file:" + imgsPath)
                .setCachePeriod(3600) // 缓存1小时
                .resourceChain(true);
        
        // 添加其他可能的静态资源路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        registry.addResourceHandler("/public/**")
                .addResourceLocations("classpath:/public/");
    }
}
