package com.byd.tools.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存到本地文件的请求载体，包含目标路径以及按照类型分组的注释集合。
 */
public record LocalSaveRequest(@NotBlank String path,
                               Map<String, List<CommentPayload>> commentsByType) {
    public LocalSaveRequest {
        Map<String, List<CommentPayload>> safeMap = commentsByType == null
                ? Collections.emptyMap()
                : new LinkedHashMap<>(commentsByType);
        this.commentsByType = Collections.unmodifiableMap(safeMap);
    }
}
