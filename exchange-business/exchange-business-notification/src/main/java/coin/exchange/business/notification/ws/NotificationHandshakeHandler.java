package coin.exchange.business.notification.ws;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class NotificationHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Object userId = attributes.get(NotificationHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (userId instanceof Long value) {
            return new NotificationPrincipal(value);
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
