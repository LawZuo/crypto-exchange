package coin.exchange.business.notification.ws;

import java.security.Principal;

public record NotificationPrincipal(Long userId) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
