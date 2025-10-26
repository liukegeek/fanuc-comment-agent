package com.byd.tools.connect;

import com.byd.tools.exceptions.ConnectFailedException;
import com.byd.tools.exceptions.InvalidParaException;

import com.byd.tools.parse.KarelWebParser;
import com.byd.tools.parse.WebParserFactory;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName: KarelConnection
 * Package: com.byd.tools.connect
 * Description:
 * 用于管理 与 fanuc控制柜服务器:karel 的连接
 * Author: LiuKe
 * Create: 2025/8/5 22:39
 * Version 1.0
 */
public class KarelConnection implements IConnection {
    private String host; //与机器人服务程序 建立连接的url
    private String baseUrl; //与机器人服务程序建立连接的 baseUrl

    private final String readPath; //对长文本进行读取操作时，将访问的url的path部分。
    private final String writePath;//对长文本进行改写操作时，将访问的url的path部分。

    //缓存部分，对某种访问过的类型，会将该类型所有的长文本都存储在这里。
    private final HashMap<CommentType, HashMap<Integer, Comment>> cache = new HashMap<>();
    private final HashMap<CommentType, Boolean> cacheValid = new HashMap<>();  //用于存放缓存实效过期的信息。

    /**
     * 从机器人的服务器中 根据类型、id 来获取长文本对象。并将获取到的该类型的所有长文本存储在缓存变量temp当中。
     *
     * @param id          要读取的长文本编号(id)
     * @param commentType 要读取的长文本类型。
     * @return 要获取的长文本对象, 如果未找到则返回null。
     * @throws ConnectFailedException 当与Fanuc控制柜服务器的连接失败时抛出
     * @throws InvalidParaException   当输入的参数无效（如id为负数）时抛出
     */
    @Override
    public Comment readComment(int id, CommentType commentType) throws ConnectFailedException, InvalidParaException {
        KarelWebParser webParser = WebParserFactory.of(commentType);
        String readUrlPara = webParser.generateReadPara(commentType);
        String readUrl = baseUrl + readPath + readUrlPara;
    
        try {
            //该类型长文本无缓存，则先从服务器中请求，然后存入到缓存temp中。之后查询便直接通过缓存中查即可。
            if (cacheValid.get(commentType) == null || cacheValid.get(commentType) == false) {
                //测试readUrl的连通性。
                if (!checkConnect(readUrl)) {
                    throw new ConnectFailedException("与服务器未建立正确连接,无法获取网址:" + readUrl);
                }
    
                //如果该类型元素之前未访问过，则先创建hashMap表放入缓存中。 subsequent将该类型元素依次添加进hashMap中即可。
                cache.put(commentType, new HashMap<>());
    
                HttpURLConnection httpURLConnection = createConnection(readUrl);
                try (InputStream karelInputStream = httpURLConnection.getInputStream()) {
                    List<Comment> commentList = webParser.parseDataFromHtml(karelInputStream, "GBK", readUrl);
                    //未查找到该类型的长文本，异常情况。
                    if (commentList.isEmpty()) {
                        System.out.println("未能获取到任何长文本信息，请检查查询范围"); //记录，不要报错
                        return null; //未查找到该类型的长文本，返回null
                    }
                    //将该类型的每一个元素都读取出来然后存放在缓存中。
                    commentList.forEach(x -> {
                        if (commentType.equals(x.getType())) {
                            cache.get(commentType).put(x.getId(), x);
                        }
                    });
                } finally {
                    // 确保关闭HTTP连接
                    httpURLConnection.disconnect();
                }
                // 全部读完则设置为缓存有效。
                cacheValid.put(commentType, true);
            }
    
    
            //先根据类型，从缓存中找到所有该type的Map集合，然后根据id直接返回对应的comment对象。
            return cache.get(commentType).get(id);
        } catch (IOException e) {
            System.out.println("由于查询操作未能正常运行");
            throw new ConnectFailedException("遭遇故障:" + e + "未能成功建立连接");
        }
    }


