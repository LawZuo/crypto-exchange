package coin.exchange.resource.mail.service;

import java.util.Map;

public interface EmailService {

    void sendTextEmail(String to, String subject, String content);

    void sendHtmlEmail(String to, String subject, Map<String, Object> variables);
}
