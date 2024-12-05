package com.ywsuoyi.hexsyncreborn.client;

import com.ywsuoyi.hexsyncreborn.server.EmbeddedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ClientUI {
    private JTextArea logArea;
    private ClientConfig config;
    private EmbeddedServer server;
    private static final Logger logger = LoggerFactory.getLogger(ClientUI.class);

    public void start() {
        try {
            config = ClientConfig.loadConfig("./config/client-config.json");
        } catch (IOException e) {
            logger.error("Failed to load client configuration: {}", e.getMessage());
            return;
        }
        logger.info("Client started");
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mod Manager");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);

            // 日志窗口
            logArea = new JTextArea();
            logArea.setEditable(false);
            JScrollPane logScrollPane = new JScrollPane(logArea);

            // 按钮和状态标签
            JButton startServerButton = new JButton("Start Server");
            JButton syncModsButton = new JButton("Sync Mods");
            JButton configButton = new JButton("Edit Config");
            JLabel statusLabel = new JLabel("Server: Not started", SwingConstants.CENTER);

            // 布局管理
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(syncModsButton);
            buttonPanel.add(configButton);
            buttonPanel.add(startServerButton);

            // 日志区域布局
            JPanel logPanel = new JPanel(new BorderLayout());
            logPanel.add(new JLabel("Logs:"), BorderLayout.NORTH);
            logPanel.add(logScrollPane, BorderLayout.CENTER);

            // 主窗口布局
            frame.setLayout(new BorderLayout());
            frame.add(buttonPanel, BorderLayout.NORTH);
            frame.add(logPanel, BorderLayout.CENTER);
            frame.add(statusLabel, BorderLayout.SOUTH);

            // 按钮逻辑
            startServerButton.addActionListener((ActionEvent e) -> serverEvent(statusLabel, startServerButton));

            syncModsButton.addActionListener((ActionEvent e) -> syncMods());

            configButton.addActionListener((ActionEvent e) -> editConfig(frame));

            frame.setVisible(true);
        });
    }

    private void editConfig(JFrame frame) {
        JTextField urlField = new JTextField(config.getUrl());
        JTextField portField = new JTextField(String.valueOf(config.getPort()));
        JTextField modsPathField = new JTextField(config.getModsPath());

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("URL:"));
        panel.add(urlField);
        panel.add(new JLabel("Port:"));
        panel.add(portField);
        panel.add(new JLabel("Mods Path:"));
        panel.add(modsPathField);

        int result = JOptionPane.showConfirmDialog(frame, panel, "Edit Config", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            config.setUrl(urlField.getText());
            config.setPort(Integer.parseInt(portField.getText()));
            config.setModsPath(modsPathField.getText());
            try {
                config.saveConfig("./config/client-config.json");
                logMessage("Configuration saved successfully!");
            } catch (IOException ex) {
                logMessage("Error saving config: " + ex.getMessage());
                JOptionPane.showMessageDialog(frame, "Error saving config: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void syncMods() {
        try {
            ModSyncService syncService = new ModSyncService(config);
            syncService.syncMods(this);
            logMessage("Mods synchronized successfully!");
        } catch (Exception ex) {
            logMessage("Error syncing mods: " + ex.getMessage());
            logger.error("Error syncing mods:", ex);
        }
    }

    private void serverEvent(JLabel statusLabel, JButton startServerButton) {
        if (server == null || !server.isRunning()) {
            logMessage("Starting Server");
            server = new EmbeddedServer();
            server.start(running -> SwingUtilities.invokeLater(() -> {
                if (running) {
                    logMessage("Server started");
                    startServerButton.setText("Stop Server");
                    statusLabel.setText("Server: Running");
                } else {
                    logMessage("Server stopped");
                    startServerButton.setText("Start Server");
                    statusLabel.setText("Server: Not started");
                }
            }));
        } else {
            server.stop();
        }
    }

    public void logMessage(String message) {
        logArea.append(message + "\n");
    }
}
