package com.example.find_campus.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.find_campus.dto.ApiResponseDto;
import com.example.find_campus.dto.NotificationDto;
import com.example.find_campus.service.AppNotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationApiController {

    private final AppNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> notifications(HttpSession session,
                                                                             @RequestParam(required = false) Long userId) {
        Long resolvedUserId = resolveUserId(session, userId);
        List<NotificationDto> notifications = notificationService.findNotifications(resolvedUserId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", notifications);
        data.put("unreadCount", notificationService.countUnread(resolvedUserId));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "알림 조회 성공", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<Map<String, Long>>> create(@RequestBody NotificationDto dto) {
        Long notificationId = notificationService.createNotification(dto);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "알림이 등록되었습니다.", Map.of("notificationId", notificationId)));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponseDto<Void>> markRead(@PathVariable Long notificationId,
                                                         HttpSession session,
                                                         @RequestParam(required = false) Long userId) {
        notificationService.markRead(notificationId, resolveUserId(session, userId));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "알림을 읽음 처리했습니다.", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponseDto<Void>> markAllRead(HttpSession session,
                                                            @RequestParam(required = false) Long userId) {
        notificationService.markAllRead(resolveUserId(session, userId));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "모든 알림을 읽음 처리했습니다.", null));
    }

    private Long resolveUserId(HttpSession session, Long fallbackUserId) {
        Object loginUserId = session.getAttribute("loginUserId");
        if (loginUserId instanceof Long userId) {
            return userId;
        }
        if (loginUserId instanceof Integer userId) {
            return userId.longValue();
        }
        if (loginUserId instanceof String userId && !userId.isBlank()) {
            return Long.valueOf(userId);
        }
        return fallbackUserId;
    }
}
