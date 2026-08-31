package com.example.find_campus.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.find_campus.service.AppNotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@ControllerAdvice(annotations = Controller.class)
@RequiredArgsConstructor
public class HeaderNotificationAdvice {

    private final AppNotificationService appNotificationService;

    @ModelAttribute
    public void addHeaderNotificationCount(HttpSession session, Model model) {
        Object value = session.getAttribute("loginUserId");
        if (value == null) {
            model.addAttribute("headerUnreadCount", 0);
            return;
        }

        try {
            Long userId = value instanceof Number
                    ? ((Number) value).longValue()
                    : Long.valueOf(String.valueOf(value));
            model.addAttribute("headerUnreadCount", appNotificationService.countUnread(userId));
        } catch (Exception ignored) {
            model.addAttribute("headerUnreadCount", 0);
        }
    }
}