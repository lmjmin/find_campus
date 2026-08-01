package com.example.find_campus.dto;

import lombok.Data;

@Data
public class ReportDto {

    private Long reportId;
    private Long reporterId;
    private String targetType;
    private Long targetId;
    private String reasonType;
    private String reasonDetail;
    private String status;
}
