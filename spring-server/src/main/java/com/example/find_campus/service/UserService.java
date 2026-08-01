package com.example.find_campus.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.find_campus.dao.IUserDao;
import com.example.find_campus.dto.JoinDto;
import com.example.find_campus.dto.LoginDto;
import com.example.find_campus.dto.UserDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final IUserDao userDao;

    @Transactional
    public void join(JoinDto joinDto) {
        validateJoin(joinDto);

        if (userDao.countByLoginId(joinDto.getLoginId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (joinDto.getEmail() != null && !joinDto.getEmail().trim().isEmpty()
                && userDao.countByEmail(joinDto.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        joinDto.setPassword(passwordEncoder.encode(joinDto.getPassword()));
        if (userDao.insertUser(joinDto) != 1) {
            throw new IllegalStateException("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    public UserDto login(LoginDto loginDto) {
        if (loginDto.getLoginId() == null || loginDto.getLoginId().trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해 주세요.");
        }
        if (loginDto.getPassword() == null || loginDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }

        UserDto user = userDao.findByLoginId(loginDto.getLoginId());
        if (user == null || !passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("사용할 수 없는 계정입니다.");
        }
        return user;
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword, String newPasswordConfirm) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 정보가 필요합니다.");
        }
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("현재 비밀번호를 입력해 주세요.");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해 주세요.");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상 입력해 주세요.");
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새 비밀번호 확인이 일치하지 않습니다.");
        }

        UserDto user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (userDao.updatePassword(userId, passwordEncoder.encode(newPassword)) != 1) {
            throw new IllegalStateException("비밀번호를 변경할 수 없습니다.");
        }
    }

    private void validateJoin(JoinDto joinDto) {
        if (joinDto == null) {
            throw new IllegalArgumentException("회원가입 정보가 없습니다.");
        }
        if (joinDto.getLoginId() == null || joinDto.getLoginId().trim().isEmpty()) {
            throw new IllegalArgumentException("아이디를 입력해 주세요.");
        }
        if (joinDto.getLoginId().length() < 4 || joinDto.getLoginId().length() > 50) {
            throw new IllegalArgumentException("아이디는 4자 이상 50자 이하로 입력해 주세요.");
        }
        if (joinDto.getPassword() == null || joinDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }
        if (joinDto.getPassword().length() < 4) {
            throw new IllegalArgumentException("비밀번호는 4자 이상 입력해 주세요.");
        }
        if (joinDto.getPasswordCheck() == null || joinDto.getPasswordCheck().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호 확인을 입력해 주세요.");
        }
        if (!joinDto.getPassword().equals(joinDto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (joinDto.getUserName() == null || joinDto.getUserName().trim().isEmpty()) {
            throw new IllegalArgumentException("이름을 입력해 주세요.");
        }
        if (joinDto.getEmail() != null && !joinDto.getEmail().trim().isEmpty()
                && !joinDto.getEmail().contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
    }
}
