package com.byd.tools.parse;

import com.byd.tools.pojo.CommentType;

import java.util.Map;

/**
 * ClassName: WebParserFactory
 * Package: com.byd.tools.parse
 * Description:
 * Author: LiuKe
 * Create: 2025/10/3 01:13
 * Version 1.0
 */
public class WebParserFactory {
    private static final Map<CommentType, KarelWebParser> REGISTRY = Map.of(
            CommentType.DI, new DioWebParser(),
            CommentType.DO, new DioWebParser()
    );

    public static KarelWebParser of(CommentType type) {
        KarelWebParser parse = REGISTRY.get(type);
        if (parse == null) throw new IllegalArgumentException("Unknown type:" + type);
        return parse;
    }
}
