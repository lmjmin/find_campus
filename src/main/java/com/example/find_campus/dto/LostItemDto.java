package com.example.find_campus.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LostItemDto {

    private Long lostId;
    private Long userId;
    private String title;
    private String itemName;
    private Long categoryId;
    private String color;
    private String brand;
    private String pattern;
    private String damageInfo;
    private String attachmentInfo;
    private String insideItem;
    private String description;
    private Long lostLocationId;
    private String lostLocationDetail;
    private LocalDate lostDate;
    private String lostTime;
    private String lostTimeRange;
    private String status;
}
