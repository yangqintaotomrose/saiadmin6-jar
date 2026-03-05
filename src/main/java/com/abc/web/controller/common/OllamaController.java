package com.abc.web.controller.common;

import com.abc.web.controller.WebController;
import com.abc.web.domain.R;
import com.abc.web.util.OllamaUtil;
import com.xtr.framework.hutool.IData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ollama AI服务控制器
 * 提供AI对话相关接口
 */
@RestController
@RequestMapping("/proxy/core/ollama/")
public class OllamaController extends WebController {

    @Autowired
    private OllamaUtil ollamaUtil;

    /**
     * AI文本生成接口
     */
    @RequestMapping(value = "generate")
    public Object generate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        String prompt = param.getString("prompt");
        String model = param.getString("model", "llama3.2"); // 默认模型
        String baseUrl = param.getString("baseUrl", "http://localhost:11434"); // 默认地址

        if (prompt == null || prompt.isEmpty()) {
            return R.fail("提示词不能为空");
        }

        String result = ollamaUtil.generateText(model, prompt, baseUrl);
        return R.ok("AI生成成功", result);
    }

    /**
     * 检查服务状态
     */
    @RequestMapping(value = "status")
    public Object status(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        String baseUrl = param.getString("baseUrl", "http://localhost:11434");

        boolean isAvailable = ollamaUtil.isServiceAvailable(baseUrl);
        
        IData result = new IData();
        result.set("available", isAvailable);
        result.set("baseUrl", baseUrl);
        result.set("timestamp", System.currentTimeMillis());
        
        if (isAvailable) {
            return R.ok("服务正常", result);
        } else {
            return R.fail("服务不可用", result);
        }
    }

    /**
     * 获取模型列表
     */
    @RequestMapping(value = "models")
    public Object models(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getRequestParam();
        String baseUrl = param.getString("baseUrl", "http://localhost:11434");

        // 先检查服务是否可用
        if (!ollamaUtil.isServiceAvailable(baseUrl)) {
            return R.fail("Ollama服务不可用");
        }

        Object modelList = ollamaUtil.getModelList(baseUrl);
        return R.ok("获取模型列表成功", modelList);
    }

    /**
     * 高级文本生成（返回完整响应）
     */
    @RequestMapping(value = "generateFull")
    public Object generateFull(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IData param = getIDataFromStream();
        String prompt = param.getString("prompt");
        String model = param.getString("model", "llama3.2");
        String baseUrl = param.getString("baseUrl", "http://localhost:11434");
        Integer timeout = param.getInt("timeout", 30000);

        if (prompt == null || prompt.isEmpty()) {
            return R.fail("提示词不能为空");
        }

        Object result = ollamaUtil.generateTextFull(model, prompt, baseUrl, timeout);
        return R.ok("AI生成完成", result);
    }
}