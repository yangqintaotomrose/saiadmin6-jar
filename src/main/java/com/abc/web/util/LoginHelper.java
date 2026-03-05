package com.abc.web.util;

import cn.dev33.satoken.stp.StpUtil;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.IData;
import org.springframework.stereotype.Component;

/**
 * 登录助手类
 * 提供获取当前登录用户信息的便捷方法
 */
@Component
public class LoginHelper {

    /**
     * 获取当前登录用户ID
     * @return 用户ID，未登录时返回null
     */
    public static Long getUserId() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsLong();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     * @return 用户名，未登录时返回null
     */
    public static String getUsername() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginIdAsString();
        }
        return null;
    }

    /**
     * 检查用户是否已登录
     * @return true表示已登录，false表示未登录
     */
    public static boolean isLogin() {
        return StpUtil.isLogin();
    }

    /**
     * 获取当前登录用户的完整信息
     * @return 用户信息对象，未登录时返回null
     */
    public static IData getCurrentUser() {
        Long userId = getUserId();
        if (userId == null) {
            return null;
        }
        
        // 从数据库查询用户详细信息
        BaseDao dao = BaseDao.getDao("");
        return dao.queryByFirst(
            "SELECT * FROM sa_system_user WHERE id=? AND delete_time IS NULL",
            userId
        );
    }

    /**
     * 获取当前登录用户的角色ID列表
     * @return 角色ID数组，未登录时返回空数组
     */
    public static Long[] getUserRoleIds() {
        Long userId = getUserId();
        if (userId == null) {
            return new Long[0];
        }
        
        // 查询用户角色关系
        BaseDao dao = BaseDao.getDao("");
        Object dataset = dao.queryList(
            "SELECT role_id FROM sa_system_user_role WHERE user_id=?",
            userId
        );
        
        if (dataset != null) {
            // 根据实际返回类型处理
            if (dataset instanceof com.xtr.framework.hutool.IDataset) {
                com.xtr.framework.hutool.IDataset dataList = (com.xtr.framework.hutool.IDataset) dataset;
                if (dataList.size() > 0) {
                    Long[] roleIds = new Long[dataList.size()];
                    for (int i = 0; i < dataList.size(); i++) {
                        IData item = (IData) dataList.get(i);
                        roleIds[i] = item.getLong("role_id");
                    }
                    return roleIds;
                }
            }
        }
        
        return new Long[0];
    }

    /**
     * 检查当前用户是否拥有指定角色
     * @param roleId 角色ID
     * @return true表示拥有该角色，false表示没有
     */
    public static boolean hasRole(Long roleId) {
        if (roleId == null) {
            return false;
        }
        
        Long[] roleIds = getUserRoleIds();
        for (Long id : roleIds) {
            if (roleId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前用户是否为超级管理员
     * @return true表示是超级管理员，false表示不是
     */
    public static boolean isSuperAdmin() {
        IData user = getCurrentUser();
        if (user != null) {
            Integer isSuper = user.getInt("is_super");
            return isSuper != null && isSuper == 1;
        }
        return false;
    }

    /**
     * 获取当前登录用户的部门ID
     * @return 部门ID，未登录或无部门时返回null
     */
    public static Long getDeptId() {
        IData user = getCurrentUser();
        if (user != null) {
            return user.getLong("dept_id");
        }
        return null;
    }

    /**
     * 获取当前登录用户的邮箱
     * @return 邮箱地址，未登录时返回null
     */
    public static String getEmail() {
        IData user = getCurrentUser();
        if (user != null) {
            return user.getString("email");
        }
        return null;
    }

    /**
     * 获取当前登录用户的手机号
     * @return 手机号，未登录时返回null
     */
    public static String getPhone() {
        IData user = getCurrentUser();
        if (user != null) {
            return user.getString("phone");
        }
        return null;
    }

    /**
     * 获取当前登录用户的真实姓名
     * @return 真实姓名，未登录时返回null
     */
    public static String getRealName() {
        IData user = getCurrentUser();
        if (user != null) {
            return user.getString("realname");
        }
        return null;
    }

    /**
     * 获取当前登录用户的性别
     * @return 性别代码，未登录时返回null
     */
    public static String getGender() {
        IData user = getCurrentUser();
        if (user != null) {
            return user.getString("gender");
        }
        return null;
    }

    /**
     * 获取当前登录用户的头像URL
     * @return 头像URL，未登录时返回null
     */
    public static String getAvatar() {
        IData user = getCurrentUser();
        if (user != null) {
            return user.getString("avatar");
        }
        return null;
    }

    /**
     * 强制获取当前登录用户ID（未登录时抛出异常）
     * @return 用户ID
     * @throws RuntimeException 当用户未登录时抛出异常
     */
    public static Long requireUserId() {
        Long userId = getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }
        return userId;
    }

    /**
     * 强制获取当前登录用户信息（未登录时抛出异常）
     * @return 用户信息对象
     * @throws RuntimeException 当用户未登录时抛出异常
     */
    public static IData requireCurrentUser() {
        IData user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("无法获取用户信息");
        }
        return user;
    }

    /**
     * 获取当前登录用户的会话超时时间
     * @return 剩余超时时间（秒）
     */
    public static long getSessionTimeout() {
        if (StpUtil.isLogin()) {
            return StpUtil.getSessionTimeout();
        }
        return 0;
    }

    /**
     * 获取当前登录用户的Token超时时间
     * @return 剩余超时时间（秒）
     */
    public static long getTokenTimeout() {
        if (StpUtil.isLogin()) {
            return StpUtil.getTokenTimeout();
        }
        return 0;
    }

    /**
     * 刷新当前用户的会话
     */
    public static void refreshSession() {
        if (StpUtil.isLogin()) {
            StpUtil.updateLastActiveToNow();
        }
    }

    /**
     * 获取当前登录设备的标识
     * @return 设备标识
     */
    public static String getLoginDevice() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginDevice();
        }
        return null;
    }

    /**
     * 获取当前登录类型
     * @return 登录类型
     */
    public static String getLoginType() {
        if (StpUtil.isLogin()) {
            return StpUtil.getLoginType();
        }
        return null;
    }
}