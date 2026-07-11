package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.find_campus.dto.ItemSearchDto;
import com.example.find_campus.service.ItemService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final ItemService itemService;

    @GetMapping({"/", "/main"})
    public String main(Model model) {
        ItemSearchDto searchDto = new ItemSearchDto();
        model.addAttribute("lostCount", itemService.countLostItems(searchDto));
        model.addAttribute("foundCount", itemService.countFoundItems(searchDto));
        model.addAttribute("recentLostItems", itemService.findRecentItems("lost", 5));
        model.addAttribute("recentFoundItems", itemService.findRecentItems("found", 5));
        return "index";
    }

    // =========================
    // USER
    // =========================
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/join")
    public String join() {
        return "user/join";
    }

    // =========================
    // LOST
    // =========================
    @GetMapping("/lost/list")
    public String lostList(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", itemService.findLostItems(searchDto));
        model.addAttribute("totalCount", itemService.countLostItems(searchDto));
        model.addAttribute("search", searchDto);
        return "lost/list";
    }

    @GetMapping("/lost/write")
    public String lostWrite() {
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

    // =========================
    // FOUND
    // =========================
    @GetMapping("/found/list")
    public String foundList(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("items", itemService.findFoundItems(searchDto));
        model.addAttribute("totalCount", itemService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        return "found/list";
    }

    @GetMapping("/found/write")
    public String foundWrite() {
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

    // =========================
    // CHAT
    // =========================

    // 채팅방 목록
    @GetMapping("/chat/rooms")
    public String chatRooms() {
        return "chat/rooms";
    }

    // 채팅방 상세 기본 주소
    @GetMapping("/chat/room")
    public String chatRoom() {
        return "chat/room";
    }

    // 채팅방 상세 주소
    @GetMapping("/chat/room/{id}")
    public String chatRoomById(@PathVariable("id") Long id, Model model) {
        model.addAttribute("roomId", id);
        return "chat/room";
    }

    // =========================
    // REPORT
    // =========================

    // lost/detail.html, found/detail.html에서 신고하기 버튼 누르면 이동
    @GetMapping("/report/write")
    public String reportWrite(@RequestParam(value = "type", required = false) String type,
                              @RequestParam(value = "id", required = false) Long id,
                              Model model) {
        model.addAttribute("reportType", type);
        model.addAttribute("targetId", id);
        return "report/write";
    }

    // =========================
    // MATCH
    // =========================

    // found/detail.html에서 "내 물건 같아요, 수령 신청하기" 버튼 누르면 여기로 이동
    @GetMapping("/match/select-lost")
    public String selectLost() {
        return "match/select-lost";
    }

    // 나중에 foundId를 붙여서 이동할 때 사용할 수 있는 주소
    @GetMapping("/match/select-lost/{foundId}")
    public String selectLostByFoundId(@PathVariable("foundId") Long foundId,
                                      jakarta.servlet.http.HttpSession session,
                                      Model model) {
        Long userId = (Long) session.getAttribute("loginUserId");
        if (userId == null) {
            return "redirect:/login";
        }
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostItems", itemService.findLostItemsByUserId(userId));
        return "match/select-lost";
    }

    // select-lost.html에서 분실글 선택 후 유사도 결과 페이지로 이동
    @GetMapping("/match/result")
    public String matchResult(@RequestParam(value = "foundId", required = false) Long foundId,
                              @RequestParam(value = "lostId", required = false) Long lostId,
                              Model model) {
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostId", lostId);
        return "match/result";
    }

    // result.html에서 보관 장소 확인하기 버튼 누르면 이동
    @GetMapping("/match/storage-guide")
    public String storageGuide(@RequestParam(value = "foundId", required = false) Long foundId,
                               @RequestParam(value = "lostId", required = false) Long lostId,
                               Model model) {
        model.addAttribute("foundId", foundId);
        model.addAttribute("lostId", lostId);
        return "match/storage-guide";
    }

    // =========================
    // SEARCH
    // =========================
    @GetMapping("/search")
    public String search(@ModelAttribute ItemSearchDto searchDto, Model model) {
        model.addAttribute("lostItems", itemService.findLostItems(searchDto));
        model.addAttribute("foundItems", itemService.findFoundItems(searchDto));
        model.addAttribute("lostCount", itemService.countLostItems(searchDto));
        model.addAttribute("foundCount", itemService.countFoundItems(searchDto));
        model.addAttribute("search", searchDto);
        return "search/search";
    }

    // =========================
    // RECOMMEND
    // =========================
    @GetMapping({"/recommend", "/recommend/list"})
    public String recommendList() {
        return "recommend/list";
    }

    // =========================
    // MAP
    // =========================
    @GetMapping("/map")
    public String map() {
        return "map/map";
    }

    // =========================
    // MYPAGE
    // =========================
    @GetMapping("/mypage")
    public String mypage(jakarta.servlet.http.HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("loginUserId");
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

    // =========================
    // MYPAGE SETTING
    // =========================
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

    // =========================
    // ADMIN
    // =========================

    // 관리자 로그인 화면
    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    // 관리자 대시보드
    // 사용자 관리
    // 분실물 관리
    // 습득물 관리
    // 보관 장소 관리
    @GetMapping("/admin/storage")
    public String adminStorageList() {
        return "admin/storage-list";
    }

    // 위치 관리
    @GetMapping("/admin/locations")
    public String adminLocationList() {
        return "admin/location-list";
    }

    // 카테고리 관리
    @GetMapping("/admin/categories")
    public String adminCategoryList() {
        return "admin/category-list";
    }

    // 신고 관리
    /*
     * 예전 관리자 통합 물품 관리 주소.
     * 혹시 기존에 /admin/items로 연결한 버튼이 있으면 깨지지 않게 유지.
     */
    @GetMapping("/admin/items")
    public String adminItemList() {
        return "admin/item-list";
    }
}