package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.service.CommonService;
import com.example.find_campus.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final ItemService itemService;
    private final CommonService commonService;

    @GetMapping({"/", "/main"})
    public String main(Model model) {
        ItemSearchDto searchDto = new ItemSearchDto();
        model.addAttribute("lostCount", itemService.countLostItems(searchDto));
        model.addAttribute("foundCount", itemService.countFoundItems(searchDto));
        model.addAttribute("recentLostItems", itemService.findRecentItems("lost", 5));
        model.addAttribute("recentFoundItems", itemService.findRecentItems("found", 5));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/join")
    public String join() {
        return "user/join";
    }

<<<<<<< HEAD:src/main/java/com/example/find_campus/controller/TestController.java
    // 상대방 공개 프로필 페이지
    @GetMapping("/profile/{id}")
    public String userProfile(@PathVariable("id") Long id, Model model) {
        model.addAttribute("userId", id);
        return "user/profile";
    }

    // 혹시 /profile로 들어왔을 때 기본 프로필로 이동
    @GetMapping("/profile")
    public String userProfileDefault(Model model) {
        model.addAttribute("userId", 1L);
        return "user/profile";
    }

    // =========================
    // LOST
    // =========================
=======
>>>>>>> main:spring-server/src/main/java/com/example/find_campus/controller/TestController.java
    @GetMapping("/lost/list")
    public String lostList(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", itemService.findLostItems(searchDto));
        model.addAttribute("totalCount", itemService.countLostItems(searchDto));
        model.addAttribute("search", searchDto);
        return "lost/list";
    }

    @GetMapping("/lost/write")
    public String lostWrite(Model model) {
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
        return "found/list";
    }

    @GetMapping("/found/write")
    public String foundWrite(Model model) {
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

    @GetMapping("/chat/rooms")
    public String chatRooms() {
        return "chat/rooms";
    }

    @GetMapping("/chat/room")
    public String chatRoom(Model model) {
        model.addAttribute("roomId", 1L);
        return "chat/room";
    }

    @GetMapping("/chat/room/{id}")
    public String chatRoomById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("roomId", id);
        return "chat/room";
    }

<<<<<<< HEAD:src/main/java/com/example/find_campus/controller/TestController.java
    // =========================
    // REPORT
    // =========================

    // 신고 기본 주소
    @GetMapping("/report")
    public String reportDefault() {
        return "report/write";
    }

    // lost/detail.html, found/detail.html에서 신고하기 버튼 누르면 이동
=======
>>>>>>> main:spring-server/src/main/java/com/example/find_campus/controller/TestController.java
    @GetMapping("/report/write")
    public String reportWrite(@RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "id", required = false) Long id,
                              Model model) {
        model.addAttribute("reportType", type);
        model.addAttribute("targetId", id);
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
        return "match/result";
    }

    @GetMapping("/match/storage-guide")
    public String storageGuide(@RequestParam(value = "foundId", required = false) Long foundId,
                               @RequestParam(value = "lostId", required = false) Long lostId,
                               Model model) {
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostId", lostId);
        return "match/storage-guide";
    }

    @GetMapping("/search")
    public String search(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("lostItems", itemService.findLostItems(searchDto));
        model.addAttribute("foundItems", itemService.findFoundItems(searchDto));
        model.addAttribute("lostCount", itemService.countLostItems(searchDto));
        model.addAttribute("foundCount", itemService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        return "search/search";
    }

    @GetMapping({"/recommend", "/recommend/list"})
    public String recommendList() {
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
        model.addAttribute("lostItems", itemService.findLostItemsByUserId(userId));
        model.addAttribute("foundItems", itemService.findFoundItemsByUserId(userId));
        return "mypage/mypage";
    }

    @GetMapping("/mypage/edit")
    public String mypageEdit() {
        return "mypage/edit";
    }

    @GetMapping({"/setting", "/mypage/setting"})
    public String setting() {
        return "mypage/setting";
    }

    @GetMapping({"/notification", "/mypage/notification"})
    public String notification() {
        return "mypage/notification";
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
    public String inquiry() {
        return "mypage/setting/inquiry";
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

    @GetMapping("/admin/storage")
    public String adminStorageList() {
        return "admin/storage-list";
    }

    @GetMapping("/admin/locations")
    public String adminLocationList() {
        return "admin/location-list";
    }

    @GetMapping("/admin/categories")
    public String adminCategoryList() {
        return "admin/category-list";
    }

    @GetMapping("/admin/items")
    public String adminItemList() {
        return "admin/item-list";
    }

    private void addCommonWriteModel(Model model) {
        model.addAttribute("categories", commonService.findCategories());
        model.addAttribute("locations", commonService.findLocations());
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
