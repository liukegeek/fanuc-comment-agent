package com.byd.tools.parse;

import com.byd.tools.exceptions.ParseFailedException;
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
 * ClassName: DiWebParser
 * Package: com.byd.tools.data
 * Description:
 * 针对用于连接机器人的url连接的参数部分的解析。比如获取「读操作」时候的url参数、获取「写操作」时候的url参数。
 * Author: LiuKe
 * Create: 2025/8/9 18:37
 * Version 1.0
 */
public class DiWebParser extends BaseWebParser {

    public List<Comment> parseDataFromHtml(InputStream sourceInputStream, String charset, String sourceUrl) throws IOException {
//        logger.info("进入ParseHtml对象的getDigitalComments程序");
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
            Element column = trs.removeFirst();
//
//            StringBuilder colName = new StringBuilder();
//            column.select("td").forEach(td -> {
//                colName.append(td.text()).append("\t\t");
//            });
//            logger.info("已执行parseHtml程序对输入流进行解析，当前读取以下列的信息");
//            logger.info(colName);
//            System.out.println("当前读取以下列的信息:");
//            System.out.println(colName);


            //对剩下的每一行都进行拆解，并构造Comment对象用来保存，通过清洗所整理出来的长文本信息。
            //logger.info("对剩下的每一行都进行拆解，并构造Comment对象用来保存，通过清洗所整理出来的长文本信息。");
            trs.forEach(
                    tr -> {

                        //name格式为:strComment1、strComment2、strComment3 等，后面的数字代表该注释的ID，因此截掉前10个字符，得到的字符串就是ID编号
                        String diCommentName = tr.select("td").get(1).select("input").first().attr("name");
                        String diCommentValue = tr.select("td").get(1).select("input").first().attr("value");
                        Comment diComment = new Comment(Integer.parseInt(diCommentName.substring(10)), diCommentValue); //通过name与value信息构造长文本对象。
                        diComment.setType(CommentType.DI); //设置长文本类型为， DI类型
                        commentList.add(diComment);//将该行所提取出来的输入长文本加入到长文本列表:commentList当中
//                        diComment.setComment("");  //用于制造 空的长文本
//                        logger.info("构建一个doComment,id:" + doComment.getId());
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
