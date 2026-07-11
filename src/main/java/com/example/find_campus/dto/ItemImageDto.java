package com.example.find_campus.dto;

import lombok.Data;

@Data
public class ItemImageDto {

    private Long imageId;
    private String itemType;
    private Long itemId;
    private String imageUrl;
    private String originalName;
    private String savedName;
    private int sortOrder;
}
