package com.abc.web.controller.common;

import com.abc.bean.CodeTemplateBean;
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
 * 代码生成业务表控制器 - 根据sa_tool_generate_tables表结构生成
 */
@RestController
@RequestMapping("/proxy/tool/demo/")
public class DemoController extends WebController {

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
        //处理数据
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
        return R.ok(one);
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
        BaseDao dao = BaseDao.getDao("");
        info.set("update_time", dao.getSysTimeLocal());
        info.setTableName("sa_tool_generate_tables");
        dao.updateById(info);
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
     * 根据数据库列类型转换为代码生成列类型
     */
    private String getColumnType(String dbType) {
        if (dbType == null) return "string";

        String lowerType = dbType.toLowerCase();
        if (lowerType.contains("int")) return "integer";
        if (lowerType.contains("decimal") || lowerType.contains("float") || lowerType.contains("double")) return "decimal";
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
     * 生成Controller代码
     */
    private String generateControllerCode(IData tableInfo, IDataset columns) {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String packageName = tableInfo.getString("package_name");

        code.append("package com.abc.web.controller.").append(packageName).append(";\n\n");
        code.append("import com.abc.web.controller.WebController;\n");
        code.append("import com.abc.web.domain.R;\n");
        code.append("import com.xtr.framework.hutool.IData;\n");
        code.append("import org.springframework.web.bind.annotation.RequestMapping;\n");
        code.append("import org.springframework.web.bind.annotation.RestController;\n\n");
        code.append("import javax.servlet.http.HttpServletRequest;\n");
        code.append("import javax.servlet.http.HttpServletResponse;\n\n");
        code.append("/**\n");
        code.append(" * ").append(tableInfo.getString("table_comment")).append("控制器\n");
        code.append(" */\n");
        code.append("@RestController\n");
        code.append("@RequestMapping(\"/api/").append(businessName).append("\")\n");
        code.append("public class ").append(className).append("Controller extends WebController {\n\n");

        // 生成基本的CRUD方法框架
        code.append("    /**\n");
        code.append("     * 查询").append(tableInfo.getString("table_comment")).append("列表\n");
        code.append("     */\n");
        code.append("    @RequestMapping(value = \"index\")\n");
        code.append("    public Object list(HttpServletRequest request, HttpServletResponse response) {\n");
        code.append("        IData param = getRequestParam();\n");
        code.append("        // TODO: 实现分页查询逻辑\n");
        code.append("        return R.ok();\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * 获取").append(tableInfo.getString("table_comment")).append("详情\n");
        code.append("     */\n");
        code.append("    @RequestMapping(value = \"read\")\n");
        code.append("    public Object detail(HttpServletRequest request, HttpServletResponse response) {\n");
        code.append("        IData param = getRequestParam();\n");
        code.append("        // TODO: 实现详情查询逻辑\n");
        code.append("        return R.ok();\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * 添加").append(tableInfo.getString("table_comment")).append("\n");
        code.append("     */\n");
        code.append("    @RequestMapping(value = \"save\")\n");
        code.append("    public Object save(HttpServletRequest request, HttpServletResponse response) {\n");
        code.append("        IData param = getIDataFromStream();\n");
        code.append("        // TODO: 实现保存逻辑\n");
        code.append("        return R.ok(\"添加成功\");\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * 更新").append(tableInfo.getString("table_comment")).append("\n");
        code.append("     */\n");
        code.append("    @RequestMapping(value = \"update\")\n");
        code.append("    public Object update(HttpServletRequest request, HttpServletResponse response) {\n");
        code.append("        IData param = getIDataFromStream();\n");
        code.append("        // TODO: 实现更新逻辑\n");
        code.append("        return R.ok(\"更新成功\");\n");
        code.append("    }\n\n");

        code.append("    /**\n");
        code.append("     * 删除").append(tableInfo.getString("table_comment")).append("\n");
        code.append("     */\n");
        code.append("    @RequestMapping(value = \"destroy\")\n");
        code.append("    public Object delete(HttpServletRequest request, HttpServletResponse response) {\n");
        code.append("        IData param = getIDataFromStream();\n");
        code.append("        // TODO: 实现删除逻辑\n");
        code.append("        return R.ok(\"删除成功\");\n");
        code.append("    }\n\n");

        code.append("}");

        return code.toString();
    }

    /**
     * 生成Vue API接口代码
     */
    private String generateVueApiCode(IData tableInfo) {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String menuName = tableInfo.getString("menu_name");
        String packageName = tableInfo.getString("package_name");

        // 生成导入语句
        code.append("import request from '@/utils/http'\n\n");

        // 生成注释
        code.append("/**\n");
        code.append(" * ").append(menuName).append(" API接口\n");
        code.append(" */\n");
        code.append("export default {\n");

        // 生成list方法
        code.append("  /**\n");
        code.append("   * 获取数据列表\n");
        code.append("   * @param params 搜索参数\n");
        code.append("   * @returns 数据列表\n");
        code.append("   */\n");
        code.append("  list(params: Record<string, any>) {\n");
        code.append("    return request.get<Api.Common.ApiPage>({\n");
        code.append("      url: '/app/saicms/admin/").append(packageName).append("/").append(className).append("/index',\n");
        code.append("      params\n");
        code.append("    })\n");
        code.append("  },\n\n");

        // 生成read方法
        code.append("  /**\n");
        code.append("   * 读取数据\n");
        code.append("   * @param id 数据ID\n");
        code.append("   * @returns 数据详情\n");
        code.append("   */\n");
        code.append("  read(id: number | string) {\n");
        code.append("    return request.get<Api.Common.ApiData>({\n");
        code.append("      url: '/app/saicms/admin/").append(packageName).append("/").append(className).append("/read?id=' + id\n");
        code.append("    })\n");
        code.append("  },\n\n");

        // 生成save方法
        code.append("  /**\n");
        code.append("   * 创建数据\n");
        code.append("   * @param params 数据参数\n");
        code.append("   * @returns 执行结果\n");
        code.append("   */\n");
        code.append("  save(params: Record<string, any>) {\n");
        code.append("    return request.post<any>({\n");
        code.append("      url: '/app/saicms/admin/").append(packageName).append("/").append(className).append("/save',\n");
        code.append("      data: params\n");
        code.append("    })\n");
        code.append("  },\n\n");

        // 生成update方法
        code.append("  /**\n");
        code.append("   * 更新数据\n");
        code.append("   * @param params 数据参数\n");
        code.append("   * @returns 执行结果\n");
        code.append("   */\n");
        code.append("  update(params: Record<string, any>) {\n");
        code.append("    return request.put<any>({\n");
        code.append("      url: '/app/saicms/admin/").append(packageName).append("/").append(className).append("/update',\n");
        code.append("      data: params\n");
        code.append("    })\n");
        code.append("  },\n\n");

        // 生成delete方法
        code.append("  /**\n");
        code.append("   * 删除数据\n");
        code.append("   * @param params 数据参数\n");
        code.append("   * @returns 执行结果\n");
        code.append("   */\n");
        code.append("  delete(params: Record<string, any>) {\n");
        code.append("    return request.del<any>({\n");
        code.append("      url: '/app/saicms/admin/").append(packageName).append("/").append(className).append("/destroy',\n");
        code.append("      data: params\n");
        code.append("    })\n");
        code.append("  }\n");

        code.append("}");

        return code.toString();
    }

    /**
     * 生成Vue搜索组件代码
     */
    private String generateVueSearchCode(IData tableInfo, IDataset columns) {
        StringBuilder code = new StringBuilder();
        String menuName = tableInfo.getString("menu_name");

        // 生成模板部分
        code.append("<template>\n");
        code.append("  <sa-search-bar\n");
        code.append("    ref=\"searchBarRef\"\n");
        code.append("    v-model=\"formData\"\n");
        code.append("    label-width=\"100px\"\n");
        code.append("    :showExpand=\"false\"\n");
        code.append("    @reset=\"handleReset\"\n");
        code.append("    @search=\"handleSearch\"\n");
        code.append("    @expand=\"handleExpand\"\n");
        code.append("  >\n");

        // 根据列信息生成搜索字段
        int fieldCount = 0;
        for (int i = 0; i < columns.size() && fieldCount < 4; i++) {  // 限制生成4个搜索字段
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            String viewType = column.getString("view_type");

            // 跳过不需要搜索的字段
            if ("id".equals(columnName) || "create_time".equals(columnName) ||
                "update_time".equals(columnName) || "delete_time".equals(columnName)) {
                continue;
            }

            // 如果没有注释，使用列名
            if (columnComment == null || columnComment.trim().isEmpty()) {
                columnComment = columnName;
            }

            code.append("    <el-col v-bind=\"setSpan(6)\">\n");
            code.append("      <el-form-item label=\"").append(columnComment).append("\" prop=\"").append(columnName).append("\">\n");

            // 根据字段类型生成不同的输入组件
            if ("textarea".equals(viewType)) {
                code.append("        <el-input v-model=\"formData.").append(columnName).append("\" type=\"textarea\" placeholder=\"请输入").append(columnComment).append("\" clearable />\n");
            } else if ("datetime".equals(viewType)) {
                code.append("        <el-date-picker\n");
                code.append("          v-model=\"formData.").append(columnName).append("\"\n");
                code.append("          type=\"datetime\"\n");
                code.append("          placeholder=\"请选择").append(columnComment).append("\"\n");
                code.append("          clearable\n");
                code.append("        />\n");
            } else if ("switch".equals(viewType)) {
                code.append("        <el-switch v-model=\"formData.").append(columnName).append("\" />\n");
            } else {
                code.append("        <el-input v-model=\"formData.").append(columnName).append("\" placeholder=\"请输入").append(columnComment).append("\" clearable />\n");
            }

            code.append("      </el-form-item>\n");
            code.append("    </el-col>\n");
            fieldCount++;
        }

        code.append("  </sa-search-bar>\n");
        code.append("</template>\n\n");

        // 生成脚本部分
        code.append("<script setup lang=\"ts\">\n");
        code.append("  interface Props {\n");
        code.append("    modelValue: Record<string, any>\n");
        code.append("  }\n");
        code.append("  interface Emits {\n");
        code.append("    (e: 'update:modelValue', value: Record<string, any>): void\n");
        code.append("    (e: 'search', params: Record<string, any>): void\n");
        code.append("    (e: 'reset'): void\n");
        code.append("  }\n");
        code.append("  const props = defineProps<Props>()\n");
        code.append("  const emit = defineEmits<Emits>()\n");
        code.append("  // 展开/收起\n");
        code.append("  const isExpanded = ref<boolean>(false)\n\n");

        code.append("  // 表单数据双向绑定\n");
        code.append("  const searchBarRef = ref()\n");
        code.append("  const formData = computed({\n");
        code.append("    get: () => props.modelValue,\n");
        code.append("    set: (val) => emit('update:modelValue', val)\n");
        code.append("  })\n\n");

        code.append("  // 重置\n");
        code.append("  function handleReset() {\n");
        code.append("    searchBarRef.value?.ref.resetFields()\n");
        code.append("    emit('reset')\n");
        code.append("  }\n\n");

        code.append("  // 搜索\n");
        code.append("  async function handleSearch() {\n");
        code.append("    emit('search', formData.value)\n");
        code.append("  }\n\n");

        code.append("  // 展开/收起\n");
        code.append("  function handleExpand(expanded: boolean) {\n");
        code.append("    isExpanded.value = expanded\n");
        code.append("  }\n\n");

        code.append("  // 栅格占据的列数\n");
        code.append("  const setSpan = (span: number) => {\n");
        code.append("    return {\n");
        code.append("      span: span,\n");
        code.append("      xs: 24, // 手机：满宽显示\n");
        code.append("      sm: span >= 12 ? span : 12, // 平板：大于等于12保持，否则用半宽\n");
        code.append("      md: span >= 8 ? span : 8, // 中等屏幕：大于等于8保持，否则用三分之一宽\n");
        code.append("      lg: span,\n");
        code.append("      xl: span\n");
        code.append("    }\n");
        code.append("  }\n");
        code.append("</script>");

        return code.toString();
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

        code.append("  /**\n");
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

    /**
     * 生成Vue主页面组件代码
     */
    private String generateVueMainCode(IData tableInfo, IDataset columns) {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String menuName = tableInfo.getString("menu_name");
        String packageName = tableInfo.getString("package_name");

        // 生成模板部分
        code.append("<template>\n");
        code.append("  <div class=\"art-full-height\">\n");
        code.append("    <!-- 搜索面板 -->\n");
        code.append("    <TableSearch v-model=\"searchForm\" @search=\"handleSearch\" @reset=\"resetSearchParams\" />\n\n");

        code.append("    <ElCard class=\"art-table-card\" shadow=\"never\">\n");
        code.append("      <!-- 表格头部 -->\n");
        code.append("      <ArtTableHeader v-model:columns=\"columnChecks\" :loading=\"loading\" @refresh=\"refreshData\">\n");
        code.append("        <template #left>\n");
        code.append("          <ElSpace wrap>\n");
        code.append("            <ElButton v-permission=\"'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":save'\" @click=\"showDialog('add')\" v-ripple>\n");
        code.append("              <template #icon>\n");
        code.append("                <ArtSvgIcon icon=\"ri:add-fill\" />\n");
        code.append("              </template>\n");
        code.append("              新增\n");
        code.append("            </ElButton>\n");
        code.append("            <ElButton\n");
        code.append("              v-permission=\"'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":destroy'\"\n");
        code.append("              :disabled=\"selectedRows.length === 0\"\n");
        code.append("              @click=\"deleteSelectedRows(api.delete, refreshData)\"\n");
        code.append("              v-ripple\n");
        code.append("            >\n");
        code.append("              <template #icon>\n");
        code.append("                <ArtSvgIcon icon=\"ri:delete-bin-5-line\" />\n");
        code.append("              </template>\n");
        code.append("              删除\n");
        code.append("            </ElButton>\n");
        code.append("          </ElSpace>\n");
        code.append("        </template>\n");
        code.append("      </ArtTableHeader>\n\n");

        code.append("      <!-- 表格 -->\n");
        code.append("      <ArtTable\n");
        code.append("        ref=\"tableRef\"\n");
        code.append("        rowKey=\"id\"\n");
        code.append("        :loading=\"loading\"\n");
        code.append("        :data=\"data\"\n");
        code.append("        :columns=\"columns\"\n");
        code.append("        :pagination=\"pagination\"\n");
        code.append("        @sort-change=\"handleSortChange\"\n");
        code.append("        @selection-change=\"handleSelectionChange\"\n");
        code.append("        @pagination:size-change=\"handleSizeChange\"\n");
        code.append("        @pagination:current-change=\"handleCurrentChange\"\n");
        code.append("      >\n");
        code.append("        <!-- 操作列 -->\n");
        code.append("        <template #operation=\"{ row }\">\n");
        code.append("          <div class=\"flex gap-2\">\n");
        code.append("            <SaButton\n");
        code.append("              v-permission=\"'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":update'\"\n");
        code.append("              type=\"secondary\"\n");
        code.append("              @click=\"showDialog('edit', row)\"\n");
        code.append("            />\n");
        code.append("            <SaButton\n");
        code.append("              v-permission=\"'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":destroy'\"\n");
        code.append("              type=\"error\"\n");
        code.append("              @click=\"deleteRow(row, api.delete, refreshData)\"\n");
        code.append("            />\n");
        code.append("          </div>\n");
        code.append("        </template>\n");
        code.append("      </ArtTable>\n");
        code.append("    </ElCard>\n\n");

        code.append("    <!-- 编辑弹窗 -->\n");
        code.append("    <EditDialog\n");
        code.append("      v-model=\"dialogVisible\"\n");
        code.append("      :dialog-type=\"dialogType\"\n");
        code.append("      :data=\"dialogData\"\n");
        code.append("      @success=\"refreshData\"\n");
        code.append("    />\n");
        code.append("  </div>\n");
        code.append("</template>\n\n");

        // 生成脚本部分
        code.append("<script setup lang=\"ts\">\n");
        code.append("  import { useTable } from '@/hooks/core/useTable'\n");
        code.append("  import { useSaiAdmin } from '@/composables/useSaiAdmin'\n");
        code.append("  import api from '../../api/").append(businessName).append("/").append(className.toLowerCase()).append("'\n");
        code.append("  import TableSearch from './modules/table-search.vue'\n");
        code.append("  import EditDialog from './modules/edit-dialog.vue'\n\n\n");

        code.append("  // 搜索表单\n");
        code.append("  const searchForm = ref({\n");

        // 生成搜索字段
        int searchFieldCount = 0;
        for (int i = 0; i < columns.size() && searchFieldCount < 3; i++) {  // 限制3个搜索字段
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");

            // 跳过不需要搜索的字段
            if ("id".equals(columnName) || "create_time".equals(columnName) ||
                "update_time".equals(columnName) || "delete_time".equals(columnName) ||
                "content".equals(columnName) || "image".equals(columnName)) {
                continue;
            }

            if (columnComment == null || columnComment.trim().isEmpty()) {
                columnComment = columnName;
            }

            code.append("    ").append(columnName).append(": undefined,\n");
            searchFieldCount++;
        }

        code.append("  })\n\n");

        code.append("  // 搜索处理\n");
        code.append("  const handleSearch = (params: Record<string, any>) => {\n");
        code.append("    Object.assign(searchParams, params)\n");
        code.append("    getData()\n");
        code.append("  }\n\n");

        code.append("  // 表格配置\n");
        code.append("  const {\n");
        code.append("    columns,\n");
        code.append("    columnChecks,\n");
        code.append("    data,\n");
        code.append("    loading,\n");
        code.append("    getData,\n");
        code.append("    searchParams,\n");
        code.append("    pagination,\n");
        code.append("    resetSearchParams,\n");
        code.append("    handleSortChange,\n");
        code.append("    handleSizeChange,\n");
        code.append("    handleCurrentChange,\n");
        code.append("    refreshData\n");
        code.append("  } = useTable({\n");
        code.append("    core: {\n");
        code.append("      apiFn: api.list,\n");
        code.append("      columnsFactory: () => [\n");
        code.append("        { type: 'selection' },\n");

        // 生成表格列配置
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            String viewType = column.getString("view_type");

            if (columnComment == null || columnComment.trim().isEmpty()) {
                columnComment = columnName;
            }

            // 跳过不需要在表格中显示的字段
            if ("content".equals(columnName) || "delete_time".equals(columnName)) {
                continue;
            }

            code.append("        { prop: '").append(columnName).append("', label: '").append(columnComment).append("'");

            // 添加特殊类型的配置
            if ("image".equals(columnName) || "avatar".equals(columnName) || "pic".equals(columnName)) {
                code.append(", saiType: 'image'");
            } else if ("status".equals(columnName)) {
                code.append(", saiType: 'dict', saiDict: 'data_status'");
            } else if ("is_".equals(columnName.substring(0, Math.min(3, columnName.length())))) {
                code.append(", saiType: 'dict', saiDict: 'yes_or_no'");
            }

            // 操作列特殊处理
            if ("id".equals(columnName)) {
                code.append(", width: 100, fixed: 'right', useSlot: true }");
            } else {
                code.append(" },");
            }

            code.append("\n");
        }

        code.append("        { prop: 'operation', label: '操作', width: 100, fixed: 'right', useSlot: true }\n");
        code.append("      ]\n");
        code.append("    }\n");
        code.append("  })\n\n");

        code.append("  // 编辑配置\n");
        code.append("  const {\n");
        code.append("    dialogType,\n");
        code.append("    dialogVisible,\n");
        code.append("    dialogData,\n");
        code.append("    showDialog,\n");
        code.append("    deleteRow,\n");
        code.append("    deleteSelectedRows,\n");
        code.append("    handleSelectionChange,\n");
        code.append("    selectedRows\n");
        code.append("  } = useSaiAdmin()\n\n");

        code.append("</script>");

        return code.toString();
    }

    /**
     * 生成数据库菜单SQL代码
     */
    private String generateMenuSqlCode(IData tableInfo) {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String menuName = tableInfo.getString("menu_name");
        String packageName = tableInfo.getString("package_name");

        // 生成SQL注释
        code.append("-- 数据库语句--\n\n");
        code.append("-- 菜单[").append(menuName).append("] SQL\n");

        // 生成主菜单插入语句
        code.append("INSERT INTO `sa_system_menu`(`parent_id`, `name`, `code`, `slug`, `type`, `path`, `component`, `icon`, `sort`, `is_iframe`, `is_keep_alive`, `is_hidden`, `is_fixed_tab`, `is_full_page`, `create_time`, `update_time`) ");
        code.append("VALUES (80, '").append(menuName).append("', 'saicms/").append(businessName).append("/").append(className.toLowerCase()).append("', '', 2, '").append(businessName).append("/").append(className.toLowerCase()).append("', '/plugin/saicms/").append(businessName).append("/").append(className.toLowerCase()).append("/index', 'ri:home-2-line', 100, 2, 2, 2, 2, 2, now(), now());\n\n");

        // 生成获取插入ID的语句
        code.append("SET @id := LAST_INSERT_ID();\n\n");

        // 生成子菜单插入语句
        code.append("INSERT INTO `sa_system_menu`(`parent_id`, `name`, `slug`, `type`, `sort`, `is_iframe`, `is_keep_alive`, `is_hidden`, `is_fixed_tab`, `is_full_page`, `create_time`, `update_time`) ");
        code.append("VALUES (@id, '列表', 'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":index', 3, 100, 2, 2, 2, 2, 2, now(), now());\n");

        code.append("INSERT INTO `sa_system_menu`(`parent_id`, `name`, `slug`, `type`, `sort`, `is_iframe`, `is_keep_alive`, `is_hidden`, `is_fixed_tab`, `is_full_page`, `create_time`, `update_time`) ");
        code.append("VALUES (@id, '保存', 'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":save', 3, 100, 2, 2, 2, 2, 2, now(), now());\n");

        code.append("INSERT INTO `sa_system_menu`(`parent_id`, `name`, `slug`, `type`, `sort`, `is_iframe`, `is_keep_alive`, `is_hidden`, `is_fixed_tab`, `is_full_page`, `create_time`, `update_time`) ");
        code.append("VALUES (@id, '更新', 'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":update', 3, 100, 2, 2, 2, 2, 2, now(), now());\n");

        code.append("INSERT INTO `sa_system_menu`(`parent_id`, `name`, `slug`, `type`, `sort`, `is_iframe`, `is_keep_alive`, `is_hidden`, `is_fixed_tab`, `is_full_page`, `create_time`, `update_time`) ");
        code.append("VALUES (@id, '读取', 'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":read', 3, 100, 2, 2, 2, 2, 2, now(), now());\n");

        code.append("INSERT INTO `sa_system_menu`(`parent_id`, `name`, `slug`, `type`, `sort`, `is_iframe`, `is_keep_alive`, `is_hidden`, `is_fixed_tab`, `is_full_page`, `create_time`, `update_time`) ");
        code.append("VALUES (@id, '删除', 'saicms:").append(businessName).append(":").append(className.toLowerCase()).append(":destroy', 3, 100, 2, 2, 2, 2, 2, now(), now());");

        return code.toString();
    }

}
