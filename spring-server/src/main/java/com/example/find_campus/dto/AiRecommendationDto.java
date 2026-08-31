package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class AiRecommendationDto {

    private Long recommendationId;
    private String sourceType;
    private Long sourceId;
    private String targetType;
    private Long targetId;
    private Double totalScore;
    private Double imageScore;
    private Double textScore;
    private Double metadataScore;
    private String reason;
    private String modelName;
    private Date createdAt;

    private String title;
    private String itemName;
    private String categoryName;
    private String color;
    private String locationName;
    private String locationDetail;
    private Date itemDate;
    private String status;
    private String imageUrl;
}
