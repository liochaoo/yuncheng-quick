package com.yuncheng.framework.mail.service;

/** 通用邮件发送能力。 */
public interface MailSenderService {

    void sendHtml(String recipient, String subject, String htmlContent);
}
