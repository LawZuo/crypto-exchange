package coin.exchange.api.notification.model;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationEventDto implements Serializable {

    private String eventId;
    private String targetType;
    private Long userId;
    private String eventType;
    private String title;
    private String content;
    private Map<String, Object> payload;
    private LocalDateTime occurredAt;
}
