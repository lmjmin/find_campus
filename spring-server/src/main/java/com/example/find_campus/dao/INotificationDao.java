package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.NotificationDto;

@Mapper
public interface INotificationDao {

    int insertNotification(NotificationDto notificationDto);

    List<NotificationDto> findNotificationsByUserId(Long userId);

    int countUnreadNotifications(Long userId);

    int markNotificationRead(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    int markAllNotificationsRead(Long userId);
}
