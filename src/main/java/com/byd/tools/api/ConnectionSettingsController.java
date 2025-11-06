package com.byd.tools.api;

import com.byd.tools.api.dto.ConnectionSettingsResponse;
import com.byd.tools.api.dto.UpdateConnectionRequest;
import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.service.ConnectionSettingsService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 提供前端用于查看与修改机器人连接信息的接口。
 */
@RestController
@RequestMapping("/api/settings/connection")
public class ConnectionSettingsController {

    private static final Logger LOGGER = LogManager.getLogger(ConnectionSettingsController.class);
    private final ConnectionSettingsService service;

    public ConnectionSettingsController(ConnectionSettingsService service) {
        this.service = service;
    }

    @GetMapping
    public ConnectionSettingsResponse current() {
        var view = service.currentSettings();
        LOGGER.debug("获取当前机器人连接配置: host={}, baseUrl={}", view.host(), view.baseUrl());
        return new ConnectionSettingsResponse(view.host(), view.baseUrl());
    }

    @PostMapping
    public ConnectionSettingsResponse update(@Valid @RequestBody UpdateConnectionRequest request) {
        try {
            var view = service.updateHost(request.host());
            LOGGER.info("前端请求更新机器人 IP: {}", view.host());
            return new ConnectionSettingsResponse(view.host(), view.baseUrl());
        } catch (InvalidParaException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }
}
