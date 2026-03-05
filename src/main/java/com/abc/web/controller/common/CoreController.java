package com.abc.web.controller.common;

import com.abc.web.annotation.Log;
import com.abc.web.controller.WebController;
import com.abc.web.domain.HttpStatus;
import com.abc.web.domain.R;
import com.abc.web.util.SaTokenUtil;
import com.abc.web.util.LoginHelper;
import com.abc.web.util.LoginLogUtil;
import cn.dev33.satoken.util.SaResult;
import java.util.Arrays;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xtr.framework.hutool.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 登录相关
 */
@RestController
@RequestMapping("/proxy/core/")
public class CoreController extends WebController {

    @Autowired
    private SaTokenUtil saTokenUtil;


    /**
     * 返回随机图形验证码
     */
    @RequestMapping(value = "captcha")
    public Object captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 生成随机验证码
        String captchaText = generateCaptchaText(4); // 生成4位验证码

        // 生成验证码图片
        BufferedImage image = generateCaptchaImage(captchaText);

        // 将验证码存储到session中（实际项目中建议存储到Redis）
        request.getSession().setAttribute("captcha", captchaText.toLowerCase());

        // 将图片转换为base64编码
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 返回验证码信息
        IData rspData = new IData();
        rspData.set("code", HttpStatus.SUCCESS);
        rspData.set("msg", "验证码生成成功");
        rspData.set("data", new IData()
            .set("captcha", captchaText)
            .set("image", "data:image/png;base64," + base64Image)
            .set("expireTime", System.currentTimeMillis() + 300000) // 5分钟后过期
        );

