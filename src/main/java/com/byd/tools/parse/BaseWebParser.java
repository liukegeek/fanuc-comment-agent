package com.byd.tools.parse;

import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * ClassName: BaseWebParser
 * Package: com.byd.tools.parse
 * Description:
 * Author: LiuKe
 * Create: 2025/10/3 00:47
 * Version 1.0
 */
public class BaseWebParser implements KarelWebParser {
    @Override
    public List<Comment> parseDataFromHtml(InputStream sourceInputStream, String charset, String sourceUrl) throws IOException {
        return null;
    }

    @Override
    public String generateReadPara(CommentType type) {
        return switch (type) {
            case DI, DO -> "?sfc=33";   //访问 数字输入、输出 信号的url参数为 '?sfc=33'
        };
    }

    @Override
    public String generateWritePara(CommentType type, int id, String value, String charset) {
        if (type == null) {
            System.out.println("未明确要提交的数据的类型");
        }
        String commentTypeCode = switch (type) {
            case DI -> "8";
            case DO -> "9";
            case null -> "null";
        };

        try {
            String sComment = URLEncoder.encode(value, charset);
            String sIndx = URLEncoder.encode(String.valueOf(id), charset);
            String sFc = URLEncoder.encode(commentTypeCode, charset);
            return "?sComment=" + sComment + "&sIndx=" + sIndx + "&sFc=" + sFc;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
