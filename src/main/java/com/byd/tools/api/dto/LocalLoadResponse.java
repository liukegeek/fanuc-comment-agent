package com.byd.tools.api.dto;

import com.byd.tools.pojo.Comment;

import java.util.List;

/**
 * 本地加载结果，包含类型标识和解析出的注释列表。
 */
public record LocalLoadResponse(String type, List<Comment> comments) {
}
