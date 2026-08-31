package com.example.find_campus.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
    private final AppNotificationService appNotificationService;

    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        ItemSearchDto visibleItems = new ItemSearchDto();
        visibleItems.setIncludeHidden(false);

        stats.setUserCount(countUsers(null, null));
        stats.setLostCount(safeCount(() -> itemDao.countLostItems(visibleItems)));
        stats.setFoundCount(safeCount(() -> itemDao.countFoundItems(visibleItems)));
        stats.setReportCount(safeCount(() -> itemDao.countReports(new ItemSearchDto())));

        ItemSearchDto waitingReports = new ItemSearchDto();
        waitingReports.setStatus("WAITING");
        stats.setWaitingReportCount(safeCount(() -> itemDao.countReports(waitingReports)));

        ItemSearchDto lostRegistered = new ItemSearchDto();
        lostRegistered.setIncludeHidden(false);
        lostRegistered.setStatus("REGISTERED");
        stats.setRegisteredCount(safeCount(() -> itemDao.countLostItems(lostRegistered)));

        ItemSearchDto foundStored = new ItemSearchDto();
        foundStored.setIncludeHidden(false);
        foundStored.setStatus("STORED");
        stats.setStoredCount(safeCount(() -> itemDao.countFoundItems(foundStored)));

        ItemSearchDto lostReturned = new ItemSearchDto();
        lostReturned.setIncludeHidden(false);
        lostReturned.setStatus("RETURNED");

        ItemSearchDto foundReturned = new ItemSearchDto();
        foundReturned.setIncludeHidden(false);
        foundReturned.setStatus("RETURNED");

        stats.setReturnedCount(
                safeCount(() -> itemDao.countLostItems(lostReturned))
                + safeCount(() -> itemDao.countFoundItems(foundReturned))
        );

        return stats;
    }

    public List<ItemViewDto> findRecentDashboardItems() {
        List<ItemViewDto> items = new ArrayList<>();
        items.addAll(itemDao.findRecentItems("lost", 5));
        items.addAll(itemDao.findRecentItems("found", 5));
        items.sort((left, right) -> {
            if (left.getCreatedAt() == null && right.getCreatedAt() == null) {
                return 0;
            }
            if (left.getCreatedAt() == null) {
                return 1;
            }
            if (right.getCreatedAt() == null) {
                return -1;
            }
            return right.getCreatedAt().compareTo(left.getCreatedAt());
        });
        return items.size() > 5 ? new ArrayList<>(items.subList(0, 5)) : items;
    }

    public List<ReportViewDto> findRecentDashboardReports() {
        List<ReportViewDto> reports = itemDao.findReports(new ItemSearchDto());
        reports.forEach(this::normalizeReportView);
        return reports.size() > 5 ? new ArrayList<>(reports.subList(0, 5)) : reports;
    }

    public List<UserDto> findUsers(String keyword, String status) {
        return findManagedUsers(keyword, status);
    }

    public int countUsers(String keyword, String status) {
        return findManagedUsers(keyword, status).size();
    }

    public List<UserDto> findManagedUsers(String keyword, String status) {
        return userDao.findAllUsers(keyword, status).stream()
                .filter(this::isManagedUser)
                .toList();
    }

    private boolean isManagedUser(UserDto user) {
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        String loginId = user.getLoginId();
        String userName = user.getUserName();
        String email = user.getEmail();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return false;
        }
        if (startsWithIgnoreCase(loginId, "admin")
                || startsWithIgnoreCase(loginId, "test")
                || startsWithIgnoreCase(loginId, "dummy")
                || startsWithIgnoreCase(loginId, "sample")) {
            return false;
        }
        if (containsIgnoreCase(userName, "테스트")
                || containsIgnoreCase(userName, "관리자")
                || containsIgnoreCase(email, "example.com")
                || containsIgnoreCase(email, "findcampus.com")) {
            return false;
        }
        return true;
    }

    private boolean startsWithIgnoreCase(String value, String prefix) {
        return value != null && value.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }
    public UserDto findUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return userDao.findById(userId);
    }

    public List<ItemViewDto> findLostItems(ItemSearchDto searchDto) {
        if (searchDto != null) {
            searchDto.setIncludeHidden(true);
        }
        return itemDao.findLostItems(searchDto);
    }

    public int countLostItems(ItemSearchDto searchDto) {
        if (searchDto != null) {
            searchDto.setIncludeHidden(true);
        }
        return itemDao.countLostItems(searchDto);
    }

    public List<ItemViewDto> findFoundItems(ItemSearchDto searchDto) {
        if (searchDto != null) {
            searchDto.setIncludeHidden(true);
            if ("REGISTERED".equals(searchDto.getStatus())) {
                searchDto.setStatus("STORED");
            }
        }
        return itemDao.findFoundItems(searchDto);
    }

    public int countFoundItems(ItemSearchDto searchDto) {
        if (searchDto != null) {
            searchDto.setIncludeHidden(true);
            if ("REGISTERED".equals(searchDto.getStatus())) {
                searchDto.setStatus("STORED");
            }
        }
        return itemDao.countFoundItems(searchDto);
    }

    public List<ReportViewDto> findReports(ItemSearchDto searchDto) {
        List<ReportViewDto> reports = itemDao.findReports(searchDto);
        reports.forEach(this::normalizeReportView);
        return reports;
    }

    public int countReports(ItemSearchDto searchDto) {
        return itemDao.countReports(searchDto);
    }

    public int countReportsByStatus(String status) {
        ItemSearchDto searchDto = new ItemSearchDto();
        searchDto.setStatus(status);
        return safeCount(() -> itemDao.countReports(searchDto));
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        if (userId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("사용자와 상태를 확인해 주세요.");
        }
        userDao.updateStatus(userId, status);
    }

    @Transactional
    public void updateLostStatus(Long lostId, String status) {
        if (lostId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("분실물과 상태를 확인해 주세요.");
        }
        if (!"REGISTERED".equals(status) && !"RETURNED".equals(status) && !"HIDDEN".equals(status)) {
            throw new IllegalArgumentException("분실물 상태는 접수중, 반환완료, 숨김만 사용할 수 있습니다.");
        }
        ItemViewDto item = itemDao.findLostItemById(lostId);
        itemDao.updateLostStatus(lostId, status);
        notifyItemStatusChanged("lost", item, status);
    }

    @Transactional
    public void updateFoundStatus(Long foundId, String status) {
        if (foundId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("습득물과 상태를 확인해 주세요.");
        }
        if ("REGISTERED".equals(status)) {
            status = "STORED";
        }
        if (!"STORED".equals(status) && !"RETURNED".equals(status) && !"HIDDEN".equals(status)) {
            throw new IllegalArgumentException("습득물 상태는 보관중, 반환완료, 숨김만 사용할 수 있습니다.");
        }
        ItemViewDto item = itemDao.findFoundItemById(foundId);
        itemDao.updateFoundStatus(foundId, status);
        notifyItemStatusChanged("found", item, status);
    }

    @Transactional
    public void updateReportStatus(Long reportId, String status) {
        if (reportId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("신고 번호와 상태를 확인해 주세요.");
        }
        ReportViewDto report = itemDao.findReportById(reportId);
        itemDao.updateReportStatus(reportId, status);
        notifyReportProcessed(report, status);
    }

    private void normalizeReportView(ReportViewDto report) {
        if (report == null) {
            return;
        }
        report.setReason(labelReportReason(report.getReason()));
    }

    private String labelReportReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "신고 접수";
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


    private void notifyItemStatusChanged(String itemType, ItemViewDto item, String status) {
        if (item == null || item.getUserId() == null) {
            return;
        }
        String label = "found".equals(itemType) ? "습득물" : "분실물";
        String targetType = "found".equals(itemType) ? "found" : "lost";
        String title;
        String message;
        if ("RETURNED".equals(status)) {
            title = "등록한 게시글이 반환완료로 변경되었습니다.";
            message = label + " 게시글이 관리자 확인 후 반환완료 처리되었습니다.";
        } else if ("HIDDEN".equals(status)) {
            title = "등록한 게시글이 숨김 처리되었습니다.";
            message = "관리자 확인으로 " + label + " 게시글이 사용자 화면에서 숨김 처리되었습니다.";
        } else {
            title = "등록한 게시글 상태가 변경되었습니다.";
            message = label + " 게시글 상태가 " + itemStatusLabel(status, itemType) + "(으)로 변경되었습니다.";
        }
        try {
            appNotificationService.notifyUser(item.getUserId(), "ITEM_STATUS", title, message, targetType, item.getId());
        } catch (RuntimeException ignored) {
        }
    }

    private void notifyReportProcessed(ReportViewDto report, String status) {
        if (report == null || report.getReporterId() == null) {
            return;
        }
        String title = switch (status) {
            case "REJECTED", "IGNORE" -> "신고가 반려되었습니다.";
            case "POST_HIDDEN" -> "신고가 처리되었습니다.";
            case "POST_RESTORED" -> "게시글 숨김이 해제되었습니다.";
            case "ACCOUNT_SUSPENDED" -> "신고 대상 계정이 정지되었습니다.";
            case "ACCOUNT_RESTORED" -> "신고 대상 계정 정지가 해제되었습니다.";
            default -> "신고 처리 결과가 등록되었습니다.";
        };
        String message = reportStatusLabel(status) + " 처리되었습니다. 신고 관리 기준에 따라 결과가 반영되었습니다.";
        try {
            appNotificationService.notifyUser(report.getReporterId(), "REPORT_RESULT", title, message, "report", report.getReportId());
        } catch (RuntimeException ignored) {
        }
    }

    private String itemStatusLabel(String status, String itemType) {
        if ("RETURNED".equals(status)) {
            return "반환완료";
        }
        if ("HIDDEN".equals(status)) {
            return "숨김";
        }
        if ("found".equals(itemType)) {
            return "보관중";
        }
        return "접수중";
    }

    private String reportStatusLabel(String status) {
        return switch (status) {
            case "WAITING" -> "답변 대기";
            case "POST_HIDDEN" -> "게시글 숨김";
            case "POST_RESTORED" -> "게시글 복구";
            case "ACCOUNT_SUSPENDED" -> "계정 정지";
            case "ACCOUNT_RESTORED" -> "계정 정지 해제";
            case "REJECTED", "IGNORE" -> "신고 반려";
            case "DONE" -> "처리 완료";
            default -> status != null ? status : "처리";
        };
    }

    private int safeCount(Supplier<Integer> supplier) {
        try {
            Integer result = supplier.get();
            return result == null ? 0 : result;
        } catch (RuntimeException ignored) {
            return 0;
        }
    }
}