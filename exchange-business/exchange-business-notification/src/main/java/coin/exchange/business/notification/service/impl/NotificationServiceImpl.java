package coin.exchange.business.notification.service.impl;

import coin.exchange.api.notification.model.NotificationEventDto;
import coin.exchange.api.notification.model.NotificationMessageVo;
import coin.exchange.api.notification.model.NotificationTargetType;
import coin.exchange.business.notification.domain.NotificationAnnouncementDo;
import coin.exchange.business.notification.domain.NotificationMessageDo;
import coin.exchange.business.notification.mapper.NotificationAnnouncementMapper;
import coin.exchange.business.notification.mapper.NotificationMessageMapper;
import coin.exchange.business.notification.mapper.NotificationReadMapper;
import coin.exchange.business.notification.service.NotificationService;
import coin.exchange.business.notification.ws.NotificationWebSocketPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationMessageMapper messageMapper;
    private final NotificationAnnouncementMapper announcementMapper;
    private final NotificationReadMapper readMapper;
    private final NotificationWebSocketPublisher webSocketPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleEvent(NotificationEventDto event) {
        validateEvent(event);
        if (NotificationTargetType.USER.equalsIgnoreCase(event.getTargetType())) {
            handleUserEvent(event);
            return;
        }
        handleAnnouncementEvent(event);
    }

    @Override
    public List<NotificationMessageVo> listNotifications(Long userId, Integer readStatus, Integer pageNum, Integer pageSize) {
        requireUserId(userId);
        Integer normalizedReadStatus = normalizeReadStatus(readStatus);
        int normalizedPageNum = normalizePageNum(pageNum);
        int normalizedPageSize = normalizePageSize(pageSize);
        int offset = (normalizedPageNum - 1) * normalizedPageSize;

        List<NotificationMessageVo> messages = messageMapper
                .listByUser(userId, normalizedReadStatus, offset, normalizedPageSize)
                .stream()
                .map(this::toMessageVo)
                .toList();

        Set<Long> readAnnouncementIds = readMapper.listReadTargetIds(userId, NotificationTargetType.ALL);
        List<NotificationMessageVo> announcements = announcementMapper
                .listForUser(userId, normalizedReadStatus, offset, normalizedPageSize)
                .stream()
                .map(announcement -> toAnnouncementVo(announcement, readAnnouncementIds))
                .toList();

        return java.util.stream.Stream.concat(messages.stream(), announcements.stream())
                .sorted(Comparator.comparing(NotificationMessageVo::getCreateTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(normalizedPageSize)
                .toList();
    }

    @Override
    public long countUnread(Long userId) {
        requireUserId(userId);
        return messageMapper.countUnread(userId) + announcementMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, String targetType, Long id) {
        requireUserId(userId);
        if (id == null) {
            throw new IllegalArgumentException("通知ID不能为空");
        }
        String normalizedTargetType = normalizeTargetType(targetType);
        LocalDateTime now = LocalDateTime.now();
        if (NotificationTargetType.USER.equals(normalizedTargetType)) {
            messageMapper.markRead(userId, id, now);
            return;
        }
        readMapper.insertIgnore(userId, NotificationTargetType.ALL, id, now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        requireUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        messageMapper.markAllRead(userId, now);
        announcementMapper.listForUser(userId, 0, 0, MAX_PAGE_SIZE)
                .forEach(announcement -> readMapper.insertIgnore(userId, NotificationTargetType.ALL, announcement.getId(), now));
    }

    private void handleUserEvent(NotificationEventDto event) {
        if (messageMapper.getByEventId(event.getEventId()) != null) {
            return;
        }
        NotificationMessageDo message = new NotificationMessageDo();
        message.setEventId(event.getEventId());
        message.setUserId(event.getUserId());
        message.setEventType(event.getEventType());
        message.setTitle(event.getTitle());
        message.setContent(event.getContent());
        message.setPayload(writePayload(event.getPayload()));
        message.setReadStatus(0);
        message.setOccurredAt(event.getOccurredAt());
        messageMapper.insert(message);
        webSocketPublisher.publishToUser(event.getUserId(), toMessageVo(message));
    }

    private void handleAnnouncementEvent(NotificationEventDto event) {
        if (announcementMapper.getByEventId(event.getEventId()) != null) {
            return;
        }
        NotificationAnnouncementDo announcement = new NotificationAnnouncementDo();
        announcement.setEventId(event.getEventId());
        announcement.setEventType(event.getEventType());
        announcement.setTitle(event.getTitle());
        announcement.setContent(event.getContent());
        announcement.setPayload(writePayload(event.getPayload()));
        announcement.setOccurredAt(event.getOccurredAt());
        announcementMapper.insert(announcement);
        webSocketPublisher.publishToAll(toAnnouncementVo(announcement, Set.of()));
    }

    private void validateEvent(NotificationEventDto event) {
        if (event == null) {
            throw new IllegalArgumentException("通知事件不能为空");
        }
        if (!StringUtils.hasText(event.getEventId())) {
            throw new IllegalArgumentException("eventId不能为空");
        }
        event.setTargetType(normalizeTargetType(event.getTargetType()));
        if (NotificationTargetType.USER.equals(event.getTargetType()) && event.getUserId() == null) {
            throw new IllegalArgumentException("用户通知userId不能为空");
        }
        if (!StringUtils.hasText(event.getEventType())) {
            throw new IllegalArgumentException("eventType不能为空");
        }
        if (!StringUtils.hasText(event.getTitle())) {
            throw new IllegalArgumentException("title不能为空");
        }
        if (event.getOccurredAt() == null) {
            event.setOccurredAt(LocalDateTime.now());
        }
    }

    private String normalizeTargetType(String targetType) {
        if (NotificationTargetType.USER.equalsIgnoreCase(targetType)) {
            return NotificationTargetType.USER;
        }
        if (NotificationTargetType.ALL.equalsIgnoreCase(targetType)) {
            return NotificationTargetType.ALL;
        }
        throw new IllegalArgumentException("targetType只支持USER或ALL");
    }

    private Integer normalizeReadStatus(Integer readStatus) {
        if (readStatus == null) {
            return null;
        }
        if (readStatus != 0 && readStatus != 1) {
            throw new IllegalArgumentException("readStatus只支持0或1");
        }
        return readStatus;
    }

    private int normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
    }

    private NotificationMessageVo toMessageVo(NotificationMessageDo message) {
        NotificationMessageVo vo = new NotificationMessageVo();
        vo.setId(message.getId());
        vo.setTargetType(NotificationTargetType.USER);
        vo.setEventType(message.getEventType());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setPayload(readPayload(message.getPayload()));
        vo.setReadStatus(message.getReadStatus());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }

    private NotificationMessageVo toAnnouncementVo(NotificationAnnouncementDo announcement, Set<Long> readAnnouncementIds) {
        NotificationMessageVo vo = new NotificationMessageVo();
        vo.setId(announcement.getId());
        vo.setTargetType(NotificationTargetType.ALL);
        vo.setEventType(announcement.getEventType());
        vo.setTitle(announcement.getTitle());
        vo.setContent(announcement.getContent());
        vo.setPayload(readPayload(announcement.getPayload()));
        vo.setReadStatus(readAnnouncementIds.contains(announcement.getId()) ? 1 : 0);
        vo.setCreateTime(announcement.getCreateTime());
        return vo;
    }

    private String writePayload(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("payload序列化失败", e);
        }
    }

    private Map<String, Object> readPayload(String payload) {
        if (!StringUtils.hasText(payload)) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }
}
