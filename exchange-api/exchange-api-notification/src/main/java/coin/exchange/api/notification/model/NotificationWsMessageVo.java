package coin.exchange.api.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationWsMessageVo {

    private String type;
    private String eventType;
    private Long timestamp;
    private Object data;
}
