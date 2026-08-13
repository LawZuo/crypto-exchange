package coin.exchange.business.notification.ws;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationWebSocketSessionRegistryTest {

    private final NotificationWebSocketSessionRegistry registry = new NotificationWebSocketSessionRegistry();

    @Test
    void shouldKeepAllSessionsForSameUser() {
        WebSocketSession first = session(1L);
        WebSocketSession second = session(1L);
        WebSocketSession other = session(2L);

        registry.register(first);
        registry.register(second);
        registry.register(other);

        assertThat(registry.findByUserId(1L)).hasSize(2);
        assertThat(registry.findByUserId(2L)).hasSize(1);
        assertThat(registry.findAll()).hasSize(3);
    }

    @Test
    void shouldRemoveClosedSessionOnly() {
        WebSocketSession first = session(1L);
        WebSocketSession second = session(1L);
        registry.register(first);
        registry.register(second);

        registry.remove(first.getId());

        assertThat(registry.findByUserId(1L)).hasSize(1);
        assertThat(registry.findByUserId(1L).get(0).getId()).isEqualTo(second.getId());
    }

    private WebSocketSession session(Long userId) {
        return new TestWebSocketSession(userId);
    }

    private static final class TestWebSocketSession implements WebSocketSession {

        private final String id = UUID.randomUUID().toString();
        private final Principal principal;
        private boolean open = true;

        private TestWebSocketSession(Long userId) {
            this.principal = new NotificationPrincipal(userId);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public URI getUri() {
            return URI.create("ws://localhost:8084/ws/notification");
        }

        @Override
        public HttpHeaders getHandshakeHeaders() {
            return new HttpHeaders();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return Map.of();
        }

        @Override
        public Principal getPrincipal() {
            return principal;
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
        }

        @Override
        public int getTextMessageSizeLimit() {
            return 0;
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
        }

        @Override
        public int getBinaryMessageSizeLimit() {
            return 0;
        }

        @Override
        public List<WebSocketExtension> getExtensions() {
            return List.of();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) throws IOException {
            if (!(message instanceof TextMessage)) {
                throw new IOException("Only text messages are supported");
            }
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }

        @Override
        public void close(CloseStatus status) {
            open = false;
        }
    }
}
