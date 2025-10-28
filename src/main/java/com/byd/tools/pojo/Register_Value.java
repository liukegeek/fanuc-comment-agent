package com.byd.tools.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * ClassName: Register_Value
 * Package: com.byd.tools.pojo
 * Description:
 * Author: LiuKe
 * Create: 2025/10/28 08:52
 * Version 1.0
 */
public class Register_Value extends Comment {
    //注解用来指明，通过Gson将对象序列化成json文件时，key的内容。 如果不使用注解则默认为属性名称(field name)。
    @SerializedName("编号")
    private int id;
    @SerializedName("注释")
    private String content;
    @SerializedName("类型")
    private CommentType type;

    public Register_Value() {
    }

    public Register_Value(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public Register_Value(int id, String content, CommentType type) {
        this.id = id;
        this.content = content;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
               ", comment='" + content + '\'' +
               ", type=" + type +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Register_Value that = (Register_Value) o;
        return id == that.getId() && Objects.equals(content, that.getContent());
    }

}
