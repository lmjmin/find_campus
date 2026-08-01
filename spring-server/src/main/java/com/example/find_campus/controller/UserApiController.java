package com.example.find_campus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.find_campus.dto.ApiResponseDto;
import com.example.find_campus.dto.JoinDto;
import com.example.find_campus.dto.LoginDto;
import com.example.find_campus.dto.LoginResponseDto;
import com.example.find_campus.dto.UserDto;
import com.example.find_campus.service.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponseDto<Void>> join(@RequestBody JoinDto joinDto) {
        userService.join(joinDto);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "회원가입이 완료되었습니다.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@RequestBody LoginDto loginDto,
                                                                  HttpSession session) {
        UserDto loginUser = userService.login(loginDto);

        session.setAttribute("loginUser", loginUser);
        session.setAttribute("loginUserId", loginUser.getUserId());
        session.setAttribute("loginName", loginUser.getUserName());
        session.setAttribute("loginRole", loginUser.getRole());

        LoginResponseDto responseData = new LoginResponseDto(
                loginUser.getUserId(),
                loginUser.getLoginId(),
                loginUser.getUserName(),
                loginUser.getRole()
        );

        return ResponseEntity.ok(new ApiResponseDto<>(true, "로그인 성공", responseData));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "로그아웃 성공", null));
    }
}
