package com.ywsuoyi.hexsyncreborn.server;

import com.ywsuoyi.hexsyncreborn.FileUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ModService {
    private final ServerConfig config;

    @Autowired
    public ModService(ServerConfig config) {
        this.config = config;
    }

    public List<Map<String, String>> listMods() throws IOException, NoSuchAlgorithmException {
        File folder = new File(config.getModsPath());
        File[] files = folder.listFiles();
        if (files == null) return Collections.emptyList();

        List<Map<String, String>> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                String hash = FileUtil.calculateSHA256(file);
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("name", file.getName());
                fileInfo.put("sha256", hash);
                result.add(fileInfo);
            }
        }
        return result;
    }

    public Resource getFile(String fileName) throws FileNotFoundException {
        File file = new File(config.getModsPath(), fileName);
        if (!file.exists()) throw new FileNotFoundException("File not found: " + fileName);
        return new FileSystemResource(file);
    }
}

