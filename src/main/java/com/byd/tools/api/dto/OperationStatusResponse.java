package com.byd.tools.api.dto;

/**
 * 通用操作结果响应体。
 */
public record OperationStatusResponse(boolean success, String message) {
    public static OperationStatusResponse ok() {
        return new OperationStatusResponse(true, null);
    }

    public static OperationStatusResponse ok(String message) {
        return new OperationStatusResponse(true, message);
    }

    public static OperationStatusResponse failed(String message) {
        return new OperationStatusResponse(false, message);
    }
}
