package com.byd.tools.view;

import com.byd.tools.connect.ConnectServer;
import com.byd.tools.pojo.ServiceResponseInfo;
import com.byd.tools.service.DownloadComment;
import com.byd.tools.service.TestConnectionState;
import com.byd.tools.service.UploadComment;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

import static javafx.application.Application.launch;


/**
 * ClassName: FanucEditor
 * Package: com.byd.tools.view
 * Description:
 * Author: LiuKe
 * Create: 2025/4/15 16:12
 * Version 1.0
 */
public class FanucEditor extends Application {


    // 主窗口组件
    private TextArea logArea; // 用来显示日志或输出信息
    private ConnectServer connectServer; //用来存储服务器连接
    private static final Logger logger = LogManager.getLogger(FanucEditor.class);

    public static void main(String[] args) {
        // 启动 JavaFX 应用程序
        launch(args);
    }

    public void start(Stage primaryStage) {
        // 设置窗口标题
        primaryStage.setTitle("让法那科再次伟大！(长文本程序编辑器)");

        // 顶部布局：使用 HBox 放置“连接网络”按钮和 IP 输入框
        Label ipLabel = new Label("请输入IP：");
        TextField ipTextField = new TextField();
        ipTextField.setPromptText("例如 192.168.1.1");
        ipTextField.setText("192.168.0.1");
        Button connectBtn = new Button("连接网络");
        connectBtn.setOnAction(e -> connectNetwork(ipTextField.getText()));

        HBox topPane = new HBox(20, ipLabel, ipTextField, connectBtn); //spacing =20 代表 各Node之间的间距为20，
        topPane.setPadding(new Insets(10)); //设置整个 topPane盒子，与上、下、左、右的距离。这里均设置为了10
        topPane.setStyle("-fx-alignment: center;");

        // 中间布局：放置4个主要功能按钮
        Button downloadBtn = new Button("下载长文本");
        downloadBtn.setOnAction(e -> downloadLongText());
        Button editBtn = new Button("编辑长文本");
        editBtn.setOnAction(e -> editLongText());
        Button uploadBtn = new Button("上传长文本");
        uploadBtn.setOnAction(e -> uploadLongText());

        HBox buttonPane = new HBox(40, downloadBtn, editBtn, uploadBtn);
        buttonPane.setPadding(new Insets(20));
        buttonPane.setStyle("-fx-alignment: center;");

        // 底部布局：日志文本域，用于显示输出信息
        String guideText = """
                ==================================================================
                 i.点击连接网络，输入IP等待连接成功;
                 ii.点击「下载长文本」，然后选择保存位置，即可下载得到JSON文件;
                 iii.点击「编辑长文本」，然后选择目标文件，即可打开JSON文件；
                 iv.点击「上传长文本」，然后选择待上传文件，即可将JSON文件进行上传。
                ==================================================================
                """;
        logArea = new TextArea(guideText);
        logArea.setEditable(false);
        logArea.setWrapText(true);


        VBox root = new VBox(10, topPane, buttonPane, logArea);
        root.setPadding(new Insets(20, 30, 0, 30));

        // 创建场景，并设置到舞台
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * 当用户点击连接网络按钮时调用
     * 这里简单输出连接状态
     */
    private void connectNetwork(String ip) {
        if (ip == null || ip.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "错误", "请输入合法的 IP 地址。");
            return;
        }
        // 模拟网络连接
        log("尝试连接到 " + ip + "...");
        // 实际操作中可以进行 ping 或 HTTP 请求测试
        if (connectServer == null) {
            connectServer = new ConnectServer("http", ip, "/karel");
        }
        logger.info("已绑定网址:{}", connectServer.getBaseUrl());
        log("已绑定网址:" + connectServer.getBaseUrl());
        TestConnectionState testConnectionState = new TestConnectionState(connectServer);
        ServiceResponseInfo serviceResponseInfo = testConnectionState.service();
        log(serviceResponseInfo.getResponseInfo());

    }

    /**
     * 当用户点击“下载长文本”时调用
     * 弹出文件保存对话框
     */
    private void downloadLongText() {
        // 创建文件保存对话框
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择保存的长文本文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON 文件", "*.json"));
        Stage stage = new Stage();
        File saveFile = fileChooser.showSaveDialog(stage);
        if (saveFile != null) {
            // 模拟下载动作：实际应用中这里应调用 RobotService.downloadLongText(url)
            String saveAbsolutePath = saveFile.getAbsolutePath();
            log("下载长文本到：" + saveAbsolutePath);
            DownloadComment downloadComment = new DownloadComment(connectServer, saveAbsolutePath);
            // 此处写入保存逻辑
            ServiceResponseInfo serviceResponseInfo = downloadComment.service();
            log(serviceResponseInfo.getResponseInfo());
        } else {
            log("未选择保存文件。");
        }
    }

    /**
     * 当用户点击“编辑长文本”时调用
     * 弹出文件打开对话框，读取文件后显示
     */
    private void editLongText() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要编辑的长文本文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON 文件", "*.json"));
        Stage stage = new Stage();
        File openFile = fileChooser.showOpenDialog(stage);
        if (openFile != null) {
            // 模拟打开和编辑：你可以在新窗口中用 TextArea 展示文件内容
            log("打开文件：" + openFile.getAbsolutePath());
            // 实际项目中可以调用 JsonService.loadData(openFile);
            showTextEditor(openFile);
        } else {
            log("未选择文件。");
        }
    }

    /**
     * 当用户点击“上传长文本”时调用
     * 弹出文件打开对话框选取文件后上传
     */
    private void uploadLongText() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择要上传的长文本文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON 文件", "*.json"));
        Stage stage = new Stage();
        File uploadFile = fileChooser.showOpenDialog(stage);
        if (uploadFile != null) {
            // 模拟上传：实际项目中调用 RobotService.uploadLongText(ip, file内容)
            String uploadFileAbsolutePath = uploadFile.getAbsolutePath();
            log("上传长文本文件：" + uploadFileAbsolutePath);
            UploadComment uploadComment = new UploadComment(connectServer, uploadFileAbsolutePath);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            uploadComment.setSegTime(20);
            uploadComment.service();

        } else {
            log("未选择文件。");
        }
    }

    /**
     * 打开一个新窗口用于编辑文件内容（简单实现）
     */
    private void showTextEditor(File file) {
        Stage editorStage = new Stage();
        editorStage.setTitle("编辑长文本 - " + file.getName());

        TextArea textArea = new TextArea();
        textArea.setPrefSize(600, 400);

        // 加载文件内容（这里简单模拟，实际请读取文件内容）
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), "UTF-8");
            textArea.setText(content);
        } catch (Exception ex) {
            textArea.setText("读取文件出错：" + ex.getMessage());
        }

        Button saveButton = new Button("保存修改");
        saveButton.setOnAction(e -> {
            // 保存编辑后的内容到文件
            try {
                java.nio.file.Files.write(file.toPath(), textArea.getText().getBytes("UTF-8"));
                log("文件保存成功：" + file.getAbsolutePath());
                editorStage.close();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "保存错误", ex.getMessage());
            }
        });

        VBox editorRoot = new VBox(10, textArea, saveButton);
        editorRoot.setPadding(new Insets(10));
        Scene scene = new Scene(editorRoot);
        editorStage.setScene(scene);
        editorStage.show();
    }

    /**
     * 向日志区域追加日志信息
     */
    private void log(String message) {
        logArea.appendText(message + "\n");
    }

    /**
     * 显示一个简单的对话框
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
