package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 邮件记录控制器 - 根据sa_system_mail表结构生成
 */
@RestController
@RequestMapping("/proxy/core/email/")
public class EmailController extends WebController {

    /**
     * 查询邮件记录列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.* ");
        parser.addSQL(" FROM sa_system_mail t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.gateway LIKE CONCAT('%', :gateway, '%') ");
        parser.addSQL(" AND t.from LIKE CONCAT('%', :from, '%') ");
        parser.addSQL(" AND t.email LIKE CONCAT('%', :email, '%') ");
        parser.addSQL(" AND t.code LIKE CONCAT('%', :code, '%') ");
        parser.addSQL(" AND t.content LIKE CONCAT('%', :content, '%') ");
        parser.addSQL(" AND t.status LIKE CONCAT('%', :status, '%') ");
        parser.addSQL(" AND t.response LIKE CONCAT('%', :response, '%') ");
        parser.addSQL(" AND t.create_time >= :startTime ");
        parser.addSQL(" AND t.create_time <= :endTime ");
        parser.addSQL(" AND t.delete_time IS NULL "); // 只查询未删除的记录
        parser.addSQL(" ORDER BY t.id DESC ");
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time", obj.getString("create_time"));
            obj.set("update_time", obj.getString("update_time"));
        });
        return wrapPageQueryList(list, page);
    }

    /**
     * 获取邮件记录详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_system_mail WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("邮件记录不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加邮件记录
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_mail");
        dao.insert(info);
        return R.ok("邮件记录添加成功");
    }

    /**
     * 编辑邮件记录
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_mail");
        dao.updateById(info);
        return R.ok("邮件记录修改成功");
    }

    /**
     * 删除邮件记录（逻辑删除）
     */
    @RequestMapping(value = "destroy")
    public Object delete(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        JSONArray ids = (JSONArray) param.getObj("ids");
        for (int i = 0; i < ids.size(); i++) {
            Integer a = ids.getInteger(i);
            IData updateData = new IData();
            updateData.set("id", a);
            updateData.set("delete_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_system_mail");
            dao.updateById(updateData);
        }
        return R.ok("邮件记录删除成功");
    }

    /**
     * 批量删除邮件记录（逻辑删除）
     */
    @RequestMapping(value = "deletes")
    public Object deletes(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            IData updateData = new IData();
            updateData.set("id", Integer.parseInt(id));
            updateData.set("delete_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_system_mail");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的邮件记录
     */
    @RequestMapping(value = "restore")
    public Object restore(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            IData updateData = new IData();
            updateData.set("id", Integer.parseInt(id));
            updateData.set("delete_time", null);
            updateData.setTableName("sa_system_mail");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }

}
