package com.abc.web.controller.common;

import com.abc.bean.CodeTemplateBean;
import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xtr.framework.hutool.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成业务表控制器 - 根据sa_tool_generate_tables表结构生成
 */
@RestController
@RequestMapping("/proxy/tool/code/")
public class GenerateTablesController extends WebController {

    /**
     * 查询代码生成业务表列表
     */
    @RequestMapping(value = "index")
    public Object list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        SQLParser parser = new SQLParser(param);
        parser.addSQL(" SELECT t.*");
        parser.addSQL(" FROM sa_tool_generate_tables t ");
        parser.addSQL(" WHERE 1=1 ");
        parser.addSQL(" AND t.table_name LIKE CONCAT('%', :tableName, '%') ");
        parser.addSQL(" AND t.table_comment LIKE CONCAT('%', :tableComment, '%') ");
        parser.addSQL(" AND t.stub LIKE CONCAT('%', :stub, '%') ");
        parser.addSQL(" AND t.template LIKE CONCAT('%', :template, '%') ");
        parser.addSQL(" AND t.namespace LIKE CONCAT('%', :namespace, '%') ");
        parser.addSQL(" AND t.package_name LIKE CONCAT('%', :packageName, '%') ");
        parser.addSQL(" AND t.business_name LIKE CONCAT('%', :businessName, '%') ");
        parser.addSQL(" AND t.class_name LIKE CONCAT('%', :className, '%') ");
        parser.addSQL(" AND t.menu_name LIKE CONCAT('%', :menuName, '%') ");
        parser.addSQL(" AND t.belong_menu_id = :belongMenuId ");
        parser.addSQL(" AND t.tpl_category LIKE CONCAT('%', :tplCategory, '%') ");
        parser.addSQL(" AND t.generate_type = :generateType ");
        parser.addSQL(" AND t.generate_path LIKE CONCAT('%', :generatePath, '%') ");
        parser.addSQL(" AND t.generate_model = :generateModel ");
        parser.addSQL(" AND t.build_menu = :buildMenu ");
        parser.addSQL(" AND t.component_type = :componentType ");
        parser.addSQL(" AND t.form_width = :formWidth ");
        parser.addSQL(" AND t.is_full = :isFull ");
        parser.addSQL(" AND t.remark LIKE CONCAT('%', :remark, '%') ");
        parser.addSQL(" AND t.source LIKE CONCAT('%', :source, '%') ");
        parser.addSQL(" AND t.create_time >= :startTime ");
        parser.addSQL(" AND t.create_time <= :endTime ");
        parser.addSQL(" AND t.delete_time IS NULL ");
        parser.addSQL(" ORDER BY t.id DESC ");
        Pagination page = request.getAttribute("is_export") == null ? this.getSinglePage(param) : this.getExportPage();
        IDataset list = dao.queryPage(parser, param, page);
        // 处理数据
        list.stream().forEach(a -> {
            IData obj = (IData) a;
            obj.set("create_time", obj.getString("create_time"));
            obj.set("update_time", obj.getString("update_time"));
        });

