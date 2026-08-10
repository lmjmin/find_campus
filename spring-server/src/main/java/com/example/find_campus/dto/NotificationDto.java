package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class NotificationDto {

    private Long notificationId;
    private Long userId;
    private String title;
    private String message;
    private String notificationType;
    private String targetType;
    private Long targetId;
    private String readYn;
    private Date createdAt;
}
