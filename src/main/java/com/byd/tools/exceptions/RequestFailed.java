package com.byd.tools.exceptions;

/**
 * ClassName: RequestInvalid
 * Package: com.byd.tools.exceptions
 * Description:
 * Author: LiuKe
 * Create: 2025/4/17 23:41
 * Version 1.0
 */
public class RequestFailed extends RuntimeException {
    public RequestFailed(String message) {
        super(message);
    }
}
