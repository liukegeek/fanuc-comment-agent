package com.byd.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ClassName: FanucWebApplication
 * Package: com.byd.tools.web
 * Description:
 * Author: LiuKe
 * Create: 2025/10/28 14:42
 * Version 1.0
 */
@Configuration(proxyBeanMethods = false)
@SpringBootApplication
public class FanucWebApplication {
    static {
        configureLoggingDirectory();
    }

    private static final Logger LOGGER = LogManager.getLogger(FanucWebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FanucWebApplication.class, args);
        LOGGER.info("Fanuc 注释管理控制台已启动，日志目录: {}", System.getProperty("log.dir"));
    }

    private static void configureLoggingDirectory() {
        Path logDir;
        String configured = System.getProperty("log.dir");
        if (configured != null && !configured.isBlank()) {
            return;
        }
        String userHome = System.getProperty("user.home", ".");
        File desktopDir = new File(userHome, "Desktop");
        //检查这个路径是否存在并且确实是一个目录
        if (!desktopDir.exists() || !desktopDir.isDirectory()) {

            // 标准桌面路径不存在，尝试使用用户主目录作为备用路径。
            desktopDir = new File(userHome);

            // 对于非标准系统（如某些Linux发行版桌面目录名可能不同），
            // 这可能需要更复杂的逻辑，或者干脆回退到用户主目录
        }
        logDir = Paths.get(userHome, ".fanuc-comment-agent", "logs");
        try {
            Files.createDirectories(logDir);
            System.setProperty("log.dir", logDir.toString());
        } catch (IOException ex) {
            System.err.println("无法创建日志目录 " + logDir + ": " + ex.getMessage());
            System.setProperty("log.dir", logDir.toAbsolutePath().toString());
        }
    }
}
