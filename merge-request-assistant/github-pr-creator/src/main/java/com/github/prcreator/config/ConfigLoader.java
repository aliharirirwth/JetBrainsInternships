package com.github.prcreator.config;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigLoader {
    public static GitHubProperties loadConfig() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("config.properties not found in classpath");
            }
            properties.load(input);

            String token = getRequiredProperty(properties, "github.token");
            String username = getRequiredProperty(properties, "github.username");

            return GitHubProperties.builder()
                    .token(token)
                    .username(username)
                    .build();
        }
    }

    private static String getRequiredProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(key + " not found in config.properties");
        }
        return value.trim();
    }
}