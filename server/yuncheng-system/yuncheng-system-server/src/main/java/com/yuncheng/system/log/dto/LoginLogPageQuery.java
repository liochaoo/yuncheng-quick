package com.yuncheng.system.log.dto;

import com.yuncheng.framework.web.page.PageQuery;

/** 登录日志分页条件。 */
public class LoginLogPageQuery extends PageQuery {

    private String loginName;
    private String eventType;
    private Boolean success;
    private String clientType;
    private String traceId;

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
