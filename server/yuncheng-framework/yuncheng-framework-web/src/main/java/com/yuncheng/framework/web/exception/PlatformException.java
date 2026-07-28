package com.yuncheng.framework.web.exception;

import org.springframework.http.HttpStatus;

/** 平台可预期异常。 */
public class PlatformException extends RuntimeException {

    private final HttpStatus status;
    private final Long retryAfterSeconds;

    private PlatformException(HttpStatus status, String message) {
        this(status, message, null);
    }

    private PlatformException(HttpStatus status, String message, Long retryAfterSeconds) {
        super(message);
        this.status = status;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public static PlatformException badRequest(String message) {
        return new PlatformException(HttpStatus.BAD_REQUEST, message);
    }

    public static PlatformException unauthorized(String message) {
        return new PlatformException(HttpStatus.UNAUTHORIZED, message);
    }

    public static PlatformException forbidden(String message) {
        return new PlatformException(HttpStatus.FORBIDDEN, message);
    }

    public static PlatformException notFound(String message) {
        return new PlatformException(HttpStatus.NOT_FOUND, message);
    }

    public static PlatformException conflict(String message) {
        return new PlatformException(HttpStatus.CONFLICT, message);
    }

    public static PlatformException serviceUnavailable(String message) {
        return new PlatformException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    public static PlatformException tooManyRequests(String message, long retryAfterSeconds) {
        return new PlatformException(
                HttpStatus.TOO_MANY_REQUESTS,
                message,
                Math.max(1, retryAfterSeconds)
        );
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
