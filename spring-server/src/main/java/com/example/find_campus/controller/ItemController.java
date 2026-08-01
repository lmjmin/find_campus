package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import java.util.List;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.find_campus.dto.FoundItemDto;
import com.example.find_campus.dto.LostItemDto;
import com.example.find_campus.dto.ProfileUpdateDto;
import com.example.find_campus.dto.ReportDto;
import com.example.find_campus.dto.UserDto;
import com.example.find_campus.service.ItemService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping("/lost/write")
    public String writeLost(@ModelAttribute LostItemDto dto,
                            @RequestParam(value = "images", required = false) List<MultipartFile> images,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Login is required.");
            return "redirect:/login";
        }
        try {
            Long lostId = itemService.createLostItem(dto, userId, images);
            redirectAttributes.addFlashAttribute("message", "???? ???????.");
            return "redirect:/lost/detail/" + lostId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lost/write";
        } catch (Exception e) {
            log.error("Failed to create lost item", e);
            redirectAttributes.addFlashAttribute("error", "Cannot create lost item.");
            return "redirect:/lost/write";
        }
    }

    @PostMapping("/found/write")
    public String writeFound(@ModelAttribute FoundItemDto dto,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Login is required.");
            return "redirect:/login";
        }
        try {
            Long foundId = itemService.createFoundItem(dto, userId, images);
            redirectAttributes.addFlashAttribute("message", "???? ???????.");
            return "redirect:/found/detail/" + foundId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/found/write";
        } catch (Exception e) {
            log.error("Failed to create found item", e);
            redirectAttributes.addFlashAttribute("error", "Cannot create found item.");
            return "redirect:/found/write";
        }
    }

    @PostMapping("/report/write")
    public String writeReport(@ModelAttribute ReportDto dto,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Login is required.");
            return "redirect:/login";
        }
        try {
            itemService.createReport(dto, userId);
            redirectAttributes.addFlashAttribute("message", "??? ???????.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/report/write";
        } catch (Exception e) {
            log.error("Failed to create report", e);
            redirectAttributes.addFlashAttribute("error", "Cannot create report.");
            return "redirect:/report/write";
        }
    }

    @PostMapping("/mypage/edit")
    public String editMypage(@ModelAttribute ProfileUpdateDto dto,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            Long userId = getLoginUserId(session);
            if (userId == null) {
                return "redirect:/login";
            }
            UserDto updatedUser = itemService.updateProfile(dto, userId);
            session.setAttribute("loginUser", updatedUser);
            session.setAttribute("loginName", updatedUser.getUserName());
            redirectAttributes.addFlashAttribute("message", "??? ???????.");
            return "redirect:/mypage";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/edit";
        }
    }

    private Long getLoginUserId(HttpSession session) {
        Object loginUserId = session.getAttribute("loginUserId");
        if (loginUserId instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
