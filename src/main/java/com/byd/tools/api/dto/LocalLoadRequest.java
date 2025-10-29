package com.byd.tools.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 本地加载请求，包含待读取的文件路径以及可选的注释类型。
 */
public record LocalLoadRequest(@NotBlank String path, String type) {
}
