package com.byd.tools.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 前端用于修改机器人连接目标 IP 的请求体。
 */
public record UpdateConnectionRequest(@NotBlank String host) {
}