        return wrapPageQueryList(list, page);
    }

    /**
     * 获取代码生成业务表详情
     */
    @RequestMapping(value = "detail")
    public Object detail(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_tool_generate_tables WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("代码生成业务表不存在");
        }
        IData two = ChangeBean.db_vo(one);
        return R.ok(two);
    }

    @RequestMapping(value = "read")
    public Object read(HttpServletRequest request, HttpServletResponse response) {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        IData one = dao.queryByFirst("SELECT * FROM sa_tool_generate_tables WHERE id=? AND delete_time IS NULL", param.getLong("id"));
        if (one == null) {
            return R.fail("代码生成业务表不存在");
        }
        one.set("create_time", one.getString("create_time"));
        one.set("update_time", one.getString("update_time"));
        return R.ok(one);
    }

    /**
     * 数据库同步到实体关联表中
     */
    @RequestMapping(value = "sync")
    public Object sync(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BaseDao dao = BaseDao.getDao("");
        IData param = getIDataFromStream();
        IData table = dao.queryByFirst("SELECT * FROM sa_tool_generate_tables WHERE id=? AND delete_time IS NULL", param.getString("id"));
        // 先清空现有的代码生成表数据
        dao.execSql("DELETE FROM sa_tool_generate_tables where id = ?", param.getString("id"));
        dao.execSql("DELETE FROM sa_tool_generate_columns where table_id = ?", param.getString("id"));

        // 查询information_schema中的所有表信息
        IDataset tables = dao.queryList("" +
                "SELECT " +
                "    t.TABLE_NAME as table_name, " +
                "    t.TABLE_COMMENT as table_comment, " +
                "    t.CREATE_TIME as create_time " +
                "FROM information_schema.TABLES t " +
                "WHERE t.TABLE_SCHEMA = 'saiadmin6' " +
                "  AND t.TABLE_TYPE = 'BASE TABLE' " +
                "  AND t.TABLE_NAME = '" + table.getString("table_name") + "'" +
                "ORDER BY t.TABLE_NAME" +
                "");

        int tableCount = 0;
        int columnCount = 0;

        // 遍历每个表，插入到sa_tool_generate_tables
        for (int i = 0; i < tables.size(); i++) {
            IData tableInfo = tables.getData(i);
            String tableName = tableInfo.getString("table_name");
            String tableComment = tableInfo.getString("table_comment");

            // 插入表信息到sa_tool_generate_tables
            IData tableData = new IData();
            tableData.setTableName("sa_tool_generate_tables");
            tableData.set("id", table.getString("id"));// 主表的ID不变
            tableData.set("table_name", tableName);
            tableData.set("table_comment", tableComment != null ? tableComment : "");
            tableData.set("stub", "think");
            tableData.set("template", "plugin");
            tableData.set("namespace", "saicms");
            tableData.set("package_name", "business");
            tableData.set("business_name", getBusinessName(tableName));
            tableData.set("class_name", getClassName(tableName));
            tableData.set("menu_name", getMenuName(tableComment, tableName));
            tableData.set("belong_menu_id", 0);
            tableData.set("tpl_category", "single");
            tableData.set("generate_type", 1);
            tableData.set("generate_path", "saiadmin-artd");
            tableData.set("generate_model", 1);
            tableData.set("generate_menus", "index,save,update,read,destroy");
            tableData.set("build_menu", 1);
            tableData.set("component_type", 1);
            tableData.set("options", "{}");
            tableData.set("form_width", 800);
            tableData.set("is_full", 1);
            tableData.set("remark", "");
            tableData.set("source", "mysql");
            tableData.set("created_by", 1);
            tableData.set("updated_by", 1);
            tableData.set("create_time", dao.getSysTimeLocal());
            tableData.set("update_time", dao.getSysTimeLocal());

            long tableId = dao.insertExt(tableData);
            tableCount++;

            // 查询该表的列信息
            IDataset columns = dao.queryList("" +
                    "SELECT " +
                    "    c.COLUMN_NAME as column_name, " +
                    "    c.COLUMN_TYPE as column_type, " +
                    "    c.IS_NULLABLE as is_nullable, " +
                    "    c.COLUMN_DEFAULT as column_default, " +
                    "    c.COLUMN_COMMENT as column_comment, " +
                    "    c.ORDINAL_POSITION as ordinal_position, " +
                    "    c.EXTRA as extra " +
                    "FROM information_schema.COLUMNS c " +
                    "WHERE c.TABLE_SCHEMA = 'saiadmin6' " +
                    "  AND c.TABLE_NAME = '" + tableName + "' " +
                    "ORDER BY c.ORDINAL_POSITION" +
                    "");

            // 插入列信息到sa_tool_generate_columns
            for (int j = 0; j < columns.size(); j++) {
                IData columnInfo = columns.getData(j);
                String columnName = columnInfo.getString("column_name");
                String columnType = columnInfo.getString("column_type");
                String isNullable = columnInfo.getString("is_nullable");
                String columnComment = columnInfo.getString("column_comment");
                int ordinalPosition = columnInfo.getInt("ordinal_position");
                String extra = columnInfo.getString("extra");

                IData columnData = new IData();
                columnData.setTableName("sa_tool_generate_columns");
                columnData.set("table_id", tableId);
                columnData.set("column_name", columnName);
                columnData.set("column_comment", columnComment != null ? columnComment : "");
                columnData.set("column_type", getColumnType(columnType));
                columnData.set("is_pk", "id".equals(columnName) ? 1 : 0);
                columnData.set("is_required", "NO".equals(isNullable) ? 1 : 0);
                columnData.set("is_insert", isInsertColumn(columnName) ? 1 : 0);
                columnData.set("is_edit", isEditColumn(columnName) ? 1 : 0);
                columnData.set("is_list", isListColumn(columnName) ? 1 : 0);
                columnData.set("is_query", isQueryColumn(columnName) ? 1 : 0);
                columnData.set("query_type", getQueryType(columnName));
                columnData.set("view_type", getViewType(columnName, columnType));
                columnData.set("dict_type", "");
                columnData.set("allow_roles", "*");
                columnData.set("options", "{}");
                columnData.set("sort", ordinalPosition);
                columnData.set("created_by", 1);
                columnData.set("updated_by", 1);
                columnData.set("create_time", dao.getSysTimeLocal());
                columnData.set("update_time", dao.getSysTimeLocal());

                dao.insertExt(columnData);
                columnCount++;
            }
        }

        return R.ok("成功加载 " + tableCount + " 个表，" + columnCount + " 个字段");
    }

    /**
     * 添加代码生成业务表
     */
    @RequestMapping(value = "save")
    public Object add(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData info = getIDataFromStream();
        BaseDao dao = BaseDao.getDao("");
        info.set("create_time", dao.getSysTimeLocal());
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_tool_generate_tables");
        dao.insert(info);
        return R.ok("代码生成业务表添加成功");
    }

    /**
     * 编辑代码生成业务表
     */
    @RequestMapping(value = "update")
    public Object edit(HttpServletRequest request, HttpServletResponse response) {
        IData info = getIDataFromStream();
        JSONArray columns = (JSONArray) info.getObj("columns");
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_tool_generate_tables");
        info.remove("columns");// 先删除
        info.remove("generate_menus");// 先删除
        info.remove("options");// 先删除
        System.out.println(info.toString());
        dao.updateById(info);
        dao.execSql("delete from sa_tool_generate_columns where table_id = ?", info.getString("id"));
        for (int j = 0; j < columns.size(); j++) {
            JSONObject columnInfo = columns.getJSONObject(j);
            String columnName = columnInfo.getString("column_name");
            String columnType = columnInfo.getString("column_type");
            String isNullable = columnInfo.getString("is_nullable");
            String columnComment = columnInfo.getString("column_comment");
            int sort = columnInfo.getInteger("sort");
            // String extra = columnInfo.getString("extra");

            IData columnData = new IData();
            columnData.setTableName("sa_tool_generate_columns");
            columnData.set("table_id", info.getString("id"));
            columnData.set("column_name", columnName);
            columnData.set("column_comment", columnComment != null ? columnComment : "");
            columnData.set("column_type", getColumnType(columnType));
            columnData.set("is_pk", columnInfo.getIntValue("is_pk"));
            columnData.set("is_required", columnInfo.getBoolean("is_required") ? 2 : 1);
            columnData.set("is_insert", columnInfo.getBoolean("is_insert") ? 2 : 1);
            columnData.set("is_edit", columnInfo.getBoolean("is_edit") ? 2 : 1);
            columnData.set("is_list", columnInfo.getBoolean("is_list") ? 2 : 1);
            columnData.set("is_query", columnInfo.getBoolean("is_query") ? 2 : 1);
            columnData.set("query_type", getQueryType(columnName));
            columnData.set("view_type", getViewType(columnName, columnType));
            columnData.set("dict_type", "");
            columnData.set("allow_roles", "*");
            columnData.set("options", "{}");
            columnData.set("sort", sort);
            columnData.set("created_by", 1);
            columnData.set("updated_by", 1);
            columnData.set("create_time", dao.getSysTimeLocal());
            columnData.set("update_time", dao.getSysTimeLocal());
            dao.insertExt(columnData);
        }

        return R.ok("代码生成业务表修改成功");
    }

    /**
     * 删除代码生成业务表（软删除）
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
            updateData.setTableName("sa_tool_generate_tables");
            dao.updateById(updateData);
        }
        return R.ok("代码生成业务表删除成功");
    }

    /**
     * 批量删除代码生成业务表（软删除）
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
            updateData.setTableName("sa_tool_generate_tables");
            dao.updateById(updateData);
        }
        return R.ok("批量删除成功");
    }

    /**
     * 恢复已删除的代码生成业务表
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
            updateData.setTableName("sa_tool_generate_tables");
            dao.updateById(updateData);
        }
        return R.ok("批量恢复成功");
    }

    /**
     * 获取模板类别选项
     */
    @RequestMapping(value = "tplCategories")
    public Object getTplCategories(HttpServletRequest request, HttpServletResponse response) {
        JSONArray categories = new JSONArray();
        categories.add(createOption("single", "单表CRUD"));
        categories.add(createOption("tree", "树表CRUD"));
        categories.add(createOption("parent_sub", "父子表CRUD"));
        return R.ok(categories);
    }

    /**
     * 获取生成类型选项
     */
    @RequestMapping(value = "generateTypes")
    public Object getGenerateTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONArray types = new JSONArray();
        types.add(createOption(1, "压缩包下载"));
        types.add(createOption(2, "生成到模块"));
        return R.ok(types);
    }

    /**
     * 获取生成模型选项
     */
    @RequestMapping(value = "generateModels")
    public Object getGenerateModels(HttpServletRequest request, HttpServletResponse response) {
        JSONArray models = new JSONArray();
        models.add(createOption(1, "软删除"));
        models.add(createOption(2, "非软删除"));
        return R.ok(models);
    }

    /**
     * 获取构建菜单选项
     */
    @RequestMapping(value = "buildMenus")
    public Object getBuildMenus(HttpServletRequest request, HttpServletResponse response) {
        JSONArray menus = new JSONArray();
        menus.add(createOption(1, "是"));
        menus.add(createOption(2, "否"));
        return R.ok(menus);
    }

    /**
     * 获取组件类型选项
     */
    @RequestMapping(value = "componentTypes")
    public Object getComponentTypes(HttpServletRequest request, HttpServletResponse response) {
        JSONArray types = new JSONArray();
        types.add(createOption(1, "弹窗表单"));
        types.add(createOption(2, "抽屉表单"));
        types.add(createOption(3, "页面表单"));
        return R.ok(types);
    }

    /**
     * 获取是否全屏选项
     */
    @RequestMapping(value = "isFullOptions")
    public Object getIsFullOptions(HttpServletRequest request, HttpServletResponse response) {
        JSONArray options = new JSONArray();
        options.add(createOption(1, "是"));
        options.add(createOption(0, "否"));
        return R.ok(options);
    }

    /**
     * 加载数据库表和列信息到代码生成表中
     */
    @RequestMapping(value = "loadTable")
    public Object loadTable(HttpServletRequest request, HttpServletResponse response) throws Exception {
        BaseDao dao = BaseDao.getDao("");

        // 先清空现有的代码生成表数据
        dao.execSql("DELETE FROM sa_tool_generate_tables");
        dao.execSql("DELETE FROM sa_tool_generate_columns");

        // 查询information_schema中的所有表信息
        IDataset tables = dao.queryList("" +
                "SELECT " +
                "    t.TABLE_NAME as table_name, " +
                "    t.TABLE_COMMENT as table_comment, " +
                "    t.CREATE_TIME as create_time " +
                "FROM information_schema.TABLES t " +
                "WHERE t.TABLE_SCHEMA = 'saiadmin6' " +
                "  AND t.TABLE_TYPE = 'BASE TABLE' " +
                "ORDER BY t.TABLE_NAME" +
                "");

        int tableCount = 0;
        int columnCount = 0;

        // 遍历每个表，插入到sa_tool_generate_tables
        for (int i = 0; i < tables.size(); i++) {
            IData tableInfo = tables.getData(i);
            String tableName = tableInfo.getString("table_name");
            String tableComment = tableInfo.getString("table_comment");

            // 插入表信息到sa_tool_generate_tables
            IData tableData = new IData();
            tableData.setTableName("sa_tool_generate_tables");
            tableData.set("table_name", tableName);
            tableData.set("table_comment", tableComment != null ? tableComment : "");
            tableData.set("stub", "think");
            tableData.set("template", "plugin");
            tableData.set("namespace", "saicms");
            tableData.set("package_name", "business");
            tableData.set("business_name", getBusinessName(tableName));
            tableData.set("class_name", getClassName(tableName));
            tableData.set("menu_name", getMenuName(tableComment, tableName));
            tableData.set("belong_menu_id", 0);
            tableData.set("tpl_category", "single");
            tableData.set("generate_type", 1);
            tableData.set("generate_path", "saiadmin-artd");
            tableData.set("generate_model", 1);
            tableData.set("generate_menus", "index,save,update,read,destroy");
            tableData.set("build_menu", 1);
            tableData.set("component_type", 1);
            tableData.set("options", "{}");
            tableData.set("form_width", 800);
            tableData.set("is_full", 1);
            tableData.set("remark", "");
            tableData.set("source", "mysql");
            tableData.set("created_by", 1);
            tableData.set("updated_by", 1);
            tableData.set("create_time", dao.getSysTimeLocal());
            tableData.set("update_time", dao.getSysTimeLocal());

            long tableId = dao.insertExt(tableData);
            tableCount++;

            // 查询该表的列信息
            IDataset columns = dao.queryList("" +
                    "SELECT " +
                    "    c.COLUMN_NAME as column_name, " +
                    "    c.COLUMN_TYPE as column_type, " +
                    "    c.IS_NULLABLE as is_nullable, " +
                    "    c.COLUMN_DEFAULT as column_default, " +
                    "    c.COLUMN_COMMENT as column_comment, " +
                    "    c.ORDINAL_POSITION as ordinal_position, " +
                    "    c.EXTRA as extra " +
                    "FROM information_schema.COLUMNS c " +
                    "WHERE c.TABLE_SCHEMA = 'saiadmin6' " +
                    "  AND c.TABLE_NAME = '" + tableName + "' " +
                    "ORDER BY c.ORDINAL_POSITION" +
                    "");

            // 插入列信息到sa_tool_generate_columns
            for (int j = 0; j < columns.size(); j++) {
                IData columnInfo = columns.getData(j);
                String columnName = columnInfo.getString("column_name");
                String columnType = columnInfo.getString("column_type");
                String isNullable = columnInfo.getString("is_nullable");
                String columnComment = columnInfo.getString("column_comment");
                int ordinalPosition = columnInfo.getInt("ordinal_position");
                String extra = columnInfo.getString("extra");

                IData columnData = new IData();
                columnData.setTableName("sa_tool_generate_columns");
                columnData.set("table_id", tableId);
                columnData.set("column_name", columnName);
                columnData.set("column_comment", columnComment != null ? columnComment : "");
                columnData.set("column_type", getColumnType(columnType));
                columnData.set("is_pk", "id".equals(columnName) ? 1 : 0);
                columnData.set("is_required", "NO".equals(isNullable) ? 1 : 0);
                columnData.set("is_insert", isInsertColumn(columnName) ? 1 : 0);
                columnData.set("is_edit", isEditColumn(columnName) ? 1 : 0);
                columnData.set("is_list", isListColumn(columnName) ? 1 : 0);
                columnData.set("is_query", isQueryColumn(columnName) ? 1 : 0);
                columnData.set("query_type", getQueryType(columnName));
                columnData.set("view_type", getViewType(columnName, columnType));
                columnData.set("dict_type", "");
                columnData.set("allow_roles", "*");
                columnData.set("options", "{}");
                columnData.set("sort", ordinalPosition);
                columnData.set("created_by", 1);
                columnData.set("updated_by", 1);
                columnData.set("create_time", dao.getSysTimeLocal());
                columnData.set("update_time", dao.getSysTimeLocal());

                dao.insertExt(columnData);
                columnCount++;
            }
        }

        return R.ok("成功加载 " + tableCount + " 个表，" + columnCount + " 个字段");
    }

    /**
     * 获取指定表的列信息
     */
    @RequestMapping(value = "getTableColumns")
    public Object getTableColumns(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String table_id = param.getString("table_id");
        IData table = dao.queryByFirst("select * from sa_tool_generate_tables where id=" + table_id);
        String tableName = table.getString("table_name");
        if (tableName == null || tableName.trim().isEmpty()) {
            return R.fail("表名不能为空");
        }
        IDataset columns = dao.queryList("select * from sa_tool_generate_columns where table_id=?", table_id);

        return R.ok(columns);
    }

    /**
     * 创建选项对象
     */
    private IData createOption(Object value, String label) {
        IData option = new IData();
        option.set("value", value);
        option.set("label", label);
        return option;
    }

    /**
     * 根据表名生成业务名称
     */
    private String getBusinessName(String tableName) {
        if (tableName.startsWith("sa_")) {
            return tableName.substring(3);
        }
        return tableName;
    }

    /**
     * 根据表名生成类名
     */
    private String getClassName(String tableName) {
        String businessName = getBusinessName(tableName);
        // 驼峰命名转换
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (char c : businessName.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        // 首字母大写
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    /**
     * 根据表注释或表名生成菜单名称
     */
    private String getMenuName(String tableComment, String tableName) {
        if (tableComment != null && !tableComment.trim().isEmpty()) {
            // 移除"表"字
            return tableComment.replace("表", "").trim();
        }
        return getBusinessName(tableName);
    }

    /**
     * 根据数据库列类型转换为代码生成列类型
     */
    private String getColumnType(String dbType) {
        if (dbType == null) return "string";

        String lowerType = dbType.toLowerCase();
        if (lowerType.contains("int")) return "integer";
        if (lowerType.contains("decimal") || lowerType.contains("float") || lowerType.contains("double"))
            return "decimal";
        if (lowerType.contains("datetime") || lowerType.contains("timestamp")) return "datetime";
        if (lowerType.contains("date")) return "date";
        if (lowerType.contains("text")) return "text";
        if (lowerType.contains("tinyint(1)")) return "boolean";
        return "string";
    }

    /**
     * 判断是否为可插入字段
     */
    private boolean isInsertColumn(String columnName) {
        return !("id".equals(columnName) || "create_time".equals(columnName) || "update_time".equals(columnName));
    }

    /**
     * 判断是否为可编辑字段
     */
    private boolean isEditColumn(String columnName) {
        return !("id".equals(columnName) || "create_time".equals(columnName));
    }

    /**
     * 判断是否为列表显示字段
     */
    private boolean isListColumn(String columnName) {
        return !("delete_time".equals(columnName) || "create_time".equals(columnName) || "update_time".equals(columnName));
    }

    /**
     * 判断是否为查询字段
     */
    private boolean isQueryColumn(String columnName) {
        return "id".equals(columnName) || "name".equals(columnName) || "title".equals(columnName) || columnName.endsWith("_id");
    }

    /**
     * 获取查询类型
     */
    private String getQueryType(String columnName) {
        if ("id".equals(columnName)) return "eq";
        if (columnName.endsWith("_id")) return "eq";
        return "like";
    }

    /**
     * 获取视图类型
     */
    private String getViewType(String columnName, String columnType) {
        if ("id".equals(columnName)) return "text";
        if (columnType != null && (columnType.contains("text") || columnType.contains("longtext"))) return "textarea";
        if (columnType != null && columnType.contains("tinyint(1)")) return "switch";
        if (columnName.contains("time") || columnName.contains("date")) return "datetime";
        return "text";
    }

    /**
     * 预览生成的代码
     */
    @RequestMapping(value = "preview")
    public Object preview(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IData param = getRequestParam();
        BaseDao dao = BaseDao.getDao("");
        String tableId = param.getString("id");

        // 获取表信息
        IData tableInfo = dao.queryByFirst("SELECT * FROM sa_tool_generate_tables WHERE id=? AND delete_time IS NULL", tableId);
        if (tableInfo == null) {
            return R.fail("代码生成表不存在");
        }

        // 获取列信息
        IDataset columns = dao.queryList("SELECT * FROM sa_tool_generate_columns WHERE table_id=? ORDER BY sort", tableId);

        // 使用CodeTemplateBean生成预览代码，返回前端需要的数据结构
        JSONArray previewList = CodeTemplateBean.generatePreviewList(dao, tableId);

        return R.ok(previewList);
    }

    /**
     * 下载后端代码
     */
    @RequestMapping("generate")
    public void generate(HttpServletResponse response) throws Exception {
        IData param = getIDataFromStream();
        JSONArray ids = (JSONArray) param.getObj("ids");
        BaseDao dao = BaseDao.getDao("");

        // 创建临时根目录用于存储生成的所有文件
        String userDir = System.getProperty("user.dir");
        String tempRootDirPath = userDir + File.separator + "target" + File.separator + "generated-code-" + System.currentTimeMillis();
        File tempRootDir = new File(tempRootDirPath);
        tempRootDir.mkdirs();

        // 为每个表创建独立的目录结构
        for (int a = 0; a < ids.size(); a++) {
            String tableId = ids.getString(a);
            // 获取表信息
            IData tableInfo = dao.queryByFirst("SELECT * FROM sa_tool_generate_tables WHERE id=? AND delete_time IS NULL", tableId);
            if (tableInfo == null) {
                continue; // 跳过不存在的表
            }

            String businessName = tableInfo.getString("business_name");
            String className = tableInfo.getString("class_name");
            String tableName = tableInfo.getString("table_name");

            // 为当前表创建目录结构
            String tableDirPath = tempRootDirPath + File.separator + businessName + File.separator + className;
            File tableDir = new File(tableDirPath);
            tableDir.mkdirs();

            // 生成前端目录结构
            String frontendDirPath = tableDirPath + File.separator + "frontend";
            File frontendDir = new File(frontendDirPath);
            frontendDir.mkdirs();

            // 生成后端目录结构
            String backendDirPath = tableDirPath + File.separator + "backend";
            File backendDir = new File(backendDirPath);
            backendDir.mkdirs();

            // 生成API目录
            String apiDirPath = frontendDirPath + File.separator + "api";
            File apiDir = new File(apiDirPath);
            apiDir.mkdirs();



            // 生成视图目录
            String viewsDirPath = frontendDirPath + File.separator + "views" + File.separator + businessName;
            File viewsDir = new File(viewsDirPath);
            viewsDir.mkdirs();

            // 生成组件目录
            String componentsDirPath = viewsDirPath + File.separator + "modules";
            File componentsDir = new File(componentsDirPath);
            componentsDir.mkdirs();

            // 生成控制器目录
            String controllerDirPath = backendDirPath + File.separator + "controller";
            File controllerDir = new File(controllerDirPath);
            controllerDir.mkdirs();

            // 生成SQL目录
            String sqlDirPath = backendDirPath + File.separator + "sql";
            File sqlDir = new File(sqlDirPath);
            sqlDir.mkdirs();

            JSONArray previewList = CodeTemplateBean.generatePreviewList(dao, tableId);

            // 将预览代码按类型写入对应的目录
            for (int i = 0; i < previewList.size(); i++) {
                JSONObject fileInfo = previewList.getJSONObject(i);
                String fileName = fileInfo.getString("tab_name");
                String fileType = fileInfo.getString("name");
                String codeContent = fileInfo.getString("code");

                File outputFile = null;

                // 根据文件类型决定存储路径
                switch (fileType) {
                    case "api":
                        outputFile = new File(apiDirPath + File.separator + fileName);
                        break;
                    case "table-search":
                    case "edit-dialog":
                        outputFile = new File(componentsDirPath + File.separator + fileName);
                        break;
                    case "index":
                        outputFile = new File(viewsDirPath + File.separator + fileName);
                        break;
                    case "controller":
                        outputFile = new File(controllerDirPath + File.separator + fileName);
                        break;
                    case "sql":
                        outputFile = new File(sqlDirPath + File.separator + fileName);
                        break;
                    default:
                        // 默认放在表根目录
                        outputFile = new File(tableDirPath + File.separator + fileName);
                        break;
                }

                // 确保父目录存在
                outputFile.getParentFile().mkdirs();

                // 写入文件内容
                try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
                    writer.write(codeContent);
                }
            }
        }

        // 设置响应头 - 使用更有意义的文件名
        String zipFileName = "generated-code-" + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".zip";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + zipFileName + "\"");
        response.setContentType("application/zip");
        response.setCharacterEncoding("UTF-8");

        // 创建压缩文件
        String zipFilePath = tempRootDirPath + ".zip";
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            Path sourcePath = Paths.get(tempRootDirPath);
            if (Files.isDirectory(sourcePath)) {
                Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            // 保持目录结构的相对路径
                            String relativePath = sourcePath.relativize(path).toString();
                            ZipEntry zipEntry = new ZipEntry(relativePath.replace("\\", "/"));
                            zipOut.putNextEntry(zipEntry);
                            Files.copy(path, zipOut);
                            zipOut.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException("添加文件到压缩包失败: " + e.getMessage());
                        }
                    });
            }
        }

        // 输出文件流
        File zipFile = new File(zipFilePath);
        try (FileInputStream fis = new FileInputStream(zipFile);
             BufferedInputStream bis = new BufferedInputStream(fis);
             OutputStream os = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }

        // 清理临时文件
        zipFile.delete();
        deleteDirectory(tempRootDir);
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }

    /**
     * 生成Vue弹窗表单组件代码
     */
    private String generateVueDialogCode(IData tableInfo, IDataset columns) {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String menuName = tableInfo.getString("menu_name");
        String packageName = tableInfo.getString("package_name");

        // 生成模板部分
        code.append("<template>\n");
        code.append("  <el-dialog\n");
        code.append("    v-model=\"visible\"\n");
        code.append("    :title=\"dialogType === 'add' ? '新增").append(menuName).append("' : '编辑").append(menuName).append("'\"\n");
        code.append("    width=\"800px\"\n");
        code.append("    align-center\n");
        code.append("    :close-on-click-modal=\"false\"\n");
        code.append("    @close=\"handleClose\"\n");
        code.append("  >\n");
        code.append("    <el-form ref=\"formRef\" :model=\"formData\" :rules=\"rules\" label-width=\"120px\">\n");

        // 根据列信息生成表单字段
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            String viewType = column.getString("view_type");
            int isRequired = column.getInt("is_required", 0);

            // 跳过不需要在表单中显示的字段
            if ("id".equals(columnName) || "create_time".equals(columnName) ||
                    "update_time".equals(columnName) || "delete_time".equals(columnName)) {
                continue;
            }

            // 如果没有注释，使用列名
            if (columnComment == null || columnComment.trim().isEmpty()) {
                columnComment = columnName;
            }

            code.append("      <el-form-item label=\"").append(columnComment).append("\" prop=\"").append(columnName).append("\">\n");

            // 根据字段类型生成不同的表单组件
            if ("image".equals(columnName) || "avatar".equals(columnName) || "pic".equals(columnName)) {
                // 图片上传组件
                code.append("        <sa-image-upload v-model=\"formData.").append(columnName).append("\" :limit=\"1\" :multiple=\"false\" />\n");
            } else if ("content".equals(columnName) || "detail".equals(columnName) || "description".equals(columnName)) {
                // 富文本编辑器
                code.append("        <sa-editor v-model=\"formData.").append(columnName).append("\" height=\"400px\" />\n");
            } else if ("status".equals(columnName) || "is_".equals(columnName.substring(0, Math.min(3, columnName.length())))) {
                // 单选框组
                code.append("        <sa-radio v-model=\"formData.").append(columnName).append("\" dict=\"data_status\" />\n");
            } else if ("sort".equals(columnName) || "order".equals(columnName)) {
                // 数字输入框
                code.append("        <el-input-number v-model=\"formData.").append(columnName).append("\" placeholder=\"请输入").append(columnComment).append("\" />\n");
            } else if ("textarea".equals(viewType)) {
                // 多行文本
                code.append("        <el-input v-model=\"formData.").append(columnName).append("\" type=\"textarea\" placeholder=\"请输入").append(columnComment).append("\" />\n");
            } else if ("datetime".equals(viewType)) {
                // 日期时间选择器
                code.append("        <el-date-picker\n");
                code.append("          v-model=\"formData.").append(columnName).append("\"\n");
                code.append("          type=\"datetime\"\n");
                code.append("          placeholder=\"请选择").append(columnComment).append("\"\n");
                code.append("          clearable\n");
                code.append("        />\n");
            } else {
                // 默认文本输入框
                code.append("        <el-input v-model=\"formData.").append(columnName).append("\" placeholder=\"请输入").append(columnComment).append("\" />\n");
            }

            code.append("      </el-form-item>\n");
        }

        code.append("    </el-form>\n");
        code.append("    <template #footer>\n");
        code.append("      <el-button @click=\"handleClose\">取消</el-button>\n");
        code.append("      <el-button type=\"primary\" @click=\"handleSubmit\">提交</el-button>\n");
        code.append("    </template>\n");
        code.append("  </el-dialog>\n");
        code.append("</template>\n\n");

        // 生成脚本部分
        code.append("<script setup lang=\"ts\">\n");
        code.append("  import api from '../../../api/").append(businessName).append("/").append(className.toLowerCase()).append("'\n");
        code.append("  import { ElMessage } from 'element-plus'\n");
        code.append("  import type { FormInstance, FormRules } from 'element-plus'\n\n");

        code.append("  interface Props {\n");
        code.append("    modelValue: boolean\n");
        code.append("    dialogType: string\n");
        code.append("    data?: Record<string, any>\n");
        code.append("  }\n\n");

        code.append("  interface Emits {\n");
        code.append("    (e: 'update:modelValue', value: boolean): void\n");
        code.append("    (e: 'success'): void\n");
        code.append("  }\n\n");

        code.append("  const props = withDefaults(defineProps<Props>(), {\n");
        code.append("    modelValue: false,\n");
        code.append("    dialogType: 'add',\n");
        code.append("    data: undefined\n");
        code.append("  })\n\n");

        code.append("  const emit = defineEmits<Emits>()\n\n");
        code.append("  const formRef = ref<FormInstance>()\n\n");

        code.append("/**\n");
        code.append("   * 弹窗显示状态双向绑定\n");
        code.append("   */\n");
        code.append("  const visible = computed({\n");
        code.append("    get: () => props.modelValue,\n");
        code.append("    set: (value) => emit('update:modelValue', value)\n");
        code.append("  })\n\n");

        code.append("  /**\n");
        code.append("   * 表单验证规则\n");
        code.append("   */\n");
        code.append("  const rules = reactive<FormRules>({\n");

        // 生成必填验证规则
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            int isRequired = column.getInt("is_required", 0);

            if (isRequired == 1 && !"id".equals(columnName) && !"create_time".equals(columnName) &&
                    !"update_time".equals(columnName) && !"delete_time".equals(columnName)) {

                if (columnComment == null || columnComment.trim().isEmpty()) {
                    columnComment = columnName;
                }

                code.append("    ").append(columnName).append(": [{ required: true, message: '").append(columnComment).append("必需填写', trigger: 'blur' }],\n");
            }
        }

        code.append("  })\n\n");

        code.append("  /**\n");
        code.append("   * 初始数据\n");
        code.append("   */\n");
        code.append("  const initialFormData = {\n");

        // 生成初始表单数据
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnType = column.getString("column_type");

            if ("id".equals(columnName)) {
                code.append("    ").append(columnName).append(": null,\n");
            } else if ("sort".equals(columnName) || "order".equals(columnName)) {
                code.append("    ").append(columnName).append(": 100,\n");
            } else if ("status".equals(columnName)) {
                code.append("    ").append(columnName).append(": 1,\n");
            } else if ("is_".equals(columnName.substring(0, Math.min(3, columnName.length())))) {
                code.append("    ").append(columnName).append(": 2,\n");
            } else if ("integer".equals(columnType) || "decimal".equals(columnType)) {
                code.append("    ").append(columnName).append(": null,\n");
            } else {
                code.append("    ").append(columnName).append(": '',\n");
            }
        }

        code.append("  }\n\n");

        code.append("  /**\n");
        code.append("   * 表单数据\n");
        code.append("   */\n");
        code.append("  const formData = reactive({ ...initialFormData })\n\n");

        code.append("  /**\n");
        code.append("   * 监听弹窗打开，初始化表单数据\n");
        code.append("   */\n");
        code.append("  watch(\n");
        code.append("    () => props.modelValue,\n");
        code.append("    (newVal) => {\n");
        code.append("      if (newVal) {\n");
        code.append("        initPage()\n");
        code.append("      }\n");
        code.append("    }\n");
        code.append("  )\n\n");

        code.append("  /**\n");
        code.append("   * 初始化页面数据\n");
        code.append("   */\n");
        code.append("  const initPage = async () => {\n");
        code.append("    // 先重置为初始值\n");
        code.append("    Object.assign(formData, initialFormData)\n");
        code.append("    // 如果有数据，则填充数据\n");
        code.append("    if (props.data) {\n");
        code.append("      await nextTick()\n");
        code.append("      initForm()\n");
        code.append("    }\n");
        code.append("  }\n\n");

        code.append("  /**\n");
        code.append("   * 初始化表单数据\n");
        code.append("   */\n");
        code.append("  const initForm = () => {\n");
        code.append("    if (props.data) {\n");
        code.append("      for (const key in formData) {\n");
        code.append("        if (props.data[key] != null && props.data[key] != undefined) {\n");
        code.append("          ;(formData as any)[key] = props.data[key]\n");
        code.append("        }\n");
        code.append("      }\n");
        code.append("    }\n");
        code.append("  }\n\n");

        code.append("  /**\n");
        code.append("   * 关闭弹窗并重置表单\n");
        code.append("   */\n");
        code.append("  const handleClose = () => {\n");
        code.append("    visible.value = false\n");
        code.append("    formRef.value?.resetFields()\n");
        code.append("  }\n\n");

        code.append("  /**\n");
        code.append("   * 提交表单\n");
        code.append("   */\n");
        code.append("  const handleSubmit = async () => {\n");
        code.append("    if (!formRef.value) return\n");
        code.append("    try {\n");
        code.append("      await formRef.value.validate()\n");
        code.append("      if (props.dialogType === 'add') {\n");
        code.append("        await api.save(formData)\n");
        code.append("        ElMessage.success('新增成功')\n");
        code.append("      } else {\n");
        code.append("        await api.update(formData)\n");
        code.append("        ElMessage.success('修改成功')\n");
        code.append("      }\n");
        code.append("      emit('success')\n");
        code.append("      handleClose()\n");
        code.append("    } catch (error) {\n");
        code.append("      console.log('表单验证失败:', error)\n");
        code.append("    }\n");
        code.append("  }\n");
        code.append("</script>");

        return code.toString();
    }


}
