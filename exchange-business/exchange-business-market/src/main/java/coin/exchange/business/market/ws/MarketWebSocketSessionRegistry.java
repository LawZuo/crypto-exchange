package coin.exchange.business.market.ws;

import coin.exchange.api.market.model.MarketStreamMessageVo;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketWebSocketSessionRegistry {

    private static final int SEND_TIME_LIMIT_MS = 5_000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 512 * 1024;

    private final Map<String, ClientSession> clients = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        WebSocketSession concurrentSession = new ConcurrentWebSocketSessionDecorator(
                session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT_BYTES);
        clients.put(session.getId(), new ClientSession(concurrentSession));
    }

    public void subscribe(String sessionId, MarketWsSubscription subscription) {
        ClientSession client = clients.get(sessionId);
        if (client == null) {
            throw new IllegalStateException("WebSocket连接不存在");
        }
        client.subscription = subscription;
    }

    public void unsubscribe(String sessionId) {
        ClientSession client = clients.get(sessionId);
        if (client != null) {
            client.subscription = null;
        }
    }

    public void remove(String sessionId) {
        clients.remove(sessionId);
    }

    public WebSocketSession getSession(String sessionId) {
        ClientSession client = clients.get(sessionId);
        return client == null ? null : client.session;
    }

    public List<WebSocketSession> findSubscribers(MarketStreamMessageVo message) {
        if (message == null || message.getSymbol() == null || message.getType() == null) {
            return List.of();
        }
        return clients.values().stream()
                .filter(client -> client.session.isOpen())
                .filter(client -> matches(client.subscription, message))
                .map(client -> client.session)
                .toList();
    }

    private boolean matches(MarketWsSubscription subscription, MarketStreamMessageVo message) {
        if (subscription == null
                || !subscription.getSymbol().equalsIgnoreCase(message.getSymbol())
                || !subscription.getTypes().contains(message.getType().toLowerCase(Locale.ROOT))) {
            return false;
        }
        return !"kline".equalsIgnoreCase(message.getType())
                || subscription.getInterval().equalsIgnoreCase(message.getInterval());
    }

    private static final class ClientSession {
        private final WebSocketSession session;
        private volatile MarketWsSubscription subscription;

        private ClientSession(WebSocketSession session) {
            this.session = session;
        }
    }
}
