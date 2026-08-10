package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.find_campus.dao.IMapDao;
import com.example.find_campus.dto.MapItemDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MapService {

    private final IMapDao mapDao;

    public List<MapItemDto> findMapItems(String keyword, String filter) {
        String normalizedFilter = filter == null || filter.isBlank() ? "all" : filter;
        return mapDao.findMapItems(keyword, normalizedFilter);
    }
}
