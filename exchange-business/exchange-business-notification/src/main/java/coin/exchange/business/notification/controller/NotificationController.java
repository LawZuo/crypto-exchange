package coin.exchange.business.notification.controller;

import coin.exchange.api.notification.model.NotificationMessageVo;
import coin.exchange.common.core.context.SecurityContextHolder;
import coin.exchange.common.core.response.R;
import coin.exchange.business.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户通知接口")
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表")
    @GetMapping("/list")
    public R<List<NotificationMessageVo>> listNotifications(
            @RequestParam(value = "readStatus", required = false) Integer readStatus,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        return R.success(notificationService.listNotifications(currentUserId(), readStatus, pageNum, pageSize));
    }

    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public R<Long> countUnread() {
        return R.success(notificationService.countUnread(currentUserId()));
    }

    @Operation(summary = "标记单条通知已读")
    @PostMapping("/read/{targetType}/{id}")
    public R<Void> markRead(@PathVariable("targetType") String targetType,
                            @PathVariable("id") Long id) {
        notificationService.markRead(currentUserId(), targetType, id);
        return R.success(null);
    }

    @Operation(summary = "标记全部通知已读")
    @PostMapping("/read-all")
    public R<Void> markAllRead() {
        notificationService.markAllRead(currentUserId());
        return R.success(null);
    }

    private Long currentUserId() {
        return SecurityContextHolder.getUserId();
    }
}
