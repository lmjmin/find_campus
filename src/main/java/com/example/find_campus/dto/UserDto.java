package com.example.find_campus.dto;

import lombok.Data;

import java.util.Date;

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