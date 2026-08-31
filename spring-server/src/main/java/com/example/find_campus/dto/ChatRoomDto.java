package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class ChatRoomDto {
    private Long roomId;
    private Long currentUserId;
    private Long otherUserId;
    private String otherUserName;
    private String otherStudentNo;
    private String otherSchool;
    private String otherInitial;
    private String itemType;
    private String itemTypeLabel;
    private Long itemId;
    private String itemTitle;
    private String itemName;
    private String itemLocationName;
    private String itemStatus;
    private String itemStatusLabel;
    private String itemDetailUrl;
    private String lastMessage;
    private Date lastMessageAt;
    private int unreadCount;
    private String muteYn;
    private String blockedYn;
    private String leftYn;
}
