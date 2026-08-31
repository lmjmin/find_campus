package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.CategoryDto;
import com.example.find_campus.dto.DashboardStatsDto;
import com.example.find_campus.dto.LocationDto;
import com.example.find_campus.dto.StoragePlaceDto;

@Mapper
public interface ICommonDao {

    List<CategoryDto> findCategories();

    List<CategoryDto> findAllCategories();

    CategoryDto findCategoryById(Long categoryId);

    int insertCategory(CategoryDto category);

    int updateCategory(CategoryDto category);

    int updateCategoryUseYn(@Param("categoryId") Long categoryId, @Param("useYn") String useYn);

    List<LocationDto> findLocations();

    List<LocationDto> findAllLocations();

    LocationDto findLocationById(Long locationId);

    LocationDto findLocationByName(String locationName);

    int insertLocation(LocationDto location);

    int updateLocation(LocationDto location);

    int updateLocationUseYn(@Param("locationId") Long locationId, @Param("useYn") String useYn);

    List<StoragePlaceDto> findStoragePlaces();

    List<StoragePlaceDto> findAllStoragePlaces();

    StoragePlaceDto findStoragePlaceById(Long storageId);

    int insertStoragePlace(StoragePlaceDto storagePlace);

    int updateStoragePlace(StoragePlaceDto storagePlace);

    int updateStoragePlaceUseYn(@Param("storageId") Long storageId, @Param("useYn") String useYn);

    int deleteCategory(Long categoryId);

    int deleteLocation(Long locationId);

    int deleteStoragePlace(Long storageId);

    DashboardStatsDto getDashboardStats();
}