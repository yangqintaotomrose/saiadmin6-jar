package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 附件信息控制器 - 根据附件信息JSON Schema生成，保持与RoleController一致的参数类型
 */
@RestController
@RequestMapping("/proxy/core/attachment/")
public class AttachmentController extends WebController {

    /**
     * 查询附件信息列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*, c.category_name");
        parser.addSQL(" FROM sa_system_attachment t ");
        parser.addSQL(" LEFT JOIN sa_system_category c ON t.category_id = c.id ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.origin_name LIKE CONCAT('%', :originName, '%') ");
        parser.addSQL(" AND t.object_name LIKE CONCAT('%', :objectName, '%') ");
        parser.addSQL(" AND t.hash = :hash ");
        parser.addSQL(" AND t.mime_type = :mimeType ");
        parser.addSQL(" AND t.category_id = :category_id ");
        parser.addSQL(" AND t.storage_mode = :storage_mode ");
        parser.addSQL(" AND t.delete_time IS NULL ");
        parser.addSQL(" ORDER BY t.create_time DESC ");
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
     * 获取附件信息详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_system_attachment WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("附件信息不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    /**
     * 添加附件信息
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_attachment");
        dao.insert(info);
        return R.ok("附件信息添加成功");
    }

    /**
     * 编辑附件信息
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_system_attachment");
        dao.updateById(info);
        return R.ok("附件信息修改成功");
    }

    /**
     * 删除附件信息（软删除）
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
            updateData.setTableName("sa_system_attachment");
            dao.updateById(updateData);
        }
        return R.ok("附件信息删除成功");
    }

    /**
     * 批量删除附件信息（软删除）
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
            updateData.setTableName("sa_system_attachment");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的附件信息
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
            updateData.setTableName("sa_system_attachment");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }

    /**
     * 根据文件哈希查询附件信息
     */
    @RequestMapping(value = "getByHash")
    public Object getByHash(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String hash = param.getString("hash");
        if (hash == null || hash.isEmpty()) {
            return R.fail("文件哈希不能为空");
        }

        IData one = dao.queryByFirst("SELECT * FROM sa_system_attachment WHERE hash=? AND delete_time IS NULL", hash);
        if (one == null) {
            return R.fail("未找到对应的附件信息");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

}
