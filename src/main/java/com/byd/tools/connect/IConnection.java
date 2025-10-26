package com.byd.tools.connect;

import com.byd.tools.exceptions.ConnectFailedException;
import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;

import java.net.URISyntaxException;
import java.util.List;

/**
 * ClassName: IConnection
 * Package: com.byd.tools.connect
 * Description:
 * 用于建立与Fanuc控制柜服务器的连接，并进行管理与交互(通过发送待参数的特定URL来上传长文本，通过请求html页面并解析来筛选出服务器中的长文本数据)
 * Author: LiuKe
 * Create: 2025/8/4 23:21
 * Version 1.0
 */
public interface IConnection {


    /**
     * 用于通过该连接，从服务器中读取特定类型type与id的长文本对象
     *
     * @param id   要读取的长文本编号(id)，用于在特定类型中唯一标识一个长文本
     * @param type 要读取的长文本类型，指定要查询的长文本类别,比如是DI、DO、AI、AO等
     * @return 读取成功返回目标Comment对象，包含长文本的完整信息；若未找到则返回null
     * @throws ConnectFailedException 当与Fanuc控制柜服务器的连接失败时抛出
     * @throws InvalidParaException   当输入的参数无效（如id为负数或type为null）时抛出
     */
    Comment readComment(int id, CommentType type) throws ConnectFailedException, InvalidParaException;

    /**
     * 用于通过该连接，从服务器中读取特定类型type的所有长文本对象
     *
     * @param commentType 要读取的comment类型
     * @return 所请求的类型的所有comment列表
     * @throws ConnectFailedException 当与Fanuc控制柜服务器的连接失败时抛出
     * @throws InvalidParaException   当输入的参数无效（如id为负数）时抛出
     */
    List<Comment> readAllComments(CommentType commentType) throws ConnectFailedException, InvalidParaException;



    /**
     * 用于通过该连接，向服务器中写入特定的长文本对象
     *
     * @param comment 要向服务器写入的对象,如果为null，则什么都不处理。
     * @return 写入成功返回true，否则返回false。
     * @throws ConnectFailedException 当与Fanuc控制柜服务器的连接失败时抛出
     * @throws InvalidParaException   当输入的参数无效（如comment为null）时抛出
     */
    boolean writeComment(Comment comment) throws ConnectFailedException, InvalidParaException;


    /**
     * 用于给定参数来创建该连接对象的baseUrl,这是给baseUrl赋值的唯一方式。。
     *
     * @param protocol 该对象将连接的服务器URL中的协议部分，例：http/https。
     * @param host     该对象将连接的服务器URL中的host地址，例：192.168.0.1。
     * @param port     该对象将连接的服务器URL中的端口号，例：80、443
     * @thorws ConnectionException 连通性失败或握手失败
     * 例如，对于对于一个“<a href="http://192.168.0.1/KAREL/COMMAIN">...</a>"的连接，那么protocol="http", host="192.168.0.1",basePath="/KAREL/COMMAIN";
     */
    void modifyBaseURL(String protocol, String host, int port) throws URISyntaxException;

}