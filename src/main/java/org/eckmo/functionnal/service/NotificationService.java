package org.eckmo.functionnal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eckmo.functionnal.model.Notification;
import org.eckmo.functionnal.model.User;
import org.eckmo.functionnal.repository.NotificationRepository;
import org.eckmo.functionnal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@SuppressWarnings("unused")
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private static final String USER_NOT_FOUND = "User not found";

    // Create notification using functional approach
    public Notification createNotification(Long userId, String title, String message,
                                          String type, String entityType, Long entityId) {
        log.info("Creating notification for user {}: {}", userId, title);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityType(entityType)
                .relatedEntityId(entityId)
                .isRead(false)
                .build();

        return notificationRepository.save(notification);
    }

    // Get unread notifications count
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return notificationRepository.countUnreadByUser(user);
    }

    // Get all notifications for user
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Get unread notifications
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    // Mark notification as read
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId)
                .ifPresent(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                    log.info("Marked notification {} as read", notificationId);
                });
    }

    // Mark all as read
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

        notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                .forEach(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                });
        log.info("Marked all notifications as read for user {}", userId);
    }

    // Delete notification
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
        log.info("Deleted notification {}", notificationId);
    }

    // Broadcast notification to all users (for admin)
    public void broadcastNotification(String title, String message, String type) {
        userRepository.findAll().stream()
                .forEach(user -> createNotification(
                        user.getId(),
                        title,
                        message,
                        type,
                        "SYSTEM",
                        null
                ));
        log.info("Broadcasted notification to all users");
    }
}

