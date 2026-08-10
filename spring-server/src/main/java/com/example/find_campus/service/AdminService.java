package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.IItemDao;
import com.example.find_campus.dao.IUserDao;
import com.example.find_campus.dto.DashboardStatsDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.ReportViewDto;
import com.example.find_campus.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final IUserDao userDao;
    private final IItemDao itemDao;

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        ItemSearchDto empty = new ItemSearchDto();
        stats.setUserCount(userDao.countUsers(null, null));
        stats.setLostCount(itemDao.countLostItems(empty));
        stats.setFoundCount(itemDao.countFoundItems(empty));
        stats.setReportCount(itemDao.countReports(empty));
        ItemSearchDto waiting = new ItemSearchDto();
        waiting.setStatus("WAITING");
        stats.setWaitingReportCount(itemDao.countReports(waiting));
        return stats;
    }

    public List<UserDto> findUsers(String keyword, String status) {
        return userDao.findAllUsers(keyword, status);
    }

    public int countUsers(String keyword, String status) {
        return userDao.countUsers(keyword, status);
    }

    public List<ItemViewDto> findLostItems(ItemSearchDto searchDto) {
        return itemDao.findLostItems(searchDto);
    }

    public int countLostItems(ItemSearchDto searchDto) {
        return itemDao.countLostItems(searchDto);
    }

    public List<ItemViewDto> findFoundItems(ItemSearchDto searchDto) {
        return itemDao.findFoundItems(searchDto);
    }

    public int countFoundItems(ItemSearchDto searchDto) {
        return itemDao.countFoundItems(searchDto);
    }

    public List<ReportViewDto> findReports(ItemSearchDto searchDto) {
        return itemDao.findReports(searchDto);
    }

    public int countReports(ItemSearchDto searchDto) {
        return itemDao.countReports(searchDto);
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        if (userId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("User id and status are required.");
        }
        userDao.updateStatus(userId, status);
    }

    @Transactional
    public void updateLostStatus(Long lostId, String status) {
        if (lostId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Lost id and status are required.");
        }
        itemDao.updateLostStatus(lostId, status);
    }

    @Transactional
    public void updateFoundStatus(Long foundId, String status) {
        if (foundId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Found id and status are required.");
        }
        itemDao.updateFoundStatus(foundId, status);
    }

    @Transactional
    public void updateReportStatus(Long reportId, String status) {
        if (reportId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Report id and status are required.");
        }
        itemDao.updateReportStatus(reportId, status);
    }
}
