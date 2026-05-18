package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping({"/", "/main"})
    public String main() {
        return "index";
    }

    /* =========================
       USER
    ========================= */

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/join")
    public String join() {
        return "user/join";
    }

    /* =========================
       LOST
    ========================= */

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

    /* =========================
       FOUND
    ========================= */

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

    /* =========================
       CHAT
    ========================= */

    @GetMapping("/chat/rooms")
    public String chatRooms() {
        return "chat/rooms";
    }

    @GetMapping("/chat/room")
    public String chatRoom() {
        return "chat/room";
    }

    /* =========================
       MYPAGE
    ========================= */

    @GetMapping("/mypage")
    public String mypage() {
        return "mypage/mypage";
    }

    /* =========================
       ADMIN
    ========================= */

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