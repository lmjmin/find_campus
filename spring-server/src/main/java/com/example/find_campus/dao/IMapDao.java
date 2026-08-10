package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.MapItemDto;

@Mapper
public interface IMapDao {

    List<MapItemDto> findMapItems(@Param("keyword") String keyword, @Param("filter") String filter);
}
