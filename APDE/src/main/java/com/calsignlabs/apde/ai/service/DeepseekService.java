package com.calsignlabs.apde.ai.service;

import com.calsignlabs.apde.ai.model.ChatMessage;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class DeepseekService implements AIService {
    private static final String BASE_URL = "https://api.deepseek.com/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private String apiKey;
    private String model;

    public DeepseekService(String apiKey, String model) {
        this.client = new OkHttpClient();
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public void sendMessage(String message, MessageCallback callback) {
        JSONObject json = new JSONObject();
        json.put("model", model);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content", "You are an AI programming assistant specialized in code editing and IDE tasks."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", message));
        json.put("messages", messages);
        json.put("temperature", 0.3); // Lower temperature for more focused coding responses
        json.put("max_tokens", 2000);

        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
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
        String prompt = String.format(
            "Execute the following command in the IDE. Format the response as a command output:\n%s",
            command
        );
        
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
        String prompt = String.format(
            "Edit and improve the following code from %s. Focus on best practices and performance:\n\n%s",
            filePath, code
        );
        
        sendMessage(prompt, new MessageCallback() {
            @Override
            public void onResponse(ChatMessage response) {
                // Extract code block from response if present
                String content = response.getMessage();
                if (content.contains("```")) {
                    content = content.split("```")[1];
                    if (content.startsWith("java")) {
                        content = content.substring(4);
                    }
                }
                callback.onSuccess(content.trim());
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    @Override
    public void validateApiKey(String apiKey, ApiKeyCallback callback) {
        JSONObject json = new JSONObject();
        json.put("model", model);
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", "test"));
        json.put("messages", messages);
        json.put("max_tokens", 1);

        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
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