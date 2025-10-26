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
            case NUM_REGISTER -> "?sfc=28";
            case POSITION_REGISTER -> "?sfc=29";
            case STRING_REGISTER -> "?sfc=30";
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
            System.out.println("未明确要提交的数据的类型");
        }
        String commentTypeCode = switch (type) {
            case NUM_REGISTER -> "1";
            //缺少 数值寄存器的值 2
            case POSITION_REGISTER -> "3";
            case STRING_REGISTER -> "14";
            //缺少 字符串寄存器的值 15
            case RI -> "6";
            case RO -> "7";
            case DI -> "8";
            case DO -> "9";
            case GI -> "10";
            case GO -> "11";
            case AI -> "12";
            case AO -> "13";
            case FLAG -> "77";
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
