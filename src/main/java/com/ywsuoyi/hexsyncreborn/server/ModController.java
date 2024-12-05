package com.ywsuoyi.hexsyncreborn.server;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
public class ModController {
    private final ModService modService;

    public ModController(ModService modService) {
        this.modService = modService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> listMods() {
        try {
            return ResponseEntity.ok(modService.listMods());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadMod(@PathVariable String fileName) {
        try {
            Resource resource = modService.getFile(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<String> showModPage() {
        StringBuilder html = new StringBuilder("<html><body><h1>Mod Files</h1><ul>");
        try {
            List<Map<String, String>> mods = modService.listMods();
            for (Map<String, String> mod : mods) {
                html.append("<li>")
                        .append(mod.get("name"))
                        .append(" - <a href='/download/")
                        .append(mod.get("name"))
                        .append("'>Download</a>")
                        .append("</li>");
            }
            html.append("</ul></body></html>");
        } catch (Exception e) {
            html.append("<p>Error loading files</p>");
        }
        return ResponseEntity.ok(html.toString());
    }
}
