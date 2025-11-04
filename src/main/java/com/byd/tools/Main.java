package com.byd.tools;

import com.byd.tools.connect.IConnection;
import com.byd.tools.connect.KarelConnection;
import com.byd.tools.exceptions.ConnectFailedException;
import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.exceptions.JsonFileIOException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import com.byd.tools.service.CommentRepository;
import com.byd.tools.service.CommentService;

import java.net.URISyntaxException;
import java.util.List;

/**
 * ClassName: Main
 * Package: com.byd.tools
 * Description:
 * Author: LiuKe
 * Create: 2025/4/20 23:22
 * Version 1.0
 */
public class Main {
    public static void main(String[] args) throws URISyntaxException, InvalidParaException, ConnectFailedException, JsonFileIOException {

        KarelConnection.Builder builder = new KarelConnection.Builder();
        builder.host("192.168.0.1")
                .port(80)
                .readPath("/karel/ComGet")
                .writePath("/karel/ComSet");

        IConnection connection = builder.build();

        CommentService commentService = new CommentService(connection);
        CommentRepository commentRepository  = new CommentRepository();

        // http://192.168.0.1:8080/karel/ComSet?sComment=%B3%CC%D0%F2%B2%E2%CA%D4&sIndx=470&sFc=8

        Comment comment = new Comment(470, "程序测试", CommentType.DI);


        commentService.updateComment(comment);

        Comment queryByID = commentService.queryByID(1, CommentType.DI);
        System.out.println(queryByID);

        Comment queryByIDDO = commentService.queryByID(52, CommentType.DI);
        System.out.println(queryByIDDO);


        List<Comment> queryList = commentService.queryByKeyword("水流量", CommentType.DI);

        commentRepository.saveToJson(queryList,"/Users/liuke/IdeaProjects/FanucHelper/水流量.json");

        List<Comment> loadedList = commentRepository.loadFromLocalFile("/Users/liuke/IdeaProjects/FanucHelper/水流量.json");
        loadedList.forEach(System.out::println);


    }
}
