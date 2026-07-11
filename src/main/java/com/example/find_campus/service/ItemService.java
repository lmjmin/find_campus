package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.find_campus.dao.IItemDao;
import com.example.find_campus.dao.IUserDao;
import com.example.find_campus.dto.FoundItemDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.LostItemDto;
import com.example.find_campus.dto.ProfileUpdateDto;
import com.example.find_campus.dto.ReportDto;
import com.example.find_campus.dto.ReportViewDto;
import com.example.find_campus.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final IItemDao itemDao;
    private final IUserDao userDao;
    private final UserService userService;
    private final ImageStorageService imageStorageService;


    public List<ItemViewDto> findLostItems(ItemSearchDto searchDto) {
        normalizeSearch(searchDto);
        return itemDao.findLostItems(searchDto);
    }

    public int countLostItems(ItemSearchDto searchDto) {
        normalizeSearch(searchDto);
        return itemDao.countLostItems(searchDto);
    }

    @Transactional
    public ItemViewDto findLostItem(Long lostId) {
        itemDao.increaseLostViewCount(lostId);
        ItemViewDto item = itemDao.findLostItemById(lostId);
        if (item == null) {
            throw new IllegalArgumentException("Lost item not found.");
        }
        return item;
    }

    public List<ItemViewDto> findRecentItems(String itemType, int limit) {
        return itemDao.findRecentItems(itemType, limit);
    }

    public List<ItemViewDto> findLostItemsByUserId(Long userId) {
        requireLogin(userId);
        return itemDao.findLostItemsByUserId(userId);
    }

    public List<ItemViewDto> findFoundItemsByUserId(Long userId) {
        requireLogin(userId);
        return itemDao.findFoundItemsByUserId(userId);
    }

    public List<ReportViewDto> findReports(ItemSearchDto searchDto) {
        normalizeSearch(searchDto);
        return itemDao.findReports(searchDto);
    }

    public int countReports(ItemSearchDto searchDto) {
        normalizeSearch(searchDto);
        return itemDao.countReports(searchDto);
    }

    @Transactional
    public void updateReportStatus(Long reportId, String status) {
        if (reportId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("Report id and status are required.");
        }
        itemDao.updateReportStatus(reportId, status);
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

    public List<ItemViewDto> findFoundItems(ItemSearchDto searchDto) {
        normalizeSearch(searchDto);
        return itemDao.findFoundItems(searchDto);
    }

    public int countFoundItems(ItemSearchDto searchDto) {
        normalizeSearch(searchDto);
        return itemDao.countFoundItems(searchDto);
    }

    @Transactional
    public ItemViewDto findFoundItem(Long foundId) {
        itemDao.increaseFoundViewCount(foundId);
        ItemViewDto item = itemDao.findFoundItemById(foundId);
        if (item == null) {
            throw new IllegalArgumentException("Found item not found.");
        }
        return item;
    }

    @Transactional
    public Long createLostItem(LostItemDto dto, Long userId) {
        return createLostItem(dto, userId, null);
    }

    @Transactional
    public Long createLostItem(LostItemDto dto, Long userId, List<MultipartFile> images) {
        requireLogin(userId);
        validateLostItem(dto);
        dto.setUserId(userId);
        dto.setStatus("REGISTERED");

        if (itemDao.insertLostItem(dto) != 1) {
            throw new IllegalStateException("Cannot create lost item.");
        }
        imageStorageService.store("lost", dto.getLostId(), images).forEach(itemDao::insertItemImage);

        return dto.getLostId();
    }

    @Transactional
    public Long createFoundItem(FoundItemDto dto, Long userId) {
        return createFoundItem(dto, userId, null);
    }

    @Transactional
    public Long createFoundItem(FoundItemDto dto, Long userId, List<MultipartFile> images) {
        requireLogin(userId);
        validateFoundItem(dto);
        dto.setUserId(userId);
        if (!StringUtils.hasText(dto.getTitle())) {
            dto.setTitle(dto.getItemName() + " found");
        }
        dto.setStatus("REGISTERED");

        if (itemDao.insertFoundItem(dto) != 1) {
            throw new IllegalStateException("Cannot create found item.");
        }
        imageStorageService.store("found", dto.getFoundId(), images).forEach(itemDao::insertItemImage);

        return dto.getFoundId();
    }

    @Transactional
    public Long createReport(ReportDto dto, Long reporterId) {
        requireLogin(reporterId);
        if (dto.getTargetId() == null) {
            throw new IllegalArgumentException("Report target is required.");
        }
        if (!StringUtils.hasText(dto.getTargetType())) {
            throw new IllegalArgumentException("Report target type is required.");
        }
        if (!StringUtils.hasText(dto.getReasonType())) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }

        dto.setReporterId(reporterId);
        dto.setStatus("WAITING");

        if (itemDao.insertReport(dto) != 1) {
            throw new IllegalStateException("?? ?? ? ??? ??????.");
        }

        return dto.getReportId();
    }

    @Transactional
    public UserDto updateProfile(ProfileUpdateDto dto, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("???? ?????.");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("??? ??? ???.");
        }
        if (!StringUtils.hasText(dto.getEmail()) || !dto.getEmail().contains("@")) {
            throw new IllegalArgumentException("??? ???? ??? ???.");
        }
        if (!StringUtils.hasText(dto.getStudentNo())) {
            throw new IllegalArgumentException("??? ??? ???.");
        }

        UserDto user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("??? ??? ?? ? ????.");
        }

        user.setUserName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStudentNo(dto.getStudentNo());

        if (userDao.updateUser(user) != 1) {
            throw new IllegalStateException("?? ?? ? ??? ??????.");
        }

        if (StringUtils.hasText(dto.getNewPassword())) {
            userService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword(), dto.getNewPasswordConfirm());
        }

        return userDao.findById(userId);
    }

    private void normalizeSearch(ItemSearchDto searchDto) {
        if (searchDto != null && !StringUtils.hasText(searchDto.getSort())) {
            searchDto.setSort("latest");
        }
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Login is required.");
        }
    }

    private void validateLostItem(LostItemDto dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("??? ??? ???.");
        }
        if (!StringUtils.hasText(dto.getItemName())) {
            throw new IllegalArgumentException("???? ??? ???.");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("????? ??? ???.");
        }
        if (!StringUtils.hasText(dto.getColor())) {
            throw new IllegalArgumentException("??? ??? ???.");
        }
        if (!StringUtils.hasText(dto.getDescription())) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }
        if (dto.getLostLocationId() == null || !StringUtils.hasText(dto.getLostLocationDetail())) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }
        if (dto.getLostDate() == null) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }
    }

    private void validateFoundItem(FoundItemDto dto) {
        if (!StringUtils.hasText(dto.getItemName())) {
            throw new IllegalArgumentException("???? ??? ???.");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("????? ??? ???.");
        }
        if (dto.getFoundLocationId() == null) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }
        if (dto.getFoundDate() == null) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }
        if (dto.getStorageId() == null) {
            throw new IllegalArgumentException("?? ??? ??? ???.");
        }
    }
}
