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
import com.example.find_campus.dto.FoundItemDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.LostItemDto;
import com.example.find_campus.dto.StatusUpdateDto;
import com.example.find_campus.service.ItemService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ItemApiController {

    private final ItemService itemService;

    @GetMapping("/lost")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> lostItems(ItemSearchDto searchDto) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", itemService.findLostItems(searchDto));
        data.put("count", itemService.countLostItems(searchDto));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "분실물 목록 조회 성공", data));
    }

    @GetMapping("/lost/{lostId}")
    public ResponseEntity<ApiResponseDto<ItemViewDto>> lostItem(@PathVariable Long lostId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "분실물 상세 조회 성공", itemService.findLostItem(lostId)));
    }

    @PostMapping("/lost")
    public ResponseEntity<ApiResponseDto<Map<String, Long>>> createLost(@RequestBody LostItemDto dto,
                                                                        HttpSession session) {
        Long lostId = itemService.createLostItem(dto, resolveUserId(session, dto.getUserId()));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "분실물이 등록되었습니다.", Map.of("lostId", lostId)));
    }

    @PatchMapping("/lost/{lostId}/status")
    public ResponseEntity<ApiResponseDto<Void>> updateLostStatus(@PathVariable Long lostId,
                                                                 @RequestBody StatusUpdateDto dto) {
        itemService.updateLostStatus(lostId, dto.getStatus());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "분실물 상태가 변경되었습니다.", null));
    }

    @GetMapping("/found")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> foundItems(ItemSearchDto searchDto) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("items", itemService.findFoundItems(searchDto));
        data.put("count", itemService.countFoundItems(searchDto));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "습득물 목록 조회 성공", data));
    }

    @GetMapping("/found/{foundId}")
    public ResponseEntity<ApiResponseDto<ItemViewDto>> foundItem(@PathVariable Long foundId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "습득물 상세 조회 성공", itemService.findFoundItem(foundId)));
    }

    @PostMapping("/found")
    public ResponseEntity<ApiResponseDto<Map<String, Long>>> createFound(@RequestBody FoundItemDto dto,
                                                                         HttpSession session) {
        Long foundId = itemService.createFoundItem(dto, resolveUserId(session, dto.getUserId()));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "습득물이 등록되었습니다.", Map.of("foundId", foundId)));
    }

    @PatchMapping("/found/{foundId}/status")
    public ResponseEntity<ApiResponseDto<Void>> updateFoundStatus(@PathVariable Long foundId,
                                                                  @RequestBody StatusUpdateDto dto) {
        itemService.updateFoundStatus(foundId, dto.getStatus());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "습득물 상태가 변경되었습니다.", null));
    }

    @GetMapping("/items/recent")
    public ResponseEntity<ApiResponseDto<Map<String, List<ItemViewDto>>>> recentItems(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, List<ItemViewDto>> data = new LinkedHashMap<>();
        data.put("lost", itemService.findRecentItems("lost", limit));
        data.put("found", itemService.findRecentItems("found", limit));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "최근 물건 조회 성공", data));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> search(ItemSearchDto searchDto) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lost", itemService.findLostItems(searchDto));
        data.put("found", itemService.findFoundItems(searchDto));
        data.put("lostCount", itemService.countLostItems(searchDto));
        data.put("foundCount", itemService.countFoundItems(searchDto));
        return ResponseEntity.ok(new ApiResponseDto<>(true, "통합 검색 성공", data));
    }

    @GetMapping("/mypage/lost")
    public ResponseEntity<ApiResponseDto<List<ItemViewDto>>> myLostItems(HttpSession session,
                                                                         @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "내 분실물 조회 성공",
                itemService.findLostItemsByUserId(resolveUserId(session, userId))));
    }

    @GetMapping("/mypage/found")
    public ResponseEntity<ApiResponseDto<List<ItemViewDto>>> myFoundItems(HttpSession session,
                                                                          @RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "내 습득물 조회 성공",
                itemService.findFoundItemsByUserId(resolveUserId(session, userId))));
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
