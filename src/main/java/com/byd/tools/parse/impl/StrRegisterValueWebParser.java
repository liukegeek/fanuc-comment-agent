package com.byd.tools.parse.impl;

import com.byd.tools.exceptions.ParseFailedException;
import com.byd.tools.parse.BaseWebParser;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import com.byd.tools.pojo.Register_Value;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: StrRegisterValueWebParser
 * Package: com.byd.tools.parse
 * Description:
 * Author: LiuKe
 * Create: 2025/10/28 09:32
 * Version 1.0
 */
public class StrRegisterValueWebParser extends BaseWebParser {

    @Override
    public String generateWritePara(CommentType type, int id, String value, String charset) {
        if (type != CommentType.STRING_REGISTER_VALUE) {
            throw new RuntimeException("type类型匹配错误");
        }

        try {
            String sValue = URLEncoder.encode(value, charset);
            String sIndx = URLEncoder.encode(String.valueOf(id), charset);
            String sFc = URLEncoder.encode("15", charset);
            /*
             寄存器值修改的参数格式与注释不同，比如 字符串寄存器第10个点存储的值更改为BB 的url链接为:
                http://192.168.0.1/karel/ComSet?sValue=BB&sIndx=10&sFc=15
             */
            return "?sValue=" + sValue + "&sIndx=" + sIndx + "&sFc=" + sFc;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Comment> parseDataFromHtml(InputStream sourceInputStream, String charset, String sourceUrl) throws IOException {
        List<Comment> commentList = new ArrayList<>();

        try {
            Document document = Jsoup.parse(sourceInputStream, charset, sourceUrl);
            Element root = document.root();
            Elements html = root.select("> html");   // 使用“>" 可以制定获取该元素的一级子标签，而不会递归到更深层的子标签
            Element body = html.select("> body").first();
            Element font = body.select("> font").first();
            Element form = font.select("> form").first();
            Element table = form.select("> table").first();
            Element tbody = table.select("> tbody").first();
            //筛选出存有长文本信息的表单所包含的所有行:trs
            Elements trs = tbody.select("tr");

            //表单第一行，为列的名字，故而进行去除
            trs.removeFirst();

            trs.forEach(
                    tr -> {
                        String strRegisterValueName = tr.select("td").get(2).select("input").first().attr("name");
                        String strRegisterValueContent = tr.select("td").get(2).select("input").first().attr("value");
                        // 和注释不同，对于寄存器的值，其html文件中的name格式为 name="iVal1"，因此剔除其前4个字符"iVal"才会得到寄存器的编号
                        Register_Value strRegisterValueComment = new Register_Value(Integer.parseInt(strRegisterValueName.substring(4)), strRegisterValueContent); //通过name与value信息构造字符串寄存器值对象。
                        strRegisterValueComment.setType(CommentType.STRING_REGISTER_VALUE); //设置类型为， 字符串寄存器值内容类型
                        commentList.add(strRegisterValueComment);//将该行所提取出来的字符串寄存器值对象加入到值列表:commentList当中
                    }
            );
        } catch (IOException | NullPointerException e) {
            throw new ParseFailedException("未能成功解析所提供的html文件:" + sourceUrl + "具体原因为：" + e);
        } finally {
            sourceInputStream.close();
        }

        return commentList;

    }
}
