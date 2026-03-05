package com.abc.web.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

/**
 * Ollama AI服务调用工具类
 * 基于Hutool框架实现
 */
@Component
public class OllamaUtil {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final String GENERATE_ENDPOINT = "/api/generate";
    private static final int DEFAULT_TIMEOUT = 30000; // 30秒超时

    /**
     * 调用Ollama生成文本
     * @param model 模型名称
     * @param prompt 提示词
     * @return AI生成的文本内容
     */
    public String generateText(String model, String prompt) {
        return generateText(model, prompt, DEFAULT_BASE_URL);
    }

    /**
     * 调用Ollama生成文本（指定服务器地址）
     * @param model 模型名称
     * @param prompt 提示词
     * @param baseUrl 服务器地址
     * @return AI生成的文本内容
     */
    public String generateText(String model, String prompt, String baseUrl) {
        try {
            // 构造请求参数
            JSONObject requestBody = JSONUtil.createObj()
                    .set("model", model)
                    .set("prompt", prompt)
                    .set("stream", false);

            // 发送POST请求
            String url = baseUrl + GENERATE_ENDPOINT;
            HttpResponse response = HttpRequest.post(url)
                    .body(requestBody.toString())
                    .contentType("application/json")
                    .timeout(DEFAULT_TIMEOUT)
                    .execute();

            // 检查响应状态
            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                return jsonResponse.getStr("response");
            } else {
                return "请求失败，状态码: " + response.getStatus();
            }

        } catch (Exception e) {
            return "调用异常: " + e.getMessage();
        }
    }

    /**
     * 调用Ollama生成文本（完整参数）
     * @param model 模型名称
     * @param prompt 提示词
     * @param baseUrl 服务器地址
     * @param timeout 超时时间（毫秒）
     * @return 完整的响应对象
     */
    public JSONObject generateTextFull(String model, String prompt, String baseUrl, int timeout) {
        try {
            // 构造请求参数
            JSONObject requestBody = JSONUtil.createObj()
                    .set("model", model)
                    .set("prompt", prompt)
                    .set("stream", false);

            // 发送POST请求
            String url = baseUrl + GENERATE_ENDPOINT;
            HttpResponse response = HttpRequest.post(url)
                    .body(requestBody.toString())
                    .contentType("application/json")
                    .timeout(timeout)
                    .execute();

            // 返回完整响应
            JSONObject result = JSONUtil.createObj();
            result.set("status", response.getStatus());
            result.set("success", response.getStatus() == 200);
            
            if (response.getStatus() == 200) {
                JSONObject jsonResponse = JSONUtil.parseObj(response.body());
                result.set("data", jsonResponse);
                result.set("response", jsonResponse.getStr("response"));
            } else {
                result.set("error", "请求失败，状态码: " + response.getStatus());
                result.set("raw_response", response.body());
            }

            return result;

        } catch (Exception e) {
            JSONObject errorResult = JSONUtil.createObj();
            errorResult.set("status", -1);
            errorResult.set("success", false);
            errorResult.set("error", "调用异常: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 检查Ollama服务是否可用
     * @param baseUrl 服务器地址
     * @return 是否可用
     */
    public boolean isServiceAvailable(String baseUrl) {
        try {
            String url = baseUrl + "/api/tags";
            HttpResponse response = HttpRequest.get(url)
                    .timeout(5000)
                    .execute();
            return response.getStatus() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取可用的模型列表
     * @param baseUrl 服务器地址
     * @return 模型列表JSON
     */
    public JSONObject getModelList(String baseUrl) {
        try {
            String url = baseUrl + "/api/tags";
            HttpResponse response = HttpRequest.get(url)
                    .timeout(10000)
                    .execute();
            
            if (response.getStatus() == 200) {
                return JSONUtil.parseObj(response.body());
            } else {
                JSONObject errorResult = JSONUtil.createObj();
                errorResult.set("error", "获取模型列表失败，状态码: " + response.getStatus());
                return errorResult;
            }
        } catch (Exception e) {
            JSONObject errorResult = JSONUtil.createObj();
            errorResult.set("error", "调用异常: " + e.getMessage());
            return errorResult;
        }
    }
}