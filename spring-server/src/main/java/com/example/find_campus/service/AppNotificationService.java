package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.INotificationDao;
import com.example.find_campus.dto.NotificationDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppNotificationService {

    private final INotificationDao notificationDao;

    public List<NotificationDto> findNotifications(Long userId) {
        requireLogin(userId);
        return notificationDao.findNotificationsByUserId(userId);
    }

    public int countUnread(Long userId) {
        requireLogin(userId);
        return notificationDao.countUnreadNotifications(userId);
    }

    @Transactional
    public Long createNotification(NotificationDto dto) {
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("알림을 받을 사용자가 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("알림 제목이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getMessage())) {
            throw new IllegalArgumentException("알림 내용이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getNotificationType())) {
            dto.setNotificationType("SYSTEM");
        }
        dto.setReadYn("N");

        if (notificationDao.insertNotification(dto) != 1) {
            throw new IllegalStateException("알림을 저장할 수 없습니다.");
        }
        return dto.getNotificationId();
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        requireLogin(userId);
        if (notificationId == null) {
            throw new IllegalArgumentException("알림 ID가 필요합니다.");
        }
        notificationDao.markNotificationRead(notificationId, userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        requireLogin(userId);
        notificationDao.markAllNotificationsRead(userId);
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }
}
