package com.byd.tools.service;

import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.exceptions.JsonFileIOException;
import com.byd.tools.pojo.Comment;
import com.byd.tools.pojo.CommentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * CommentRepository类的测试类 (JUnit 4版本)
 * 测试数据存储访问类的各项功能
 */
public class CommentRepositoryTest {

    private CommentRepository commentRepository;
    private List<Comment> testCommentList1;
    private List<Comment> testCommentList2;
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder(); // JUnit 4提供的临时目录规则
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none(); // 用于异常测试

    @Before
    public void setUp() {
        // 初始化测试对象
        commentRepository = new CommentRepository();
        
        // 准备测试数据
        testCommentList1 = new ArrayList<>();
        testCommentList1.add(new Comment(1, "信号1描述", CommentType.DI));
        testCommentList1.add(new Comment(2, "信号2描述", CommentType.DI));
        
        testCommentList2 = new ArrayList<>();
        testCommentList2.add(new Comment(3, "IO点1描述", CommentType.DO));
        testCommentList2.add(new Comment(4, "IO点2描述", CommentType.DO));
    }

    @After
    public void tearDown() {
        // 清理测试资源
        commentRepository = null;
        testCommentList1.clear();
        testCommentList2.clear();
    }

    /**
     * 测试saveToJson方法的正常功能
     */
    @Test
    public void testSaveToJson_Success() throws Exception {
        // 准备测试文件路径
        File tempFile = tempFolder.newFile("test_comments.json");
        String savePath = tempFile.getAbsolutePath();
        
        // 执行保存操作
        commentRepository.saveToJson(testCommentList1, savePath);
        
        // 验证文件是否创建成功
        assertTrue("保存文件应该存在", tempFile.exists());
        assertTrue("保存文件不应该为空", tempFile.length() > 0);
        
        // 验证保存的数据是否可以正确加载
        List<Comment> loadedComments = commentRepository.loadFromLocalFile(savePath);
        assertNotNull("加载的评论列表不应为null", loadedComments);
        assertEquals("加载的评论数量应与保存的相同", testCommentList1.size(), loadedComments.size());
        assertEquals("第一个评论的ID应相同", testCommentList1.get(0).getId(), loadedComments.get(0).getId());
        assertEquals("第一个评论的内容应相同", testCommentList1.get(0).getComment(), loadedComments.get(0).getComment());
    }

    /**
     * 测试saveToJson方法对空列表的处理
     */
    @Test(expected = InvalidParaException.class)
    public void testSaveToJson_EmptyList() throws Exception {
        // 准备测试文件路径
        File tempFile = tempFolder.newFile("empty_comments.json");
        String savePath = tempFile.getAbsolutePath();
        
        // 测试空列表参数
        commentRepository.saveToJson(new ArrayList<>(), savePath);
        
        // 使用ExpectedException规则方式验证异常信息
        // 注意：如果使用这种方式，需要移除上面的@Test(expected = ...)注解
        /*
        expectedException.expect(InvalidParaException.class);
        expectedException.expectMessage("要保存的长文本列表为空");
        commentRepository.saveToJson(new ArrayList<>(), savePath);
        */
    }

    /**
     * 测试mergeAndSave方法的正常功能
     */
    @Test
    public void testMergeAndSave_Success() throws Exception {
        // 准备测试文件路径
        File tempFile = tempFolder.newFile("merged_comments.json");
        String savePath = tempFile.getAbsolutePath();
        
        // 执行合并保存操作
        commentRepository.mergeAndSave(savePath, testCommentList1, testCommentList2);
        
        // 验证文件是否创建成功
        assertTrue("合并保存文件应该存在", tempFile.exists());
        assertTrue("合并保存文件不应该为空", tempFile.length() > 0);
        
        // 验证合并后的数据是否正确
        List<Comment> loadedComments = commentRepository.loadFromLocalFile(savePath);
        assertNotNull("加载的评论列表不应为null", loadedComments);
        assertEquals("加载的评论数量应等于合并的列表总和", 
                testCommentList1.size() + testCommentList2.size(), loadedComments.size());
    }

    /**
     * 测试mergeAndSave方法对空参数的处理
     */
    @Test(expected = InvalidParaException.class)
    public void testMergeAndSave_EmptyParams() throws Exception {
        // 准备测试文件路径
        File tempFile = tempFolder.newFile("should_not_exist.json");
        String savePath = tempFile.getAbsolutePath();
        
        // 测试空参数
        commentRepository.mergeAndSave(savePath);
    }

