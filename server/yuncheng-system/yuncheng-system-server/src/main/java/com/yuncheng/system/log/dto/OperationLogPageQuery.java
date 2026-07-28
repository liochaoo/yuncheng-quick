package com.yuncheng.system.log.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 操作日志分页条件。 */
public class OperationLogPageQuery extends PageQuery {

    private String action;
    private String username;
    private String requestPath;
    private Boolean success;
    private String traceId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
