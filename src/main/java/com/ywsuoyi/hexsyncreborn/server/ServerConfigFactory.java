package com.ywsuoyi.hexsyncreborn.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ServerConfigFactory {

    @Bean
    public ServerConfig serverConfig() {
        try {
            return ServerConfig.loadConfig("./config/server-config.json");
        } catch (IOException e) {
            System.err.println("Failed to load server configuration: " + e.getMessage());
            return new ServerConfig(); // 返回默认配置
        }
    }
}
