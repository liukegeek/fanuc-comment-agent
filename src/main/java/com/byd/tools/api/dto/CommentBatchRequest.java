package com.byd.tools.api.dto;

import com.byd.tools.pojo.Comment;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 前端批量上传注释时的请求载体。
 */
public record CommentBatchRequest(@NotNull List<Comment> commentList) {
    public CommentBatchRequest {
        List<Comment> safeComments =
                commentList == null ? List.of() : new ArrayList<>(commentList);
        commentList = List.copyOf(safeComments);
    }
}
