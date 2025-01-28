package com.calsignlabs.apde.ai.service;

import com.calsignlabs.apde.ai.model.AIProvider;

public class AIServiceFactory {
    public static AIService createService(String provider, String apiKey, String model) {
        switch (provider) {
            case AIProvider.PROVIDER_ANTHROPIC:
                return new AnthropicAIService(apiKey, model);
            case AIProvider.PROVIDER_OPENAI:
                return new OpenAIService(apiKey, model);
            case AIProvider.PROVIDER_DEEPSEEK:
                return new DeepseekService(apiKey, model);
            default:
                throw new IllegalArgumentException("Unknown AI provider: " + provider);
        }
    }
}