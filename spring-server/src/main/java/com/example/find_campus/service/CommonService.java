package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.ICommonDao;
import com.example.find_campus.dto.CategoryDto;
import com.example.find_campus.dto.DashboardStatsDto;
import com.example.find_campus.dto.LocationDto;
import com.example.find_campus.dto.StoragePlaceDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonService {

    private final ICommonDao commonDao;

    public List<CategoryDto> findCategories() {
        return commonDao.findCategories();
    }

    public List<CategoryDto> findAllCategories() {
        return commonDao.findAllCategories();
    }

    public CategoryDto findCategoryById(Long categoryId) {
        return categoryId == null ? null : commonDao.findCategoryById(categoryId);
    }

    @Transactional
    public void saveCategory(CategoryDto category) {
        if (category == null || !StringUtils.hasText(category.getCategoryName())) {
            throw new IllegalArgumentException("카테고리명을 입력해 주세요.");
        }
        category.setCategoryName(category.getCategoryName().trim());
        category.setUseYn("Y");
        if (category.getCategoryId() == null) {
            commonDao.insertCategory(category);
        } else {
            commonDao.updateCategory(category);
        }
    }

    @Transactional
    public void updateCategoryUseYn(Long categoryId, String useYn) {
        if (categoryId == null || !StringUtils.hasText(useYn)) {
            throw new IllegalArgumentException("카테고리와 사용 여부가 필요합니다.");
        }
        commonDao.updateCategoryUseYn(categoryId, useYn);
    }

    public List<LocationDto> findLocations() {
        return commonDao.findLocations();
    }

    public List<LocationDto> findAllLocations() {
        return commonDao.findAllLocations();
    }

    public LocationDto findLocationById(Long locationId) {
        return locationId == null ? null : commonDao.findLocationById(locationId);
    }

    @Transactional
    public void saveLocation(LocationDto location) {
        if (location == null || !StringUtils.hasText(location.getLocationName())) {
            throw new IllegalArgumentException("위치명을 입력해 주세요.");
        }
        location.setLocationName(location.getLocationName().trim());
        location.setBuildingName(location.getLocationName());
        location.setFloor(null);
        location.setDetail(null);
        location.setUseYn("Y");
        if (location.getLocationId() == null) {
            commonDao.insertLocation(location);
        } else {
            commonDao.updateLocation(location);
        }
    }

    @Transactional
    public void updateLocationUseYn(Long locationId, String useYn) {
        if (locationId == null || !StringUtils.hasText(useYn)) {
            throw new IllegalArgumentException("위치와 사용 여부가 필요합니다.");
        }
        commonDao.updateLocationUseYn(locationId, useYn);
    }

    public List<StoragePlaceDto> findStoragePlaces() {
        return commonDao.findStoragePlaces();
    }

    public List<StoragePlaceDto> findAllStoragePlaces() {
        return commonDao.findAllStoragePlaces();
    }

    public StoragePlaceDto findStoragePlaceById(Long storageId) {
        return storageId == null ? null : commonDao.findStoragePlaceById(storageId);
    }

    @Transactional
    public void saveStoragePlace(StoragePlaceDto storagePlace) {
        if (storagePlace == null || !StringUtils.hasText(storagePlace.getStorageName())) {
            throw new IllegalArgumentException("보관 장소명을 입력해 주세요.");
        }
        storagePlace.setStorageName(storagePlace.getStorageName().trim());
        storagePlace.setUseYn("Y");
        if (storagePlace.getStorageId() == null) {
            commonDao.insertStoragePlace(storagePlace);
        } else {
            commonDao.updateStoragePlace(storagePlace);
        }
    }

    @Transactional
    public void updateStoragePlaceUseYn(Long storageId, String useYn) {
        if (storageId == null || !StringUtils.hasText(useYn)) {
            throw new IllegalArgumentException("보관 장소와 사용 여부가 필요합니다.");
        }
        commonDao.updateStoragePlaceUseYn(storageId, useYn);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("移댄뀒怨좊━ ?뺣낫媛 ?꾩슂?⑸땲??");
        }
        commonDao.deleteCategory(categoryId);
    }

    @Transactional
    public void deleteLocation(Long locationId) {
        if (locationId == null) {
            throw new IllegalArgumentException("?꾩튂 ?뺣낫媛 ?꾩슂?⑸땲??");
        }
        commonDao.deleteLocation(locationId);
    }

    @Transactional
    public void deleteStoragePlace(Long storageId) {
        if (storageId == null) {
            throw new IllegalArgumentException("蹂닿? ?μ냼 ?뺣낫媛 ?꾩슂?⑸땲??");
        }
        commonDao.deleteStoragePlace(storageId);
    }

    public DashboardStatsDto getDashboardStats() {
        return commonDao.getDashboardStats();
    }
}