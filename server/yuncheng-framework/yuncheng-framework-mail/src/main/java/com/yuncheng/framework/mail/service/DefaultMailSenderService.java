package com.yuncheng.framework.mail.service;

import com.yuncheng.framework.mail.config.PlatformMailProperties;
import com.yuncheng.framework.mail.exception.MailDeliveryException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 使用 Spring Mail 发送邮件，开发环境可以仅记录邮件内容。 */
@Service
public class DefaultMailSenderService implements MailSenderService {

    private static final Logger log = LoggerFactory.getLogger(DefaultMailSenderService.class);

    private final JavaMailSender mailSender;
    private final PlatformMailProperties properties;

    public DefaultMailSenderService(
            JavaMailSender mailSender,
            PlatformMailProperties properties
    ) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public void sendHtml(String recipient, String subject, String htmlContent) {
        if (properties.isMockEnabled()) {
            log.info("模拟发送邮件：收件人={}，主题={}，内容={}", recipient, subject, htmlContent);
            return;
        }
        if (!StringUtils.hasText(properties.getFromAddress())) {
            throw new MailDeliveryException("发件邮箱账号尚未配置");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(properties.getFromAddress(), properties.getFromName());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception exception) {
            Throwable rootCause = rootCause(exception);
            throw new MailDeliveryException(
                    "邮件发送失败：" + rootCause.getClass().getSimpleName()
                            + " - " + rootCause.getMessage(),
                    exception
            );
        }
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
