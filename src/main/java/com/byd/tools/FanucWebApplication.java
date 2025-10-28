package com.byd.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

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
    public static void main(String[] args) {
        SpringApplication.run(FanucWebApplication.class, args);
    }
}
