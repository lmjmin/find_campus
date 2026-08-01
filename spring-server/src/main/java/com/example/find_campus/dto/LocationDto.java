package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class LocationDto {

    private Long locationId;
    private String locationName;
    private String buildingName;
    private String floor;
    private String detail;
    private Double latitude;
    private Double longitude;
    private String useYn;
    private Date createdAt;
}
