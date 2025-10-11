package com.byd.tools.service;

import com.byd.tools.connect.ConnectServer;

import com.byd.tools.pojo.ServiceResponseInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ClassName: TestConnectionState
 * Package: com.byd.tools.service.comment
 * Description:
 * Author: LiuKe
 * Create: 2025/4/21 22:52
 * Version 1.0
 */
public class TestConnectionState {
    private final ConnectServer connectServer;
    private static final Logger logger = LogManager.getLogger(TestConnectionState.class);

    public TestConnectionState(ConnectServer connectServer) {
        this.connectServer = connectServer;
    }

    public ServiceResponseInfo service() {
        String responseStr = "";
        ServiceResponseInfo serviceResponseInfo;

        try {
            int responseCode = connectServer.testConnectionState("/COMMAIN","");
            switch (responseCode) {
                case 200, 201, 202:
                    responseStr = "与服务器成功连接";
                    logger.info(responseStr);
                    serviceResponseInfo = new ServiceResponseInfo("TestConnectionState",responseStr);
                    break;
                case 404, 403:
                    responseStr = "服务器未响应导致连接失败,错误代码：" + responseCode;
                    logger.error(responseStr);
                    serviceResponseInfo = new ServiceResponseInfo("TestConnectionState",responseStr);
                    break;
                default:
                    responseStr = "连接失败,错误代码：" + responseCode;
                    logger.error(responseStr);
                    serviceResponseInfo = new ServiceResponseInfo("TestConnectionState",responseStr);
            }
        } catch (Exception e) {
            responseStr = "发生异常错误，测试执行失败:" + e.getMessage();
            logger.error(responseStr);
            serviceResponseInfo = new ServiceResponseInfo("TestConnectionState",responseStr);
        }
        return serviceResponseInfo;
    }

}
