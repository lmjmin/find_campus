package com.example.find_campus.dto;

import lombok.Data;

@Data
public class ProfileUpdateDto {

    private String loginId;
    private String name;
    private String email;
    private String phone;
    private String studentNo;
    private String department;
    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;
}
