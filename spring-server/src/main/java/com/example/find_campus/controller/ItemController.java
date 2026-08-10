package com.example.find_campus.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            Long lostId = itemService.createLostItem(dto, userId, images);
            redirectAttributes.addFlashAttribute("message", "분실물이 등록되었습니다.");
            return "redirect:/lost/detail/" + lostId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lost/write";
        } catch (Exception e) {
            log.error("Failed to create lost item", e);
            redirectAttributes.addFlashAttribute("error", "분실물을 등록하지 못했습니다. 입력값과 위치/카테고리 데이터를 확인해 주세요.");
            return "redirect:/lost/write";
        }
    }

    @PostMapping("/lost/edit/{lostId}")
    public String editLost(@PathVariable Long lostId,
                           @ModelAttribute LostItemDto dto,
                           @RequestParam(value = "images", required = false) List<MultipartFile> images,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            itemService.updateLostItem(lostId, dto, userId, images);
            redirectAttributes.addFlashAttribute("message", "분실물이 수정되었습니다.");
            return "redirect:/lost/detail/" + lostId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lost/detail/" + lostId;
        } catch (Exception e) {
            log.error("Failed to update lost item", e);
            redirectAttributes.addFlashAttribute("error", "분실물을 수정하지 못했습니다.");
            return "redirect:/lost/detail/" + lostId;
        }
    }

    @PostMapping("/lost/delete/{lostId}")
    public String deleteLost(@PathVariable Long lostId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            itemService.deleteLostItem(lostId, userId);
            redirectAttributes.addFlashAttribute("message", "분실물이 삭제되었습니다.");
            return "redirect:/lost/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/lost/detail/" + lostId;
        }
    }

    @PostMapping("/found/write")
    public String writeFound(@ModelAttribute FoundItemDto dto,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            Long foundId = itemService.createFoundItem(dto, userId, images);
            redirectAttributes.addFlashAttribute("message", "습득물이 등록되었습니다.");
            return "redirect:/found/detail/" + foundId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/found/write";
        } catch (Exception e) {
            log.error("Failed to create found item", e);
            redirectAttributes.addFlashAttribute("error", "습득물을 등록하지 못했습니다. 입력값과 위치/보관장소 데이터를 확인해 주세요.");
            return "redirect:/found/write";
        }
    }

    @PostMapping("/found/edit/{foundId}")
    public String editFound(@PathVariable Long foundId,
                            @ModelAttribute FoundItemDto dto,
                            @RequestParam(value = "images", required = false) List<MultipartFile> images,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            itemService.updateFoundItem(foundId, dto, userId, images);
            redirectAttributes.addFlashAttribute("message", "습득물이 수정되었습니다.");
            return "redirect:/found/detail/" + foundId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/found/detail/" + foundId;
        } catch (Exception e) {
            log.error("Failed to update found item", e);
            redirectAttributes.addFlashAttribute("error", "습득물을 수정하지 못했습니다.");
            return "redirect:/found/detail/" + foundId;
        }
    }

    @PostMapping("/found/delete/{foundId}")
    public String deleteFound(@PathVariable Long foundId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            itemService.deleteFoundItem(foundId, userId);
            redirectAttributes.addFlashAttribute("message", "습득물이 삭제되었습니다.");
            return "redirect:/found/list";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/found/detail/" + foundId;
        }
    }

    @PostMapping("/report/write")
    public String writeReport(@ModelAttribute ReportDto dto,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/login";
        }
        try {
            itemService.createReport(dto, userId);
            redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다.");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/report/write";
        } catch (Exception e) {
            log.error("Failed to create report", e);
            redirectAttributes.addFlashAttribute("error", "신고를 접수하지 못했습니다.");
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
            redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
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
        if (loginUserId instanceof Integer userId) {
            return userId.longValue();
        }
        if (loginUserId instanceof String userId && !userId.isBlank()) {
            return Long.valueOf(userId);
        }
        return null;
    }
}