    /**
     * 用于通过该连接，从服务器中读取特定类型type的所有长文本对象
     *
     * @param commentType 要读取的comment类型
     * @return 所请求的类型的所有comment列表
     * @throws ConnectFailedException 当与Fanuc控制柜服务器的连接失败时抛出
     * @throws InvalidParaException   当输入的参数无效（如id为负数）时抛出
     */
    @Override
    public List<Comment> readAllComments(CommentType commentType) throws ConnectFailedException, InvalidParaException {
        List<Comment> allCommentList = new ArrayList<>();

        //尝试访问一个长文本，确保缓存里面已经存储了所有该类型的长文本最新信息
        readComment(1, commentType);

        for (int i = 0; i < cache.get(commentType).size(); i++) {
            //将hashmap转换成有序的list列表。由于IO注释通常从1开始，而不是0，因此这里是i+1
            allCommentList.add(cache.get(commentType).get(i + 1));
        }
        return allCommentList;
    }


    /**
     * 用于通过该连接，向服务器中写入某个长文本对象。
     *
     * @param comment 要向服务器写入的对象，如果为null则什么都不处理直接返回true。
     * @return 写入成功返回true，否则返回false。
     * @throws ConnectFailedException 当与Fanuc控制柜服务器的连接失败时抛出
     * @throws InvalidParaException   当输入的参数无效（如comment为null）时抛出
     */
    @Override
    public boolean writeComment(Comment comment) throws ConnectFailedException, InvalidParaException {
        if (comment == null) {
            //写入的长文本对象为空，什么都不处理
            return true;
        }

        KarelWebParser webParser = WebParserFactory.of(comment.getType());

        String urlPara = webParser.generateWritePara(comment.getType(), comment.getId(), comment.getContent(), "GBK");
        String writeUrl = baseUrl + writePath + urlPara;

        try {
            //测试用来更改的url连接是否连通
            if (!checkConnect(writeUrl)) {
                throw new ConnectFailedException("与服务器未建立正常连接与通信");
            }

            HttpURLConnection httpURLConnection = createConnection(writeUrl);
            try {
                int responseCode = httpURLConnection.getResponseCode();
                httpURLConnection.getInputStream().close(); //获取流并直接关闭，丢弃服务器可能返回的数据实体，释放资源
                if (200 <= responseCode && responseCode < 300) {
                    System.out.println("成功发送请求:" + httpURLConnection);
                    //同时销毁旧的缓存
                    cacheValid.put(comment.getType(), false);
                    return true;
                } else {
                    System.out.println("请求发送失败");
                    return false;
                }
            } finally {
                // 确保关闭HTTP连接
                httpURLConnection.disconnect();
            }
        } catch (IOException e) {
            System.out.println("由于写入操作未能正常运行");
            throw new ConnectFailedException("遭遇故障:" + e + "未能成功建立连接");
        }
    }


    /**
     * 私有构造函数，用于根据Builder模式创建KarelConnection对象。
     *
     * @param builder 建造器对象，用于设置KarelConnection的属性。
     * @throws URISyntaxException 当URL语法错误时抛出
     */
    private KarelConnection(Builder builder) throws URISyntaxException {
        this.host = builder.host;
        this.readPath = builder.readPath;
        this.writePath = builder.writePath;
        modifyBaseURL(builder.defaultProtocol, host, builder.defaultPort);
        System.out.println("构建成功:\n" + this);
    }

    /**
     * 建造器，用于实现外部类 KarelConnection的实例化。
     */
    public static class Builder {
        private String host;

        private String readPath;
        private String writePath;

        private String defaultProtocol = "http";  //默认协议采用protocol
        private int defaultPort = 80; //默认端口号采用80

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder protocol(String protocol) {
            this.defaultProtocol = protocol;
            return this;
        }

        public Builder port(int port) {
            this.defaultPort = port;
            return this;
        }

        public Builder readPath(String readPath) {
            this.readPath = readPath;
            return this;
        }

        public Builder writePath(String writePath) {
            this.writePath = writePath;
            return this;
        }


