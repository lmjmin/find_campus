package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.AiEmbeddingDto;
import com.example.find_campus.dto.AiRecommendationDto;

@Mapper
public interface IAiRecommendationDao {

    int upsertEmbedding(AiEmbeddingDto embedding);

    int deleteRecommendations(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);

    int insertRecommendation(AiRecommendationDto recommendation);

    List<AiRecommendationDto> findRecommendations(@Param("sourceType") String sourceType,
                                                  @Param("sourceId") Long sourceId);
}
