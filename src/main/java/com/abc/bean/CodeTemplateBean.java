package com.abc.bean;

import com.alibaba.fastjson.JSONArray;
import com.xtr.framework.hutool.BaseDao;
import com.xtr.framework.hutool.IData;
import com.xtr.framework.hutool.IDataset;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 代码模板Bean类
 * 封装各种代码模板的生成逻辑
 */
public class CodeTemplateBean {

    private IData tableInfo;
    private IDataset columns;

    public CodeTemplateBean() {}

    public CodeTemplateBean(IData tableInfo, IDataset columns) {
        this.tableInfo = tableInfo;
        this.columns = columns;
    }

    public IData getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(IData tableInfo) {
        this.tableInfo = tableInfo;
    }

    public IDataset getColumns() {
        return columns;
    }

    public void setColumns(IDataset columns) {
        this.columns = columns;
    }


    /**
     * 生成Vue搜索组件代码
     */
    public String generateVueSearchCode() {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String menuName = tableInfo.getString("menu_name");
        String packageName = tableInfo.getString("package_name");

        // 生成模板部分
        code.append("<template>\n");
        code.append("  <sa-search-bar v-model=\"formData\" @search=\"handleSearch\" @reset=\"handleReset\">\n");

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
        code.append("import type { FormInstance } from 'element-plus'\n\n");

        // 定义Props
        code.append("const props = defineProps({\n");
        code.append("  modelValue: {\n");
        code.append("    type: Object,\n");
        code.append("    default: () => ({})\n");
        code.append("  }\n");
        code.append("})\n\n");

        // 定义Emits
        code.append("const emit = defineEmits(['update:modelValue', 'search', 'reset'])\n\n");

        // 定义响应式数据
        code.append("// 搜索表单数据\n");
        code.append("const formData = computed({\n");
        code.append("  get: () => props.modelValue,\n");
        code.append("  set: (val) => emit('update:modelValue', val)\n");
        code.append("})\n\n");

        // 定义方法
        code.append("// 处理搜索\n");
        code.append("const handleSearch = () => {\n");
        code.append("  emit('search', formData.value)\n");
        code.append("}\n\n");

        code.append("// 处理重置\n");
        code.append("const handleReset = () => {\n");
        code.append("  emit('reset')\n");
        code.append("}\n\n");

        code.append("// 设置栅格间距\n");
        code.append("const setSpan = (span: number) => ({\n");
        code.append("  xs: 24,\n");
        code.append("  sm: 12,\n");
        code.append("  md: span,\n");
        code.append("  lg: span,\n");
        code.append("  xl: span\n");
        code.append("})\n");
        code.append("</script>");

        return code.toString();
    }

