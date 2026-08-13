package coin.exchange.business.notification.service;

import coin.exchange.api.notification.model.NotificationEventDto;
import coin.exchange.api.notification.model.NotificationMessageVo;

import java.util.List;

public interface NotificationService {

    void handleEvent(NotificationEventDto event);

    List<NotificationMessageVo> listNotifications(Long userId, Integer readStatus, Integer pageNum, Integer pageSize);

    long countUnread(Long userId);

    void markRead(Long userId, String targetType, Long id);

    void markAllRead(Long userId);
}
