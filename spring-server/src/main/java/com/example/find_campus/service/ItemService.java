package com.example.find_campus.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.find_campus.dao.ICommonDao;
import com.example.find_campus.dao.IItemDao;
import com.example.find_campus.dao.IUserDao;
import com.example.find_campus.dto.CategoryDto;
import com.example.find_campus.dto.FoundItemDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.LocationDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.LostItemDto;
import com.example.find_campus.dto.NotificationDto;
import com.example.find_campus.dto.ProfileUpdateDto;
import com.example.find_campus.dto.ReportDto;
import com.example.find_campus.dto.ReportViewDto;
import com.example.find_campus.dto.StoragePlaceDto;
import com.example.find_campus.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final IItemDao itemDao;
    private final ICommonDao commonDao;
    private final IUserDao userDao;
    private final UserService userService;
    private final ImageStorageService imageStorageService;
    private final AiRecommendationService aiRecommendationService;
    private final AppNotificationService appNotificationService;

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
            throw new IllegalArgumentException("분실물을 찾을 수 없습니다.");
        }
        return item;
    }

    public ItemViewDto findLostItemForEdit(Long lostId, Long userId) {
        requireLogin(userId);
        ItemViewDto item = itemDao.findLostItemById(lostId);
        requireEditableItem(item, userId, "분실물");
        clearGeneratedDescriptionForEdit(item);
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
            throw new IllegalArgumentException("신고 ID와 상태가 필요합니다.");
        }
        itemDao.updateReportStatus(reportId, status);
    }

    @Transactional
    public void updateLostStatus(Long lostId, String status) {
        if (lostId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("분실물 ID와 상태가 필요합니다.");
        }
        itemDao.updateLostStatus(lostId, status);
    }

    @Transactional
    public void updateFoundStatus(Long foundId, String status) {
        if (foundId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("습득물 ID와 상태가 필요합니다.");
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
            throw new IllegalArgumentException("습득물을 찾을 수 없습니다.");
        }
        return item;
    }

    public ItemViewDto findFoundItemForEdit(Long foundId, Long userId) {
        requireLogin(userId);
        ItemViewDto item = itemDao.findFoundItemById(foundId);
        requireEditableItem(item, userId, "습득물");
        clearGeneratedDescriptionForEdit(item);
        return item;
    }

    @Transactional
    public Long createLostItem(LostItemDto dto, Long userId) {
        return createLostItem(dto, userId, null);
    }

    @Transactional
    public Long createLostItem(LostItemDto dto, Long userId, List<MultipartFile> images) {
        requireLogin(userId);
        normalizeOptionalLostFields(dto);
        resolveLostLocationName(dto);
applyLostRegistrationDefaults(dto);
        validateLostItem(dto);
        dto.setUserId(userId);
        dto.setStatus("REGISTERED");
        dto.setDescription(defaultString(dto.getDescription()));

        if (itemDao.insertLostItem(dto) != 1) {
            throw new IllegalStateException("분실물을 등록하지 못했습니다.");
        }
        imageStorageService.store("lost", dto.getLostId(), images).forEach(itemDao::insertItemImage);
        aiRecommendationService.refreshRecommendationsAfterCommit("lost", dto.getLostId());
        return dto.getLostId();
    }

    @Transactional
    public void updateLostItem(Long lostId, LostItemDto dto, Long userId) {
        updateLostItem(lostId, dto, userId, null);
    }

    @Transactional
    public void updateLostItem(Long lostId, LostItemDto dto, Long userId, List<MultipartFile> images) {
        requireLogin(userId);
        requireOwner(itemDao.countLostItemByOwner(lostId, userId), "분실물");
        normalizeOptionalLostFields(dto);
        resolveLostLocationName(dto);
applyLostRegistrationDefaults(dto);
        validateLostItem(dto);
        dto.setLostId(lostId);
        dto.setUserId(userId);
        dto.setDescription(defaultString(dto.getDescription()));

        if (itemDao.updateLostItem(dto) != 1) {
            throw new IllegalStateException("분실물을 수정하지 못했습니다.");
        }
        replaceImagesIfUploaded("lost", lostId, images);
        aiRecommendationService.refreshRecommendationsAfterCommit("lost", lostId);
    }

    @Transactional
    public void deleteLostItem(Long lostId, Long userId) {
        requireLogin(userId);
        requireOwner(itemDao.countLostItemByOwner(lostId, userId), "분실물");
        if (itemDao.deleteLostItem(lostId, userId) != 1) {
            throw new IllegalStateException("분실물을 삭제하지 못했습니다.");
        }
    }

    @Transactional
    public Long createFoundItem(FoundItemDto dto, Long userId) {
        return createFoundItem(dto, userId, null);
    }

    @Transactional
    public Long createFoundItem(FoundItemDto dto, Long userId, List<MultipartFile> images) {
        requireLogin(userId);
        normalizeOptionalFoundFields(dto);
        resolveFoundLocationName(dto);
applyFoundRegistrationDefaults(dto);
        validateFoundItem(dto);
        dto.setUserId(userId);
        dto.setStatus("STORED");

        if (itemDao.insertFoundItem(dto) != 1) {
            throw new IllegalStateException("습득물을 등록하지 못했습니다.");
        }
        imageStorageService.store("found", dto.getFoundId(), images).forEach(itemDao::insertItemImage);
        aiRecommendationService.refreshRecommendationsAfterCommit("found", dto.getFoundId());
        return dto.getFoundId();
    }

    @Transactional
    public void updateFoundItem(Long foundId, FoundItemDto dto, Long userId) {
        updateFoundItem(foundId, dto, userId, null);
    }

    @Transactional
    public void updateFoundItem(Long foundId, FoundItemDto dto, Long userId, List<MultipartFile> images) {
        requireLogin(userId);
        requireOwner(itemDao.countFoundItemByOwner(foundId, userId), "습득물");
        normalizeOptionalFoundFields(dto);
        resolveFoundLocationName(dto);
applyFoundRegistrationDefaults(dto);
        validateFoundItem(dto);
        dto.setFoundId(foundId);
        dto.setUserId(userId);

        if (itemDao.updateFoundItem(dto) != 1) {
            throw new IllegalStateException("습득물을 수정하지 못했습니다.");
        }
        replaceImagesIfUploaded("found", foundId, images);
        aiRecommendationService.refreshRecommendationsAfterCommit("found", foundId);
    }

    @Transactional
    public void deleteFoundItem(Long foundId, Long userId) {
        requireLogin(userId);
        requireOwner(itemDao.countFoundItemByOwner(foundId, userId), "습득물");
        if (itemDao.deleteFoundItem(foundId, userId) != 1) {
            throw new IllegalStateException("습득물을 삭제하지 못했습니다.");
        }
    }

    @Transactional
    public Long createReport(ReportDto dto, Long reporterId) {
        requireLogin(reporterId);
        if (dto.getTargetId() == null) {
            throw new IllegalArgumentException("신고 대상이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getTargetType())) {
            throw new IllegalArgumentException("신고 대상 유형이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getReasonType())) {
            throw new IllegalArgumentException("신고 사유가 필요합니다.");
        }

        dto.setTargetType(dto.getTargetType().trim().toLowerCase());
        dto.setReasonType(dto.getReasonType().trim());
        dto.setReasonDetail(defaultString(dto.getReasonDetail()));
        dto.setReporterId(reporterId);
        dto.setStatus("WAITING");

        if (itemDao.insertReport(dto) != 1) {
            throw new IllegalStateException("신고를 등록하지 못했습니다.");
        }
        notifyReportCreated(dto);
        return dto.getReportId();
    }


    private void notifyReportCreated(ReportDto report) {
        if (report == null || report.getReportId() == null) {
            return;
        }
        try {
            appNotificationService.notifyUser(
                    report.getReporterId(),
                    "REPORT_RECEIVED",
                    "신고가 접수되었습니다.",
                    "접수된 신고는 관리자가 확인한 뒤 처리 결과를 알려드립니다.",
                    "report",
                    report.getReportId());
            appNotificationService.notifyAdmins(
                    "REPORT_RECEIVED",
                    "새 신고가 접수되었습니다.",
                    reportReasonLabel(report.getReasonType()) + " 신고가 들어왔습니다.",
                    "report",
                    report.getReportId());
        } catch (RuntimeException ignored) {
        }
    }

    private String reportReasonLabel(String reason) {
        if (!StringUtils.hasText(reason)) {
            return "신고";
        }
        return switch (reason) {
            case "FALSE_INFO" -> "허위 게시글";
            case "PRIVATE_INFO" -> "개인정보 노출";
            case "DUPLICATE" -> "중복 게시글";
            case "BAD_CONTENT" -> "부적절한 내용";
            case "SPAM" -> "광고/스팸";
            case "ETC" -> "기타";
            default -> reason;
        };
    }

    @Transactional
    public UserDto updateProfile(ProfileUpdateDto dto, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("이름을 입력해 주세요.");
        }
        if (!StringUtils.hasText(dto.getPhone())) {
            throw new IllegalArgumentException("전화번호를 입력해 주세요.");
        }

        UserDto user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        user.setUserName(dto.getName());
        user.setPhone(dto.getPhone().trim());

        if (userDao.updateUser(user) != 1) {
            throw new IllegalStateException("회원 정보를 수정하지 못했습니다.");
        }

        if (StringUtils.hasText(dto.getNewPassword())) {
            userService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword(), dto.getNewPasswordConfirm());
        }
        return userDao.findById(userId);
    }

    private void clearGeneratedDescriptionForEdit(ItemViewDto item) {
        if (item == null || !StringUtils.hasText(item.getDescription())) {
            return;
        }

        String description = item.getDescription().trim();
        String generatedPrefix = "";
        if (StringUtils.hasText(item.getTitle()) && StringUtils.hasText(item.getItemName())) {
            generatedPrefix = item.getTitle().trim() + " - " + item.getItemName().trim();
        }

        if (description.contains("[")
                || (!generatedPrefix.isBlank() && description.equals(generatedPrefix))
                || (!generatedPrefix.isBlank() && description.startsWith(generatedPrefix + "\n"))) {
            item.setDescription("");
        }
    }
    private void replaceImagesIfUploaded(String itemType, Long itemId, List<MultipartFile> images) {
        if (!hasFiles(images)) {
            return;
        }
        itemDao.deleteItemImages(itemType, itemId);
        imageStorageService.store(itemType, itemId, images).forEach(itemDao::insertItemImage);
    }

    private boolean hasFiles(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return false;
        }
        return images.stream().anyMatch(image -> image != null && !image.isEmpty());
    }

    private String mergeLostDescription(LostItemDto dto) {
        StringBuilder description = new StringBuilder(dto.getDescription().trim());
        StringBuilder features = new StringBuilder();

        appendFeature(features, "무늬/디자인", dto.getPattern());
        appendFeature(features, "흠집/사용 흔적", dto.getDamageInfo());
        appendFeature(features, "부착물/스티커", dto.getAttachmentInfo());
        appendFeature(features, "내용물", dto.getInsideItem());

        if (features.length() > 0 && !description.toString().contains("[추가 특징]")) {
            description.append("\n\n[추가 특징]\n").append(features);
        }
        return description.toString();
    }

    private void applyLostRegistrationDefaults(LostItemDto dto) {
        if (dto.getCategoryId() == null) {
            dto.setCategoryId(defaultCategoryId());
        }
        if (dto.getLostLocationId() == null) {
            dto.setLostLocationId(defaultLocationId());
        }
        if (dto.getLostDate() == null) {
            dto.setLostDate(LocalDate.now());
        }
    }

    private void applyFoundRegistrationDefaults(FoundItemDto dto) {
        if (dto.getCategoryId() == null) {
            dto.setCategoryId(defaultCategoryId());
        }
        if (dto.getFoundDate() == null) {
            dto.setFoundDate(LocalDate.now());
        }
        if (dto.getStorageId() == null) {
            dto.setStorageId(defaultStorageId());
        }
        if (dto.getFoundLocationId() == null) {
            dto.setFoundLocationId(defaultFoundLocationId(dto.getStorageId()));
        }
    }


    private void resolveLostLocationName(LostItemDto dto) {
        if (dto.getLostLocationId() != null || !StringUtils.hasText(dto.getLostLocationName())) {
            return;
        }
        dto.setLostLocationId(resolveLocationId(dto.getLostLocationName()));
    }

    private void resolveFoundLocationName(FoundItemDto dto) {
        if (dto.getFoundLocationId() != null || !StringUtils.hasText(dto.getFoundLocationName())) {
            return;
        }
        dto.setFoundLocationId(resolveLocationId(dto.getFoundLocationName()));
    }

    private Long resolveLocationId(String locationName) {
        String normalizedName = locationName.trim();
        LocationDto existing = commonDao.findLocationByName(normalizedName);
        if (existing != null) {
            return existing.getLocationId();
        }

        LocationDto location = new LocationDto();
        location.setLocationName(normalizedName);
        location.setBuildingName(normalizedName);
        location.setDetail(normalizedName);
        commonDao.insertLocation(location);
        return location.getLocationId();
    }
    private Long defaultCategoryId() {
        List<CategoryDto> categories = commonDao.findCategories();
        if (categories == null || categories.isEmpty()) {
            return null;
        }
        return categories.stream()
                .filter(category -> "기타".equals(category.getCategoryName()))
                .findFirst()
                .orElse(categories.get(0))
                .getCategoryId();
    }

    private Long defaultLocationId() {
        List<LocationDto> locations = commonDao.findLocations();
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        return locations.get(0).getLocationId();
    }

    private Long defaultStorageId() {
        List<StoragePlaceDto> storagePlaces = commonDao.findStoragePlaces();
        if (storagePlaces == null || storagePlaces.isEmpty()) {
            return null;
        }
        return storagePlaces.get(0).getStorageId();
    }

    private Long defaultFoundLocationId(Long storageId) {
        List<StoragePlaceDto> storagePlaces = commonDao.findStoragePlaces();
        if (storagePlaces != null && storageId != null) {
            for (StoragePlaceDto storagePlace : storagePlaces) {
                if (storageId.equals(storagePlace.getStorageId()) && storagePlace.getLocationId() != null) {
                    return storagePlace.getLocationId();
                }
            }
        }
        return defaultLocationId();
    }

    private String defaultItemDescription(String title, String itemName) {
        if (StringUtils.hasText(title) && StringUtils.hasText(itemName)) {
            return title.trim() + " - " + itemName.trim();
        }
        if (StringUtils.hasText(title)) {
            return title.trim();
        }
        if (StringUtils.hasText(itemName)) {
            return itemName.trim();
        }
        return "상세 설명 없음";
    }
    private void normalizeOptionalLostFields(LostItemDto dto) {
        dto.setColor(trimToNull(dto.getColor()));
        dto.setBrand(trimToNull(dto.getBrand()));
        dto.setPattern(trimToNull(dto.getPattern()));
        dto.setDamageInfo(trimToNull(dto.getDamageInfo()));
        dto.setAttachmentInfo(trimToNull(dto.getAttachmentInfo()));
        dto.setInsideItem(trimToNull(dto.getInsideItem()));
        dto.setLostLocationDetail(defaultString(dto.getLostLocationDetail()));
        dto.setLostTime(trimToNull(dto.getLostTime()));
        dto.setLostTimeRange(trimToNull(dto.getLostTimeRange()));

        if (dto.getLostDate() == null) {
            dto.setLostDate(LocalDate.now());
        }
    }

    private void normalizeOptionalFoundFields(FoundItemDto dto) {
        dto.setColor(trimToNull(dto.getColor()));
        dto.setBrand(trimToNull(dto.getBrand()));
        dto.setDescription(defaultString(dto.getDescription()));
        dto.setFoundLocationDetail(defaultString(dto.getFoundLocationDetail()));
        dto.setFoundTime(trimToNull(dto.getFoundTime()));
        dto.setFoundTimeRange(trimToNull(dto.getFoundTimeRange()));
    }

    private String defaultString(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private void appendFeature(StringBuilder features, String label, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        features.append("- ").append(label).append(": ").append(value.trim()).append("\n");
    }

    private void normalizeSearch(ItemSearchDto searchDto) {
        if (searchDto == null) {
            return;
        }

        searchDto.setKeyword(trimToNull(searchDto.getKeyword()));
        searchDto.setLocationName(trimToNull(searchDto.getLocationName()));
        searchDto.setStatus(trimToNull(searchDto.getStatus()));
        searchDto.setSort(trimToNull(searchDto.getSort()));

        if (!StringUtils.hasText(searchDto.getSort())) {
            searchDto.setSort("latest");
        }
        if ("RECEIVED".equals(searchDto.getStatus())) {
            searchDto.setStatus("REGISTERED");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }

    private void requireOwner(int count, String itemName) {
        if (count < 1) {
            throw new IllegalArgumentException(itemName + " 작성자만 수정하거나 삭제할 수 있습니다.");
        }
    }

    private void requireEditableItem(ItemViewDto item, Long userId, String itemName) {
        if (item == null) {
            throw new IllegalArgumentException(itemName + "을 찾을 수 없습니다.");
        }
        if (item.getUserId() == null || !item.getUserId().equals(userId)) {
            throw new IllegalArgumentException(itemName + " 작성자만 수정할 수 있습니다.");
        }
    }

    private void validateLostItem(LostItemDto dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        if (!StringUtils.hasText(dto.getItemName())) {
            throw new IllegalArgumentException("물건명을 입력해 주세요.");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리를 선택해 주세요.");
        }
    }

    private void validateFoundItem(FoundItemDto dto) {
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        if (!StringUtils.hasText(dto.getItemName())) {
            throw new IllegalArgumentException("물건명을 입력해 주세요.");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리를 선택해 주세요.");
        }
        if (dto.getFoundLocationId() == null) {
            throw new IllegalArgumentException("습득 위치를 입력해 주세요.");
        }
        if (dto.getFoundDate() == null) {
            throw new IllegalArgumentException("습득 날짜를 입력해 주세요.");
        }
        if (dto.getStorageId() == null) {
            throw new IllegalArgumentException("보관 장소를 선택해 주세요.");
        }
    }
}
