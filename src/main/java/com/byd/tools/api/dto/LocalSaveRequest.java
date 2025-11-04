package com.byd.tools.api.dto;

import com.byd.tools.pojo.Comment;
import jakarta.validation.constraints.NotBlank;

import java.util.*;

/**
 * 保存到本地文件的请求载体，包含目标路径以及按照类型分组的注释集合。
 */
public record LocalSaveRequest(@NotBlank String path, List<CommentPayLoad> commentList) {
    public LocalSaveRequest {
        List<CommentPayLoad> safeList = commentList == null
                ? Collections.emptyList()
                : new ArrayList<>(commentList);
        commentList = Collections.unmodifiableList(safeList);
    }
}
