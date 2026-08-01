package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class MatchViewDto {

    private Long matchId;
    private Long lostId;
    private Long foundId;
    private Double matchScore;
    private String matchReason;
    private String matchStatus;
    private Date createdAt;
    private String lostTitle;
    private String lostItemName;
    private String foundTitle;
    private String foundItemName;
    private String foundLocationName;
    private String foundLocationDetail;
    private String imageUrl;
}
