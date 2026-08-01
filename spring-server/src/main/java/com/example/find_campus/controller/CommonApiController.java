package com.example.find_campus.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.find_campus.dto.ApiResponseDto;
import com.example.find_campus.dto.CategoryDto;
import com.example.find_campus.dto.DashboardStatsDto;
import com.example.find_campus.dto.LocationDto;
import com.example.find_campus.dto.StoragePlaceDto;
import com.example.find_campus.service.CommonService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommonApiController {

    private final CommonService commonService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponseDto<List<CategoryDto>>> categories() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "카테고리 조회 성공", commonService.findCategories()));
    }

    @GetMapping("/locations")
    public ResponseEntity<ApiResponseDto<List<LocationDto>>> locations() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "위치 조회 성공", commonService.findLocations()));
    }

    @GetMapping("/storage-places")
    public ResponseEntity<ApiResponseDto<List<StoragePlaceDto>>> storagePlaces() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "보관소 조회 성공", commonService.findStoragePlaces()));
    }

    @GetMapping("/home/summary")
    public ResponseEntity<ApiResponseDto<DashboardStatsDto>> homeSummary() {
        return ResponseEntity.ok(new ApiResponseDto<>(true, "홈 요약 조회 성공", commonService.getDashboardStats()));
    }
}
