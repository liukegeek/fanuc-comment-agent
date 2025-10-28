package com.byd.tools.parse.impl;

import com.byd.tools.exceptions.ParseFailedException;
import com.byd.tools.parse.BaseWebParser;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: DoWebParser
 * Package: com.byd.tools.parse
 * Description:
 * 针对用于连接机器人的url连接的参数部分的解析。比如获取「读操作」时候的url参数、获取「写操作」时候的url参数。适用于处理数字输出信号相关操作
 * Author: LiuKe
 * Create: 2025/10/10 01:26
 * Version 1.0
 */
public class DoWebParser extends BaseWebParser {
    @Override
    public List<Comment> parseDataFromHtml(InputStream sourceInputStream, String charset, String sourceUrl) throws IOException {
        List<Comment> commentList = new ArrayList<>();

        try {
            Document document = Jsoup.parse(sourceInputStream, charset, sourceUrl);
            Element root = document.root();
            Elements html = root.select("> html"); // 使用“>" 可以制定获取该元素的一级子标签，而不会递归到更深层的子标签
            Element body = html.select("> body").first();
            Element font = body.select("> font").first();
            Element form = font.select("> form").first();
            Element table = form.select("> table").first();
            Element tbody = table.select("> tbody").first();
            //筛选出存有长文本信息的表单所包含的所有行:trs
            Elements trs = tbody.select("tr");

            //表单第一行，为表格中每一列的名字，故而进行去除
            trs.removeFirst();

            trs.forEach(
                    tr -> {
                        //该类型的td共有4个，分别存储：DI编号、DI注释、DO编号、DO注释。 其中DI注释一栏既能体现编号，如name=strComment7，也存有注释，如value=报警复位，我们直接解析该栏的name和value即可。
                        //name格式为:strComment1、strComment2、strComment3 等，后面的数字代表该注释的ID，因此截掉前10个字符，得到的字符串就是ID编号
                        String doCommentName = tr.select("td").get(3).select("input").first().attr("name");
                        String doCommentValue = tr.select("td").get(3).select("input").first().attr("value");
                        Comment doComment = new Comment(Integer.parseInt(doCommentName.substring(10)), doCommentValue);
                        doComment.setType(CommentType.DO); //设置长文本类型为，DO类型
                        commentList.add(doComment);
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
