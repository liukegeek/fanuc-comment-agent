package com.byd.tools.exceptions;

/**
 * ClassName: InvalidParaException
 * Package: com.byd.tools.exceptions
 * Description:
 * Author: LiuKe
 * Create: 2025/8/9 17:44
 * Version 1.0
 */
public class InvalidParaException extends Exception {
    public InvalidParaException(String message) {
        super(message);
    }

    public InvalidParaException(String message, Throwable cause) {
        super(message, cause);
    }
}
