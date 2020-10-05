package com.spring.boot.example.mybatis.configuration;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    @Getter
    private final ApiDocs apiDocs = new ApiDocs();

    @Getter
    private final Jwt jwt = new Jwt();

    @Getter
    private final Image image = new Image();

    @Data
    public static class ApiDocs {

        private String title = "Application API";
        private String description = "API documentation";
        private String version = "0.0.1";
        private String termsOfServiceUrl;
        private String contactName;
        private String contactUrl;
        private String contactEmail;
        private String license;
        private String licenseUrl;
        private String defaultIncludePattern = "/api/.*";
        private String host;
        private String[] protocols = {};
        private Server[] servers = {};
        private boolean useDefaultResponseMessages = true;

        @Data
        public static class Server {

            private String name;
            private String url;
            private String description;

        }
    }

    @Data
    public static class Jwt {

        private String secret;
        private int sessionTime;

    }

    @Data
    public static class Image {

        private String defaultImage;

    }

}
