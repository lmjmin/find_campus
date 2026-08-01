package com.example.find_campus.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.IUserDao;
import com.example.find_campus.dto.PushTokenDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final IUserDao userDao;

    public void savePushToken(PushTokenDto dto, Long sessionUserId) {
        Long userId = sessionUserId != null ? sessionUserId : dto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getFcmToken())) {
            throw new IllegalArgumentException("FCM 토큰이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getDeviceType())) {
            dto.setDeviceType("android");
        }

        dto.setUserId(userId);
        dto.setUseYn("Y");
        userDao.savePushToken(dto);
    }

    public void deactivatePushToken(PushTokenDto dto, Long sessionUserId) {
        Long userId = sessionUserId != null ? sessionUserId : dto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getFcmToken())) {
            throw new IllegalArgumentException("FCM 토큰이 필요합니다.");
        }

        userDao.deactivatePushToken(userId, dto.getFcmToken());
    }
}
