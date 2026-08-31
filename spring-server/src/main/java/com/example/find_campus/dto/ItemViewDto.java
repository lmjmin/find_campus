package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ItemViewDto {

    private Long id;
    private Long userId;
    private String itemType;
    private String title;
    private Long categoryId;
    private String categoryName;
    private String itemName;
    private String color;
    private String brand;
    private Long locationId;
    private String locationName;
    private String locationDetail;
    private Date itemDate;
    private String itemTime;
    private String itemTimeRange;
    private Long storageId;
    private String storageName;
    private Long storageLocationId;
    private String storageLocationName;
    private Double storageLatitude;
    private Double storageLongitude;
    private String storageManagerName;
    private String storagePhone;
    private String storageOperatingHours;
    private String storageDescription;
    private String description;
    private String status;
    private int viewCount;
    private Date createdAt;
    private String writerName;
    private String studentNo;
    private String school;
    private String imageUrl;
}
