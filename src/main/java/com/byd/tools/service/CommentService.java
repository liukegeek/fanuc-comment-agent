package com.byd.tools.service;

import com.byd.tools.connect.IConnection;
import com.byd.tools.exceptions.ConnectFailedException;
import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOGGER = LogManager.getLogger(CommentService.class);
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
        LOGGER.debug("查询注释详情: id={}, type={}", id, commentType);
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
        LOGGER.debug("按关键字查询注释: keyword={}, type={}", keyword, commentType);
        return connection.readAllComments(commentType).stream()
                .filter(comment -> comment.getContent().contains(keyword))
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
        LOGGER.debug("按范围查询注释: start={}, end={}, type={}", startId, endId, commentType);
        List<Comment> comments = new ArrayList<>();
        for (int i = startId; i <= endId; i++) {
            Comment comment = queryByID(i, commentType);
            if (comment != null) {
                comments.add(comment);
            } else {
                LOGGER.debug("范围查询未找到注释: id={}, type={}", i, commentType);
            }
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
        LOGGER.debug("查询类型下的全部注释: {}", commentType);
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
            boolean updated = connection.writeComment(newComment);
            if (!updated) {
                LOGGER.warn("更新注释失败: id={}, type={}", newComment.getId(), newComment.getType());
            } else {
                LOGGER.info("已更新注释: id={}, type={}", newComment.getId(), newComment.getType());
            }
            return updated;
        } catch (ConnectFailedException | InvalidParaException e) {
            LOGGER.error("更新注释时发生异常: id={}, type={}", newComment.getId(), newComment.getType(), e);
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
        if (comments == null || comments.isEmpty()) {
            LOGGER.info("没有需要上传的注释记录");
            return true;
        }
        LOGGER.info("准备上传 {} 条注释。", comments.size());
        for (Comment comment : comments) {
            boolean updated = false;
            for (int attempt = 1; attempt <= 5 && !updated; attempt++) {
                if (attempt > 1) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new ConnectFailedException("上传任务被中断", e);
                    }
                }
                if (updateComment(comment)) {
                    updated = true;
                } else if (attempt < 5) {
                    LOGGER.warn("上传注释失败，将进行第 {} 次重试: id={}, type={}", attempt + 1, comment.getId(), comment.getType());
                }
            }
            if (!updated) {
                LOGGER.error("上传注释失败且超过最大重试次数: id={}, type={}", comment.getId(), comment.getType());
                return false;
            }
        }
        LOGGER.info("成功上传 {} 条注释。", comments.size());
        return true;
    }
}