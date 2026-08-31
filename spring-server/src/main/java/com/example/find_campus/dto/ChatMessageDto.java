package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ChatMessageDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String messageContent;
    private Date createdAt;
    private String readYn;
    private boolean mine;
}
