package com.calsignlabs.apde.ai.model;

public class AIProvider {
    public static final String PROVIDER_ANTHROPIC = "anthropic";
    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_DEEPSEEK = "deepseek";

    private String name;
    private String apiKey;
    private String model;
    private boolean isActive;

    public AIProvider(String name, String apiKey, String model) {
        this.name = name;
        this.apiKey = apiKey;
        this.model = model;
        this.isActive = false;
    }

    public String getName() {
        return name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}