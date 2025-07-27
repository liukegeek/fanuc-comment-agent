package com.byd.tools.pojo;

/**
 * ClassName: ServiceResponseInfo
 * Package: com.byd.tools.pojo
 * Description:
 * Author: LiuKe
 * Create: 2025/5/12 06:03
 * Version 1.0
 */
public final class ServiceResponseInfo {
    private String serviceName;     //服务的名称
    private String responseInfo;    //服务执行完反馈的信息
    private Exception exceptionInfo;  //如果发生故障，那么存储该故障。
    private boolean hasException = false;  //执行过程中是否发生故障

    public ServiceResponseInfo(String serviceName, String responseInfo) {
        this.serviceName = serviceName;
        this.responseInfo = responseInfo;
    }

    public ServiceResponseInfo(String serviceName, String responseInfo, Exception exceptionInfo) {
        this.serviceName = serviceName;
        this.responseInfo = responseInfo;
        this.exceptionInfo = exceptionInfo;
        if (exceptionInfo != null) {
            hasException = true;
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getResponseInfo() {
        return responseInfo;
    }

    public void setResponseInfo(String responseInfo) {
        this.responseInfo = responseInfo;
    }

    public boolean isHasException() {
        return hasException;
    }

    public Exception getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(Exception exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }

    @Override
    public String toString() {
        return "ServiceResponseInfo{" +
               "serviceName='" + serviceName + '\'' +
               ", responseInfo='" + responseInfo + '\'' +
               ", exceptionInfo=" + exceptionInfo +
               ", hasException=" + hasException +
               '}';
    }
}
