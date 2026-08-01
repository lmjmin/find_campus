package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.find_campus.dto.CategoryDto;
import com.example.find_campus.dto.DashboardStatsDto;
import com.example.find_campus.dto.LocationDto;
import com.example.find_campus.dto.StoragePlaceDto;

@Mapper
public interface ICommonDao {

    List<CategoryDto> findCategories();

    List<LocationDto> findLocations();

    List<StoragePlaceDto> findStoragePlaces();

    DashboardStatsDto getDashboardStats();
}
