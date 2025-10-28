package com.byd.tools.parse;

import com.byd.tools.exceptions.InvalidParaException;
import com.byd.tools.parse.impl.*;
import com.byd.tools.pojo.CommentType;

import java.util.HashMap;
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

    private static final Map<CommentType, KarelWebParser> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(CommentType.NUM_REGISTER_COMMENT, new NumRegisterCommentWebParser());
        REGISTRY.put(CommentType.NUM_REGISTER_VALUE, new NumRegisterValueWebParser());
        REGISTRY.put(CommentType.POSITION_REGISTER, new PositionRegisterWebParser());
        REGISTRY.put(CommentType.STRING_REGISTER_COMMENT, new StrRegisterCommentWebParser());
        REGISTRY.put(CommentType.STRING_REGISTER_VALUE, new StrRegisterValueWebParser());
        REGISTRY.put(CommentType.RI, new RiWebParser());
        REGISTRY.put(CommentType.RO, new RoWebParser());
        REGISTRY.put(CommentType.DI, new DiWebParser());
        REGISTRY.put(CommentType.DO, new DoWebParser());
        REGISTRY.put(CommentType.GI, new GiWebParser());
        REGISTRY.put(CommentType.GO, new GoWebParser());
        REGISTRY.put(CommentType.AI, new AiWebParser());
        REGISTRY.put(CommentType.AO, new AoWebParser());
        REGISTRY.put(CommentType.FLAG, new FlagWebParser());
    }

    public static KarelWebParser of(CommentType type) throws InvalidParaException {
        KarelWebParser parse = REGISTRY.get(type);
        if (parse == null) throw new InvalidParaException("Unknown type:" + type);
        return parse;
    }
}
