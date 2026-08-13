package coin.exchange.business.market.ws;

import coin.exchange.api.market.model.MarketCacheSnapshotVo;
import coin.exchange.api.market.model.MarketStreamMessageVo;
import coin.exchange.business.market.service.MarketCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketWebSocketPublisher {

    private final ObjectMapper objectMapper;
    private final MarketCacheService marketCacheService;
    private final MarketWebSocketSessionRegistry sessionRegistry;

    public void sendConnected(String sessionId) {
        send(sessionId, Map.of("type", "connected", "sessionId", sessionId));
    }

    public void sendSubscribed(String sessionId, MarketWsSubscription subscription) {
        send(sessionId, Map.of(
                "type", "subscribed",
                "symbol", subscription.getSymbol(),
                "interval", subscription.getInterval(),
                "types", subscription.getTypes()
        ));
        sendInitialSnapshot(sessionId, subscription);
    }

    public void sendUnsubscribed(String sessionId) {
        send(sessionId, Map.of("type", "unsubscribed"));
    }

    public void sendError(String sessionId, String message) {
        send(sessionId, Map.of("type", "error", "message", message == null ? "请求处理失败" : message));
    }

    public void publish(MarketStreamMessageVo marketData) {
        Map<String, Object> response = response("market", marketData.getTimestamp(), marketData);
        String payload = serialize(response);
        if (payload == null) {
            return;
        }
        sessionRegistry.findSubscribers(marketData).forEach(session -> send(session, payload));
    }

    private void sendInitialSnapshot(String sessionId, MarketWsSubscription subscription) {
        try {
            MarketCacheSnapshotVo snapshot = marketCacheService.getSnapshot(
                    subscription.getSymbol(), subscription.getInterval(), subscription.getTypes());
            send(sessionId, response("snapshot", Instant.now().toEpochMilli(), snapshot));
        } catch (Exception e) {
            log.warn("【exchange-business-market】读取行情初始快照失败: sessionId={}, symbol={}", sessionId, subscription.getSymbol(), e);
            sendError(sessionId, "行情快照暂不可用，后续实时数据不受影响");
        }
    }

    private Map<String, Object> response(String type, Long timestamp, Object data) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", type);
        response.put("timestamp", timestamp == null ? Instant.now().toEpochMilli() : timestamp);
        response.put("data", data);
        return response;
    }

    private void send(String sessionId, Object response) {
        WebSocketSession session = sessionRegistry.getSession(sessionId);
        if (session == null) {
            return;
        }
        String payload = serialize(response);
        if (payload != null) {
            send(session, payload);
        }
    }

    private String serialize(Object response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("【exchange-business-market】序列化WebSocket消息失败", e);
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
            log.debug("【exchange-business-market】发送WebSocket消息失败: sessionId={}", session.getId(), e);
        }
    }
}
