/**
 * ClassName: module-info
 * Package:
 * Description:
 * Author: LiuKe
 * Create: 2025/5/4 19:15
 * Version 1.0
 */

module FanucHelper {
    requires com.google.gson;
    requires javafx.controls;
    requires javafx.graphics;
    requires org.jsoup;
    requires java.logging;
    requires org.apache.logging.log4j;

    exports com.byd.tools.connect;
    exports com.byd.tools.control;
    exports com.byd.tools.exceptions;
    exports com.byd.tools.pojo;
    exports com.byd.tools.service;

    exports com.byd.tools.view;           // ✅ 重点：主类所在包必须 exports

    exports com.byd.tools;         // 如果你的 FXML 或外部访问这个包，需要 export

    opens com.byd.tools.pojo to com.google.gson; // ✅ 解决 Gson 无法访问的问题
}