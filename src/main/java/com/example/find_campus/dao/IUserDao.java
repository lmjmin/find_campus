package com.example.find_campus.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.UserDto;

@Mapper
public interface IUserDao {

    UserDto findByLoginId(@Param("loginId") String loginId);

    int countByLoginId(@Param("loginId") String loginId);

    int countByEmail(@Param("email") String email);

    int insertUser(UserDto userDto);
}