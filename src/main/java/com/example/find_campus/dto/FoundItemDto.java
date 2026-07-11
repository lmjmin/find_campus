package com.example.find_campus.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class FoundItemDto {

    private Long foundId;
    private Long userId;
    private String title;
    private String itemName;
    private Long categoryId;
    private String color;
    private String brand;
    private String description;
    private Long foundLocationId;
    private String foundLocationDetail;
    private LocalDate foundDate;
    private String foundTime;
    private String foundTimeRange;
    private String handoverStatus;
    private Long storageId;
    private String status;
}
