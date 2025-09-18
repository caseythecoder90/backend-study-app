package com.flashcards.backend.flashcards.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "openapi")
public class OpenApiProperties {

    private Info info = new Info();
    private List<Server> servers = List.of();

    @Data
    public static class Info {
        private String title;
        private String description;
        private String version;
        private Contact contact = new Contact();
        private License license = new License();
    }

    @Data
    public static class Contact {
        private String name;
        private String email;
        private String url;
    }

    @Data
    public static class License {
        private String name;
        private String url;
    }

    @Data
    public static class Server {
        private String url;
        private String description;
    }
}