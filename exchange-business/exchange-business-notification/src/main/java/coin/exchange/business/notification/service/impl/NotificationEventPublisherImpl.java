package coin.exchange.business.notification.service.impl;

import coin.exchange.api.notification.model.NotificationEventDto;
import coin.exchange.common.core.constant.MqConstants;
import coin.exchange.common.rabbitmq.service.MqMessageService;
import coin.exchange.business.notification.service.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationEventPublisherImpl implements NotificationEventPublisher {

    private final MqMessageService mqMessageService;

    @Override
    public void publish(NotificationEventDto event) {
        if (event == null || !StringUtils.hasText(event.getEventId())) {
            throw new IllegalArgumentException("通知事件eventId不能为空");
        }
        mqMessageService.send(
                MqConstants.NOTIFICATION_EXCHANGE,
                MqConstants.NOTIFICATION_EVENT_ROUTING_KEY,
                MqConstants.NOTIFICATION_BIZ_TYPE,
                event
        );
    }
}
