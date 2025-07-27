package com.byd.tools.data;

import com.byd.tools.pojo.CommentType;
import com.byd.tools.pojo.CommentURLPara;
import com.byd.tools.pojo.DigitalComment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ClassName: DataToURL
 * Package: com.byd.data
 * Description:
 * 用来将Di、Do中的Json数据转换成对应的URL
 * Author: LiuKe
 * Create: 2025/4/7 23:50
 * Version 1.0
 */
public class DataToURL {

    /**
     * 根据一个 生成一个DigitalComment。  生成的对象能够专注于从对象参数->url参数 部分，提供 更灵活、可拓展的的传递参数，解析参数方式。
     * @param digitalComment 依赖的长文本对象
     * @return 专注于提交时URL应包含的参数的长文本对象。
     */
    public CommentURLPara transferToPara(DigitalComment digitalComment) {
        int id = digitalComment.getId();
        String index = String.valueOf(id);
        String comment = digitalComment.getComment();
        CommentType commentType = digitalComment.getType();
        return new CommentURLPara(comment, index, commentType);
    }

    /**
     * 将一个系列DigitalComment对象，转换成一系列DigitalComment
     * @param digitalCommentList 依赖的长文本对象列表
     * @return 专注于提交时URL应包含的参数的长文本对象列表。
     */
    public List<CommentURLPara> transferToPara(List<DigitalComment> digitalCommentList) {
        return digitalCommentList.stream().map(this::transferToPara).toList();

    }


    private HashMap<String, String> diComment = new HashMap<>();
    private HashMap<String, String> doComment = new HashMap<>();

    public void loadData() {
        Gson gson = new Gson();
        try (
                FileReader fileReaderDi = new FileReader("Di.json");
                FileReader fileReaderDo = new FileReader("Do.json");
        ) {
            Type diType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Type doType = new TypeToken<HashMap<String, String>>() {
            }.getType();

            diComment = gson.fromJson(fileReaderDi, diType);
            doComment = gson.fromJson(fileReaderDo, doType);

            diComment.forEach((k, v) -> {
                System.out.println(k + "=" + v);
            });

            doComment.forEach((k, v) -> {
                System.out.println(k + "=" + v);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<String> getDiURLs(String robotIP, Integer begin, Integer end) throws UnsupportedEncodingException {
        //end代表最后一位信号点，如果其位null，这代表，至结尾
        if (end == null) {
            end = diComment.size();
        }
        if (end > diComment.size()) {
            throw new RuntimeException("IO信号点超过所能设置的最大限度了");
        }
        loadData();
        System.out.println("-------------");

        List<String> diURLs = new ArrayList<>();
        for (int i = begin - 1; i < end; i++) {
            String encodeComment = URLEncoder.encode(diComment.get(i + "") + "", "GBK");

            encodeComment = URLEncoder.encode("", "GBK");
            String URL = "http://" + robotIP + "/karel/ComSet?sComment=" + encodeComment + "&sIndx=" + (i + 1) + "&sFc=" + "8";
            diURLs.add(URL);
        }
        return diURLs;
    }

    public List<String> getDoURL(String robotIP, Integer begin, Integer end) throws UnsupportedEncodingException {
        //end代表最后一位信号点，如果其位null，这代表，至结尾
        if (end == null) {
            end = doComment.size();
        }
        if (end > doComment.size()) {
            throw new RuntimeException("IO信号点超过所能设置的最大限度了");
        }
        loadData();
        System.out.println("-------------");

        List<String> diURLs = new ArrayList<>();
        for (int i = begin - 1; i < diComment.size(); i++) {
            String encodeComment = URLEncoder.encode(diComment.get(i + "") + "", "GBK");
            String URL = "http://" + robotIP + "/karel/ComSet?sComment=" + encodeComment + "&sIndx=" + (i + 1) + "&sFc=" + "8";
            diURLs.add(URL);
        }
        return diURLs;
    }


    public static void main(String[] args) {
        new DataToURL().loadData();
    }
}