        public KarelConnection build() throws InvalidParaException {
            //参数必须非空，地址必须以'/'开始。
            try {
                if (host == null) {
                    throw new InvalidParaException("目标网络地址未设置:host is null");
                }
                if (readPath == null || readPath.charAt(0) != '/') {
                    throw new InvalidParaException("Fanuc长文本的服务器读取路径设置错误:readPath is " + readPath);
                }
                if (writePath == null || writePath.charAt(0) != '/') {
                    throw new InvalidParaException("Fanuc长文本的服务器修改路径设置错误:writePath is " + writePath);
                }

                return new KarelConnection(this);
            } catch (URISyntaxException e) {
                throw new InvalidParaException(e.getMessage());
            }
        }
    }

    /**
     * 用于给定参数来创建该连接对象的baseUrl,这是给baseUrl赋值的唯一方式。
     *
     * @param protocol 该对象将连接的服务器URL中的协议部分，例：http/https。
     * @param host     该对象将连接的服务器URL中的host地址，例：192.168.0.1。
     * @param port     该对象将连接的服务器URL中的端口号，例：80、443
     * @throws URISyntaxException 当URL语法错误时抛出
     */
    @Override
    public void modifyBaseURL(String protocol, String host, int port) throws URISyntaxException {

        // URI的构造方法自带 对 protocol、host、port等参数的校验。参数不正确会抛出URISyntaxException异常。
        URI baseuri = new URI(protocol, null, host, port, null, null, null);
        this.host = host;
        this.baseUrl = baseuri.toString();//拼接成基本的url访问串。
    }


    /**
     * 用于根据URL字符串创建HTTP连接对象。
     *
     * @param urlStr 目标URL字符串。
     * @return 建立的HTTP连接对象。
     * @throws ConnectFailedException 当连接失败时抛出。
     */
    private HttpURLConnection createConnection(String urlStr) throws ConnectFailedException {
        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //设置一些初始化连接属性。可省略，对fanuc的web server无影响。
            httpURLConnection.setConnectTimeout(5000);  // 连接超时5秒
            httpURLConnection.setReadTimeout(5000);     // 读取超时5秒
            httpURLConnection.setRequestMethod("GET");  //设置请求方式
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Java)");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            return httpURLConnection;
        } catch (IOException e) {
            throw new ConnectFailedException("创建URL对象或建立连接时，出现了IO相关异常:「" + e + "」,未能成功与" + urlStr + "建立连接");
        }
    }


    /**
     * 用于检查与目标URL的连接是否正常。
     *
     * @param targetUrl 目标URL字符串。
     * @return 如果连接正常返回true，否则返回false。
     */
    private boolean checkConnect(String targetUrl) {

        boolean connectOpen = false;
        int responseCode = -1;
        HttpURLConnection httpURLConnection = null;
    
        try {
            httpURLConnection = createConnection(targetUrl);
            responseCode = httpURLConnection.getResponseCode();
            httpURLConnection.getInputStream().close(); //只需要状态码就行，丢弃响应体释放连接资源。
            connectOpen = switch (responseCode) {
                case 200, 201, 202 -> {
                    System.out.println("测试连接成功:" + targetUrl);
                    yield true;
                }
                case 404, 403 -> {
                    System.out.println("测试连接时，服务器未响应导致连接失败,错误代码：" + responseCode);
                    yield false;
                }
                default -> {
                    System.out.println("测试连接时，未成功连接:" + targetUrl + "错误代码为:" + responseCode);
                    yield false;
                }
            };
        } catch (IOException e) {
            System.out.println("进行连接通信时，出现IO异常：" + e);
            connectOpen = false;
        } catch (ConnectFailedException e) {
            System.out.println("无法创建连接" + e);
            connectOpen = false;
        } finally {
            // 确保关闭HTTP连接
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return connectOpen;
    }

    public void resetCache() {
        cacheValid.clear();
        cache.clear();
    }


    public String getHost() {
        return host;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getReadPath() {
        return readPath;
    }

    public String getWritePath() {
        return writePath;
    }

    @Override
    public String toString() {
        return "KarelConnection{" +
               "host='" + host + '\'' +
               ", baseUrl='" + baseUrl + '\'' +
               ", readPath='" + readPath + '\'' +
               ", writePath='" + writePath + '\'' +
               '}';
    }
}