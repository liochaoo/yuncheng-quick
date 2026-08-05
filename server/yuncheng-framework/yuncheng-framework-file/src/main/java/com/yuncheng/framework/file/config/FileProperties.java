package com.yuncheng.framework.file.config;

import com.yuncheng.framework.file.constant.FilePolicyCodes;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/** 平台文件上传策略配置。 */
@ConfigurationProperties(prefix = "platform.file")
public class FileProperties {

    private Map<String, Policy> policies = defaultPolicies();

    public Map<String, Policy> getPolicies() {
        return policies;
    }

    public void setPolicies(Map<String, Policy> policies) {
        this.policies = policies;
    }

    private static Map<String, Policy> defaultPolicies() {
        Map<String, Policy> policies = new LinkedHashMap<>();
        policies.put(FilePolicyCodes.ATTACHMENT, new Policy(
                DataSize.ofMegabytes(20),
                setOf(
                        "pdf", "txt", "csv", "xls", "xlsx", "doc", "docx", "ppt", "pptx",
                        "zip", "rar", "7z", "jpg", "jpeg", "png", "gif", "webp", "bmp", "ico"
                )
        ));
        policies.put(FilePolicyCodes.IMAGE, new Policy(
                DataSize.ofMegabytes(10),
                setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "ico")
        ));
        policies.put(FilePolicyCodes.AVATAR, new Policy(
                DataSize.ofMegabytes(5),
                setOf("jpg", "jpeg", "png", "webp")
        ));
        policies.put(FilePolicyCodes.PUBLIC_IMAGE, new Policy(
                DataSize.ofMegabytes(10),
                setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "ico")
        ));
        return policies;
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... values) {
        Set<T> result = new LinkedHashSet<>();
        for (T value : values) {
            result.add(value);
        }
        return result;
    }

    /** 单项文件上传策略。 */
    public static class Policy {

        private DataSize maxSize = DataSize.ofMegabytes(20);
        private Set<String> allowedExtensions = new LinkedHashSet<>();
        public Policy() {
        }

        public Policy(
                DataSize maxSize,
                Set<String> allowedExtensions
        ) {
            this.maxSize = maxSize;
            this.allowedExtensions = allowedExtensions;
        }

        public DataSize getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(DataSize maxSize) {
            this.maxSize = maxSize;
        }

        public Set<String> getAllowedExtensions() {
            return allowedExtensions;
        }

        public void setAllowedExtensions(Set<String> allowedExtensions) {
            this.allowedExtensions = allowedExtensions;
        }

    }
}
