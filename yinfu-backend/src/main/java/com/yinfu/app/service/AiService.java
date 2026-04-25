package com.yinfu.app.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.AiChatRequest;
import com.yinfu.app.dto.AiDiagnosisRequest;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {

    @Value("${siliconflow.api-key}")
    private String apiKey;

    @Value("${siliconflow.base-url}")
    private String baseUrl;

    @Value("${siliconflow.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public Result<?> chat(AiChatRequest request) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("messages", request.getMessages());
            requestBody.put("stream", false);
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.5f);

            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(),
                    MediaType.parse("application/json")
            );

            Request httpRequest = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(httpRequest).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    return Result.error(500, "AI服务调用失败: " + responseBody);
                }

                JSONObject jsonResponse = JSON.parseObject(responseBody);
                String content = jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                return Result.success(new AiResponse(content));
            }
        } catch (IOException e) {
            return Result.error(500, "网络错误: " + e.getMessage());
        }
    }

    public Result<?> diagnosis(AiDiagnosisRequest request) {
        try {
            String systemPrompt = "你是专业AI医疗助手。请简洁回复用户症状咨询。\n规则：\n1. 建议仅供参考，不能替代医生诊断\n2. 严重症状请及时就医\n3. 回答简洁明了，不超过200字\n4. 不确定时建议咨询专业医生";

            java.util.List<AiChatRequest.Message> messages = new ArrayList<>();

            AiChatRequest.Message systemMessage = new AiChatRequest.Message();
            systemMessage.setRole("system");
            systemMessage.setContent(systemPrompt);
            messages.add(systemMessage);

            AiChatRequest.Message userMessage = new AiChatRequest.Message();
            userMessage.setRole("user");
            userMessage.setContent(request.getSymptom());
            messages.add(userMessage);

            AiChatRequest chatRequest = new AiChatRequest();
            chatRequest.setMessages(messages);

            return chat(chatRequest);
        } catch (Exception e) {
            return Result.error(500, "AI服务调用失败: " + e.getMessage());
        }
    }

    public static class AiResponse {
        public String content;

        public AiResponse(String content) {
            this.content = content;
        }
    }
}