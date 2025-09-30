import com.byd.tools.connect.KarelConnection;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import org.junit.Test;

import java.net.URI;

/**
 * ClassName: KarelConnectionTest
 * Package: PACKAGE_NAME
 * Description:
 * Author: LiuKe
 * Create: 2025/8/9 00:04
 * Version 1.0
 */
public class KarelConnectionTest {
    @Test
    public void testBuilder() {
        com.byd.tools.connect.KarelConnection karelConnection = new com.byd.tools.connect.KarelConnection.Builder()
                .port(443)
                .readPath("/aaa")
                .writePath("/bbb")
                .host("123456")
                .host("haha")
                .protocol("hwwp")
                .build();
        System.out.println(karelConnection);

    }

    @Test
    public void test111() throws Exception {
        String ip = "192.168.0.1";

        String host = "nu   ";
        int port = 30;
        String schema = "http";
        URI uri = new URI(schema, null, host, 0, "/null", "bbb=777&aaa=47312", "aaa");

        String str = uri.toString();
        System.out.println(str);
    }


    @Test
    public void testRead() {
        KarelConnection karelConnection = new KarelConnection.Builder()
                .protocol("http")
                .host("192.168.0.1")
                .port(80)
                .readPath("/karel/ComGet")
                .writePath("/karel/ComSet")
                .build();


        System.out.println("baseUrl is :" + karelConnection.getBaseUrl());
        System.out.println("-----------------------------");
        for (int i = 1; i < 6; i++) {
            Comment comment = karelConnection.readComment(i, CommentType.DI);
            System.out.println(comment);
        }
        System.out.println("END");
    }

    @Test
    public void testWrite() throws Exception {
        KarelConnection karelConnection = new KarelConnection.Builder()
                .protocol("http")
                .host("192.168.0.1")
                .port(80)
                .readPath("/karel/ComGet")
                .writePath("/karel/ComSet")
                .build();

        Comment modifyComment = new Comment(1, "PLC生命位_190", CommentType.DI);
        karelConnection.writeComment(modifyComment);
    }
}
