package com.calsignlabs.apde.ai.service;

import com.calsignlabs.apde.ai.model.ChatMessage;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class OpenAIService implements AIService {
    private static final String BASE_URL = "https://api.openai.com/v1";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private String apiKey;
    private String model;

    public OpenAIService(String apiKey, String model) {
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
                .put("content", "You are an AI programming assistant helping with code editing and IDE tasks."));
        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", message));
        json.put("messages", messages);
        json.put("temperature", 0.7);
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
            "Execute the following command in the IDE. Respond with ONLY the command output, no additional text:\n%s",
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
            "Edit the following code from %s. Respond with ONLY the edited code, no additional text:\n\n%s",
            filePath, code
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