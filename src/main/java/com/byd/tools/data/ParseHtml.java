package com.byd.tools.data;

import com.byd.tools.exceptions.ParseFailedException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * ClassName: parseComBlankHtml
 * Package: com.byd.parseData
 * Description:
 * Author: LiuKe
 * Create: 2025/4/7 21:54
 * Version 1.0
 */
public class ParseHtml {

    InputStream htmlInputStream;  //要解析的 输入流对象
    String charset;   // 解析时使用的解码方式
    String htmlBaseUri; //目标资源的路径。
    private static final Logger logger = LogManager.getLogger(ParseHtml.class);

    public ParseHtml(InputStream htmlInputStream, String htmlBaseUri) {
        this(htmlInputStream, htmlBaseUri, "GBK");
    }

    public ParseHtml(InputStream htmlInputStream, String htmlBaseUri, String charset) {
        this.htmlInputStream = htmlInputStream;
        this.charset = charset;
        this.htmlBaseUri = htmlBaseUri;
    }

    /**
     * 从html文件中 解析出所有的 数字量长文本
     *
     * @return 存储着 数字量长文本对象 的列表
     */
    public List<Comment> getDigitalComments() {
        logger.info("进入ParseHtml对象的getDigitalComments程序");
        List<Comment> commentList = new ArrayList<>();

        try {
            Document document = Jsoup.parse(htmlInputStream, charset, htmlBaseUri);
            Element root = document.root();
            Elements html = root.select("> html");
            Element body = html.select("> body").first();
            Element font = body.select("> font").first();
            Element form = font.select("> form").first();
            Element table = form.select("> table").first();
            Element tbody = table.select("> tbody").first();
            Elements trs = tbody.select("tr");
            //筛选出存有长文本信息的表单所包含的所有行:trs

            //表单第一行，为列的名字，故而进行去除
            Element column = trs.removeFirst();
            StringBuilder colName = new StringBuilder();
            column.select("td").forEach(td -> {
                colName.append(td.text()).append("\t\t");
            });

            logger.info("已执行parseHtml程序对输入流进行解析，当前读取以下列的信息");
            logger.info(colName);
            //            System.out.println("当前读取以下列的信息:");
//            System.out.println(colName);


            //对剩下的每一行都进行拆解，并构造Comment对象用来保存，通过清洗所整理出来的长文本信息。
            logger.info("对剩下的每一行都进行拆解，并构造Comment对象用来保存，通过清洗所整理出来的长文本信息。");
            trs.forEach(
                    tr -> {

                        //name格式为:strComment1、strComment2、strComment3 等，后面的数字代表该注释的ID，因此截掉前10个字符，得到的字符串就是ID编号
                        String diCommentName = tr.select("td").get(1).select("input").first().attr("name");
                        String diCommentValue = tr.select("td").get(1).select("input").first().attr("value");
                        Comment diComment = new Comment(Integer.parseInt(diCommentName.substring(10)), diCommentValue); //通过name与value信息构造长文本对象。
                        diComment.setType(CommentType.DI); //设置长文本类型为， DI类型
//                        diComment.setComment("");  //用于制造 空的长文本
                        commentList.add(diComment);//将该行所提取出来的输入长文本加入到长文本列表:commentList当中
                        logger.info("构建一个diComment,id:" + diComment.getId());


                        String doCommentName = tr.select("td").get(3).select("input").first().attr("name");
                        String doCommentValue = tr.select("td").get(3).select("input").first().attr("value");
                        Comment doComment = new Comment(Integer.parseInt(doCommentName.substring(10)), doCommentValue);


                        doComment.setType(CommentType.DO); //设置长文本类型为，DO类型
//                        doComment.setComment("");  //用于制造空的长文本
                        commentList.add(doComment);
                        logger.info("构建一个doComment,id:" + doComment.getId());
                    }
            );
        } catch (IOException | NullPointerException e) {
            logger.error("执行ParseHtml遇到错误：" + e.getMessage());
            throw new ParseFailedException("未能成功解析所提供html文件:" + htmlBaseUri + "具体原因为：" + e);
        }
        return commentList;
    }
}
