package com.byd.tools.api;

import com.byd.tools.connect.IConnection;
import com.byd.tools.connect.KarelConnection;
import com.byd.tools.service.CommentRepository;
import com.byd.tools.service.CommentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: FanucConfig
 * Package: com.byd.tools.api
 * Description: 配置类，用于配置与Fanuc机器人的连接以及相关服务，为SpringBoot应用提供必要的Bean。
 * Author: LiuKe
 * Create: 2025/10/29 16:58
 * Version 1.0
 */

@Configuration
public class FanucConfig {

    @Bean
    public IConnection fanucConnection() throws Exception {
        return new KarelConnection.Builder()
                .host("127.0.0.1")       // 改为机器人IP
                .port(80)
                .readPath("/karel/ComGet")
                .writePath("/karel/ComSet")
                .build();
    }

    @Bean
    public CommentService commentService(IConnection conn) {
        return new CommentService(conn);
    }

    @Bean
    public CommentRepository commentRepository() {
        return new CommentRepository();
    }
}