package com.byd.tools.pojo;

/**
 * ClassName: Type
 * Package: com.byd.tools.pojo
 * Description:
 * 定义长文本信息的类型，比如DI代表数字输入信号、DO代表数字输出信号。
 * Author: LiuKe
 * Create: 2025/4/17 23:11
 * Version 1.0
 */


public enum CommentType {
    /**
     * DI代表数字输入信号、DO代表数字输出信号。
     */
    DI, DO;

    /**
     * 在将长文本写入到服务器时，需要根据输入还是输出信号，在url连接中添加上sFc参数。
     * 比如：写入长文本的连接格式:`<a href="http://192.168.0.1/karel/ComSet?sComment=aaa&sIndx=123&sFc=8">...</a>`
     * 该方法就是将Di和Do换算成对应的sFc参数。
     *
     * @return 最终返回的 sFc参数。
     */
}
