package com.yuncheng.framework.file.service;

import com.yuncheng.framework.file.config.FileProperties;
import com.yuncheng.framework.file.constant.FilePolicyCodes;
import com.yuncheng.framework.file.enums.FileAccessType;
import com.yuncheng.framework.web.exception.PlatformException;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 文件上传策略校验。 */
@Service
public class FilePolicyService {

    private final FileProperties properties;

    public FilePolicyService(FileProperties properties) {
        this.properties = properties;
    }

    public FileProperties.Policy requirePolicy(String policyCode) {
        String normalizedCode = normalizePolicyCode(policyCode);
        FileProperties.Policy policy = properties.getPolicies().get(normalizedCode);
        if (policy == null) {
            throw PlatformException.badRequest("文件上传策略不存在");
        }
        return policy;
    }

    public String normalizePolicyCode(String policyCode) {
        return StringUtils.hasText(policyCode)
                ? policyCode.trim().toLowerCase(Locale.ROOT)
                : FilePolicyCodes.ATTACHMENT;
    }

    /**
     * 文件访问类型属于业务安全规则，不由部署配置动态改变。
     * 未明确声明为公开的策略一律按私有文件处理。
     */
    public FileAccessType resolveAccessType(String policyCode) {
        String normalizedCode = normalizePolicyCode(policyCode);
        return FilePolicyCodes.AVATAR.equals(normalizedCode)
                || FilePolicyCodes.PUBLIC_IMAGE.equals(normalizedCode)
                ? FileAccessType.PUBLIC
                : FileAccessType.PRIVATE;
    }

    public void validateBeforeUpload(
            MultipartFile file,
            String extension,
            FileProperties.Policy policy
    ) {
        if (file == null || file.isEmpty()) {
            throw PlatformException.badRequest("请选择需要上传的文件");
        }
        if (file.getSize() > policy.getMaxSize().toBytes()) {
            throw PlatformException.badRequest(
                    "文件大小不能超过" + policy.getMaxSize().toMegabytes() + "MB"
            );
        }
        if (!StringUtils.hasText(extension)
                || !containsIgnoreCase(policy.getAllowedExtensions(), extension)) {
            throw PlatformException.badRequest("不支持上传该类型的文件");
        }
    }

    private boolean containsIgnoreCase(Set<String> values, String value) {
        return values.stream().anyMatch(item -> item.equalsIgnoreCase(value));
    }
}
