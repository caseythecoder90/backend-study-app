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
}