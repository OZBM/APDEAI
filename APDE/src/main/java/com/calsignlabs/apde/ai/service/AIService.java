package com.calsignlabs.apde.ai.service;

import com.calsignlabs.apde.ai.model.ChatMessage;

public interface AIService {
    void sendMessage(String message, MessageCallback callback);
    void executeCommand(String command, CommandCallback callback);
    void editCode(String filePath, String code, CodeEditCallback callback);
    void validateApiKey(String apiKey, ApiKeyCallback callback);

    interface MessageCallback {
        void onResponse(ChatMessage response);
        void onError(String error);
    }

    interface CommandCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    interface CodeEditCallback {
        void onSuccess(String editedCode);
        void onError(String error);
    }

    interface ApiKeyCallback {
        void onSuccess();
        void onError(String error);
    }
}