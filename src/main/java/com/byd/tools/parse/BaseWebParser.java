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
            case NUM_REGISTER_COMMENT, NUM_REGISTER_VALUE -> "?sfc=28";
            case POSITION_REGISTER -> "?sfc=29";
            case STRING_REGISTER_COMMENT, STRING_REGISTER_VALUE -> "?sfc=30";
            case RI, RO -> "?sfc=32";
            case DI, DO -> "?sfc=33";   //访问 数字输入、输出 信号的url参数为 '?sfc=33'
            case GI, GO -> "?sfc=34";
            case AI, AO -> "?sfc=35";
            case FLAG -> "?sfc=76";
        };
    }

    @Override
    public String generateWritePara(CommentType type, int id, String value, String charset) {
        if (type == null) {
            System.out.println("未明确要提交的数据的类型");   //不存在的类型，返回null。
            throw new RuntimeException("type为空");
        }
        String commentTypeCode = switch (type) {
            case NUM_REGISTER_COMMENT -> "1";
            case NUM_REGISTER_VALUE -> null;    //用于寄存器值修改的参数格式与注释修改不同，故而需要子类覆写这个方法。
            case POSITION_REGISTER -> "3";
            case STRING_REGISTER_COMMENT -> "14";
            case STRING_REGISTER_VALUE -> null; //用于寄存器值修改的参数格式与注释修改不同，故而需要子类覆写这个方法。
            case RI -> "6";
            case RO -> "7";
            case DI -> "8";
            case DO -> "9";
            case GI -> "10";
            case GO -> "11";
            case AI -> "12";
            case AO -> "13";
            case FLAG -> "19";
        };

        if (commentTypeCode == null) {
            System.out.println("未明确要提交的数据的类型的代码");   //不存在的类型，返回null。
            throw new RuntimeException("type为不存在的CommentType类型");
        }

        try {
            String sComment = URLEncoder.encode(value, charset);
            String sIndx = URLEncoder.encode(String.valueOf(id), charset);
            String sFc = URLEncoder.encode(commentTypeCode, charset);
            String writePara = "?sComment=" + sComment + "&sIndx=" + sIndx + "&sFc=" + sFc;
            //早期浏览器，提交表单时会把空格编码成 + ，JAVA中的URLEncoder因此会默认把空格编码成"+"。
            //而现代URL标准规定，空格应该编码成"%20"，因此这里手动将"+"替换为"%20"。
            writePara = writePara.replace("+", "%20");
            return writePara;

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
