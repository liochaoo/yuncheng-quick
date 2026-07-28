package com.yuncheng.framework.log.aop;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.lang.reflect.Array;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

/** 将操作入参转换为可控、脱敏的日志内容。 */
@Component
public class OperationParameterSanitizer {

    private static final int MAX_TEXT_LENGTH = 8192;
    private static final int MAX_COLLECTION_SIZE = 100;
    private static final String MASKED_VALUE = "******";
    private static final Set<String> EXACT_SENSITIVE_NAMES = Set.of(
            "code",
            "emailcode",
            "smscode",
            "verificationcode",
            "securitycode",
            "otp",
            "totp"
    );

    private final JsonMapper jsonMapper;

    public OperationParameterSanitizer(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public String serialize(String[] parameterNames, Object[] arguments) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int index = 0; index < arguments.length; index++) {
            Object argument = arguments[index];
            if (argument instanceof ServletRequest || argument instanceof ServletResponse) {
                continue;
            }
            String name = parameterNames != null && index < parameterNames.length
                    ? parameterNames[index]
                    : "arg" + index;
            result.put(name, sanitize(name, argument));
        }
        try {
            return truncate(jsonMapper.writeValueAsString(result));
        } catch (RuntimeException exception) {
            return "{\"记录状态\":\"参数序列化失败\"}";
        }
    }

    private Object sanitize(String name, Object value) {
        if (isSensitiveName(name)) {
            return MASKED_VALUE;
        }
        if (value == null
                || value instanceof Number
                || value instanceof Boolean
                || value instanceof Enum<?>
                || value instanceof TemporalAccessor) {
            return value;
        }
        if (value instanceof CharSequence sequence) {
            return truncate(sequence.toString());
        }
        if (value instanceof MultipartFile file) {
            return Map.of(
                    "fileName", file.getOriginalFilename() == null ? "" : file.getOriginalFilename(),
                    "contentType", file.getContentType() == null ? "" : file.getContentType(),
                    "size", file.getSize()
            );
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            int count = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (count++ >= MAX_COLLECTION_SIZE) {
                    break;
                }
                String key = String.valueOf(entry.getKey());
                sanitized.put(key, sanitize(key, entry.getValue()));
            }
            return sanitized;
        }
        if (value instanceof Collection<?> collection) {
            List<Object> sanitized = new ArrayList<>();
            int count = 0;
            for (Object item : collection) {
                if (count++ >= MAX_COLLECTION_SIZE) {
                    break;
                }
                sanitized.add(sanitize("item", item));
            }
            return sanitized;
        }
        if (value.getClass().isArray()) {
            List<Object> sanitized = new ArrayList<>();
            int length = Math.min(Array.getLength(value), MAX_COLLECTION_SIZE);
            for (int index = 0; index < length; index++) {
                sanitized.add(sanitize("item", Array.get(value, index)));
            }
            return sanitized;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> converted = jsonMapper.convertValue(value, Map.class);
            return sanitize("object", converted);
        } catch (RuntimeException exception) {
            return value.getClass().getSimpleName();
        }
    }

    private boolean isSensitiveName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.toLowerCase(Locale.ROOT)
                .chars()
                .filter(Character::isLetterOrDigit)
                .collect(
                        StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append
                )
                .toString();
        if (EXACT_SENSITIVE_NAMES.contains(normalized)) {
            return true;
        }
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.endsWith("pwd")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("authorization")
                || normalized.contains("cookie")
                || normalized.contains("captcha")
                || normalized.contains("verification")
                || normalized.contains("credential")
                || normalized.endsWith("accesskey")
                || normalized.endsWith("privatekey")
                || normalized.endsWith("apikey");
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LENGTH) + "…";
    }
}
