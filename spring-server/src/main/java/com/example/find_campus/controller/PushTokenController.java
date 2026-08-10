package com.example.find_campus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.find_campus.dto.ApiResponseDto;
import com.example.find_campus.dto.PushTokenDto;
import com.example.find_campus.service.PushTokenService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @PostMapping("/push-token")
    public ResponseEntity<ApiResponseDto<Void>> savePushToken(@RequestBody PushTokenDto dto,
                                                              HttpSession session) {
        pushTokenService.savePushToken(dto, getLoginUserId(session));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "푸시 토큰이 저장되었습니다.", null));
    }

    @DeleteMapping("/push-token")
    public ResponseEntity<ApiResponseDto<Void>> deactivatePushToken(@RequestBody PushTokenDto dto,
                                                                    HttpSession session) {
        pushTokenService.deactivatePushToken(dto, getLoginUserId(session));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "푸시 토큰이 비활성화되었습니다.", null));
    }

    private Long getLoginUserId(HttpSession session) {
        Object loginUserId = session.getAttribute("loginUserId");
        if (loginUserId instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
