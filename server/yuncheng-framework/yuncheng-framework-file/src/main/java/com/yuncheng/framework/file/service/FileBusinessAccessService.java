package com.yuncheng.framework.file.service;

import com.yuncheng.framework.web.exception.PlatformException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 通用 HTTP 文件接口对业务关联数据的访问校验。 */
@Service
public class FileBusinessAccessService {

    private final List<FileBusinessAccessHandler> handlers;

    public FileBusinessAccessService(List<FileBusinessAccessHandler> handlers) {
        this.handlers = List.copyOf(handlers);
    }

    public void requireRead(String businessType, Long businessId) {
        requireHandler(businessType).requireRead(businessId);
    }

    public void requireWrite(String businessType, Long businessId) {
        requireHandler(businessType).requireWrite(businessId);
    }

    private FileBusinessAccessHandler requireHandler(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            throw PlatformException.badRequest("文件业务关联信息不完整");
        }
        List<FileBusinessAccessHandler> matched = handlers.stream()
                .filter(handler -> businessType.trim().equals(handler.businessType()))
                .toList();
        if (matched.size() != 1) {
            throw PlatformException.forbidden("当前业务类型不允许通过通用文件接口操作");
        }
        return matched.getFirst();
    }
}
