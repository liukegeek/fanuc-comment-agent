package com.byd.tools.connect;

/**
 * ClassName: ConnectServer
 * Package: com.byd.tools.service
 * Description:
 * Author: LiuKe
 * Create: 2025/4/16 21:56
 * Version 1.0
 */

import com.byd.tools.exceptions.CreateConnectFailed;
import com.byd.tools.exceptions.RequestFailed;
import com.byd.tools.service.comment.TestConnectionState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;


public class ConnectServer {
    private String protocol; //协议
    private String ip; //具体的IP地址
    private String rootPath = "/karel";
    private String baseUrl; //协议+ip+访问路径拼接成的url
    private static final Logger logger = LogManager.getLogger(ConnectServer.class);


    public ConnectServer(String ip) {
        this("http", ip, "");
    }

    public ConnectServer(String protocol, String ip) {
        this(protocol, ip, "");

    }

    public ConnectServer(String protocol, String ip, String rootPath) {
        this.protocol = protocol;
        this.ip = ip;
        this.rootPath = rootPath;
        baseUrl = protocol + ":" + "//" + ip + rootPath;
    }

    private HttpURLConnection getConnection(String urlPath) {
        try {
            URL url = URI.create(urlPath).toURL();
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new CreateConnectFailed("URL: " + urlPath + "  未能正确解析,所给URL不合法");
        } catch (IOException e) {
            throw new CreateConnectFailed("出现了IO异常，未能成功与" + urlPath + "建立连接");
        }
    }

    public int testConnectionState(String childPath, String para) {
        String requestMethod = "GET";
        String testUrl = baseUrl + childPath + para;
        logger.info("执行网络测试连接，测试网址为:{}", testUrl);

        HttpURLConnection httpURLConnection = getConnection(testUrl);
        try {
            //设置一些初始化连接属性。可省略，对fanuc的web server无影响。
            httpURLConnection.setConnectTimeout(5000);  // 连接超时5秒
            httpURLConnection.setReadTimeout(5000);     // 读取超时5秒
            httpURLConnection.setRequestMethod(requestMethod);  //设置请求方式
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");

            logger.info("完成连接请求属性设置，即将执行:httpURLConnection.getResponseCode()方法进行连接");

            //使用了switch表达式，变量responseCode为200或201，则返回true，否则返回false。
            int responseCode = httpURLConnection.getResponseCode();
            logger.info("连接执行完成，反馈码为:{}", responseCode);
            return responseCode;
        } catch (Exception e) {
            throw new RuntimeException("与服务器建立连接时产生错误");
        }
    }


    public InputStream getResource(String childPath, String para) {
        String requestMethod = "GET";
        String downloadUrl = baseUrl + childPath + para;
        HttpURLConnection httpDownloadConn = getConnection(downloadUrl);

        try {
            //设置一些初始化连接属性。可省略，对fanuc的web server无影响。
            httpDownloadConn.setConnectTimeout(5000);  // 连接超时5秒
            httpDownloadConn.setReadTimeout(5000);     // 读取超时5秒
            httpDownloadConn.setRequestMethod(requestMethod);  //设置请求方式
            httpDownloadConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");
            httpDownloadConn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");

            InputStream inputStream = httpDownloadConn.getInputStream();
            return inputStream;
        } catch (ProtocolException e) {
            throw new RequestFailed("使用了不满足要求的请求方法:" + requestMethod);
        } catch (IOException e) {
            throw new RuntimeException("获取响应并下载资源的过程中发生了IO错误");
        }
    }

    public void uploadByPara(String childPath, String para) {
        String requestMethod = "GET";
        String uploadUrl = baseUrl + childPath + para;
        HttpURLConnection httpUploadConn = getConnection(uploadUrl);

        try {
            httpUploadConn.setConnectTimeout(5000);  // 连接超时5秒
            httpUploadConn.setReadTimeout(5000);     // 读取超时5秒
            httpUploadConn.setRequestMethod(requestMethod);
            httpUploadConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");
            httpUploadConn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");

            int responseCode = httpUploadConn.getResponseCode();
            if (responseCode != 0) {
                System.out.println("成功发送请求:" + uploadUrl);
            } else {
                throw new RequestFailed("请求发送失败");
            }
        } catch (ProtocolException e) {
            throw new RequestFailed("使用了不满足要求的请求方法:" + requestMethod);
        } catch (IOException e) {
            throw new RequestFailed("通过get参数发送请求的过程中出现错误" + e);
        }

    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
        baseUrl = protocol + ":" + "//" + ip + rootPath;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        baseUrl = protocol + ":" + "//" + ip + rootPath;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
        baseUrl = protocol + ":" + "//" + ip + rootPath;

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return baseUrl;
    }
}
