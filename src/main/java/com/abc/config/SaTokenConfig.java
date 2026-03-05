package com.abc.config;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfig {

    /**
     * Sa-Token 权限认证接口实现
     */
    @Bean
    public StpInterface stpInterface() {
        return new StpInterface() {
            /**
             * 返回一个账号所拥有的权限码集合
             */
            @Override
            public List<String> getPermissionList(Object loginId, String loginType) {
                // 这里可以从数据库查询用户的权限列表
                // 示例：根据loginId查询用户权限
                List<String> list = new ArrayList<>();
                // list.add("user-add");
                // list.add("user-delete");
                // list.add("user-update");
                // list.add("user-get");
                return list;
            }

            /**
             * 返回一个账号所拥有的角色标识集合
             */
            @Override
            public List<String> getRoleList(Object loginId, String loginType) {
                // 这里可以从数据库查询用户的角色列表
                // 示例：根据loginId查询用户角色
                List<String> list = new ArrayList<>();
                // list.add("admin");
                // list.add("super-admin");
                return list;
            }
        };
    }

    /**
     * Sa-Token 整合 jwt (简单模式)
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}