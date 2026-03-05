package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.HttpStatus;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtr.framework.hutool.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 参数配置信息控制器 - 根据参数配置信息JSON Schema生成，保持与ConfigGroupController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/config/")
public class ConfigController extends WebController {

    /**
     * 查询参数配置信息列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_system_config t ");
        parser.addSQL(" LEFT JOIN sa_system_config_group g ON t.group_id = g.id ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.name LIKE CONCAT('%', :name, '%') ");
        parser.addSQL(" AND t.key LIKE CONCAT('%', :key, '%') ");
        parser.addSQL(" AND t.group_id = :group_id ");
        parser.addSQL(" AND t.delete_time IS NULL ");
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        //处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            if(StringUtils.isNotEmpty(obj.getString("config_select_data")))
            {
                obj.set("config_select_data", JSONArray.parseArray(obj.getString("config_select_data")));
            }else{
                obj.set("config_select_data", null);
            }
            obj.set("update_time", obj.getString("update_time"));
        });

        return successResponse(list);
    }

    /**
     * 获取参数配置信息详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_system_config WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("参数配置信息不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加参数配置信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        JSONArray config_select_data = (JSONArray) info.getObj("config_select_data");
        BaseDao dao = BaseDao.getDao("");
        if(StringUtils.isNotEmpty(info.getString("config_select_data")))
        {
            info.set("config_select_data", config_select_data.toJSONString());
        }else{
            info.set("config_select_data", null);
        }
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_config");
        dao.insert(info);
        return R.ok("参数配置信息添加成功");
    }

    /**
     * 编辑参数配置信息
     */
    @RequestMapping(value = "batchUpdate")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        JSONArray config = (JSONArray) info.getObj("config");
        BaseDao dao = BaseDao.getDao("");
        for (int i = 0; i < config.size(); i++) {
            JSONObject obj = config.getJSONObject(i);

            IData updateData = new IData(obj.toJSONString());
            if(StringUtils.isNotEmpty(obj.getString("config_select_data")))
            {
                updateData.set("config_select_data", obj.getJSONArray("config_select_data").toJSONString());
            }else{
                updateData.set("config_select_data", null);
            }
            updateData.remove("display");
            updateData.remove("create_time");
            updateData.set("update_time", dao.getSysTimeLocal());
            updateData.setTableName("sa_system_config");
            dao.updateById(updateData);
        }

        return R.ok("参数配置信息修改成功");
    }

    /**
     * 删除参数配置信息（软删除）
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
            updateData.setTableName("sa_system_config");
            dao.updateById(updateData);
        }
        return R.ok("参数配置信息删除成功");
    }

    /**
     * 批量删除参数配置信息（软删除）
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
            updateData.setTableName("sa_system_config");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的参数配置信息
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
            updateData.setTableName("sa_system_config");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }
}
