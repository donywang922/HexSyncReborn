package com.ywsuoyi.hexsyncreborn.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

@SpringBootApplication
public class EmbeddedServer {
    private TrayIcon trayIcon;
    private ConfigurableApplicationContext ctx;
    private Consumer<Boolean> serverStateCallback; // 用于通知客户端的回调
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedServer.class);
    ServerConfig config;

    public void start(Consumer<Boolean> stateCallback) {
        this.serverStateCallback = stateCallback; // 注册回调
        Thread serverThread = new Thread(this::runServer);
        serverThread.start();
        logger.info("Starting embedded server");
        addTrayIcon();
    }

    private void runServer() {
        try {
            config = ServerConfig.loadConfig("./config/server-config.json");
        } catch (IOException e) {
            logger.error("Failed to load server configuration", e);
            notifyServerState(false); // 通知服务器未启动
            return;
        }
        SpringApplication app = new SpringApplication(EmbeddedServer.class);
        app.setDefaultProperties(Map.of("server.port", config.getPort()));
        ctx = app.run();
        logger.info("Server started at http://localhost:{}", config.getPort());
        notifyServerState(true); // 通知服务器已启动
    }

    private void addTrayIcon() {
        if (!SystemTray.isSupported()) {
            logger.error("System tray is not supported!");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image icon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("/server-icon.png"));
            trayIcon = getTrayIcon(icon);
            tray.add(trayIcon);
        } catch (Exception e) {
            logger.error("Failed to add tray icon", e);
        }
    }

    private TrayIcon getTrayIcon(Image icon) {
        TrayIcon trayIcon = new TrayIcon(icon, "Mod Server");
        trayIcon.setImageAutoSize(true);

        PopupMenu popupMenu = new PopupMenu();

        // 状态菜单项
        MenuItem statusItem = new MenuItem("Server Running");
        statusItem.setEnabled(false);
        popupMenu.add(statusItem);

        // 打开浏览器菜单项
        MenuItem openBrowserItem = new MenuItem("Open in Browser");
        openBrowserItem.addActionListener((ActionEvent e) -> openInBrowser());
        popupMenu.add(openBrowserItem);

        // 退出菜单项
        MenuItem exitItem = new MenuItem("Exit Server");
        exitItem.addActionListener((ActionEvent e) -> stop());
        popupMenu.add(exitItem);

        trayIcon.setPopupMenu(popupMenu);
        return trayIcon;
    }

    private void openInBrowser() {
        try {
            URI uri = new URI("http://localhost:" + config.getPort());
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            logger.error("Failed to open browser", e);
        }
    }

    public void stop() {
        if (ctx != null && ctx.isRunning()) {
            ctx.stop();
            logger.info("Server stopped.");
        }
        if (trayIcon != null) {
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }
        notifyServerState(false); // 通知服务器已停止
    }

    private void notifyServerState(boolean running) {
        if (serverStateCallback != null) {
            serverStateCallback.accept(running);
        }
    }

    public boolean isRunning() {
        return ctx != null && ctx.isRunning();
    }
}
