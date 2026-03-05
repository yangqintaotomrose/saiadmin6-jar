package com.abc.web.timer;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;

import com.xtr.framework.hutool.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Component
public class TaskTimer {

	public static volatile SimpleDateFormat DAY_FMT = new SimpleDateFormat("yyyyMMdd");
	public static volatile SimpleDateFormat MONTH_FMT = new SimpleDateFormat("yyyyMM");
    public static volatile long refreshTime = 0l;
    private final Logger log = LoggerFactory.getLogger(TaskTimer.class);


   @Scheduled(fixedRate=20*60*1000)
    public void a1() throws Exception  {
        if(System.getProperty("os.name").indexOf("Mac")>=0 || System.getProperty("os.name").indexOf("Windows")>=0)
        {
             return;
        }
        log.info("刷新任务数据开始");
        BaseDao dao = new BaseDao("mysql7");

        try {
            dao.execSql("update huijia.shop_ip set exec='0'");
            dao.commit();
        } catch (Exception e) {
            e.printStackTrace();
            dao.rollback();
        }
        log.info("刷新任务数据结束。");
    }

}
