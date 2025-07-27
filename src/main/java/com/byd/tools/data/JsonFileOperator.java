package com.byd.tools.data;

import com.byd.tools.exceptions.JsonFileIOException;
import com.byd.tools.pojo.DigitalComment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ClassName: FileOperator
 * Package: com.byd.tools.data
 * Description:
 * Author: LiuKe
 * Create: 2025/4/20 19:13
 * Version 1.0
 */
public class JsonFileOperator {
    public void saveToJson(List<DigitalComment> commentList, String savePath) {
        // 如果直接如下生成gson对象，那么生成的String就是默认压缩格式，即所有json对象挤在一行，无空格与换行符。无阅读体验，舍弃。
        // Gson gson = new Gson();

        /*
         * 拼串来实现，一个JSON一行。 但由于 注释长度不同，导致上下Json对象之间，属性不对齐。 阅读体验一般，故而舍弃。
         */
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

        // 通过下面的方式，可以自动添加上必要的空格与换行符，生成便于查看与编写的json格式。
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String commentsJson = gson.toJson(commentList);

        // 保存到指定的文件中。 默认文件编码为UTF_8;
        try (FileWriter fileWriter = new FileWriter(savePath, StandardCharsets.UTF_8)) {
            fileWriter.write(commentsJson);
        } catch (IOException e) {
            throw new JsonFileIOException("将长文本保存到本地JSON文件:" + savePath + "的过程中发生了错误");
        }
    }

    public List<DigitalComment> loadFromJson(String readPath){
        List<DigitalComment> commentList;
        Gson gson = new Gson();
        Type type = new TypeToken<List<DigitalComment>>() {
        }.getType();

        try (
                FileReader fileReader = new FileReader(readPath);
        ) {
            commentList = gson.fromJson(fileReader, type);
            return commentList;
        } catch (IOException e) {
            throw new JsonFileIOException("从本地JSON文件:" + readPath + "读取长文本的过程中发生了错误");
        }
    }
}
