package coin.exchange.business.notification.ws;

import coin.exchange.api.notification.model.NotificationMessageVo;
import coin.exchange.api.notification.model.NotificationWsMessageVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketPublisher {

    private final ObjectMapper objectMapper;
    private final NotificationWebSocketSessionRegistry sessionRegistry;

    public void sendConnected(WebSocketSession session) {
        send(session, Map.of("type", "connected", "sessionId", session.getId()));
    }

    public void publishToUser(Long userId, NotificationMessageVo message) {
        NotificationWsMessageVo wsMessage = toWsMessage(message);
        String payload = serialize(wsMessage);
        if (payload == null) {
            return;
        }
        sessionRegistry.findByUserId(userId).forEach(session -> send(session, payload));
    }

    public void publishToAll(NotificationMessageVo message) {
        NotificationWsMessageVo wsMessage = toWsMessage(message);
        String payload = serialize(wsMessage);
        if (payload == null) {
            return;
        }
        sessionRegistry.findAll().forEach(session -> send(session, payload));
    }

    private NotificationWsMessageVo toWsMessage(NotificationMessageVo message) {
        return NotificationWsMessageVo.builder()
                .type("notification")
                .eventType(message.getEventType())
                .timestamp(Instant.now().toEpochMilli())
                .data(message)
                .build();
    }

    private void send(WebSocketSession session, Object message) {
        String payload = serialize(message);
        if (payload != null) {
            send(session, payload);
        }
    }

    private String serialize(Object message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("【exchange-business-notification】序列化WebSocket通知失败", e);
            return null;
        }
    }

    private void send(WebSocketSession session, String payload) {
        if (!session.isOpen()) {
            sessionRegistry.remove(session.getId());
            return;
        }
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException | IllegalStateException e) {
            sessionRegistry.remove(session.getId());
            log.debug("【exchange-business-notification】发送WebSocket通知失败: sessionId={}", session.getId(), e);
        }
    }
}
