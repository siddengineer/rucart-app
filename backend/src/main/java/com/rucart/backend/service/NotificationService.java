package com.rucart.backend.service;

import com.rucart.backend.dto.NotificationDto;
import com.rucart.backend.model.Notification;
import com.rucart.backend.model.NotificationType;
import com.rucart.backend.model.User;
import com.rucart.backend.repository.NotificationRepository;
import com.rucart.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Notification> getUserNotificationsPaginated(Long userId, int page, int size, boolean unreadOnly) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)));
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        } else {
            return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification sendNotification(Long userId, String title, String message, NotificationType type, String refId) {
        // Prevent duplicate spam for rapid consecutive triggers of identical event on same reference
        if (refId != null && title != null) {
            Optional<Notification> existing = notificationRepository
                    .findTop1ByUserIdAndReferenceIdAndTitleOrderByCreatedAtDesc(userId, refId, title);
            if (existing.isPresent()) {
                Notification prev = existing.get();
                if (prev.getCreatedAt() != null && prev.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(2))) {
                    return prev;
                }
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Notification notification = new Notification(user, title, message, type, refId);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification sendNotification(NotificationDto dto) {
        return sendNotification(dto.getUserId(), dto.getTitle(), dto.getMessage(), dto.getType(), dto.getReferenceId());
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to notification: " + notificationId);
        }
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to notification: " + notificationId);
        }
        notificationRepository.delete(notification);
    }
}