        return rspData;
    }

    @RequestMapping(value = "login")
    public Object login(HttpServletRequest request, HttpServletResponse response) {
        try {
            IData param = getIDataFromStream();
            String username = param.getString("username");
            String password = param.getString("password");
            String captcha = param.getString("code");

            // 参数校验
            if (username == null || username.isEmpty()) {
                return R.fail("用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return R.fail("密码不能为空");
            }
            if (captcha == null || captcha.isEmpty()) {
                return R.fail("验证码不能为空");
            }

            // 验证验证码
            String storedCaptcha = (String) request.getSession().getAttribute("captcha");
            if (storedCaptcha == null) {
                return R.fail("验证码已过期，请重新获取");
            }
            if (!storedCaptcha.equalsIgnoreCase(captcha)) {
                return R.fail("验证码错误");
            }

            // 清除已使用的验证码
            request.getSession().removeAttribute("captcha");

            // 查询用户信息
            BaseDao dao = BaseDao.getDao("");
            IData user = dao.queryByFirst(
                "SELECT * FROM sa_system_user WHERE username=? AND delete_time IS NULL",
                username
            );

            if (user == null) {
                // 记录登录失败日志
                LoginLogUtil.logLoginFailure(username, "用户名不存在", "用户输入不存在的用户名");
                return R.fail("用户名或密码错误");
            }

            // 验证密码（这里假设数据库中存储的是明文密码，实际项目中应该是加密的）
            String dbPassword = user.getString("password");
            // 先不验证密码
            // if (!password.equals(dbPassword)) {
            //     // 记录登录失败日志
            //     LoginLogUtil.logLoginFailure(username, "密码错误", "用户输入错误的密码");
            //     return R.fail("用户名或密码错误");
            // }

            // 检查用户状态
            Integer status = user.getInt("status");
            if (status != null && status == 0) {
                // 记录登录失败日志
                LoginLogUtil.logLoginFailure(username, "账户已被禁用", "用户账户状态为禁用");
                return R.fail("账户已被禁用");
            }

            // 使用Sa-Token进行登录
            Long userId = user.getLong("id");
            SaResult loginResult = saTokenUtil.login(userId, username);

            // 记录登录成功日志
            LoginLogUtil.logLoginSuccess(username, "用户登录成功");

            // 更新最后登录时间
            IData updateData = new IData();
            updateData.set("id", userId);
            updateData.set("login_time", dao.getSysTimeLocal());
            updateData.set("login_ip", getClientIp(request));
            updateData.set("update_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_system_user");
            dao.updateById(updateData);

            // 同时返回用户基本信息
            IData userInfo = new IData();
            userInfo.set("id", userId);
            userInfo.set("username", user.getString("username"));
            userInfo.set("realname", user.getString("realname"));
            userInfo.set("avatar", user.getString("avatar"));
            userInfo.set("gender", user.getString("gender"));
            userInfo.set("email", user.getString("email"));
            userInfo.set("phone", user.getString("phone"));
            userInfo.set("dept_id", user.getLong("dept_id"));
            userInfo.set("is_super", user.getInt("is_super"));

            // 将用户信息添加到返回结果中
            JSONObject resultData = (JSONObject) loginResult.getData();
            resultData.put("user", userInfo);
            return R.ok("登录成功", resultData);

        } catch (Exception e) {
            return R.fail("登录失败：" + e.getMessage());
        }
    }


    /**
     * 验证图形验证码
     */
    @RequestMapping(value = "verifyCaptcha")
    public Object verifyCaptcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        String userInput = param.getString("captcha");

        if (userInput == null || userInput.isEmpty()) {
            return R.fail("验证码不能为空");
        }

        // 从session中获取存储的验证码
        String storedCaptcha = (String) request.getSession().getAttribute("captcha");

        if (storedCaptcha == null) {
            return R.fail("验证码已过期，请重新获取");
        }

        // 验证验证码（忽略大小写）
        if (storedCaptcha.equalsIgnoreCase(userInput)) {
            // 验证成功后清除session中的验证码
            request.getSession().removeAttribute("captcha");
            return R.ok("验证码正确");
        } else {
            return R.fail("验证码错误");
        }
    }

    @Log(serviceName = "角色查询", remark = "获取角色列表信息")
    @RequestMapping(value = "accessRole")
    public Object accessRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_role t ");
        parser.addSQL(" WHERE 1=1 and status=1");
        IDataset list = dao.queryPage(parser, param,this.getSinglePage(param));
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });
        return successResponse(list);
    }


    @Log(serviceName = "菜单查询", remark = "根据角色获取菜单权限")
    @RequestMapping(value = "getMenuByRole")
    public Object getMenuByRole(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        // 根据角色获取菜单列表
        IDataset  menus = dao.queryList("SELECT * FROM sa_system_menu " +
                "where status=1 " +
                "and id in ( select menu_id from sa_system_role_menu where role_id='"+param.getString("id")+"' ) ");

        //处理数据
        menus.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time",obj.getString("create_time"));

        });
        IData role = new IData();
        role.set("id", param.getString("id"));
        role.set("menus", menus);

        return successResponse(role);
    }


    @Log(serviceName = "菜单授权", remark = "为角色分配菜单权限")
    @RequestMapping(value = "menuPermission")
    public Object menuPermission(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray menu_ids =(JSONArray)param.getObj("menu_ids");
        // 删除现有菜单
        dao.delete("sa_system_role_menu", "role_id", param.getString("id"));
        // 批量插入菜单
        for (int i = 0; i < menu_ids.size(); i++) {
            Integer a = menu_ids.getInteger( i);
            IData item = new IData();
            item.set("menu_id", a);
            item.set("role_id", param.getString("id"));
            item.setTableName("sa_system_role_menu");
            dao.insertExt(item);
        }
        return R.ok("授权成功");
    }

    /**
     * 获取角色信息详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM role_info WHERE id=?", param.getLong("id"));
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加角色信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time",dao.getSysTimeLocal());
        info.setTableName("sa_system_role");
        dao.insert(info);
        return R.ok("角色信息添加成功");
    }

    /**
     * 编辑角色信息
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.setTableName("sa_system_role");
        dao.updateById(info);
        return R.ok("角色信息修改成功");
    }

    /**
     * 删除角色信息
     */
    @RequestMapping(value = "destroy")
    public Object delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids =(JSONArray)param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger( i);
            dao.delete("sa_system_role", "id", a);
        }
        return R.ok("删除成功");
    }

    /**
     * 批量删除角色信息
     */
    @RequestMapping(value = "deletes")
    public Object deletes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            dao.delete("role_info", "id", Integer.parseInt(id));
        }
        return R.ok("批量删除成功");
    }

    /**
     * 获取当前用户信息
     */
    @RequestMapping(value = "getUserInfo")
    public Object getUserInfo(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 从session获取用户信息
            IData user = (IData) request.getSession().getAttribute("currentUser");
            if (user == null) {
                return R.fail("用户未登录");
            }

            // 构建返回的用户信息
            IData userInfo = new IData();
            userInfo.set("id", user.getLong("id"));
            userInfo.set("username", user.getString("username"));
            userInfo.set("realname", user.getString("realname"));
            userInfo.set("avatar", user.getString("avatar"));
            userInfo.set("gender", user.getString("gender"));
            userInfo.set("email", user.getString("email"));
            userInfo.set("phone", user.getString("phone"));
            userInfo.set("signed", user.getString("signed"));
            userInfo.set("dashboard", user.getString("dashboard"));
            userInfo.set("dept_id", user.getLong("dept_id"));
            userInfo.set("is_super", user.getInt("is_super"));
            userInfo.set("status", user.getInt("status"));
            userInfo.set("remark", user.getString("remark"));
            userInfo.set("login_time", user.getString("login_time"));
            userInfo.set("login_ip", user.getString("login_ip"));
            userInfo.set("create_time", user.getString("create_time"));
            userInfo.set("update_time", user.getString("update_time"));

            return R.ok(userInfo);

        } catch (Exception e) {
            return R.fail("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @RequestMapping(value = "logout")
    public Object logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取当前登录用户名
            String username = LoginHelper.getUsername();

            // 清除session中的用户信息
            request.getSession().removeAttribute("currentUser");
            request.getSession().removeAttribute("userId");
            request.getSession().removeAttribute("username");
            request.getSession().invalidate();

            // 记录登出日志
            if (username != null) {
                LoginLogUtil.logLogin(username, true, "用户主动登出", "用户正常退出系统");
            }

            return R.ok("退出成功");
        } catch (Exception e) {
            // 记录登出异常日志
            String username = LoginHelper.getUsername();
            if (username != null) {
                LoginLogUtil.logLogin(username, false, "登出异常: " + e.getMessage(), "用户登出过程中发生异常");
            }
            return R.fail("退出失败：" + e.getMessage());
        }
    }

    /**
     * 刷新访问令牌
     */
    @RequestMapping(value = "refresh")
    public Object refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 使用Sa-Token刷新token
            SaResult result = saTokenUtil.refreshToken();
            if (result.getCode() == 200) {
                return R.ok("令牌刷新成功", result.getData());
            } else {
                return R.fail(result.getMsg());
            }
        } catch (Exception e) {
            return R.fail("令牌刷新失败：" + e.getMessage());
        }
    }

    /**
     * 测试 LoginHelper 功能
     */
    @RequestMapping(value = "testLoginHelper")
    public Object testLoginHelper(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 检查登录状态
            boolean isLoggedIn = LoginHelper.isLogin();

            IData result = new IData();
            result.set("is_logged_in", isLoggedIn);

            if (isLoggedIn) {
                // 获取各种用户信息
                result.set("user_id", LoginHelper.getUserId());
                result.set("username", LoginHelper.getUsername());
                result.set("real_name", LoginHelper.getRealName());
                result.set("email", LoginHelper.getEmail());
                result.set("phone", LoginHelper.getPhone());
                result.set("dept_id", LoginHelper.getDeptId());
                result.set("gender", LoginHelper.getGender());
                result.set("avatar", LoginHelper.getAvatar());
                result.set("is_super_admin", LoginHelper.isSuperAdmin());
                result.set("role_ids", Arrays.asList(LoginHelper.getUserRoleIds()));
                result.set("session_timeout", LoginHelper.getSessionTimeout());
                result.set("token_timeout", LoginHelper.getTokenTimeout());
                result.set("login_device", LoginHelper.getLoginDevice());
                result.set("login_type", LoginHelper.getLoginType());

                // 获取完整用户信息
                IData currentUser = LoginHelper.getCurrentUser();
                result.set("full_user_info", currentUser);
            }

            return R.ok("LoginHelper 测试结果", result);
        } catch (Exception e) {
            return R.fail("测试失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        return ip;
    }

    /**
     * 从请求中获取token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从Header中获取
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        // 从参数中获取
        token = request.getParameter("token");
        if (token != null && !token.isEmpty()) {
            return token;
        }

        return null;
    }

    /**
     * 生成随机验证码文本
     */
    private String generateCaptchaText(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 排除容易混淆的字符
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片
     */
    private BufferedImage generateCaptchaImage(String captchaText) {
        int width = 120;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // 绘制干扰线
        Random random = new Random();
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 10; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码文字
        Font font = new Font("Arial", Font.BOLD, 24);
        g2d.setFont(font);

        for (int i = 0; i < captchaText.length(); i++) {
            // 随机颜色
            g2d.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));

            // 随机旋转角度
            double theta = (random.nextDouble() - 0.5) * 0.3;
            AffineTransform transform = new AffineTransform();
            transform.rotate(theta, 20 + i * 25, 25);
            g2d.setTransform(transform);

            // 绘制字符
            g2d.drawString(String.valueOf(captchaText.charAt(i)), 15 + i * 25, 28);
        }

        g2d.dispose();
        return image;
    }
}
