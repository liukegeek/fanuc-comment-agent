import com.byd.tools.connect.ConnectServer;
import com.byd.tools.data.DataToURL;
import com.byd.tools.exceptions.JsonFileIOException;
import com.byd.tools.service.CommentRepository;
import com.byd.tools.data.ParseHtml;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentURLPara;
import com.byd.tools.service.DownloadComment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ClassName: ServerTest
 * Package: PACKAGE_NAME
 * Description:
 * Author: LiuKe
 * Create: 2025/4/16 22:44
 * Version 1.0
 */
public class ServerTest {
    /**
     * 测试是否能通过JAVA与 所提供的URL网址建立连接。
     *
     * @throws IOException 为使得直观看到 测试结果通过与否，使用Jsoup直接解析，并按照语法规范抛出IO异常
     */
    @Test
    public void testConnect() throws IOException {
        ConnectServer connectServer = new ConnectServer("http", "192.168.0.1", "/karel");
        System.out.println(connectServer);
        InputStream inputStream = connectServer.getResource("/ComGet", "?sFc=33");
        Document document = Jsoup.parse(inputStream, "GBK", "");
        System.out.println(document);
    }

    /**
     * 测试解析程序是否能正常运行。 这里提供了一份 "ComBlank.html" 对象 作为InputStream，用来测试
     */
    @Test
    public void testParse() {
        InputStream resourceAsStream = ClassLoader.getSystemClassLoader().getResourceAsStream("ComBlank.html");
        ParseHtml parseHtml = new ParseHtml(resourceAsStream, "", "GBK");
        List<Comment> comments = parseHtml.getDigitalComments();
        comments.forEach(System.out::println);
    }


    /**
     * 测试能否将 数字量长文本对象列表 按指定路径保存成JSON文件.
     */
    @Test
    public void testSaveJson() {

        //用于测试(测试时未连接服务器，无法通过Connector对象获取资源),故而临时重写服务器方法进行测试。
        ConnectServer connectServer = new ConnectServer("") {
            @Override
            public InputStream getResource(String childPath, String para) {
                return ClassLoader.getSystemClassLoader().getResourceAsStream("ComBlank.html");
            }
        };

        String filePath = "/Users/liuke/IdeaProjects/FanucHelper/src/main/java/com/byd/tools/CommentTest.json";
        DownloadComment test = new DownloadComment(connectServer, filePath);
        test.service();
    }

    /**
     * 测试能否从Json文件中读取出信息并将其成功保存成pojo对象。
     */
    @Test
    public void testLoadJson() throws JsonFileIOException {
        CommentRepository commentRepository = new CommentRepository();
        List<Comment> commentList = commentRepository.loadFromLocalFile("/Users/liuke/IdeaProjects/FanucHelper/src/main/java/com/byd/tools/CommentTest_input.json");
        commentList.forEach(System.out::println);
    }

    /**
     * 测试能否成功将 长文本对象:Comment  转换成 能够作为url参数发送的: CommentURLPara 对象。
     */
    @Test
    public void testDataToUrl() throws JsonFileIOException {
        CommentRepository commentRepository = new CommentRepository();
        DataToURL dataToURL = new DataToURL();
        List<Comment> commentList = commentRepository.loadFromLocalFile("/Users/liuke/IdeaProjects/FanucHelper/src/main/java/com/byd/tools/CommentTest_input.json");
        List<CommentURLPara> commentURLParaList = dataToURL.transferToPara(commentList);
        commentURLParaList.forEach(System.out::println);
    }

    /**
     * 测试上传长文本
     */
    @Test
    public void testUploadComment() throws JsonFileIOException {
        //用于测试(测试时未连接服务器，无法通过Connector对象获取资源),故而临时重写服务器方法进行测试。
        ConnectServer connectServer1 = new ConnectServer("http","192.168.0.1","/karel") {
            @Override
            public void uploadByPara(String childPath, String para) {
                String uploadUrl = getBaseUrl() + childPath + para;
                System.out.println(uploadUrl);
            }
        };

        ConnectServer connectServer = new ConnectServer("http", "192.168.0.1", "/karel");

        CommentRepository commentRepository = new CommentRepository();
        DataToURL dataToURL = new DataToURL();
        List<Comment> commentList = commentRepository.loadFromLocalFile("/Users/liuke/Documents/fanuc测试/Untitled.json");
        List<CommentURLPara> commentURLParaList = dataToURL.transferToPara(commentList);

        commentURLParaList.forEach(
                commentURLPara -> {
                    String urlParaStr = commentURLPara.getParaURL("GBK");
                    connectServer.uploadByPara("/ComSet", urlParaStr);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
