package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class InquiryDto {

    private Long inquiryId;
    private Long userId;
    private String userName;
    private String loginId;
    private String studentNo;
    private String inquiryType;
    private String title;
    private String content;
    private String replyEmail;
    private String status;
    private String answerContent;
    private Date answeredAt;
    private Long answerAdminId;
    private String answerAdminName;
    private Date createdAt;
    private Date updatedAt;
}