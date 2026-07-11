package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.JoinDto;
import com.example.find_campus.dto.UserDto;

@Mapper
public interface IUserDao {

    int countByLoginId(String loginId);

    int countByEmail(String email);

    int insertUser(JoinDto joinDto);

    UserDto findByLoginId(String loginId);

    UserDto findById(Long userId);

    int updateUser(UserDto userDto);

    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    List<UserDto> findAllUsers(@Param("keyword") String keyword, @Param("status") String status);

    int countUsers(@Param("keyword") String keyword, @Param("status") String status);

    int updateStatus(@Param("userId") Long userId, @Param("status") String status);
}