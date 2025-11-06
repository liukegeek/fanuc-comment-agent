package com.byd.tools.service;

import com.byd.tools.connect.IConnection;
import com.byd.tools.connect.KarelConnection;
import com.byd.tools.exceptions.InvalidParaException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * 提供运行时修改机器人连接目标的能力。
 */
@Service
public class ConnectionSettingsService {
    private static final Logger LOGGER = LogManager.getLogger(ConnectionSettingsService.class);
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}$"
    );

    private final IConnection connection;
    private final String protocol;
    private final int port;

    public ConnectionSettingsService(IConnection connection,
                                     @Value("${fanuc.connection.protocol:http}") String protocol,
                                     @Value("${fanuc.connection.port:80}") int port) {
        this.connection = connection;
        this.protocol = protocol;
        this.port = port;
    }

    public ConnectionSettingsView currentSettings() {
        return new ConnectionSettingsView(resolveHost(), resolveBaseUrl());
    }

    public ConnectionSettingsView updateHost(String host) throws InvalidParaException {
        String targetHost = normalizeHost(host);
        try {
            connection.modifyBaseURL(protocol, targetHost, port);
            resetCacheIfNecessary();
            LOGGER.info("已将机器人连接地址更新为 {}://{}:{}", protocol, targetHost, port);
            return new ConnectionSettingsView(resolveHost(), resolveBaseUrl());
        } catch (URISyntaxException ex) {
            LOGGER.error("机器人 IP 地址无效: {}", targetHost, ex);
            throw new InvalidParaException("机器人 IP 地址不合法: " + targetHost, ex);
        }
    }

    private String normalizeHost(String host) throws InvalidParaException {
        if (!StringUtils.hasText(host)) {
            throw new InvalidParaException("机器人 IP 地址不能为空");
        }
        String trimmed = host.trim();
        if (!IPV4_PATTERN.matcher(trimmed).matches()) {
            throw new InvalidParaException("机器人 IP 地址格式不正确: " + trimmed);
        }
        return trimmed;
    }

    private void resetCacheIfNecessary() {
        if (connection instanceof KarelConnection karelConnection) {
            karelConnection.resetCache();
        }
    }

    private String resolveHost() {
        if (connection instanceof KarelConnection karelConnection) {
            return karelConnection.getHost();
        }
        return null;
    }

    private String resolveBaseUrl() {
        if (connection instanceof KarelConnection karelConnection) {
            return karelConnection.getBaseUrl();
        }
        return null;
    }

    public record ConnectionSettingsView(String host, String baseUrl) {
        public ConnectionSettingsView {
            if (host != null) {
                host = host.trim();
            }
            if (baseUrl != null) {
                baseUrl = baseUrl.trim();
            }
        }
    }
}