    /**
     * 生成Vue弹窗表单组件代码
     */
    public String generateVueDialogCode() {
        StringBuilder code = new StringBuilder();
        String className = tableInfo.getString("class_name");
        String businessName = tableInfo.getString("business_name");
        String menuName = tableInfo.getString("menu_name");
        String packageName = tableInfo.getString("package_name");

        // 生成模板部分
        code.append("<template>\n");
        code.append("  <el-dialog\n");
        code.append("    v-model=\"dialogVisible\"\n");
        code.append("    :title=\"dialogTitle\"\n");
        code.append("    width=\"500px\"\n");
        code.append("    :close-on-click-modal=\"false\"\n");
        code.append("    @close=\"handleClose\"\n");
        code.append("  >\n");
        code.append("    <el-form\n");
        code.append("      ref=\"formRef\"\n");
        code.append("      :model=\"formData\"\n");
        code.append("      :rules=\"formRules\"\n");
        code.append("      label-width=\"100px\"\n");
        code.append("      @submit.prevent=\"handleSubmit\"\n");
        code.append("    >\n");

        // 根据列信息生成表单字段
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            String isRequired = column.getString("is_required");
            String viewType = column.getString("view_type");

            // 跳过主键和时间字段
            if ("id".equals(columnName) || "create_time".equals(columnName) ||
                "update_time".equals(columnName) || "delete_time".equals(columnName)) {
                continue;
            }

            // 如果没有注释，使用列名
            if (columnComment == null || columnComment.trim().isEmpty()) {
                columnComment = columnName;
            }

            code.append("      <el-form-item label=\"").append(columnComment).append("\" prop=\"").append(columnName).append("\">\n");

            // 根据字段类型生成不同的输入组件
            if ("imageUpload".equals(viewType)) {
                code.append("        <sa-image-upload v-model=\"formData.").append(columnName).append("\" />\n");
            } else if ("editor".equals(viewType)) {
                code.append("        <sa-editor v-model=\"formData.").append(columnName).append("\" />\n");
            } else if ("radio".equals(viewType)) {
                code.append("        <sa-radio v-model=\"formData.").append(columnName).append("\" :options=\"").append(columnName).append("Options\" />\n");
            } else if ("checkbox".equals(viewType)) {
                code.append("        <sa-checkbox v-model=\"formData.").append(columnName).append("\" :options=\"").append(columnName).append("Options\" />\n");
            } else if ("select".equals(viewType)) {
                code.append("        <sa-select v-model=\"formData.").append(columnName).append("\" :options=\"").append(columnName).append("Options\" />\n");
            } else if ("switch".equals(viewType)) {
                code.append("        <el-switch v-model=\"formData.").append(columnName).append("\" />\n");
            } else if ("textarea".equals(viewType)) {
                code.append("        <el-input v-model=\"formData.").append(columnName).append("\" type=\"textarea\" placeholder=\"请输入").append(columnComment).append("\" />\n");
            } else if ("datetime".equals(viewType)) {
                code.append("        <el-date-picker\n");
                code.append("          v-model=\"formData.").append(columnName).append("\"\n");
                code.append("          type=\"datetime\"\n");
                code.append("          placeholder=\"请选择").append(columnComment).append("\"\n");
                code.append("        />\n");
            } else {
                code.append("        <el-input v-model=\"formData.").append(columnName).append("\" placeholder=\"请输入").append(columnComment).append("\" />\n");
            }

            code.append("      </el-form-item>\n");
        }

        code.append("    </el-form>\n");
        code.append("    <template #footer>\n");
        code.append("      <el-button @click=\"handleClose\">取消</el-button>\n");
        code.append("      <el-button type=\"primary\" @click=\"handleSubmit\" :loading=\"submitLoading\">确定</el-button>\n");
        code.append("    </template>\n");
        code.append("  </el-dialog>\n");
        code.append("</template>\n\n");

        // 生成脚本部分
        code.append("<script setup lang=\"ts\">\n");
        code.append("import type { FormInstance, FormRules } from 'element-plus'\n");
        code.append("import api from './api'\n\n");

        // 定义Props
        code.append("const props = defineProps({\n");
        code.append("  dialogType: {\n");
        code.append("    type: String as PropType<'add' | 'edit'>,\n");
        code.append("    default: 'add'\n");
        code.append("  },\n");
        code.append("  rowData: {\n");
        code.append("    type: Object,\n");
        code.append("    default: () => ({})\n");
        code.append("  }\n");
        code.append("})\n\n");

        // 定义Emits
        code.append("const emit = defineEmits(['success', 'update:dialogType'])\n\n");

        // 定义响应式数据
        code.append("// 弹窗显示状态\n");
        code.append("const dialogVisible = computed({\n");
        code.append("  get: () => !!props.dialogType,\n");
        code.append("  set: (val) => !val && emit('update:dialogType', undefined)\n");
        code.append("})\n\n");

        code.append("// 弹窗标题\n");
        code.append("const dialogTitle = computed(() => props.dialogType === 'add' ? '新增").append(menuName).append("' : '编辑").append(menuName).append("')\n\n");

        code.append("// 表单引用\n");
        code.append("const formRef = ref<FormInstance>()\n\n");

        code.append("// 提交加载状态\n");
        code.append("const submitLoading = ref(false)\n\n");

