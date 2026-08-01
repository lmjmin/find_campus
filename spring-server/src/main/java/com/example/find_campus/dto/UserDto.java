package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class UserDto {

    private Long userId;
    private String loginId;
    private String password;
    private String userName;
    private String studentNo;
    private String phone;
    private String email;
    private String role;
    private String status;
    private Date createdAt;
    private Date updatedAt;
}