package com.byd.tools.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Component
public class BrowserAutoLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8910}")
    private String port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 当端口配置为0时，表示使用随机端口，需要从环境中获取实际使用的端口
        if ("0".equals(port)) {
            // 获取实际使用的端口
            port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
        }

        String url = "http://localhost:" + port;
        System.out.println("应用已启动，访问地址: " + url);

        // 打开浏览器
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // 使用Java Desktop API打开浏览器
                Desktop.getDesktop().browse(new URI(url));
            } else {
                // 针对不支持Desktop API的环境，尝试使用命令行方式
                openBrowserWithCommand(url);
            }
        } catch (Exception e) {
            System.err.println("无法自动打开浏览器: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openBrowserWithCommand(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime runtime = Runtime.getRuntime();
        try {
            if (os.contains("win")) {
                // Windows
                runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                // macOS
                runtime.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux/Unix
                runtime.exec(new String[]{"xdg-open", url});
            }
        } catch (IOException e) {
            System.err.println("使用命令行打开浏览器失败: " + e.getMessage());
        }
    }
}