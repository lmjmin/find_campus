package com.example.find_campus.dto;

import lombok.Data;

@Data
public class ItemSearchDto {

    private String keyword;
    private String sort = "latest";
    private Long categoryId;
    private Long locationId;
    private String status;
}
