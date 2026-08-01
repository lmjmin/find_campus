package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;

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

    public List<LocationDto> findLocations() {
        return commonDao.findLocations();
    }

    public List<StoragePlaceDto> findStoragePlaces() {
        return commonDao.findStoragePlaces();
    }

    public DashboardStatsDto getDashboardStats() {
        return commonDao.getDashboardStats();
    }
}
