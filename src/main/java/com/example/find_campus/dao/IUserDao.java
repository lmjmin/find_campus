package com.example.find_campus.dao;

import org.apache.ibatis.annotations.Mapper;

import com.example.find_campus.dto.JoinDto;
import com.example.find_campus.dto.UserDto;

@Mapper
public interface IUserDao {

    int countByLoginId(String loginId);

    int countByEmail(String email);

    int insertUser(JoinDto joinDto);

    UserDto findByLoginId(String loginId);
}