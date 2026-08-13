package coin.exchange.business.notification.config;

import coin.exchange.business.notification.ws.NotificationHandshakeHandler;
import coin.exchange.business.notification.ws.NotificationHandshakeInterceptor;
import coin.exchange.business.notification.ws.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class NotificationWebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final NotificationHandshakeInterceptor notificationHandshakeInterceptor;
    private final NotificationHandshakeHandler notificationHandshakeHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("【exchange-business-notification】注册用户私有化WebSocket");
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .addInterceptors(notificationHandshakeInterceptor)
                .setHandshakeHandler(notificationHandshakeHandler)
                .setAllowedOrigins("*");
    }
}
