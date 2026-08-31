package com.example.find_campus.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AiItemPayload {

    @JsonProperty("item_type")
    private String itemType;

    @JsonProperty("item_id")
    private Long itemId;

    private String title;

    @JsonProperty("item_name")
    private String itemName;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("category_name")
    private String categoryName;

    private String color;
    private String brand;

    @JsonProperty("location_id")
    private Long locationId;

    @JsonProperty("location_name")
    private String locationName;

    @JsonProperty("location_detail")
    private String locationDetail;

    @JsonProperty("item_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date itemDate;

    @JsonProperty("item_time")
    private String itemTime;

    private String description;

    @JsonProperty("image_url")
    private String imageUrl;
}
