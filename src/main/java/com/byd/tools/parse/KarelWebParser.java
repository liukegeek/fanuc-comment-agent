package com.byd.tools.parse;

import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ClassName: KarelWebParser
 * Package: com.byd.tools.parse
 * Description: 用于和法那科的web 服务器进行连接时各种数据的解析与转变。比如从返回的html页面中获取所需要的数据，
 * 生成进行「读操作」时候的url参数、生成「写操作」时候的url参数。
 * Author: LiuKe
 * Create: 2025/10/2 22:57
 * Version 1.0
 */
public interface KarelWebParser {
    /**
     * 根据某karel服务器的html流进行解析，从而得到需要的数据。
     * @param sourceInputStream 用于解析的html输入流
     * @param charset 输入流的编码格式，如果为null将会根据http协议进行自动侦测。
     * @param sourceUrl 解析流的http连接，防止有超链接需要进行二次跳转。
     * @return 解析出来的数据对象。
     */
    List<Comment> parseDataFromHtml(InputStream sourceInputStream, String charset, String sourceUrl) throws IOException;


    /**
     * 用于在合成 读取连接，时提供url连接的参数部分
     *
     * @param type 将要请求的数据类型，比如DI、GO等
     * @return 将请求类型翻译为对应的参数字符串。
     */
    String generateReadPara(CommentType type);



    /**
     * 用于在更改 上传 某数据时，提供url连接的参数部分
     * @param type 要上传的数据类型，如 数字输入DI、组输出信号GO、寄存器R[]等
     * @param id 数据的编号
     * @param value 上传的数据值
     * @param charset 编码的字符集，用于将显示文本编码成url。
     * @return 所生成的url参数。
     */
    String generateWritePara(CommentType type, int id, String value, String charset);
}
