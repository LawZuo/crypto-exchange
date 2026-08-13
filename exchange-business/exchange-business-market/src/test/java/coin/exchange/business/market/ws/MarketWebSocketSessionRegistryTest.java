package coin.exchange.business.market.ws;

import coin.exchange.api.market.model.MarketStreamMessageVo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarketWebSocketSessionRegistryTest {

    private final MarketWebSocketSessionRegistry registry = new MarketWebSocketSessionRegistry();

    @Test
    void shouldMatchSymbolTypeAndKlineInterval() {
        WebSocketSession session = session();
        registry.register(session);
        registry.subscribe(session.getId(), subscription());

        assertEquals(1, registry.findSubscribers(message("ticker", null)).size());
        assertEquals(1, registry.findSubscribers(message("kline", "1m")).size());
        assertTrue(registry.findSubscribers(message("trade", null)).isEmpty());
        assertTrue(registry.findSubscribers(message("kline", "5m")).isEmpty());
    }

    @Test
    void shouldKeepConnectionButStopMatchingAfterUnsubscribe() {
        WebSocketSession session = session();
        registry.register(session);
        registry.subscribe(session.getId(), subscription());

        registry.unsubscribe(session.getId());

        assertTrue(registry.findSubscribers(message("ticker", null)).isEmpty());
        assertEquals(session.getId(), registry.getSession(session.getId()).getId());
    }

    private WebSocketSession session() {
        return new TestWebSocketSession();
    }

    private MarketWsSubscription subscription() {
        MarketWsSubscription subscription = new MarketWsSubscription();
        subscription.setAction("subscribe");
        subscription.setSymbol("BTCUSDT");
        subscription.setInterval("1m");
        subscription.setTypes(List.of("ticker", "kline"));
        return subscription;
    }

    private MarketStreamMessageVo message(String type, String interval) {
        MarketStreamMessageVo message = new MarketStreamMessageVo();
        message.setType(type);
        message.setSymbol("BTCUSDT");
        message.setInterval(interval);
        return message;
    }

    private static final class TestWebSocketSession implements WebSocketSession {
        private final String id = UUID.randomUUID().toString();
        private final Map<String, Object> attributes = new HashMap<>();
        private boolean open = true;
        private int textMessageSizeLimit;
        private int binaryMessageSizeLimit;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public HttpHeaders getHandshakeHeaders() {
            return HttpHeaders.EMPTY;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Principal getPrincipal() {
            return null;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public String getAcceptedProtocol() {
            return null;
        }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) {
            textMessageSizeLimit = messageSizeLimit;
        }

        @Override
        public int getTextMessageSizeLimit() {
            return textMessageSizeLimit;
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
            binaryMessageSizeLimit = messageSizeLimit;
        }

        @Override
        public int getBinaryMessageSizeLimit() {
            return binaryMessageSizeLimit;
        }

        @Override
        public List<WebSocketExtension> getExtensions() {
            return List.of();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) {
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() throws IOException {
            open = false;
        }

        @Override
        public void close(CloseStatus status) throws IOException {
            open = false;
        }
    }
}
