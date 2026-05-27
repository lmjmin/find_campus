package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TestController {

    @GetMapping({"/", "/main"})
    public String main() {
        return "index";
    }

    // USER
    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/join")
    public String join() {
        return "user/join";
    }

    // LOST
    @GetMapping("/lost/list")
    public String lostList() {
        return "lost/list";
    }

    @GetMapping("/lost/write")
    public String lostWrite() {
        return "lost/write";
    }

    @GetMapping("/lost/detail")
    public String lostDetail() {
        return "lost/detail";
    }

    @GetMapping("/lost/detail/{id}")
    public String lostDetailById(@PathVariable Long id, Model model) {
        model.addAttribute("lostId", id);
        return "lost/detail";
    }

    // FOUND
    @GetMapping("/found/list")
    public String foundList() {
        return "found/list";
    }

    @GetMapping("/found/write")
    public String foundWrite() {
        return "found/write";
    }

    @GetMapping("/found/detail")
    public String foundDetail() {
        return "found/detail";
    }

    @GetMapping("/found/detail/{id}")
    public String foundDetailById(@PathVariable Long id, Model model) {
        model.addAttribute("foundId", id);
        return "found/detail";
    }

    // SEARCH
    @GetMapping("/search")
    public String search() {
        return "search/search";
    }

    // RECOMMEND
    @GetMapping({"/recommend", "/recommend/list"})
    public String recommendList() {
        return "recommend/list";
    }

    // MAP
    @GetMapping("/map")
    public String map() {
        return "map/map";
    }

    // CHAT
    @GetMapping("/chat/rooms")
    public String chatRooms() {
        return "chat/rooms";
    }

    @GetMapping("/chat/room")
    public String chatRoom() {
        return "chat/room";
    }

    @GetMapping("/chat/room/{id}")
    public String chatRoomById(@PathVariable Long id, Model model) {
        model.addAttribute("roomId", id);
        return "chat/room";
    }

    // MYPAGE
    @GetMapping("/mypage")
    public String mypage() {
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

    // MYPAGE SETTING
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

    // ADMIN
    @GetMapping("/admin/items")
    public String adminItemList() {
        return "admin/item-list";
    }

    @GetMapping("/admin/categories")
    public String adminCategoryList() {
        return "admin/category-list";
    }

    @GetMapping("/admin/locations")
    public String adminLocationList() {
        return "admin/location-list";
    }

    @GetMapping("/admin/reports")
    public String adminReportList() {
        return "admin/report-list";
    }
}