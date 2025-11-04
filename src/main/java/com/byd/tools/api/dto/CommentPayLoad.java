package com.byd.tools.api.dto;


import com.byd.tools.pojo.Comment;

/**
 * ClassName: CommentPayLoad
 * Package: com.byd.tools.api.dto
 * Description: 表示前端传递的单条注释数据体
 * Author: LiuKe
 * Create: 2025/11/4 09:12
 * Version 1.0
 */

public record CommentPayLoad (String id,String content,String type){
}
