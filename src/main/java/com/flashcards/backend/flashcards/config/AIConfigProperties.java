package com.flashcards.backend.flashcards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfigProperties {

    private Limits limits = new Limits();
    private Cache cache = new Cache();
    private Fallback fallback = new Fallback();

    @Data
    public static class Limits {
        private int maxFlashcardsPerRequest = 20;
        private int maxTextLength = 10000;
        private int rateLimitPerMinute = 10;
    }

    @Data
    public static class Cache {
        private boolean enabled = true;
        private long ttl = 3600;
    }

    @Data
    public static class Fallback {
        private boolean enabled = true;
        private int maxRetries = 2;
        private String[] fallbackModels = {"GPT_4O_MINI", "CLAUDE_3_5_HAIKU", "GEMINI_1_5_FLASH"};
    }
}