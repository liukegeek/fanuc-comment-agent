package com.byd.tools.service;

import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.exceptions.JsonFileIOException;
import com.byd.tools.pojo.Comment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: FileOperator
 * Package: com.byd.tools.data
 * Description:
 * 数据存储访问类，封装Comment对象的本地存储和读取逻辑。
 * 本地存储: 将Comment对象列表保存到本地JSON文件中。
 * 本地读取: 从本地JSON文件中读取Comment对象列表。
 * Author: LiuKe
 * Create: 2025/4/20 19:13
 * Version 1.0
 */
public class CommentRepository {

    /**
     * 保存长文本列表到本地JSON文件, 本质上就是将JAVA对象转换成 Json风格的 字符串， 然后将字符串写入到文件当中。
     *
     * @param commentList 要保存的对象列表
     * @param savePath    保存路径
     * @throws JsonFileIOException 当 读写JSON 文件遇到异常时抛出。
     */
    public void saveToJson(List<Comment> commentList, String savePath) throws JsonFileIOException, InvalidParaException {
        if (commentList == null || commentList.isEmpty()) {
            throw new InvalidParaException("要保存的长文本列表为空");
        }

        // 如果直接如下生成gson对象，那么生成的String就「压缩格式」:即所有json对象挤在一行，无空格与换行符。无阅读体验，舍弃。
//        StringBuilder commentsJson = new StringBuilder();
//        int size = commentList.size();
//        commentsJson.append("[\n");
//        Gson gson = new Gson();
//        for (int i = 0; i < size; i++) {
//            commentsJson.append("\t");
//            commentsJson.append(gson.toJson(commentList.get(i)));
//            if (i<size-1){
//                commentsJson.append(",\n");
//            }
//        }
//        commentsJson.append("\n]");


        // 通过`setPrettyPrinting()` 方法可以自动添加上必要的空格与换行符，生成便于查看与编写的json格式。
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String commentsJson = gson.toJson(commentList);
        // 保存到指定的文件中。 默认文件编码为UTF_8;
        try (FileWriter fileWriter = new FileWriter(savePath, StandardCharsets.UTF_8)) {
            fileWriter.write(commentsJson);
        } catch (IOException e) {
            throw new JsonFileIOException("将长文本保存到本地JSON文件:" + savePath + "的过程中发生了错误");
        }
    }


    /**
     * 将几个Comment列表进行合并然后存储在一个文件中
     *
     * @param savePath     合并后的长文本列表要保存的文件路径
     * @param commentLists 要合并的Comment列表
     * @throws JsonFileIOException 当 读写JSON 文件遇到异常时抛出。
     */
    @SafeVarargs
    public final void mergeAndSave(String savePath, List<Comment>... commentLists) throws JsonFileIOException, InvalidParaException {
        if (commentLists == null || commentLists.length == 0) {
            throw new InvalidParaException("要合并的长文本列表为空");
        }
        List<Comment> mergedList = new ArrayList<>();
        for (List<Comment> commentList : commentLists) {
            if (commentList != null && !commentList.isEmpty()) {
                mergedList.addAll(commentList);
            }
        }
        saveToJson(mergedList, savePath);
    }

    /**
     * 从JSON文件中加载出Comment对象, 本质上就是将Json风格的 字符串 解析成JAVA对象。
     *
     * @param readPath 要读取的文件路径
     * @return 从文件中加载出的Comment对象列表
     * @throws JsonFileIOException 当 读写JSON 文件遇到异常时抛出。
     */
    public List<Comment> loadFromLocalFile(String readPath) throws JsonFileIOException {
        Gson gson = new Gson();
        Type type = new TypeToken<List<Comment>>() {
        }.getType();  //定义要 按照什么格式解析 Json文件流。 这里按照 List<Comment> 格式解析 json文件
        try (
                FileReader fileReader = new FileReader(readPath)  //获得文件的读取流
        ) {
            return gson.fromJson(fileReader, type);   //按照格式解析文件流，并转换成List<Comment>格式返回
        } catch (IOException e) {
            throw new JsonFileIOException("从本地JSON文件:" + readPath + "读取长文本的过程中发生了错误");
        }
    }

}
