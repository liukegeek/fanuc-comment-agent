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
 * ClassName: FlagWebParser
 * Package: com.byd.tools.parse.impl
 * Description:
 * Author: LiuKe
 * Create: 2025/10/28 10:40
 * Version 1.0
 */
public class FlagWebParser extends BaseWebParser {
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
                        String flagCommentName = tr.select("td").get(1).select("input").first().attr("name");
                        String flagCommentValue = tr.select("td").get(1).select("input").first().attr("value");
                        Comment flagComment = new Comment(Integer.parseInt(flagCommentName.substring(10)), flagCommentValue); //通过name与value信息构造flag标签注释对象。
                        flagComment.setType(CommentType.FLAG); //设置类型为， FLAG类型
                        commentList.add(flagComment);//将该行所提取出来的flag标签注释对象加入到注释列表:commentList当中
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
