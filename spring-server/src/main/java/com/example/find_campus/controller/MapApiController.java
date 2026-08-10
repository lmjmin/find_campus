package com.example.find_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.find_campus.dto.ApiResponseDto;
import com.example.find_campus.dto.MapItemDto;
import com.example.find_campus.service.MapService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/map")
public class MapApiController {

    private final MapService mapService;

    @GetMapping("/items")
    public ResponseEntity<ApiResponseDto<List<MapItemDto>>> items(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "all") String filter) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "지도 물건 조회 성공", mapService.findMapItems(keyword, filter)));
    }
}
