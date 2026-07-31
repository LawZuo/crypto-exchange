package coin.exchange.module.mail.service;

import java.util.Map;

public interface EmailService {

    /**
     * 发送邮件 - 纯文本
     * @param to      接收者
     * @param subject 主题
     * @param content 内容
     */
    public void sendTextEmail(String to, String subject, String content);


    /**
     * 发送邮件 - HTML
     * @param to      接收者
     * @param subject 主题
     * @param variables { username, code } 内容
     */
    public void sendHtmlEmail(String to, String subject, Map<String, Object> variables);
}
