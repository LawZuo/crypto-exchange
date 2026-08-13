package coin.exchange.api.notification.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class NotificationMessageVo {

    private Long id;
    private String targetType;
    private String eventType;
    private String title;
    private String content;
    private Map<String, Object> payload;
    private Integer readStatus;
    private LocalDateTime createTime;
}
