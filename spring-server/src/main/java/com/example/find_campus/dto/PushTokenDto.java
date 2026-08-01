package com.example.find_campus.dto;

import lombok.Data;

@Data
public class PushTokenDto {

    private Long tokenId;
    private Long userId;
    private String fcmToken;
    private String deviceType;
    private String useYn;
}
