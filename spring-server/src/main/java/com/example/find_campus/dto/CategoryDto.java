package com.example.find_campus.dto;

import java.util.Date;

import lombok.Data;

@Data
public class CategoryDto {

    private Long categoryId;
    private String categoryName;
    private String description;
    private String useYn;
    private Date createdAt;
}
