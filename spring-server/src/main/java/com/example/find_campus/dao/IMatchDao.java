package com.example.find_campus.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.find_campus.dto.MatchViewDto;

@Mapper
public interface IMatchDao {

    List<MatchViewDto> findMatchesByLostId(Long lostId);

    List<MatchViewDto> findMatchesByUserId(Long userId);

    int updateMatchStatus(@Param("matchId") Long matchId, @Param("status") String status);
}
