package com.yuncheng.system.log.dto;

import com.yuncheng.framework.web.page.PageQuery;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;

/** 操作日志分页条件。 */
public class OperationLogPageQuery extends PageQuery {

    private String action;
    private String username;
    private String requestPath;
    private Boolean success;
    private String traceId;
    @PositiveOrZero(message = "最低执行耗时不能小于 0")
    private Long minDurationMillis;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant occurredAtStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant occurredAtEnd;

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

    public Long getMinDurationMillis() {
        return minDurationMillis;
    }

    public void setMinDurationMillis(Long minDurationMillis) {
        this.minDurationMillis = minDurationMillis;
    }

    public Instant getOccurredAtStart() {
        return occurredAtStart;
    }

    public void setOccurredAtStart(Instant occurredAtStart) {
        this.occurredAtStart = occurredAtStart;
    }

    public Instant getOccurredAtEnd() {
        return occurredAtEnd;
    }

    public void setOccurredAtEnd(Instant occurredAtEnd) {
        this.occurredAtEnd = occurredAtEnd;
    }
}
