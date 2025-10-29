package com.byd.tools.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 表示前端传递的单条注释数据载体。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentPayload(Integer id, String content, String type) {
}
