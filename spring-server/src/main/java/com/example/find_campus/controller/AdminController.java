package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.service.AdminService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/dashboard";
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "status", required = false) String status,
                        Model model) {
        model.addAttribute("users", adminService.findUsers(keyword, status));
        model.addAttribute("totalCount", adminService.countUsers(keyword, status));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/user-list";
    }

    @GetMapping("/admin/lost")
    public String lost(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", adminService.findLostItems(searchDto));
        model.addAttribute("totalCount", adminService.countLostItems(searchDto));
        model.addAttribute("search", searchDto);
        return "admin/lost-list";
    }

    @GetMapping("/admin/found")
    public String found(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", adminService.findFoundItems(searchDto));
        model.addAttribute("totalCount", adminService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        return "admin/found-list";
    }

    @GetMapping("/admin/reports")
    public String reports(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("reports", adminService.findReports(searchDto));
        model.addAttribute("totalCount", adminService.countReports(searchDto));
        model.addAttribute("search", searchDto);
        return "admin/report-list";
    }

    @PostMapping("/admin/users/status")
    public String updateUserStatus(@RequestParam Long userId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        adminService.updateUserStatus(userId, status);
        redirectAttributes.addFlashAttribute("message", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/lost/status")
    public String updateLostStatus(@RequestParam Long lostId,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        adminService.updateLostStatus(lostId, status);
        redirectAttributes.addFlashAttribute("message", "Lost item status updated.");
        return "redirect:/admin/lost";
    }

    @PostMapping("/admin/found/status")
    public String updateFoundStatus(@RequestParam Long foundId,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        adminService.updateFoundStatus(foundId, status);
        redirectAttributes.addFlashAttribute("message", "Found item status updated.");
        return "redirect:/admin/found";
    }

    @PostMapping("/admin/reports/status")
    public String updateReportStatus(@RequestParam Long reportId,
                                     @RequestParam String status,
                                     RedirectAttributes redirectAttributes) {
        adminService.updateReportStatus(reportId, status);
        redirectAttributes.addFlashAttribute("message", "Report status updated.");
        return "redirect:/admin/reports";
    }
}
