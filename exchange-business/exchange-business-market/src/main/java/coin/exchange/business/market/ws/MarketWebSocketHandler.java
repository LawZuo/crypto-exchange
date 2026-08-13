package coin.exchange.business.market.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketWebSocketHandler extends TextWebSocketHandler {

    private static final List<String> DEFAULT_TYPES = List.of("ticker", "depth", "trade", "kline");
    private static final Set<String> SUPPORTED_TYPES = Set.copyOf(DEFAULT_TYPES);

    private final ObjectMapper objectMapper;
    private final MarketWebSocketSessionRegistry sessionRegistry;
    private final MarketWebSocketPublisher publisher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("【exchange-business-market】WS连接成功: sessionId={}", session.getId());
        sessionRegistry.register(session);
        publisher.sendConnected(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            log.info("【exchange-business-market】WS订阅消息: sessionId={}, payload={}", session.getId(), message.getPayload());
            MarketWsSubscription subscription = parseSubscription(message.getPayload());
            if ("unsubscribe".equals(subscription.getAction())) {
                sessionRegistry.unsubscribe(session.getId());
                publisher.sendUnsubscribed(session.getId());
                return;
            }

            sessionRegistry.subscribe(session.getId(), subscription);
            publisher.sendSubscribed(session.getId(), subscription);
        } catch (Exception e) {
            log.debug("【exchange-business-market】WS订阅消息处理失败: sessionId={}, payload={}", session.getId(), message.getPayload(), e);
            publisher.sendError(session.getId(), e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.debug("【exchange-business-market】WS连接关闭: sessionId={}, status={}", session.getId(), status);
        sessionRegistry.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.debug("【exchange-business-market】WS连接异常: sessionId={}", session.getId(), exception);
        sessionRegistry.remove(session.getId());
    }

    private MarketWsSubscription parseSubscription(String payload) throws Exception {
        MarketWsSubscription subscription = objectMapper.readValue(payload, MarketWsSubscription.class);
        String action = normalizeAction(subscription.getAction());
        subscription.setAction(action);
        if ("unsubscribe".equals(action)) {
            return subscription;
        }

        if (subscription.getSymbol() == null || subscription.getSymbol().isBlank()) {
            throw new IllegalArgumentException("symbol不能为空");
        }
        subscription.setSymbol(subscription.getSymbol().trim().toUpperCase(Locale.ROOT));
        subscription.setInterval(normalizeInterval(subscription.getInterval()));
        subscription.setTypes(normalizeTypes(subscription.getTypes()));
        return subscription;
    }

    private String normalizeAction(String action) {
        String normalized = action == null ? "subscribe" : action.trim().toLowerCase(Locale.ROOT);
        if (!"subscribe".equals(normalized) && !"unsubscribe".equals(normalized)) {
            throw new IllegalArgumentException("action只支持subscribe或unsubscribe");
        }
        return normalized;
    }

    private String normalizeInterval(String interval) {
        return interval == null || interval.isBlank() ? "1m" : interval.trim();
    }

    private List<String> normalizeTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return DEFAULT_TYPES;
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String type : types) {
            String value = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
            if (!SUPPORTED_TYPES.contains(value)) {
                throw new IllegalArgumentException("不支持的行情类型: " + type);
            }
            normalized.add(value);
        }
        return List.copyOf(normalized);
    }
}
