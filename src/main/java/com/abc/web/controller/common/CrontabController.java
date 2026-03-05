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
 * 定时任务控制器 - 根据sa_tool_crontab表结构生成
 */
@RestController
@RequestMapping("/proxy/tool/crontab/")
public class CrontabController extends WebController {

    /**
     * 查询定时任务列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.* ");
        parser.addSQL(" FROM sa_tool_crontab t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
        parser.addSQL(" AND t.type = :type ");
        parser.addSQL(" AND t.target LIKE CONCAT('%', :target, '%') ");
        parser.addSQL(" AND t.task_style = :taskStyle ");
        parser.addSQL(" AND t.rule LIKE CONCAT('%', :rule, '%') ");
        parser.addSQL(" AND t.singleton = :singleton ");
        parser.addSQL(" AND t.status = :status ");
        parser.addSQL(" AND t.remark LIKE CONCAT('%', :remark, '%') ");
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
     * 获取定时任务详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_tool_crontab WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("定时任务不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加定时任务
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_tool_crontab");
        dao.insert(info);
        return R.ok("定时任务添加成功");
    }

    /**
     * 编辑定时任务
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_tool_crontab");
        dao.updateById(info);
        return R.ok("定时任务修改成功");
    }

    /**
     * 删除定时任务（逻辑删除）
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
            updateData.setTableName("sa_tool_crontab");
            dao.updateById(updateData);
        }
        return R.ok("定时任务删除成功");
    }

    /**
     * 批量删除定时任务（逻辑删除）
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
            updateData.setTableName("sa_tool_crontab");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的定时任务
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
            updateData.setTableName("sa_tool_crontab");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }

    /**
     * 启用定时任务
     */
    @RequestMapping(value = "enable")
    public Object enable(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            IData updateData = new IData();
            updateData.set("id", Integer.parseInt(id));
            updateData.set("status", 1); // 1表示正常状态
            updateData.set("update_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_tool_crontab");
            dao.updateById(updateData);
        }
        return R.ok("定时任务启用成功");
    }

    /**
     * 停用定时任务
     */
    @RequestMapping(value = "disable")
    public Object disable(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String ids = param.getString("ids");
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            IData updateData = new IData();
            updateData.set("id", Integer.parseInt(id));
            updateData.set("status", 2); // 2表示停用状态
            updateData.set("update_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_tool_crontab");
            dao.updateById(updateData);
        }
        return R.ok("定时任务停用成功");
    }

    /**
     * 获取任务类型选项
     */
    @RequestMapping(value = "types")
    public Object getTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONArray types = new JSONArray();
        // 任务类型选项
        types.add(createOption(1, "Shell脚本"));
        types.add(createOption(2, "Python脚本"));
        types.add(createOption(3, "Java程序"));
        types.add(createOption(4, "HTTP请求")); // 默认值
        types.add(createOption(5, "数据库脚本"));
        return R.ok(types);
    }

    /**
     * 获取执行类型选项
     */
    @RequestMapping(value = "taskStyles")
    public Object getTaskStyles(HttpServletRequest request, HttpServletResponse response) {
        JSONArray styles = new JSONArray();
        // 执行类型选项
        styles.add(createOption(1, "一次性执行"));
        styles.add(createOption(2, "循环执行"));
        styles.add(createOption(3, "定时执行"));
        return R.ok(styles);
    }

    /**
     * 获取单次执行选项
     */
    @RequestMapping(value = "singletons")
    public Object getSingletons(HttpServletRequest request, HttpServletResponse response) {
        JSONArray singletons = new JSONArray();
        // 是否单次执行选项
        singletons.add(createOption(1, "是"));
        singletons.add(createOption(2, "否"));
        return R.ok(singletons);
    }

    /**
     * 获取状态选项
     */
    @RequestMapping(value = "statuses")
    public Object getStatuses(HttpServletRequest request, HttpServletResponse response) {
        JSONArray statuses = new JSONArray();
        // 状态选项
        statuses.add(createOption(1, "正常"));
        statuses.add(createOption(2, "停用"));
        return R.ok(statuses);
    }

    /**
     * 创建选项对象
     */
    private IData createOption(int value, String label) {
        IData option = new IData();
        option.set("value", value);
        option.set("label", label);
        return option;
    }
}
