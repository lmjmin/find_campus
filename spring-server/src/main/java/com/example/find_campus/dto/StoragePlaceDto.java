package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class StoragePlaceDto {

    private Long storageId;
    private String storageName;
    private Long locationId;
    private String locationName;
    private String managerName;
    private String phone;
    private String operatingHours;
    private String description;
    private String useYn;
    private Date createdAt;
}
