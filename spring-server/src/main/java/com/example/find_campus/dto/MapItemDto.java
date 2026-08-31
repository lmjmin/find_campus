package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class MapItemDto {

    private Long id;
    private String itemType;
    private String title;
    private String itemName;
    private String status;
    private String statusName;
    private String locationName;
    private String locationDetail;
    private Date itemDate;
    private String itemTime;
    private int viewCount;
    private Double latitude;
    private Double longitude;
    private String detailUrl;
    private String imageUrl;
}