    /**
     * 测试mergeAndSave方法对包含空列表的处理
     */
    @Test
    public void testMergeAndSave_WithEmptyList() throws Exception {
        // 准备测试文件路径
        File tempFile = tempFolder.newFile("merged_with_empty.json");
        String savePath = tempFile.getAbsolutePath();
        
        // 执行包含空列表的合并保存操作
        commentRepository.mergeAndSave(savePath, testCommentList1, new ArrayList<>(), testCommentList2);
        
        // 验证结果
        List<Comment> loadedComments = commentRepository.loadFromLocalFile(savePath);
        assertEquals("应忽略空列表，合并后的数量应等于非空列表总和", 
                testCommentList1.size() + testCommentList2.size(), loadedComments.size());
    }

    /**
     * 测试loadFromLocalFile方法的正常功能
     */
    @Test
    public void testLoadFromLocalFile_Success() throws Exception {
        // 先保存一个测试文件
        File tempFile = tempFolder.newFile("load_test.json");
        String filePath = tempFile.getAbsolutePath();
        commentRepository.saveToJson(testCommentList1, filePath);
        
        // 执行加载操作
        List<Comment> loadedComments = commentRepository.loadFromLocalFile(filePath);
        
        // 验证加载结果
        assertNotNull("加载的评论列表不应为null", loadedComments);
        assertEquals("加载的评论数量应与保存的相同", testCommentList1.size(), loadedComments.size());
        
        // 验证每条评论的内容
        for (int i = 0; i < testCommentList1.size(); i++) {
            Comment original = testCommentList1.get(i);
            Comment loaded = loadedComments.get(i);
            assertEquals("ID应相同", original.getId(), loaded.getId());
            assertEquals("评论内容应相同", original.getComment(), loaded.getComment());
            assertEquals("评论类型应相同", original.getType(), loaded.getType());
        }
    }

    /**
     * 测试loadFromLocalFile方法对不存在文件的处理
     */
    @Test(expected = JsonFileIOException.class)
    public void testLoadFromLocalFile_FileNotExists() throws Exception {
        // 不存在的文件路径
        String nonExistentPath = tempFolder.getRoot().getAbsolutePath() + File.separator + "non_existent_file.json";
        
        // 测试加载不存在的文件
        commentRepository.loadFromLocalFile(nonExistentPath);
    }

    /**
     * 测试保存和加载的一致性
     */
    @Test
    public void testSaveAndLoadConsistency() throws Exception {
        // 创建一个更复杂的测试数据
        List<Comment> complexList = new ArrayList<>();
        complexList.add(new Comment(100, "复杂描述包含特殊字符: !@#$%^&*()", CommentType.DI));
        complexList.add(new Comment(200, "多行描述\n第二行\n第三行", CommentType.DI));
        complexList.add(new Comment(300, "中文描述测试", CommentType.DI));
        
        // 保存到文件
        File tempFile = tempFolder.newFile("complex_test.json");
        String filePath = tempFile.getAbsolutePath();
        commentRepository.saveToJson(complexList, filePath);
        
        // 加载文件
        List<Comment> loadedList = commentRepository.loadFromLocalFile(filePath);
        
        // 验证完全一致
        assertEquals("列表大小应一致", complexList.size(), loadedList.size());
        for (int i = 0; i < complexList.size(); i++) {
            assertEquals("ID应一致", complexList.get(i).getId(), loadedList.get(i).getId());
            assertEquals("评论内容应一致", complexList.get(i).getComment(), loadedList.get(i).getComment());
            assertEquals("类型应一致", complexList.get(i).getType(), loadedList.get(i).getType());
        }
    }

    /**
     * 测试saveToJson方法的异常信息内容（使用ExpectedException规则）
     */
    @Test
    public void testSaveToJson_ExceptionMessage() throws Exception {
        // 设置期望的异常类型和消息
        expectedException.expect(InvalidParaException.class);
        expectedException.expectMessage("要保存的长文本列表为空");
        
        // 准备测试文件路径
        File tempFile = tempFolder.newFile("exception_test.json");
        String savePath = tempFile.getAbsolutePath();
        
        // 执行会抛出异常的操作
        commentRepository.saveToJson(new ArrayList<>(), savePath);
    }
}