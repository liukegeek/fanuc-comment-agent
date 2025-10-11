package com.byd.tools.parse;

import com.byd.tools.exceptions.InvalidParaException;
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
            CommentType.DI, new DiWebParser(),
            CommentType.DO, new DoWebParser()
    );

    public static KarelWebParser of(CommentType type) throws InvalidParaException {
        KarelWebParser parse = REGISTRY.get(type);
        if (parse == null) throw new InvalidParaException("Unknown type:" + type);
        return parse;
    }
}
