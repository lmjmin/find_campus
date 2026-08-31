package com.example.find_campus.dto;

import lombok.Data;

@Data
public class AiEmbeddingDto {

    private Long embeddingId;
    private String itemType;
    private Long itemId;
    private String modelName;
    private String imageEmbedding;
    private String textEmbedding;
}
