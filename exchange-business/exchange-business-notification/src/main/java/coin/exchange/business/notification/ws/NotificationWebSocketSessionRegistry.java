package coin.exchange.business.notification.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketSessionRegistry {

    private static final int SEND_TIME_LIMIT_MS = 5_000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 512 * 1024;

    private final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(WebSocketSession session) {
        Long userId = resolveUserId(session);
        WebSocketSession concurrentSession = new ConcurrentWebSocketSessionDecorator(
                session, SEND_TIME_LIMIT_MS, BUFFER_SIZE_LIMIT_BYTES);
        sessions.put(session.getId(), new ClientSession(userId, concurrentSession));
        userSessions.computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .put(session.getId(), concurrentSession);
    }

    public void remove(String sessionId) {
        ClientSession clientSession = sessions.remove(sessionId);
        if (clientSession == null) {
            return;
        }
        Map<String, WebSocketSession> userSessionMap = userSessions.get(clientSession.userId());
        if (userSessionMap != null) {
            userSessionMap.remove(sessionId);
            if (userSessionMap.isEmpty()) {
                userSessions.remove(clientSession.userId());
            }
        }
    }

    public List<WebSocketSession> findByUserId(Long userId) {
        Map<String, WebSocketSession> userSessionMap = userSessions.get(userId);
        if (userSessionMap == null) {
            return List.of();
        }
        return userSessionMap.values().stream()
                .filter(WebSocketSession::isOpen)
                .toList();
    }

    public List<WebSocketSession> findAll() {
        return sessions.values().stream()
                .map(ClientSession::session)
                .filter(WebSocketSession::isOpen)
                .toList();
    }

    private Long resolveUserId(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof NotificationPrincipal notificationPrincipal) {
            return notificationPrincipal.userId();
        }
        Object userId = session.getAttributes().get(NotificationHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (userId instanceof Long value) {
            return value;
        }
        throw new IllegalStateException("WebSocket用户身份不存在");
    }

    private record ClientSession(Long userId, WebSocketSession session) {
    }
}
