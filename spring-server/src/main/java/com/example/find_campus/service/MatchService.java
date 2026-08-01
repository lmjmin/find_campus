package com.example.find_campus.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.IMatchDao;
import com.example.find_campus.dto.MatchViewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final IMatchDao matchDao;

    public List<MatchViewDto> findMatchesByLostId(Long lostId) {
        if (lostId == null) {
            throw new IllegalArgumentException("분실물 ID가 필요합니다.");
        }
        return matchDao.findMatchesByLostId(lostId);
    }

    public List<MatchViewDto> findMatchesByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        return matchDao.findMatchesByUserId(userId);
    }

    @Transactional
    public void updateMatchStatus(Long matchId, String status) {
        if (matchId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("매칭 ID와 상태가 필요합니다.");
        }
        matchDao.updateMatchStatus(matchId, status);
    }
}
