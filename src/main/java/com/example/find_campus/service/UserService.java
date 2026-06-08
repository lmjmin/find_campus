package com.example.find_campus.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.find_campus.config.PasswordConfig;
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

        if (joinDto.getEmail() != null && !joinDto.getEmail().trim().isEmpty()) {
            if (userDao.countByEmail(joinDto.getEmail()) > 0) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }

        UserDto userDto = new UserDto();
        userDto.setLoginId(joinDto.getLoginId());
        joinDto.setPassword(passwordEncoder.encode(joinDto.getPassword()));
        userDto.setUserName(joinDto.getUserName());
        userDto.setStudentNo(joinDto.getStudentNo());
        userDto.setPhone(joinDto.getPhone());
        userDto.setEmail(joinDto.getEmail());

        int result = userDao.insertUser(joinDto);

        if (result != 1) {
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

        if (user == null) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("사용할 수 없는 계정입니다.");
        }

        if (!loginDto.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return user;
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

        if (joinDto.getEmail() != null && !joinDto.getEmail().trim().isEmpty()) {
            if (!joinDto.getEmail().contains("@")) {
                throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
            }
        }
    }
}