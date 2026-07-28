package com.yuncheng.framework.mybatis.web;

import com.yuncheng.framework.web.response.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 将常见数据库约束异常转换为稳定的 HTTP 响应。 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataAccessExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateKey() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("数据已存在"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.failure("数据存在引用关系或不符合约束"));
    }
}
