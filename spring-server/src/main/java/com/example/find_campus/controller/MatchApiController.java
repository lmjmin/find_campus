package com.example.find_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.find_campus.dto.ApiResponseDto;
import com.example.find_campus.dto.MatchViewDto;
import com.example.find_campus.service.MatchService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchApiController {

    private final MatchService matchService;

    @GetMapping("/lost/{lostId}")
    public ResponseEntity<ApiResponseDto<List<MatchViewDto>>> matchesByLost(@PathVariable Long lostId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "분실물 매칭 조회 성공",
                matchService.findMatchesByLostId(lostId)));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponseDto<List<MatchViewDto>>> myMatches(HttpSession session,
                                                                        @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "내 매칭 조회 성공",
                matchService.findMatchesByUserId(resolveUserId(session, userId))));
    }

    @PatchMapping("/{matchId}/status")
    public ResponseEntity<ApiResponseDto<Void>> updateStatus(@PathVariable Long matchId,
                                                             @RequestParam String status) {
        matchService.updateMatchStatus(matchId, status);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "매칭 상태가 변경되었습니다.", null));
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
