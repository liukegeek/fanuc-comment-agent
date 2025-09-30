package com.byd.tools.pojo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * ClassName: CommentURLPara
 * Package: com.byd.tools.pojo
 * Description:
 * Author: LiuKe
 * Create: 2025/4/17 22:33
 * Version 1.0
 */
public class CommentURLPara {

    private String comment;
    private String index;
    private String commentTypeCode;

    public CommentURLPara() {
    }

    public CommentURLPara(String comment, String id, CommentType type) {
        this.comment = comment;
        this.index = id;
        switch (type) {
            case DI:
                commentTypeCode = "8";
                break;
            case DO:
                commentTypeCode = "9";
                break;
            default:
                throw new RuntimeException("未指明输入信号的类型");
        }
    }

    public String getParaURL(String charset) {
        try {
            String sComment = URLEncoder.encode(comment, charset);
            String sIndx = URLEncoder.encode(index, charset);
            String sFc = URLEncoder.encode(commentTypeCode, charset);
            return "?sComment=" + sComment + "&sIndx=" + sIndx + "&sFc=" + sFc;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "\"?sComment=" + comment + "&sIndx=" + index + "&sFc=" + commentTypeCode + "\"";
    }
}
