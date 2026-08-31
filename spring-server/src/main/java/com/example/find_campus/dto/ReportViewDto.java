package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ReportViewDto {

    private Long reportId;
    private Long reporterId;
    private String reporterName;
    private String targetType;
    private Long targetId;
    private Long targetUserId;
    private String targetUserName;
    private String targetUserStatus;
    private String targetTitle;
    private String reason;
    private String detail;
    private String status;
    private Date createdAt;
    private Date processedAt;
}
