package com.example.find_campus.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.find_campus.dto.CategoryDto;
import com.example.find_campus.dto.InquiryDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.LocationDto;
import com.example.find_campus.dto.StoragePlaceDto;
import com.example.find_campus.dto.UserDto;
import com.example.find_campus.service.AdminService;
import com.example.find_campus.service.AppNotificationService;
import com.example.find_campus.service.ClaimRequestService;
import com.example.find_campus.service.CommonService;
import com.example.find_campus.service.InquiryService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InquiryService inquiryService;
    private final AppNotificationService appNotificationService;
    private final ClaimRequestService claimRequestService;
    private final CommonService commonService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        model.addAttribute("recentItems", adminService.findRecentDashboardItems());
        model.addAttribute("recentReports", adminService.findRecentDashboardReports());
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "status", required = false) String status,
                        Model model) {
        model.addAttribute("users", adminService.findUsers(keyword, status));
        model.addAttribute("totalCount", adminService.countUsers(keyword, status));
        model.addAttribute("allUserCount", adminService.countUsers(null, null));
        model.addAttribute("activeCount", adminService.countUsers(null, "ACTIVE"));
        model.addAttribute("suspendedCount", adminService.countUsers(null, "SUSPENDED"));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/user-list";
    }
    @GetMapping("/admin/users/detail")
    public String userDetail(@RequestParam Long userId,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        UserDto user = adminService.findUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/admin/users";
        }
        model.addAttribute("user", user);
        return "admin/user-detail";
    }

    @GetMapping("/admin/lost")
    public String lost(@ModelAttribute ItemSearchDto searchDto, Model model) {
        List<ItemViewDto> items = adminService.findLostItems(searchDto);
        model.addAttribute("items", items);
        model.addAttribute("totalCount", adminService.countLostItems(searchDto));
        model.addAttribute("search", searchDto);
        model.addAttribute("registeredCount", countByStatus(items, "REGISTERED"));
        model.addAttribute("returnedCount", countByStatus(items, "RETURNED"));
        model.addAttribute("hiddenCount", countByStatus(items, "HIDDEN"));
        model.addAttribute("categories", commonService.findCategories());
        return "admin/lost-list";
    }

    @GetMapping("/admin/found")
    public String found(@ModelAttribute ItemSearchDto searchDto, Model model) {
        String requestedStatus = searchDto != null ? searchDto.getStatus() : null;
        boolean claimWaitingFilter = "CLAIM_WAITING".equals(requestedStatus);
        if (claimWaitingFilter) {
            searchDto.setStatus(null);
        }

        List<ItemViewDto> items = adminService.findFoundItems(searchDto);
        long claimWaitingCount = applyClaimWaitingStatus(items);
        if (claimWaitingFilter) {
            items = items.stream()
                    .filter(item -> "CLAIM_WAITING".equals(item.getStatus()))
                    .toList();
        }
        if (searchDto != null) {
            searchDto.setStatus(requestedStatus);
        }

        model.addAttribute("items", items);
        model.addAttribute("totalCount", claimWaitingFilter ? items.size() : adminService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        model.addAttribute("claimWaitingCount", claimWaitingCount);
        model.addAttribute("storedCount", countByStatus(items, "STORED") + countByStatus(items, "REGISTERED"));
        model.addAttribute("returnedCount", countByStatus(items, "RETURNED"));
        model.addAttribute("hiddenCount", countByStatus(items, "HIDDEN"));
        model.addAttribute("categories", commonService.findCategories());
        return "admin/found-list";
    }

    @GetMapping("/admin/storage")
    public String storage(@RequestParam(value = "editId", required = false) Long editId, Model model) {
        List<StoragePlaceDto> storagePlaces = commonService.findAllStoragePlaces();
        model.addAttribute("storagePlaces", storagePlaces);
        model.addAttribute("locations", commonService.findLocations());
        model.addAttribute("editStorage", commonService.findStoragePlaceById(editId));
        model.addAttribute("totalCount", storagePlaces.size());
        model.addAttribute("activeCount", storagePlaces.stream().filter(s -> "Y".equals(s.getUseYn())).count());
        return "admin/storage-list";
    }

    @GetMapping("/admin/locations")
    public String locations(@RequestParam(value = "editId", required = false) Long editId, Model model) {
        List<LocationDto> locations = commonService.findAllLocations();
        model.addAttribute("locations", locations);
        model.addAttribute("editLocation", commonService.findLocationById(editId));
        model.addAttribute("totalCount", locations.size());
        model.addAttribute("activeCount", locations.stream().filter(l -> "Y".equals(l.getUseYn())).count());
        return "admin/location-list";
    }

    @GetMapping("/admin/categories")
    public String categories(@RequestParam(value = "editId", required = false) Long editId, Model model) {
        List<CategoryDto> categories = commonService.findAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("editCategory", commonService.findCategoryById(editId));
        model.addAttribute("totalCount", categories.size());
        model.addAttribute("activeCount", categories.stream().filter(c -> "Y".equals(c.getUseYn())).count());
        return "admin/category-list";
    }

    @GetMapping("/admin/reports")
    public String reports(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("reports", adminService.findReports(searchDto));
        model.addAttribute("totalCount", adminService.countReports(searchDto));
        model.addAttribute("waitingCount", adminService.countReportsByStatus("WAITING"));
        model.addAttribute("doneCount", adminService.countReportsByStatus("POST_HIDDEN") + adminService.countReportsByStatus("POST_RESTORED") + adminService.countReportsByStatus("ACCOUNT_SUSPENDED") + adminService.countReportsByStatus("ACCOUNT_RESTORED"));
        model.addAttribute("rejectedCount", adminService.countReportsByStatus("REJECTED") + adminService.countReportsByStatus("IGNORE"));
        model.addAttribute("search", searchDto);
        return "admin/report-list";
    }

    @GetMapping("/admin/inquiries")
    public String inquiries(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("inquiries", inquiryService.findInquiries(searchDto));
        model.addAttribute("totalCount", inquiryService.countInquiries(searchDto));
        model.addAttribute("waitingCount", inquiryService.countWaitingInquiries());
        model.addAttribute("answeredCount", inquiryService.countAnsweredInquiries());
        model.addAttribute("closedCount", inquiryService.countClosedInquiries());
        model.addAttribute("search", searchDto);
        return "admin/inquiry-list";
    }


    @GetMapping("/admin/notifications")
    public String adminNotifications(Model model) {
        int waitingClaimCount = claimRequestService.countClaims("WAITING");
        model.addAttribute("notifications", appNotificationService.findAdminNotifications());
        model.addAttribute("claimAlerts", claimRequestService.findAdminClaims("WAITING"));
        model.addAttribute("claimAlertCount", waitingClaimCount);
        model.addAttribute("unreadCount", appNotificationService.countAdminUnread());
        return "admin/notification-list";
    }

    @GetMapping("/admin/notifications/open/{notificationId}")
    public String openAdminNotification(@PathVariable Long notificationId) {
        return "redirect:" + appNotificationService.openAdminNotification(notificationId);
    }

    @PostMapping("/admin/locations/save")
    public String saveLocation(@ModelAttribute LocationDto location, RedirectAttributes redirectAttributes) {
        commonService.saveLocation(location);
        redirectAttributes.addFlashAttribute("message", "\uC704\uCE58 \uC815\uBCF4\uAC00 \uC800\uC7A5\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/locations";
    }

    @PostMapping("/admin/locations/status")
    public String updateLocationStatus(@RequestParam Long locationId, @RequestParam String useYn, RedirectAttributes redirectAttributes) {
        commonService.updateLocationUseYn(locationId, useYn);
        redirectAttributes.addFlashAttribute("message", "\uC704\uCE58 \uC0AC\uC6A9 \uC5EC\uBD80\uAC00 \uBCC0\uACBD\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/locations";
    }

    @PostMapping("/admin/locations/delete")
    public String deleteLocation(@RequestParam Long locationId, RedirectAttributes redirectAttributes) {
        commonService.deleteLocation(locationId);
        redirectAttributes.addFlashAttribute("message", "\uC704\uCE58\uAC00 \uC0AD\uC81C\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/locations";
    }

    @PostMapping("/admin/storage/save")
    public String saveStorage(@ModelAttribute StoragePlaceDto storagePlace, RedirectAttributes redirectAttributes) {
        commonService.saveStoragePlace(storagePlace);
        redirectAttributes.addFlashAttribute("message", "\uBCF4\uAD00 \uC7A5\uC18C \uC815\uBCF4\uAC00 \uC800\uC7A5\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/storage";
    }

    @PostMapping("/admin/storage/status")
    public String updateStorageStatus(@RequestParam Long storageId, @RequestParam String useYn, RedirectAttributes redirectAttributes) {
        commonService.updateStoragePlaceUseYn(storageId, useYn);
        redirectAttributes.addFlashAttribute("message", "\uBCF4\uAD00 \uC7A5\uC18C \uC0AC\uC6A9 \uC5EC\uBD80\uAC00 \uBCC0\uACBD\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/storage";
    }

    @PostMapping("/admin/storage/delete")
    public String deleteStorage(@RequestParam Long storageId, RedirectAttributes redirectAttributes) {
        commonService.deleteStoragePlace(storageId);
        redirectAttributes.addFlashAttribute("message", "\uBCF4\uAD00 \uC7A5\uC18C\uAC00 \uC0AD\uC81C\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/storage";
    }

    @PostMapping("/admin/categories/save")
    public String saveCategory(@ModelAttribute CategoryDto category, RedirectAttributes redirectAttributes) {
        commonService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "\uCE74\uD14C\uACE0\uB9AC \uC815\uBCF4\uAC00 \uC800\uC7A5\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/status")
    public String updateCategoryStatus(@RequestParam Long categoryId, @RequestParam String useYn, RedirectAttributes redirectAttributes) {
        commonService.updateCategoryUseYn(categoryId, useYn);
        redirectAttributes.addFlashAttribute("message", "\uCE74\uD14C\uACE0\uB9AC \uC0AC\uC6A9 \uC5EC\uBD80\uAC00 \uBCC0\uACBD\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/admin/categories/delete")
    public String deleteCategory(@RequestParam Long categoryId, RedirectAttributes redirectAttributes) {
        commonService.deleteCategory(categoryId);
        redirectAttributes.addFlashAttribute("message", "\uCE74\uD14C\uACE0\uB9AC\uAC00 \uC0AD\uC81C\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        return "redirect:/admin/categories";
    }
    @PostMapping("/admin/users/status")
    public String updateUserStatus(@RequestParam Long userId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        adminService.updateUserStatus(userId, status);
        redirectAttributes.addFlashAttribute("message", "사용자 상태가 변경되었습니다.");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/lost/status")
    public String updateLostStatus(@RequestParam Long lostId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        adminService.updateLostStatus(lostId, status);
        redirectAttributes.addFlashAttribute("message", "분실물 상태가 변경되었습니다.");
        return "redirect:/admin/lost";
    }

    @PostMapping("/admin/found/status")
    public String updateFoundStatus(@RequestParam Long foundId,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        adminService.updateFoundStatus(foundId, status);
        if ("RETURNED".equals(status)) {
            claimRequestService.markWaitingClaimsReturnedForFound(foundId);
        } else if ("STORED".equals(status) || "HIDDEN".equals(status)) {
            claimRequestService.rejectWaitingClaimsForFound(foundId);
        }
        redirectAttributes.addFlashAttribute("message", "습득물 상태가 변경되었습니다.");
        return "redirect:/admin/found";
    }

    @PostMapping("/admin/reports/status")
    public String updateReportStatus(@RequestParam Long reportId,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        adminService.updateReportStatus(reportId, status);
        redirectAttributes.addFlashAttribute("message", "신고 상태가 변경되었습니다.");
        return "redirect:/admin/reports";
    }
    @PostMapping("/admin/reports/hide-target")
    public String hideReportedTarget(@RequestParam Long reportId,
                                     @RequestParam String targetType,
                                     @RequestParam Long targetId,
                                     RedirectAttributes redirectAttributes) {
        if ("lost".equals(targetType)) {
            adminService.updateLostStatus(targetId, "HIDDEN");
            adminService.updateReportStatus(reportId, "POST_HIDDEN");
            redirectAttributes.addFlashAttribute("message", "신고 대상 분실물 게시글을 숨김 처리했습니다.");
        } else if ("found".equals(targetType)) {
            adminService.updateFoundStatus(targetId, "HIDDEN");
            adminService.updateReportStatus(reportId, "POST_HIDDEN");
            redirectAttributes.addFlashAttribute("message", "신고 대상 습득물 게시글을 숨김 처리했습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "게시글 숨김은 분실물/습득물 신고에서만 사용할 수 있습니다.");
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/admin/reports/show-target")
    public String showReportedTarget(@RequestParam Long reportId,
                                     @RequestParam String targetType,
                                     @RequestParam Long targetId,
                                     RedirectAttributes redirectAttributes) {
        if ("lost".equals(targetType)) {
            adminService.updateLostStatus(targetId, "REGISTERED");
            adminService.updateReportStatus(reportId, "POST_RESTORED");
            redirectAttributes.addFlashAttribute("message", "신고 대상 분실물 게시글을 다시 보이게 처리했습니다.");
        } else if ("found".equals(targetType)) {
            adminService.updateFoundStatus(targetId, "STORED");
            adminService.updateReportStatus(reportId, "POST_RESTORED");
            redirectAttributes.addFlashAttribute("message", "신고 대상 습득물 게시글을 다시 보이게 처리했습니다.");
        } else {
            redirectAttributes.addFlashAttribute("error", "게시글 복구는 분실물/습득물 신고에서만 사용할 수 있습니다.");
        }
        return "redirect:/admin/reports";
    }

    @PostMapping("/admin/reports/suspend-target")
    public String suspendReportedTarget(@RequestParam Long userId,
                                        @RequestParam(required = false) Long reportId,
                                        RedirectAttributes redirectAttributes) {
        adminService.updateUserStatus(userId, "SUSPENDED");
        if (reportId != null) {
            adminService.updateReportStatus(reportId, "ACCOUNT_SUSPENDED");
        }
        redirectAttributes.addFlashAttribute("message", "신고 대상 계정을 정지했습니다.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/admin/reports/activate-target")
    public String activateReportedTarget(@RequestParam Long userId,
                                         @RequestParam(required = false) Long reportId,
                                         RedirectAttributes redirectAttributes) {
        adminService.updateUserStatus(userId, "ACTIVE");
        if (reportId != null) {
            adminService.updateReportStatus(reportId, "ACCOUNT_RESTORED");
        }
        redirectAttributes.addFlashAttribute("message", "신고 대상 계정의 정지를 해제했습니다.");
        return "redirect:/admin/reports";
    }

    @GetMapping("/admin/inquiries/{inquiryId}")
    public String inquiryDetail(@PathVariable Long inquiryId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        InquiryDto inquiry = inquiryService.findInquiryById(inquiryId);
        if (inquiry == null) {
            redirectAttributes.addFlashAttribute("error", "\uBB38\uC758\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.");
            return "redirect:/admin/inquiries";
        }
        model.addAttribute("inquiry", inquiry);
        return "admin/inquiry-detail";
    }

    @PostMapping("/admin/inquiries/answer")
    public String answerInquiry(@RequestParam Long inquiryId,
                                @RequestParam String answerContent,
                                RedirectAttributes redirectAttributes) {
        try {
            inquiryService.answerInquiry(inquiryId, answerContent, null);
            redirectAttributes.addFlashAttribute("message", "\uB2F5\uBCC0\uC774 \uC800\uC7A5\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/inquiries/" + inquiryId;
    }
    @PostMapping("/admin/inquiries/status")
    public String updateInquiryStatus(@RequestParam Long inquiryId,
                                      @RequestParam String status,
                                      RedirectAttributes redirectAttributes) {
        inquiryService.updateInquiryStatus(inquiryId, status);
        redirectAttributes.addFlashAttribute("message", "문의 상태가 변경되었습니다.");
        return "redirect:/admin/inquiries";
    }


    @GetMapping("/admin/claims")
    public String claimRequests() {
        return "redirect:/admin/found?status=CLAIM_WAITING";
    }

    @PostMapping("/admin/claims/status")
    public String updateClaimStatus(@RequestParam Long claimId,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            claimRequestService.updateClaimStatus(claimId, status);
            redirectAttributes.addFlashAttribute("message", "수령 신청 상태가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/claims";
    }


    private long applyClaimWaitingStatus(List<ItemViewDto> items) {
        long count = 0;
        for (ItemViewDto item : items) {
            if (item == null || item.getId() == null) {
                continue;
            }
            if ("RETURNED".equals(item.getStatus()) || "HIDDEN".equals(item.getStatus())) {
                continue;
            }
            if (claimRequestService.hasWaitingClaimForFound(item.getId())) {
                item.setStatus("CLAIM_WAITING");
                count++;
            }
        }
        return count;
    }

    private long countByStatus(List<ItemViewDto> items, String status) {
        return items.stream().filter(item -> status.equals(item.getStatus())).count();
    }
}