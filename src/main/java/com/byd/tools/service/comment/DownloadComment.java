package com.byd.tools.service.comment;

import com.byd.tools.connect.ConnectServer;
import com.byd.tools.data.JsonFileOperator;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import com.byd.tools.data.ParseHtml;
import com.byd.tools.pojo.ServiceResponseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.List;

/**
 * ClassName: DownloadCommentService
 * Package: com.byd.tools.service.comment
 * Description:用来实现 UI界面中 "下载长文本" 功能的类：将从网站上下载长文本，并保存在指定路径中
 * Author: LiuKe
 * Create: 2025/4/20 15:55
 * Version 1.0
 */


public class DownloadComment {
    private ConnectServer connectServer;
    private String path;
    private static final Logger logger = LogManager.getLogger(DownloadComment.class);


    public DownloadComment(ConnectServer connectServer, String path) {
        this.connectServer = connectServer;
        this.path = path;
    }

    public ServiceResponseInfo service() {
        try {
            //通过服务器连接获取 输入流资源
            InputStream inputStream = connectServer.getResource("/ComGet", "?sFc=33");
            logger.info("成功通过服务器连接获取到输入流资源");

            //解析资源，并据此构建 长文本的对象
            ParseHtml parseHtml = new ParseHtml(inputStream, connectServer.getBaseUrl());
            logger.info("成功创建ParseHtml对象，即将调用getDigitalComments进行资源解析");
            List<Comment> comments = parseHtml.getDigitalComments();
            logger.info("成功解析资源，并构建长文本对象");


            JsonFileOperator jsonFileOperator = new JsonFileOperator();
            //保存所有信号的长文本为Json文件。
            jsonFileOperator.saveToJson(comments, path);
            logger.info("成功将所有信号的长文本保存为Json文件。路径名为:" + path);


            //额外生成一份只有Input信号长文本的 json文件。 路径名为给定的 路径名后拼接"_input"
            List<Comment> inputComments = comments.stream().filter(
                    digitalComment -> digitalComment.getType() == CommentType.DI
            ).toList();
            String inputCommentSavePath = path.substring(0, path.lastIndexOf(".")) + "_input.json";
            jsonFileOperator.saveToJson(inputComments, inputCommentSavePath);
            logger.info("成功将输入信号的长文本另保存为Json文件。路径名为:" + inputCommentSavePath);

            //额外生成一份自由Output信号长文本的 json文件。 路径名为给定的 路径名后拼接"_output"
            List<Comment> outputComments = comments.stream().filter(
                    digitalComment -> digitalComment.getType() == CommentType.DO
            ).toList();
            String outputCommentSavePath = path.substring(0, path.lastIndexOf(".")) + "_output.json";
            jsonFileOperator.saveToJson(outputComments, outputCommentSavePath);
            logger.info("成功将输出信号的长文本另保存为Json文件。路径名为:" + outputCommentSavePath);
            return new ServiceResponseInfo("DownloadComment", "成功将长文本保存为JSON文件，路径为：" + path);
        } catch (Exception e) {
            logger.error("由于发生了异常而导致程序终止：" + e.getMessage());
            e.printStackTrace();
            return new ServiceResponseInfo("DownloadComment", "由于发生了异常而导致程序终止:" + e.getMessage(), e);
        }
    }
}