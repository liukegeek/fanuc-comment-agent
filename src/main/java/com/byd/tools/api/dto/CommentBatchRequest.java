package com.byd.tools.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端批量上传注释时的请求载体。
 */
public record CommentBatchRequest(@NotNull List<CommentPayload> comments) {
    public CommentBatchRequest {
        List<CommentPayload> safeComments =
                comments == null ? List.of() : new ArrayList<>(comments);
        this.comments = List.copyOf(safeComments);
    }
}
