package com.ywsuoyi.hexsyncreborn.server;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ServerConfig {
    private int port = 8080;           // 默认端口
    private String modsPath = "./mods"; // 默认文件夹路径

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getModsPath() {
        return modsPath;
    }

    public void setModsPath(String modsPath) {
        this.modsPath = modsPath;
    }

    public static ServerConfig loadConfig(String filePath) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (!parentDir.exists()) {
            // 如果父目录不存在，则创建
            parentDir.mkdirs();
        }

        if (!file.exists()) {
            // 文件不存在，生成默认配置
            ServerConfig defaultConfig = new ServerConfig();
            defaultConfig.saveConfig(filePath);
            return defaultConfig;
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, ServerConfig.class);
    }

    public void saveConfig(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), this);
    }
}

