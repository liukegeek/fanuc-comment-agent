package com.byd.tools.service;

import com.byd.tools.connect.IConnection;
import com.byd.tools.exceptions.ConnectFailedException;
import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: CommentService
 * Package: com.byd.tools.service.comment
 * Description:
 * 长文本服务类，封装了与长文本相关的业务逻辑。
 * 提供了根据ID、关键词和ID范围查询长文本的方法。
 * Author: LiuKe
 * Create: 2025/10/10 02:13
 * Version 1.0
 */
public class CommentService {
    private final IConnection connection;

    /**
     * 构造函数，用于创建一个CommentService对象，该对象将通过connection参数与服务器进行通信。
     *
     * @param connection 用于与服务器进行通信的连接对象。
     */
    public CommentService(IConnection connection) {
        this.connection = connection;
    }

    /**
     * 根据ID和类型查询特定的长文本对象
     *
     * @param id          要查询的长文本编号
     * @param commentType 要查询的长文本类型
     * @return 查询到的长文本对象
     * @throws ConnectFailedException 当与服务器连接失败时抛出
     * @throws InvalidParaException   当输入参数无效时抛出
     */
    public Comment queryByID(int id, CommentType commentType) throws ConnectFailedException, InvalidParaException {
        return connection.readComment(id, commentType);
    }

    /**
     * 根据关键词查询包含该关键词的长文本对象列表
     *
     * @param keyword     用于查询的关键词
     * @param commentType 要查询的长文本类型
     * @return 包含该关键词的长文本对象列表
     */
    public List<Comment> queryByKeyword(String keyword, CommentType commentType) throws ConnectFailedException, InvalidParaException {
        return connection.readAllComments(commentType).stream()
                .filter(comment -> comment.getComment().contains(keyword))
                .toList();
    }

    /**
     * 根据ID范围查询指定范围内的长文本对象列表
     *
     * @param startId     范围的起始ID（包含）
     * @param endId       范围的结束ID（包含）
     * @param commentType 要查询的长文本类型
     * @return 指定范围内的长文本对象列表
     */
    public List<Comment> queryByIdRange(int startId, int endId, CommentType commentType) throws ConnectFailedException, InvalidParaException {
        List<Comment> comments = new ArrayList<>();
        for (int i = startId; i <= endId; i++) {
            comments.add(queryByID(i, commentType));
        }
        return comments;
    }

    /**
     * 从服务器查询所有长文本对象
     *
     * @param commentType 要查询的长文本类型
     * @return 所有长文本对象的列表
     * @throws ConnectFailedException 当与服务器连接失败时抛出
     * @throws InvalidParaException   当输入参数无效时抛出
     */
    public List<Comment> queryAllFromServer(CommentType commentType) throws ConnectFailedException, InvalidParaException {
        return connection.readAllComments(commentType);
    }


    /**
     * 更新指定的长文本对象
     *
     * @param newComment 包含更新信息的长文本对象
     * @return 如果更新成功则返回true，否则返回false
     */
    public boolean updateComment(Comment newComment) {
        try {
            return connection.writeComment(newComment);
        } catch (ConnectFailedException | InvalidParaException e) {
            return false;
        }

    }


    /**
     * 将列表中的所有长文本对象上传到服务器
     *
     * @param comments 要上传的长文本对象列表
     * @return 如果上传成功则返回true，否则返回false
     * @throws ConnectFailedException 当与服务器连接失败时抛出
     * @throws InvalidParaException   当输入参数无效时抛出
     */
    public boolean uploadAllToServer(List<Comment> comments) throws ConnectFailedException, InvalidParaException {
        for (Comment comment : comments) {
            //每个注释上传前都间隔0.1s,如果失败则重复，最多重复5次
            int retryCount = 0;
            while (retryCount < 5) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (updateComment(comment)) {
                    break;
                }
                retryCount++;
            }
            if (retryCount == 5) {
                return false;
            }
        }
        return true;
    }
}