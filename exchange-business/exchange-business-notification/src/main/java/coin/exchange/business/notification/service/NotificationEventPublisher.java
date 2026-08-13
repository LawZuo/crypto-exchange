package coin.exchange.business.notification.service;

import coin.exchange.api.notification.model.NotificationEventDto;

public interface NotificationEventPublisher {

    void publish(NotificationEventDto event);
}
