package org.eckmo.functionnal.controller;

import lombok.RequiredArgsConstructor;
import org.eckmo.functionnal.model.Notification;
import org.eckmo.functionnal.service.NotificationService;
import org.eckmo.functionnal.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/count")
    public Map<String, Long> getUnreadCount(Authentication auth) {
        Long userId = extractUserId(auth);
        return Map.of("count", notificationService.getUnreadCount(userId));
    }

    @GetMapping
    public List<Notification> getNotifications(Authentication auth) {
        Long userId = extractUserId(auth);
        return notificationService.getNotifications(userId);
    }

    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications(Authentication auth) {
        Long userId = extractUserId(auth);
        return notificationService.getUnreadNotifications(userId);
    }

    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PostMapping("/read-all")
    public void markAllAsRead(Authentication auth) {
        Long userId = extractUserId(auth);
        notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    private Long extractUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        // Extract email from authentication principal and get user ID
        String email = auth.getName();
        return userService.getUserByEmail(email).getId();
    }
}

