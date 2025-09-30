package com.byd.tools.service.comment;

/**
 * ClassName: UploadCommentService
 * Package: com.byd.tools.service.comment
 * Description:用来实现 UI界面中 "上传长文本" 功能的类： 上传所指定名字JSON文件以覆盖fanuc机器人原本的长文本
 * Author: LiuKe
 * Create: 2025/4/20 16:09
 * Version 1.0
 */

import com.byd.tools.connect.ConnectServer;
import com.byd.tools.data.DataToURL;
import com.byd.tools.data.JsonFileOperator;
import com.byd.tools.exceptions.RequestFailed;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentURLPara;
import com.byd.tools.pojo.ServiceResponseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * 上传指定的
 */
public class UploadComment {
    private final ConnectServer connectServer;
    private final String path;
    private int segTime = 10;
    private static final Logger logger = LogManager.getLogger(UploadComment.class);


    public UploadComment(ConnectServer connectServer, String path) {
        this.connectServer = connectServer;
        this.path = path;
    }

    public ServiceResponseInfo service() {
        JsonFileOperator jsonFileOperator = new JsonFileOperator();
        DataToURL dataToURL = new DataToURL();

        try {
            List<Comment> commentList = jsonFileOperator.loadFromJson(path);

            List<CommentURLPara> commentURLParaList = dataToURL.transferToPara(commentList);

            commentURLParaList.forEach(
                    commentURLPara -> {
                        String urlParaStr = commentURLPara.getParaURL("GBK");
                        boolean uploadSuccess = false;
                        int reUploadCount = 0;
                        while (!uploadSuccess) {
                            try {
                                connectServer.uploadByPara("/ComSet", urlParaStr);
                                uploadSuccess = true;
                                Thread.sleep(segTime);
                            } catch (RequestFailed e) {
                                if (reUploadCount > 5) {
                                    throw new RuntimeException("无法完成上传");
                                }
                                logger.warn("上传失败，正在进行重新上传");
                                reUploadCount++;
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            );
            return new ServiceResponseInfo("UploadComment", "成功上传所有长文本信息！");
        } catch (Exception e) {
            logger.error("由于发生了异常而未能成功上传所有长文本信息:" + e.getMessage());
            return new ServiceResponseInfo("UploadComment", "由于发生了异常而未能成功上传所有长文本信息:" + e.getMessage(), e);
        }
    }

    public void setSegTime(int segTime) {
        this.segTime = segTime;
    }
}
