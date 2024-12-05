package com.ywsuoyi.hexsyncreborn.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ywsuoyi.hexsyncreborn.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModSyncService {
    private static final Logger log = LoggerFactory.getLogger(ModSyncService.class);
    private final String serverUrl;
    private final String modsPath;

    public ModSyncService(ClientConfig config) {
        this.serverUrl = config.getUrl() + ":" + config.getPort();
        this.modsPath = config.getModsPath();
    }

    public void syncMods(ClientUI clientUI) throws IOException, NoSuchAlgorithmException {
        File folder = new File(modsPath);
        if (!folder.exists()) folder.mkdirs();
        log.info("Fetch file list");
        clientUI.logMessage("Fetch file list");
        List<Map<String, String>> serverFiles = fetchServerFiles(serverUrl + "/list");
        for (Map<String, String> serverFile : serverFiles) {
            String fileName = serverFile.get("name");
            String serverHash = serverFile.get("sha256");

            File localFile = new File(modsPath, fileName);
            if (!localFile.exists() || !serverHash.equals(FileUtil.calculateSHA256(localFile))) {
                clientUI.logMessage("Updating file: " + fileName);
                log.info("Updating file: {}", fileName);
                FileUtil.downloadFile(serverUrl + "/download/" + fileName, localFile.getPath());
            }
        }
    }

    private List<Map<String, String>> fetchServerFiles(String url) throws IOException {
        try (InputStream in = URI.create(url).toURL().openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String response = reader.lines().collect(Collectors.joining());
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response, new TypeReference<>() {
            });
        }
    }
}
