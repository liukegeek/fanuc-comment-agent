package com.byd.tools.api.dto;

/**
 * 返回给前端的机器人连接配置。
 */
public record ConnectionSettingsResponse(String host, String baseUrl) {
}
