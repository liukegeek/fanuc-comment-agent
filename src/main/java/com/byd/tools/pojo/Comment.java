package com.byd.tools.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * ClassName: FanucComment
 * Package: com.byd.tools.pojo
 * Description:
 * 用来包装一行长文本信息的类,包括:编号(id)、内容(comment)、类型(type)
 * 例如： 对于 数字量输出信号289，其长文本内容为：水复位，那么用该类包装即为 Comment(289,"水复位",DI)
 * Author: LiuKe
 * Create: 2025/4/16 21:46
 * Version 1.0
 */
public class Comment {
    //注解用来指明，通过Gson将对象序列化成json文件时，key的内容。 如果不使用注解则默认为属性名称(field name)。
    @SerializedName("编号")
    private int id;
    @SerializedName("内容")
    private String comment;
    @SerializedName("类型")
    private CommentType type;

    public Comment() {
    }

    public Comment(int id, String comment) {
        this.id = id;
        this.comment = comment;
    }

    public Comment(int id, String comment, CommentType type) {
        this.id = id;
        this.comment = comment;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CommentType getType() {
        return type;
    }

    public void setType(CommentType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FanucComment{" +
               "id=" + id +
               ", comment='" + comment + '\'' +
               ", type=" + type +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment that = (Comment) o;
        return id == that.id && Objects.equals(comment, that.comment);
    }
}
