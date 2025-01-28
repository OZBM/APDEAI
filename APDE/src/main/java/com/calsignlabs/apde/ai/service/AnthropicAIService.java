package com.calsignlabs.apde.ai.service;

import com.calsignlabs.apde.ai.model.ChatMessage;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class AnthropicAIService implements AIService {
    private static final String BASE_URL = "https://api.anthropic.com/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private String apiKey;
    private String model;

    public AnthropicAIService(String apiKey, String model) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public void sendMessage(String message, MessageCallback callback) {
        JSONObject json = new JSONObject();
        json.put("model", model);
        json.put("messages", new JSONObject[]{ 
            new JSONObject().put("role", "user").put("content", message)
        });
        json.put("max_tokens", 1000);

        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/completions")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        callback.onError("API request failed: " + response.code());
                        return;
                    }

                    JSONObject jsonResponse = new JSONObject(responseBody.string());
                    String content = jsonResponse.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    callback.onResponse(new ChatMessage(content, ChatMessage.TYPE_AI));
                } catch (Exception e) {
                    callback.onError("Failed to parse response: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void executeCommand(String command, CommandCallback callback) {
        // Format the command execution request
        String prompt = "Execute the following command in the IDE: " + command;
        
        sendMessage(prompt, new MessageCallback() {
            @Override
            public void onResponse(ChatMessage response) {
                callback.onSuccess(response.getMessage());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public void editCode(String filePath, String code, CodeEditCallback callback) {
        // Format the code editing request
        String prompt = "Edit the following code from " + filePath + ":\n\n" + code;
        
        sendMessage(prompt, new MessageCallback() {
            @Override
            public void onResponse(ChatMessage response) {
                callback.onSuccess(response.getMessage());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public void validateApiKey(String apiKey, ApiKeyCallback callback) {
        // Send a simple test request to validate the API key
        JSONObject json = new JSONObject();
        json.put("model", model);
        json.put("messages", new JSONObject[]{ 
            new JSONObject().put("role", "user").put("content", "test")
        });
        json.put("max_tokens", 1);

        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/completions")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .post(RequestBody.create(json.toString(), JSON))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Failed to validate API key: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Invalid API key");
                }
                response.close();
            }
        });
    }
}