        code.append("// 表单数据\n");
        code.append("const formData = ref({\n");
        // 初始化表单字段
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            if (!"id".equals(columnName) && !"create_time".equals(columnName) &&
                !"update_time".equals(columnName) && !"delete_time".equals(columnName)) {
                code.append("  ").append(columnName).append(": '',\n");
            }
        }
        code.append("})\n\n");

        // 生成表单验证规则
        code.append("// 表单验证规则\n");
        code.append("const formRules: FormRules = {\n");
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            String isRequired = column.getString("is_required");

            if ("1".equals(isRequired) && !"id".equals(columnName) && !"create_time".equals(columnName) &&
                !"update_time".equals(columnName) && !"delete_time".equals(columnName)) {
                if (columnComment == null || columnComment.trim().isEmpty()) {
                    columnComment = columnName;
                }
                code.append("  ").append(columnName).append(": [\n");
                code.append("    { required: true, message: '请输入").append(columnComment).append("', trigger: 'blur' }\n");
                code.append("  ],\n");
            }
        }
        code.append("}\n\n");

        // 生成方法
        code.append("// 关闭弹窗\n");
        code.append("const handleClose = () => {\n");
        code.append("  dialogVisible.value = false\n");
        code.append("  resetForm()\n");
        code.append("}\n\n");

        code.append("// 重置表单\n");
        code.append("const resetForm = () => {\n");
        code.append("  formRef.value?.resetFields()\n");
        code.append("  formData.value = {\n");
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            if (!"id".equals(columnName) && !"create_time".equals(columnName) &&
                !"update_time".equals(columnName) && !"delete_time".equals(columnName)) {
                code.append("    ").append(columnName).append(": '',\n");
            }
        }
        code.append("  }\n");
        code.append("}\n\n");

        code.append("// 处理提交\n");
        code.append("const handleSubmit = async () => {\n");
        code.append("  try {\n");
        code.append("    await formRef.value?.validate()\n");
        code.append("    submitLoading.value = true\n");
        code.append("    if (props.dialogType === 'add') {\n");
        code.append("      await api.save(formData.value)\n");
        code.append("      ElMessage.success('新增成功')\n");
        code.append("    } else {\n");
        code.append("      await api.update(formData.value)\n");
        code.append("      ElMessage.success('修改成功')\n");
        code.append("    }\n");
        code.append("    emit('success')\n");
        code.append("    handleClose()\n");
        code.append("  } catch (error) {\n");
        code.append("    console.log('表单验证失败:', error)\n");
        code.append("  } finally {\n");
        code.append("    submitLoading.value = false\n");
        code.append("  }\n");
        code.append("}\n\n");

        code.append("// 监听rowData变化，用于编辑时填充表单\n");
        code.append("watch(\n");
        code.append("  () => props.rowData,\n");
        code.append("  (newVal) => {\n");
        code.append("    if (newVal && Object.keys(newVal).length > 0) {\n");
        code.append("      formData.value = { ...newVal }\n");
        code.append("    }\n");
        code.append("  },\n");
        code.append("  { immediate: true }\n");
        code.append(")\n");

        code.append("</script>");

        return code.toString();
    }

    /**
     * 生成Vue主页面组件代码
     */
    public String generateVueMainCode() {
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
        code.append("    <!-- 表格卡片 -->\n");
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
        code.append("                <ArtSvgIcon icon=\"ri:delete-bin-line\" />\n");
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
        code.append("              type=\"danger\"\n");
        code.append("              @click=\"deleteRow(row, api.delete, refreshData)\"\n");
        code.append("            />\n");
        code.append("          </div>\n");
        code.append("        </template>\n");
        code.append("      </ArtTable>\n");
        code.append("    </ElCard>\n\n");
        code.append("    <!-- 编辑弹窗 -->\n");
        code.append("    <EditDialog v-model:dialog-type=\"dialogType\" :row-data=\"currentRow\" @success=\"refreshData\" />\n");
        code.append("  </div>\n");
        code.append("</template>\n\n");

        // 生成脚本部分
        code.append("<script setup lang=\"ts\">\n");
        code.append("import { useTable } from '@/hooks'\n");
        code.append("import { useSaiAdmin } from '@/composables/useSaiAdmin'\n");
        code.append("import api from './api'\n");
        code.append("import TableSearch from './components/table-search.vue'\n");
        code.append("import EditDialog from './components/edit-dialog.vue'\n\n");

        code.append("const { loading, data, pagination, columnChecks, columns, tableRef, selectedRows, searchForm, dialogType, currentRow,\n");
        code.append("  handleSearch, resetSearchParams, refreshData, handleSortChange, handleSelectionChange,\n");
        code.append("  handleSizeChange, handleCurrentChange, showDialog, deleteRow, deleteSelectedRows } = useTable({\n");
        code.append("  api,\n");
        code.append("  columns: [\n");

        // 生成表格列配置
        for (int i = 0; i < columns.size(); i++) {
            IData column = columns.getData(i);
            String columnName = column.getString("column_name");
            String columnComment = column.getString("column_comment");
            String isList = column.getString("is_list");

            if ("1".equals(isList)) {
                if (columnComment == null || columnComment.trim().isEmpty()) {
                    columnComment = columnName;
                }
                code.append("    { prop: '").append(columnName).append("', label: '").append(columnComment).append("' },\n");
            }
        }

        code.append("  ]\n");
        code.append("})\n\n");

        code.append("// 页面初始化\n");
        code.append("onMounted(() => {\n");
        code.append("  refreshData()\n");
        code.append("})\n");

        code.append("</script>");

        return code.toString();
    }

    /**
     * 表名转驼峰命名
     * 首字母大写，如果下划线数量超过指定数量，从后往前删除前面的字符
     * @param tableName 表名
     * @param maxUnderscores 最大允许的下划线数量
     * @return 驼峰命名的类名
     */
    public static String tableNameToCamelCase(String tableName, int maxUnderscores) {
        if (tableName == null || tableName.isEmpty()) {
            return "";
        }

        // 统计下划线数量
        long underscoreCount = tableName.chars().filter(ch -> ch == '_').count();

        String processedTableName = tableName;

        // 如果下划线超过指定数量，从后往前保留最后几个部分
        if (underscoreCount > maxUnderscores) {
            String[] parts = tableName.split("_");
            if (parts.length > maxUnderscores) {
                // 保留最后maxUnderscores+1个部分
                int startIndex = parts.length - (maxUnderscores + 1);
                StringBuilder sb = new StringBuilder();
                for (int i = startIndex; i < parts.length; i++) {
                    if (sb.length() > 0) {
                        sb.append("_");
                    }
                    sb.append(parts[i]);
                }
                processedTableName = sb.toString();
            }
        }

        // 转换为驼峰命名
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = true; // 首字母大写

        for (char c : processedTableName.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }

        return result.toString();
    }

    /**
     * 表名转驼峰命名（默认最大3个下划线）
     * @param tableName 表名
     * @return 驼峰命名的类名
     */
    public static String tableNameToCamelCase(String tableName) {
        return tableNameToCamelCase(tableName, 3);
    }

    /**
     * 生成预览代码列表
     */
    public static JSONArray generatePreviewList(BaseDao dao,String id) {
        JSONArray previewList = new JSONArray();

        // 读取代码生成表信息
        IDataset tables = dao.queryList("SELECT * FROM sa_tool_generate_tables where id=?",id);

        System.out.println("找到 " + tables.size() + " 个代码生成表");

        // 初始化Velocity引擎
        VelocityEngine ve = new VelocityEngine();
        Properties props = new Properties();
        props.setProperty("resource.loader", "file");
        props.setProperty("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        props.setProperty("file.resource.loader.path",
                "src/main/resources/code_template/stub/saiadmin/vue");
        props.setProperty("input.encoding", "UTF-8");
        props.setProperty("output.encoding", "UTF-8");
        ve.init(props);

        // 处理每个表

        IData tableInfo = tables.getData(0);
        System.out.println("  处理表: " + tableInfo);
        Long tableId = tableInfo.getLong("id");
        String tableName = tableInfo.getString("table_name");
        String tableComment = tableInfo.getString("table_comment");

        System.out.println("处理表: " + tableName + " (" + tableComment + ")");

        // 读取该表的列信息
        IDataset columns = dao.queryList(
                "SELECT * FROM sa_tool_generate_columns " +
                        "WHERE table_id = " + tableId + " " +
                        "ORDER BY sort"
        );

        System.out.println("  找到 " + columns.size() + " 个列");

        // 创建Velocity上下文
        VelocityContext context = new VelocityContext();

        // 设置表相关信息
        context.put("tables", tables);
        context.put("table_id", tableId);
        context.put("table_name", tableName);
        context.put("table_comment", tableComment);
        context.put("class_name", tableInfo.getString("class_name"));
        context.put("business_name", tableInfo.getString("business_name"));
        context.put("menu_name", tableInfo.getString("menu_name"));
        context.put("package_name", tableInfo.getString("package_name"));
        context.put("namespace", tableInfo.getString("namespace"));
        context.put("url_path", tableInfo.getString("url_path"));
        context.put("tpl_category", tableInfo.getString("tpl_category"));
        context.put("url_path","api/"+tableInfo.getString("business_name"));// 新增加 /api为前缀

        // 处理列信息
        List<IData> columnList = new ArrayList<>();
        for (int j = 0; j < columns.size(); j++) {
            IData column = columns.getData(j);

            // 处理列的各种属性
            IData processedColumn = new IData();
            processedColumn.putAll(column);

            // 处理布尔值转换
            processedColumn.set("is_pk", column.getInt("is_pk", 0) == 2 ? 2 : 1);
            processedColumn.set("is_required", column.getInt("is_required", 0) == 2 ? 2 : 1);
            processedColumn.set("is_insert", column.getInt("is_insert", 0) == 2 ? 2 : 1);
            processedColumn.set("is_edit", column.getInt("is_edit", 0) == 2 ? 2 : 1);
            processedColumn.set("is_list", column.getInt("is_list", 0) == 2 ? 2 : 1);
            processedColumn.set("is_query", column.getInt("is_query", 0) == 2 ? 2 : 1);
            processedColumn.set("is_sort", column.getInt("is_sort", 0) == 2 ? 2 : 1);

            // 处理视图类型
            String viewType = column.getString("view_type", "");
            if (viewType.contains("|")) {
                viewType = viewType.split("\\|")[0]; // 取第一个值
            }
            processedColumn.set("view_type", viewType);

            // 处理选项数据
            String optionsStr = column.getString("options", "{}");
            IData options = new IData(optionsStr);
            processedColumn.set("options", options);

            columnList.add(processedColumn);
        }

        context.put("columns", columnList);
        System.out.println("  完成表 " + tableName + " 的代码生成\n");

        // 生成Controller代码
        String controllerCode = generateCodeByString(ve, context, "saiadmin/java/controller.vm", CodeTemplateBean.tableNameToCamelCase(tableName) + "Controller.java");
        IData controllerPreview = new IData();
        controllerPreview.set("tab_name", tableInfo.getString("class_name")+"Controller.java");
        controllerPreview.set("name", "controller");
        controllerPreview.set("lang", "java");
        controllerPreview.set("code", controllerCode);
        previewList.add(controllerPreview);

        // 生成Vue API代码
        String vueApiCode = generateCodeByString(ve, context, "saiadmin/ts/api.vm", tableName + "_api.ts");
        IData vueApiPreview = new IData();
        vueApiPreview.set("tab_name", "api.ts");
        vueApiPreview.set("name", "api");
        vueApiPreview.set("lang", "typescript");
        vueApiPreview.set("code", vueApiCode);
        previewList.add(vueApiPreview);

        // 生成Vue搜索组件代码
        String vueSearchCode = generateCodeByString(ve, context, "saiadmin/vue/table-search.vm", tableName + "_search.vue");
        IData vueSearchPreview = new IData();
        vueSearchPreview.set("tab_name", "table-search.vue");
        vueSearchPreview.set("name", "table-search");
        vueSearchPreview.set("lang", "html");
        vueSearchPreview.set("code", vueSearchCode);
        previewList.add(vueSearchPreview);

        // 生成Vue弹窗表单组件代码
        String vueDialogCode = generateCodeByString(ve, context, "saiadmin/vue/edit-dialog.vm", tableName + "_dialog.vue");
        IData vueDialogPreview = new IData();
        vueDialogPreview.set("tab_name", "edit-dialog.vue");
        vueDialogPreview.set("name", "edit-dialog");
        vueDialogPreview.set("lang", "html");
        vueDialogPreview.set("code", vueDialogCode);
        previewList.add(vueDialogPreview);

        // 生成Vue主页面组件代码
        String vueMainCode = generateCodeByString(ve, context, "saiadmin/vue/index.vm", tableName + "_index.vue");
        IData vueMainPreview = new IData();
        vueMainPreview.set("tab_name", "index.vue");
        vueMainPreview.set("name", "index");
        vueMainPreview.set("lang", "html");
        vueMainPreview.set("code", vueMainCode);
        previewList.add(vueMainPreview);

        // 生成数据库菜单SQL代码
        String sqlCode = generateCodeByString(ve, context, "saiadmin/sql/sql.vm", tableName + "Menu.sql");
        IData sqlPreview = new IData();
        sqlPreview.set("tab_name", tableName+"Menu.sql");
        sqlPreview.set("name", "sql");
        sqlPreview.set("lang", "sql");
        sqlPreview.set("code", sqlCode);
        previewList.add(sqlPreview);

        return previewList;
    }

    /**
     * 测试表名转驼峰方法
     */
    public static void main(String[] args) {
        System.out.println("=== 表名转驼峰测试（默认3个下划线）===");
        System.out.println("user_info -> " + tableNameToCamelCase("user_info"));
        System.out.println("sys_user_role_permission -> " + tableNameToCamelCase("sys_user_role_permission"));
        System.out.println("sa_system_menu_config_test -> " + tableNameToCamelCase("sa_system_menu_config_test"));
        System.out.println("very_long_table_name_with_many_parts -> " + tableNameToCamelCase("very_long_table_name_with_many_parts"));

        System.out.println("\n=== 表名转驼峰测试（自定义下划线数量）===");
        System.out.println("user_info (max=2) -> " + tableNameToCamelCase("user_info", 2));
        System.out.println("sys_user_role_permission (max=2) -> " + tableNameToCamelCase("sys_user_role_permission", 2));
        System.out.println("sa_system_menu_config_test (max=4) -> " + tableNameToCamelCase("sa_system_menu_config_test", 4));
        System.out.println("a_b_c_d_e_f_g (max=3) -> " + tableNameToCamelCase("a_b_c_d_e_f_g", 3));
        System.out.println("a_b_c_d_e_f_g (max=1) -> " + tableNameToCamelCase("a_b_c_d_e_f_g", 1));

        System.out.println("\n=== 边界情况测试 ===");
        System.out.println("simple -> " + tableNameToCamelCase("simple"));
        System.out.println("null -> '" + tableNameToCamelCase(null) + "'");
        System.out.println("empty -> '" + tableNameToCamelCase("") + "'");
    }


    /**
     * 生成单个代码文件
     * @param ve Velocity引擎
     * @param context Velocity上下文
     * @param templateName 模板文件名
     * @param outputFileName 输出文件名
     */
    private static String generateCodeByString(VelocityEngine ve, VelocityContext context,
                                         String templateName, String outputFileName) {
        try {
            String currentDir = System.getProperty("user.dir");
            System.out.println("  生成文件11: " + currentDir);

            // 读取模板内容
            String templatePath = currentDir+"/src/main/resources/code_template/stub/" + templateName;
            java.nio.file.Path path = java.nio.file.Paths.get(templatePath);
            String templateContent = new String(java.nio.file.Files.readAllBytes(path));

            // 处理模板内容中的特殊语法
            templateContent = processTemplateContent(templateContent);

            // 创建StringWriter来捕获输出
            StringWriter writer = new StringWriter();

            // 应用Velocity模板
            ve.evaluate(context, writer, templateName, templateContent);

            // 输出生成的代码
            return  writer.toString();


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("    生成文件 " + templateName + " 时出错: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 处理模板内容中的特殊语法
     * @param content 模板内容
     * @return 处理后的内容
     */
    private static String processTemplateContent(String content) {
        // 处理常见的过滤器语法
        content = content.replaceAll("\\|(bool|formatNumber|parseNumber)", "");

        // 处理三元运算符
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{[^}]*\\s*\\?\\s*[^:]*:\\s*[^}]*\\}");
        java.util.regex.Matcher matcher = pattern.matcher(content);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String expr = matcher.group();
            String replacement = expr.replaceAll("\\s*\\?\\s*[^:]*:\\s*", " ? 'true' : 'false'");
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        content = sb.toString();

        return content;
    }

}
