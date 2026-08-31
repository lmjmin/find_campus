package com.example.find_campus.controller;

import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.find_campus.dto.InquiryDto;
import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.ReportDto;
import com.example.find_campus.dto.UserDto;
import com.example.find_campus.service.AiRecommendationService;
import com.example.find_campus.service.AppNotificationService;
import com.example.find_campus.service.ChatService;
import com.example.find_campus.service.ClaimRequestService;
import com.example.find_campus.service.CommonService;
import com.example.find_campus.service.InquiryService;
import com.example.find_campus.service.ItemService;
import com.example.find_campus.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestController {

    private static final List<String> CAMPUS_LOCATION_NAMES = List.of(
            "1. 대학본관",
            "2. 보건복지관",
            "3. 건축디자인관",
            "4. 대학원",
            "5. 건축공학실습동",
            "6. 학생회관",
            "7. 동명스타디움",
            "8. 학생복지관",
            "9. 동명생활관 (2호관)",
            "10. 중앙도서관",
            "11. 동명관",
            "12. 반려동물관",
            "13. ICT 2관",
            "14. ICT 3관",
            "15. ICT 1관",
            "16. 교수연구동",
            "17. 국제대학관",
            "18. 동명공업고등학교",
            "19. 국제대학 본관",
            "20. 쉼터(편의점)",
            "21. 국제산학협력관(학생군사관)",
            "22. 가온누리(학생휴게점)",
            "23. 선당",
            "24. 창의인재관",
            "25. 분수대",
            "26. 용마광장",
            "27. 제1정문",
            "28. 제2정문",
            "29. 제3정문",
            "30. 제1주차장",
            "31. 제2주차장",
            "32. 대운동장 1",
            "33. 대운동장 2",
            "34. 원형광장",
            "35. 주차관제소",
            "36. 종합체육시설",
            "37. 제3주차장",
            "38. 제5주차장",
            "39. 순환버스 정류장",
            "40. 마지뜨락",
            "41. 분수대",
            "42. 학생휴게점(cafe)",
            "43. 동명생활관 (1호관)",
            "44. 동명뜰",
            "45. 체육시설관리동",
            "46. 그린스타트업타운",
            "47. 대학동물병원",
            "48. 창업거점지구"
    );

    private final ItemService itemService;
    private final CommonService commonService;
    private final UserService userService;
    private final InquiryService inquiryService;
    private final AiRecommendationService aiRecommendationService;
    private final AppNotificationService appNotificationService;
    private final ChatService chatService;
    private final ClaimRequestService claimRequestService;

    @GetMapping({"/", "/main"})
    public String main(Model model) {
        ItemSearchDto searchDto = new ItemSearchDto();
        model.addAttribute("lostCount", itemService.countLostItems(searchDto));
        model.addAttribute("foundCount", itemService.countFoundItems(searchDto));
        model.addAttribute("recentLostItems", itemService.findRecentItems("lost", 5));
        model.addAttribute("recentFoundItems", itemService.findRecentItems("found", 5));
        return "index";
    }


    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/join")
    public String join() {
        return "user/join";
    }

    @GetMapping("/profile/{id}")
    public String userProfile(@PathVariable("id") Long id, Model model) {
        model.addAttribute("userId", id);
        model.addAttribute("profileUser", userService.findById(id));
        model.addAttribute("lostItems", itemService.findLostItemsByUserId(id));
        model.addAttribute("foundItems", itemService.findFoundItemsByUserId(id));
        return "user/profile";
    }

    @GetMapping("/profile")
    public String userProfileDefault(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("userId", userId);
        model.addAttribute("profileUser", userService.findById(userId));
        model.addAttribute("lostItems", itemService.findLostItemsByUserId(userId));
        model.addAttribute("foundItems", itemService.findFoundItemsByUserId(userId));
        return "user/profile";
    }

    @GetMapping("/lost/list")
    public String lostList(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", itemService.findLostItems(searchDto));
        model.addAttribute("totalCount", itemService.countLostItems(searchDto));
        model.addAttribute("search", searchDto);
        addCommonListModel(model);
        return "lost/list";
    }

    @GetMapping("/lost/write")
    public String lostWrite(Model model) {
        addCommonWriteModel(model);
        model.addAttribute("editMode", false);
        return "lost/write";
    }

    @GetMapping("/lost/edit/{id}")
    public String lostEdit(@PathVariable("id") Long id,
                           jakarta.servlet.http.HttpSession session,
                           Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("item", itemService.findLostItemForEdit(id, userId));
        model.addAttribute("editMode", true);
        addCommonWriteModel(model);
        return "lost/write";
    }

    @GetMapping("/lost/detail")
    public String lostDetail() {
        return "redirect:/lost/list";
    }

    @GetMapping("/lost/detail/{id}")
    public String lostDetailById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("item", itemService.findLostItem(id));
        model.addAttribute("lostId", id);
        return "lost/detail";
    }

    @GetMapping("/found/list")
    public String foundList(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", itemService.findFoundItems(searchDto));
        model.addAttribute("totalCount", itemService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        addCommonListModel(model);
        return "found/list";
    }

    @GetMapping("/found/write")
    public String foundWrite(Model model) {
        addCommonWriteModel(model);
        model.addAttribute("storagePlaces", commonService.findStoragePlaces());
        model.addAttribute("editMode", false);
        return "found/write";
    }

    @GetMapping("/found/edit/{id}")
    public String foundEdit(@PathVariable("id") Long id,
                            jakarta.servlet.http.HttpSession session,
                            Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("item", itemService.findFoundItemForEdit(id, userId));
        model.addAttribute("editMode", true);
        addCommonWriteModel(model);
        model.addAttribute("storagePlaces", commonService.findStoragePlaces());
        return "found/write";
    }

    @GetMapping("/found/detail")
    public String foundDetail() {
        return "redirect:/found/list";
    }

    @GetMapping("/found/detail/{id}")
    public String foundDetailById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("item", itemService.findFoundItem(id));
        model.addAttribute("foundId", id);
        return "found/detail";
    }

    @GetMapping("/chat")
    public String chatDefault() {
        return "redirect:/chat/rooms";
    }

    @GetMapping("/chat/rooms")
    public String chatRooms(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("rooms", chatService.findRooms(userId));
        return "chat/rooms";
    }

    @GetMapping("/chat/room")
    public String chatRoom() {
        return "redirect:/chat/rooms";
    }

    @GetMapping("/chat/room/{id}")
    public String chatRoomById(@PathVariable("id") Long id,
                               jakarta.servlet.http.HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            com.example.find_campus.dto.ChatRoomDto selectedRoom = chatService.findRoom(id, userId);
            if (selectedRoom == null) {
                chatService.repairRoomAccess(id, userId);
                selectedRoom = chatService.findRoom(id, userId);
            }
            if (selectedRoom == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "채팅방을 열 수 없습니다. 다시 채팅하기를 눌러 주세요.");
                return "redirect:/chat/rooms";
            }
            model.addAttribute("selectedRoom", selectedRoom);
            model.addAttribute("messages", chatService.findMessages(id, userId));
            model.addAttribute("rooms", chatService.findRooms(userId));
            model.addAttribute("roomId", id);
            return "chat/room";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/chat/rooms";
        }
    }

    @GetMapping("/chat/start/{itemType}/{itemId}")
    public String startChat(@PathVariable("itemType") String itemType,
                            @PathVariable("itemId") Long itemId,
                            jakarta.servlet.http.HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            Long roomId = chatService.findOrCreateRoom(itemType, itemId, userId);
            return "redirect:/chat/room/" + roomId;
        } catch (RuntimeException e) {
            String normalizedType = "found".equalsIgnoreCase(itemType) || "습득물".equals(itemType) ? "found" : "lost";
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/" + normalizedType + "/detail/" + itemId;
        }
    }

    @PostMapping("/chat/room/{id}/messages")
    public String sendChatMessage(@PathVariable("id") Long id,
                                  @RequestParam("messageContent") String messageContent,
                                  jakarta.servlet.http.HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            chatService.sendMessage(id, userId, messageContent);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/chat/room/" + id;
    }

    @PostMapping("/chat/room/{id}/mute")
    public String muteChatRoom(@PathVariable("id") Long id,
                               @RequestParam("muteYn") String muteYn,
                               jakarta.servlet.http.HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            chatService.setMute(id, userId, "Y".equalsIgnoreCase(muteYn));
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/chat/room/" + id;
    }

    @PostMapping("/chat/room/{id}/leave")
    public String leaveChatRoom(@PathVariable("id") Long id,
                                jakarta.servlet.http.HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            chatService.leaveRoom(id, userId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/chat/rooms";
    }

    @PostMapping("/chat/room/{id}/block")
    public String blockChatRoom(@PathVariable("id") Long id,
                                jakarta.servlet.http.HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            chatService.blockRoom(id, userId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/chat/rooms";
    }

    @GetMapping("/report")
    public String reportDefault() {
        return "redirect:/report/write";
    }

    @GetMapping("/report/write")
    public String reportWrite(@RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "id", required = false) Long id,
                              Model model) {
        model.addAttribute("reportType", type);
        model.addAttribute("targetId", id);
        if (!model.containsAttribute("report")) {
            ReportDto report = new ReportDto();
            report.setTargetType(type);
            report.setTargetId(id);
            model.addAttribute("report", report);
        }
        addReportTargetModel(type, id, model);
        return "report/write";
    }

    @GetMapping("/match/select-lost")
    public String selectLost() {
        return "match/select-lost";
    }

    @GetMapping("/match/select-lost/{foundId}")
    public String selectLostByFoundId(@PathVariable("foundId") Long foundId,
                                      jakarta.servlet.http.HttpSession session,
                                      Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostItems", itemService.findLostItemsByUserId(userId));
        return "match/select-lost";
    }

    @GetMapping("/match/result")
    public String matchResult(@RequestParam(value = "foundId", required = false) Long foundId,
                              @RequestParam(value = "lostId", required = false) Long lostId,
                              Model model) {
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostId", lostId);
        if (foundId != null) {
            model.addAttribute("foundItem", itemService.findFoundItem(foundId));
        }
        if (lostId != null) {
            model.addAttribute("lostItem", itemService.findLostItem(lostId));
        }
        int matchScorePercent = claimRequestService.calculateScorePercent(foundId, lostId);
        if (foundId != null && lostId != null) {
            try {
                com.example.find_campus.dto.AiRecommendationDto matchRecommendation =
                        aiRecommendationService.comparePair("lost", lostId, "found", foundId);
                if (matchRecommendation != null) {
                    model.addAttribute("matchRecommendation", matchRecommendation);
                }
            } catch (RuntimeException ignored) {
            }
        }
        model.addAttribute("matchScorePercent", matchScorePercent);
        model.addAttribute("minClaimScorePercent", ClaimRequestService.MIN_CLAIM_SCORE_PERCENT);
        model.addAttribute("claimAllowed", matchScorePercent >= ClaimRequestService.MIN_CLAIM_SCORE_PERCENT);
        return "match/result";
    }

    @GetMapping("/match/storage-guide")
    public String storageGuide(@RequestParam(value = "foundId", required = false) Long foundId,
                               @RequestParam(value = "lostId", required = false) Long lostId,
                               jakarta.servlet.http.HttpSession session,
                               Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        int matchScorePercent = claimRequestService.calculateScorePercent(foundId, lostId);
        boolean hasClaim = claimRequestService.hasActiveClaim(foundId, lostId, userId);
        if (foundId != null && lostId != null && !hasClaim && matchScorePercent < ClaimRequestService.MIN_CLAIM_SCORE_PERCENT) {
            return "redirect:/match/result?foundId=" + foundId + "&lostId=" + lostId;
        }
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostId", lostId);
        if (foundId != null) {
            model.addAttribute("foundItem", itemService.findFoundItem(foundId));
        }
        if (lostId != null) {
            model.addAttribute("lostItem", itemService.findLostItem(lostId));
        }
        if (foundId != null && lostId != null) {
            try {
                com.example.find_campus.dto.AiRecommendationDto matchRecommendation =
                        aiRecommendationService.comparePair("lost", lostId, "found", foundId);
                if (matchRecommendation != null) {
                    model.addAttribute("matchRecommendation", matchRecommendation);
                }
            } catch (RuntimeException ignored) {
            }
        }
        model.addAttribute("matchScorePercent", matchScorePercent);
        model.addAttribute("minClaimScorePercent", ClaimRequestService.MIN_CLAIM_SCORE_PERCENT);
        return "match/storage-guide";
    }

    @PostMapping("/match/request")
    public String submitClaimRequest(@RequestParam(value = "foundId", required = false) Long foundId,
                                     @RequestParam(value = "lostId", required = false) Long lostId,
                                     jakarta.servlet.http.HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        try {
            claimRequestService.submitClaim(foundId, lostId, userId);
            redirectAttributes.addFlashAttribute("claimMessage", "\uC218\uB839 \uC2E0\uCCAD\uC774 \uAD00\uB9AC\uC790\uC5D0\uAC8C \uC804\uB2EC\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uAD00\uB9AC\uC790 \uD655\uC778 \uD6C4 \uBC18\uD658 \uC808\uCC28\uAC00 \uC9C4\uD589\uB429\uB2C8\uB2E4.");
            return "redirect:/match/storage-guide?foundId=" + foundId + "&lostId=" + lostId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("claimError", e.getMessage());
            return "redirect:/match/result?foundId=" + foundId + "&lostId=" + lostId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("claimError", "\uC218\uB839 \uC2E0\uCCAD \uC911 \uC624\uB958\uAC00 \uBC1C\uC0DD\uD588\uC2B5\uB2C8\uB2E4. \uC120\uD0DD\uD55C \uAC8C\uC2DC\uAE00\uC744 \uB2E4\uC2DC \uD655\uC778\uD574 \uC8FC\uC138\uC694.");
            return "redirect:/match/result?foundId=" + foundId + "&lostId=" + lostId;
        }
    }

    @GetMapping("/search")
    public String search(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("lostItems", itemService.findLostItems(searchDto));
        model.addAttribute("foundItems", itemService.findFoundItems(searchDto));
        model.addAttribute("lostCount", itemService.countLostItems(searchDto));
        model.addAttribute("foundCount", itemService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        addCommonListModel(model);
        return "search/search";
    }

    @GetMapping({"/recommend", "/recommend/list"})
    public String recommendList(@RequestParam(value = "targetType", required = false) String targetType,
                                @RequestParam(value = "targetId", required = false) Long targetId,
                                jakarta.servlet.http.HttpSession session,
                                Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<ItemViewDto> lostItems = itemService.findLostItemsByUserId(userId);
        List<ItemViewDto> foundItems = itemService.findFoundItemsByUserId(userId);

        if ((targetType == null || targetId == null) && !lostItems.isEmpty()) {
            targetType = "lost";
            targetId = lostItems.get(0).getId();
        } else if ((targetType == null || targetId == null) && !foundItems.isEmpty()) {
            targetType = "found";
            targetId = foundItems.get(0).getId();
        }

        model.addAttribute("lostItems", lostItems);
        model.addAttribute("foundItems", foundItems);
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);

        if (targetType != null && targetId != null) {
            if ("lost".equals(targetType)) {
                model.addAttribute("targetItem", itemService.findLostItemForEdit(targetId, userId));
            } else if ("found".equals(targetType)) {
                model.addAttribute("targetItem", itemService.findFoundItemForEdit(targetId, userId));
            }
            model.addAttribute("recommendations", aiRecommendationService.recommendNow(targetType, targetId));
        }
        return "recommend/list";
    }

    @GetMapping("/map")
    public String map() {
        return "map/map";
    }

    @GetMapping("/mypage")
    public String mypage(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("loginUser", userService.findById(userId));
        model.addAttribute("lostItems", itemService.findLostItemsByUserId(userId));
        model.addAttribute("foundItems", itemService.findFoundItemsByUserId(userId));
        return "mypage/mypage";
    }

    @GetMapping("/mypage/edit")
    public String mypageEdit(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("loginUser", userService.findById(userId));
        return "mypage/edit";
    }

    @GetMapping({"/setting", "/mypage/setting"})
    public String setting(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId != null) {
            model.addAttribute("loginUser", userService.findById(userId));
        }
        return "mypage/setting";
    }

    @GetMapping({"/notification", "/mypage/notification"})
    public String notification(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("notifications", appNotificationService.findNotifications(userId));
        model.addAttribute("unreadCount", appNotificationService.countUnread(userId));
        return "mypage/notification";
    }

    @PostMapping("/notification/read-all")
    public String markAllNotificationsRead(jakarta.servlet.http.HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        appNotificationService.markAllRead(userId);
        return "redirect:/notification";
    }

    @PostMapping("/notification/{notificationId}/read")
    public String markNotificationRead(@PathVariable Long notificationId,
                                       @RequestParam(required = false) String targetType,
                                       jakarta.servlet.http.HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        appNotificationService.markRead(notificationId, userId);
        if ("inquiry".equalsIgnoreCase(targetType)) {
            return "redirect:/mypage/setting/inquiry#inquiryHistory";
        }
        return "redirect:/notification";
    }

    @GetMapping("/mypage/privacy")
    public String mypagePrivacy() {
        return "mypage/privacy";
    }

    @GetMapping("/mypage/setting/notice")
    public String notice() {
        return "mypage/setting/notice";
    }

    @GetMapping("/mypage/setting/guide")
    public String guide() {
        return "mypage/setting/guide";
    }

    @GetMapping("/mypage/setting/inquiry")
    public String inquiry(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = getLoginUserId(session);
        if (userId == null) {
            return "redirect:/login";
        }
        if (!model.containsAttribute("inquiry")) {
            model.addAttribute("inquiry", new InquiryDto());
        }
        model.addAttribute("userInquiries", inquiryService.findUserInquiries(userId));


        return "mypage/setting/inquiry";
    }

    @PostMapping("/mypage/setting/inquiry")
    public String submitInquiry(@ModelAttribute InquiryDto inquiry,
                                jakarta.servlet.http.HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = getLoginUserId(session);
        inquiry.setUserId(userId);

        try {
            inquiryService.createInquiry(inquiry);
            redirectAttributes.addFlashAttribute("inquiryMessage", "\uBB38\uC758\uAC00 \uC811\uC218\uB418\uC5C8\uC2B5\uB2C8\uB2E4. \uAD00\uB9AC\uC790 \uB2F5\uBCC0\uC744 \uAE30\uB2E4\uB824\uC8FC\uC138\uC694.");
            return "redirect:/mypage/setting/inquiry#inquiryHistory";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("inquiry", inquiry);
            redirectAttributes.addFlashAttribute("inquiryError", e.getMessage());
            redirectAttributes.addFlashAttribute("focusInquiryForm", true);
            return "redirect:/mypage/setting/inquiry#inquiryForm";
        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("inquiry", inquiry);
            redirectAttributes.addFlashAttribute("inquiryError", "\uBB38\uC758\uB97C \uC811\uC218\uD558\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. DB \uD14C\uC774\uBE14\uACFC \uC2DC\uD000\uC2A4\uB97C \uD655\uC778\uD574 \uC8FC\uC138\uC694.");
            redirectAttributes.addFlashAttribute("focusInquiryForm", true);
            return "redirect:/mypage/setting/inquiry#inquiryForm";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("inquiry", inquiry);
            redirectAttributes.addFlashAttribute("inquiryError", "\uBB38\uC758\uB97C \uC811\uC218\uD558\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uC785\uB825\uD55C \uB0B4\uC6A9\uC744 \uD655\uC778\uD55C \uB4A4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574 \uC8FC\uC138\uC694.");
            redirectAttributes.addFlashAttribute("focusInquiryForm", true);
            return "redirect:/mypage/setting/inquiry#inquiryForm";
        }
    }

    @GetMapping("/mypage/setting/policy")
    public String policy() {
        return "mypage/setting/policy";
    }

    @GetMapping("/mypage/setting/privacy")
    public String settingPrivacy() {
        return "mypage/setting/privacy";
    }

    @GetMapping("/mypage/setting/terms")
    public String terms() {
        return "mypage/setting/terms";
    }

    @GetMapping("/mypage/setting/display")
    public String display() {
        return "mypage/setting/display";
    }

    @GetMapping("/mypage/setting/app-info")
    public String appInfo() {
        return "mypage/setting/app-info";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    private void addCommonWriteModel(Model model) {
        var dbLocations = commonService.findLocations();
        model.addAttribute("categories", commonService.findCategories());
        model.addAttribute("locations", dbLocations);
        model.addAttribute("locationOptions", buildLocationOptions(dbLocations));
    }

    private void addCommonListModel(Model model) {
        var dbLocations = commonService.findLocations();
        model.addAttribute("categories", commonService.findCategories());
        model.addAttribute("locations", dbLocations);
        model.addAttribute("locationOptions", buildLocationOptions(dbLocations));
    }

    private LinkedHashSet<String> buildLocationOptions(List<com.example.find_campus.dto.LocationDto> dbLocations) {
        LinkedHashSet<String> locationOptions = new LinkedHashSet<>(CAMPUS_LOCATION_NAMES);
        if (dbLocations == null) {
            return locationOptions;
        }

        dbLocations.stream()
                .map(location -> location.getLocationName())
                .filter(locationName -> locationName != null && !locationName.isBlank())
                .map(String::trim)
                .filter(locationName -> locationName.matches("^\\d+\\.\\s+.*"))
                .filter(locationName -> !isKnownCampusLocationName(locationName))
                .forEach(locationOptions::add);
        return locationOptions;
    }

    private boolean isKnownCampusLocationName(String locationName) {
        String normalized = removeLocationNumber(locationName);
        return CAMPUS_LOCATION_NAMES.stream()
                .anyMatch(campusName -> campusName.equals(locationName)
                        || removeLocationNumber(campusName).equals(normalized));
    }

    private String removeLocationNumber(String locationName) {
        return locationName == null ? "" : locationName.replaceFirst("^\\d+\\.\\s*", "").trim();
    }
    private void addReportTargetModel(String type, Long id, Model model) {
        String targetType = type == null ? "" : type.trim().toLowerCase();
        model.addAttribute("targetReady", false);
        model.addAttribute("targetTypeLabel", "");
        model.addAttribute("targetTitle", "");
        model.addAttribute("targetDescription", "");
        model.addAttribute("targetIcon", "fa-triangle-exclamation");

        if (id == null || targetType.isBlank()) {
            model.addAttribute("targetDescription", "게시글 상세 화면 또는 채팅방에서 신고 버튼을 눌러주세요.");
            return;
        }

        try {
            if ("lost".equals(targetType)) {
                ItemViewDto item = itemService.findLostItem(id);
                model.addAttribute("targetReady", true);
                model.addAttribute("targetTypeLabel", "분실물 게시글");
                model.addAttribute("targetTitle", item.getTitle());
                model.addAttribute("targetDescription", item.getLocationName());
                model.addAttribute("targetIcon", "fa-briefcase");
                return;
            }

            if ("found".equals(targetType)) {
                ItemViewDto item = itemService.findFoundItem(id);
                model.addAttribute("targetReady", true);
                model.addAttribute("targetTypeLabel", "습득물 게시글");
                model.addAttribute("targetTitle", item.getTitle());
                model.addAttribute("targetDescription", item.getLocationName());
                model.addAttribute("targetIcon", "fa-box-open");
                return;
            }

            if ("chat".equals(targetType)) {
                UserDto user = userService.findById(id);
                model.addAttribute("targetReady", true);
                model.addAttribute("targetTypeLabel", "채팅 사용자");
                model.addAttribute("targetTitle", user != null ? user.getUserName() : "채팅 사용자");
                model.addAttribute("targetDescription", user != null && user.getStudentNo() != null ? user.getStudentNo() : "채팅에서 선택된 사용자");
                model.addAttribute("targetIcon", "fa-user");
                return;
            }
        } catch (RuntimeException ignored) {
            model.addAttribute("targetReady", false);
            model.addAttribute("targetDescription", "신고 대상을 찾을 수 없습니다. 다시 신고 버튼을 눌러주세요.");
        }
    }

    private Long getLoginUserId(jakarta.servlet.http.HttpSession session) {
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
