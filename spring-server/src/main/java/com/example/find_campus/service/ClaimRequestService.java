package com.example.find_campus.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.find_campus.dto.ItemViewDto;
import com.example.find_campus.dto.NotificationDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimRequestService {

    public static final int MIN_CLAIM_SCORE_PERCENT = 70;

    private final JdbcTemplate jdbcTemplate;
    private final AiRecommendationService aiRecommendationService;
    private final ItemService itemService;
    private final AppNotificationService appNotificationService;
    private boolean schemaChecked;

    public int calculateScorePercent(Long foundId, Long lostId) {
        if (foundId == null || lostId == null) {
            return 0;
        }
        try {
            var recommendation = aiRecommendationService.comparePair("lost", lostId, "found", foundId);
            if (recommendation == null) {
                return 0;
            }
            int score = (int) Math.round(recommendation.getTotalScore() * 100.0);
            return Math.max(0, Math.min(100, score));
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    public boolean hasActiveClaim(Long foundId, Long lostId, Long userId) {
        ensureSchema();
        if (foundId == null || lostId == null || userId == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM CLAIM_REQUESTS WHERE USER_ID = ? AND LOST_ID = ? AND FOUND_ID = ? AND STATUS IN ('WAITING', 'APPROVED')",
                    Integer.class,
                    userId,
                    lostId,
                    foundId);
            return count != null && count > 0;
        } catch (DataAccessException ignored) {
            return false;
        }
    }

    @Transactional
    public Long submitClaim(Long foundId, Long lostId, Long userId) {
        ensureSchema();
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        if (foundId == null || lostId == null) {
            throw new IllegalArgumentException("수령 신청할 게시글을 확인해 주세요.");
        }

        int score = calculateScorePercent(foundId, lostId);
        if (score < MIN_CLAIM_SCORE_PERCENT) {
            throw new IllegalArgumentException("유사도가 " + MIN_CLAIM_SCORE_PERCENT + "% 이상일 때만 수령 신청할 수 있습니다.");
        }

        List<Long> existing = jdbcTemplate.queryForList(
                "SELECT CLAIM_ID FROM (SELECT CLAIM_ID FROM CLAIM_REQUESTS WHERE USER_ID = ? AND LOST_ID = ? AND FOUND_ID = ? AND STATUS IN ('WAITING', 'APPROVED') ORDER BY CREATED_AT DESC) WHERE ROWNUM = 1",
                Long.class,
                userId,
                lostId,
                foundId);
        if (!existing.isEmpty()) {
            Long claimId = existing.get(0);
            notifyAdminsNonBlocking(claimId, foundId, lostId, score);
            notifyFoundOwnerNonBlocking(claimId, foundId, lostId, userId, score);
            return claimId;
        }

        Long claimId = jdbcTemplate.queryForObject("SELECT SEQ_CLAIM_REQUESTS.NEXTVAL FROM DUAL", Long.class);
        jdbcTemplate.update(
                "INSERT INTO CLAIM_REQUESTS (CLAIM_ID, USER_ID, LOST_ID, FOUND_ID, MATCH_SCORE, STATUS, CREATED_AT, UPDATED_AT) VALUES (?, ?, ?, ?, ?, 'WAITING', SYSDATE, SYSDATE)",
                claimId,
                userId,
                lostId,
                foundId,
                score);

        notifyAdminsNonBlocking(claimId, foundId, lostId, score);
        notifyFoundOwnerNonBlocking(claimId, foundId, lostId, userId, score);
        return claimId;
    }

    public List<Map<String, Object>> findAdminClaims(String status) {
        ensureSchema();
        StringBuilder sql = new StringBuilder(adminClaimSelectSql());
        if (status != null && !status.isBlank()) {
            sql.append(" WHERE C.STATUS = ?");
            sql.append(adminClaimOrderSql());
            return jdbcTemplate.queryForList(sql.toString(), status);
        }
        sql.append(adminClaimOrderSql());
        return jdbcTemplate.queryForList(sql.toString());
    }

    public int countClaims(String status) {
        ensureSchema();
        if (status != null && !status.isBlank()) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM CLAIM_REQUESTS WHERE STATUS = ?",
                    Integer.class,
                    status);
            return count == null ? 0 : count;
        }
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CLAIM_REQUESTS", Integer.class);
        return count == null ? 0 : count;
    }

    @Transactional
    public void updateClaimStatus(Long claimId, String status) {
        ensureSchema();
        String normalizedStatus = normalizeClaimStatus(status);
        Map<String, Object> claim = findClaimById(claimId);
        if (claim == null) {
            throw new IllegalArgumentException("수령 신청을 찾을 수 없습니다.");
        }

        jdbcTemplate.update(
                "UPDATE CLAIM_REQUESTS SET STATUS = ?, UPDATED_AT = SYSDATE WHERE CLAIM_ID = ?",
                normalizedStatus,
                claimId);

        if ("RETURNED".equals(normalizedStatus)) {
            jdbcTemplate.update("UPDATE FOUND_ITEMS SET STATUS = 'RETURNED' WHERE FOUND_ID = ?", claim.get("foundId"));
            jdbcTemplate.update("UPDATE LOST_ITEMS SET STATUS = 'RETURNED' WHERE LOST_ID = ?", claim.get("lostId"));
        }

        notifyRequesterNonBlocking(claim, normalizedStatus);
    }

    private Map<String, Object> findClaimById(Long claimId) {
        if (claimId == null) {
            return null;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                adminClaimSelectSql() + " WHERE C.CLAIM_ID = ?",
                claimId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String adminClaimSelectSql() {
        return """
                SELECT
                    C.CLAIM_ID AS "claimId",
                    C.USER_ID AS "userId",
                    C.LOST_ID AS "lostId",
                    C.FOUND_ID AS "foundId",
                    C.MATCH_SCORE AS "matchScore",
                    C.STATUS AS "status",
                    C.CREATED_AT AS "createdAt",
                    C.UPDATED_AT AS "updatedAt",
                    F.TITLE AS "foundTitle",
                    F.ITEM_NAME AS "foundItemName",
                    L.TITLE AS "lostTitle",
                    L.ITEM_NAME AS "lostItemName",
                    NVL(U.USER_NAME, '-') AS "requesterName",
                    NVL(U.STUDENT_NO, '-') AS "requesterStudentNo",
                    NVL(S.STORAGE_NAME, '-') AS "storageName"
                FROM CLAIM_REQUESTS C
                LEFT JOIN FOUND_ITEMS F ON F.FOUND_ID = C.FOUND_ID
                LEFT JOIN LOST_ITEMS L ON L.LOST_ID = C.LOST_ID
                LEFT JOIN USERS U ON U.USER_ID = C.USER_ID
                LEFT JOIN STORAGE_PLACES S ON S.STORAGE_ID = F.STORAGE_ID
                """;
    }

    private String adminClaimOrderSql() {
        return """
                 ORDER BY
                    CASE C.STATUS
                        WHEN 'WAITING' THEN 1
                        WHEN 'APPROVED' THEN 2
                        WHEN 'RETURNED' THEN 3
                        WHEN 'REJECTED' THEN 4
                        ELSE 5
                    END,
                    C.CREATED_AT DESC
                """;
    }

    private String normalizeClaimStatus(String status) {
        if ("APPROVED".equals(status) || "RETURNED".equals(status) || "REJECTED".equals(status) || "WAITING".equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("처리할 수 없는 수령 신청 상태입니다.");
    }


    private void notifyFoundOwnerNonBlocking(Long claimId, Long foundId, Long lostId, Long requesterId, int score) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    """
                            SELECT
                                F.USER_ID AS OWNER_ID,
                                NVL(F.TITLE, F.ITEM_NAME) AS FOUND_TITLE,
                                NVL(L.TITLE, L.ITEM_NAME) AS LOST_TITLE
                            FROM FOUND_ITEMS F
                            LEFT JOIN LOST_ITEMS L ON L.LOST_ID = ?
                            WHERE F.FOUND_ID = ?
                            """,
                    lostId,
                    foundId);
            if (rows.isEmpty()) {
                return;
            }

            Map<String, Object> row = rows.get(0);
            Object ownerValue = row.get("OWNER_ID");
            if (ownerValue == null) {
                return;
            }
            Long ownerId = toLong(ownerValue);
            if (requesterId != null && requesterId.equals(ownerId)) {
                return;
            }

            String requesterName = "사용자";
            if (requesterId != null) {
                List<String> names = jdbcTemplate.queryForList(
                        "SELECT NVL(USER_NAME, '사용자') FROM USERS WHERE USER_ID = ?",
                        String.class,
                        requesterId);
                if (!names.isEmpty() && names.get(0) != null && !names.get(0).isBlank()) {
                    requesterName = names.get(0);
                }
            }

            String foundTitle = value(row.get("FOUND_TITLE"), "습득물");
            String lostTitle = value(row.get("LOST_TITLE"), "분실물");
            appNotificationService.notifyUser(
                    ownerId,
                    "CLAIM_REQUEST",
                    "내 습득물에 수령 신청이 들어왔습니다.",
                    requesterName + "님이 " + foundTitle + " 게시물에 수령 신청을 했습니다. 선택한 분실물은 "
                            + lostTitle + "이며, AI 유사도는 " + score + "%입니다.",
                    "claim",
                    claimId);
        } catch (RuntimeException ignored) {
        }
    }

    private void notifyAdminsNonBlocking(Long claimId, Long foundId, Long lostId, int score) {
        try {
            ItemViewDto foundItem = itemService.findFoundItem(foundId);
            ItemViewDto lostItem = itemService.findLostItem(lostId);
            String foundTitle = foundItem != null && foundItem.getTitle() != null ? foundItem.getTitle() : "습득물";
            String lostTitle = lostItem != null && lostItem.getTitle() != null ? lostItem.getTitle() : "분실물";
            appNotificationService.notifyAdmins(
                    "CLAIM_REQUEST",
                    "수령 신청이 접수되었습니다.",
                    foundTitle + "에 대한 수령 신청이 들어왔습니다. 신청자가 선택한 분실물은 " + lostTitle + "이며, AI 유사도는 " + score + "%입니다.",
                    "claim",
                    claimId);
        } catch (RuntimeException ignored) {
        }
    }

    private void notifyRequesterNonBlocking(Map<String, Object> claim, String status) {
        try {
            Object userIdValue = claim.get("userId");
            if (userIdValue == null) {
                return;
            }
            Long userId = toLong(userIdValue);
            String foundTitle = value(claim.get("foundTitle"), "습득물");
            String title;
            String message;
            if ("APPROVED".equals(status)) {
                title = "수령 신청이 승인되었습니다.";
                message = foundTitle + " 수령 신청이 승인되었습니다. 보관처 안내를 확인해 주세요.";
            } else if ("RETURNED".equals(status)) {
                title = "반환완료 처리되었습니다.";
                message = foundTitle + " 수령이 완료되어 게시글 상태가 반환완료로 변경되었습니다.";
            } else if ("REJECTED".equals(status)) {
                title = "수령 신청이 반려되었습니다.";
                message = foundTitle + " 수령 신청이 관리자 확인 후 반려되었습니다.";
            } else {
                return;
            }

            NotificationDto notification = new NotificationDto();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setNotificationType("CLAIM_RESULT");
            notification.setTargetType("claim");
            notification.setTargetId(toLong(claim.get("claimId")));
            appNotificationService.createNotification(notification);
        } catch (RuntimeException ignored) {
        }
    }

    private Long toLong(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.longValue();
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String value(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value);
        return text.isBlank() ? fallback : text;
    }


    public boolean hasWaitingClaimForFound(Long foundId) {
        ensureSchema();
        if (foundId == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM CLAIM_REQUESTS WHERE FOUND_ID = ? AND STATUS IN ('WAITING', 'APPROVED')",
                    Integer.class,
                    foundId);
            return count != null && count > 0;
        } catch (DataAccessException ignored) {
            return false;
        }
    }

    @Transactional
    public void markWaitingClaimsReturnedForFound(Long foundId) {
        ensureSchema();
        if (foundId == null) {
            return;
        }
        List<Long> claimIds = jdbcTemplate.queryForList(
                "SELECT CLAIM_ID FROM CLAIM_REQUESTS WHERE FOUND_ID = ? AND STATUS IN ('WAITING', 'APPROVED')",
                Long.class,
                foundId);
        for (Long claimId : claimIds) {
            updateClaimStatus(claimId, "RETURNED");
        }
    }

    @Transactional
    public void rejectWaitingClaimsForFound(Long foundId) {
        ensureSchema();
        if (foundId == null) {
            return;
        }
        List<Long> claimIds = jdbcTemplate.queryForList(
                "SELECT CLAIM_ID FROM CLAIM_REQUESTS WHERE FOUND_ID = ? AND STATUS IN ('WAITING', 'APPROVED')",
                Long.class,
                foundId);
        for (Long claimId : claimIds) {
            updateClaimStatus(claimId, "REJECTED");
        }
    }

    private synchronized void ensureSchema() {
        if (schemaChecked) {
            return;
        }
        ensureTable();
        ensureColumn("USER_ID", "USER_ID NUMBER");
        ensureColumn("LOST_ID", "LOST_ID NUMBER");
        ensureColumn("FOUND_ID", "FOUND_ID NUMBER");
        ensureColumn("MATCH_SCORE", "MATCH_SCORE NUMBER(5,2) DEFAULT 0");
        ensureColumn("STATUS", "STATUS VARCHAR2(30) DEFAULT 'WAITING'");
        ensureColumn("CREATED_AT", "CREATED_AT DATE DEFAULT SYSDATE");
        ensureColumn("UPDATED_AT", "UPDATED_AT DATE");
        ensureSequence();
        schemaChecked = true;
    }

    private void ensureTable() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CLAIM_REQUESTS", Integer.class);
        } catch (DataAccessException ignored) {
            executeDdl("""
                    CREATE TABLE CLAIM_REQUESTS (
                        CLAIM_ID NUMBER PRIMARY KEY,
                        USER_ID NUMBER NOT NULL,
                        LOST_ID NUMBER NOT NULL,
                        FOUND_ID NUMBER NOT NULL,
                        MATCH_SCORE NUMBER(5,2) DEFAULT 0 NOT NULL,
                        STATUS VARCHAR2(30) DEFAULT 'WAITING' NOT NULL,
                        CREATED_AT DATE DEFAULT SYSDATE NOT NULL,
                        UPDATED_AT DATE
                    )
                    """);
        }
    }

    private void ensureColumn(String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'CLAIM_REQUESTS' AND COLUMN_NAME = ?",
                    Integer.class,
                    columnName);
            if (count == null || count == 0) {
                executeDdl("ALTER TABLE CLAIM_REQUESTS ADD (" + ddl + ")");
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void ensureSequence() {
        try {
            jdbcTemplate.queryForObject("SELECT SEQ_CLAIM_REQUESTS.NEXTVAL FROM DUAL", Long.class);
        } catch (DataAccessException ignored) {
            executeDdl("CREATE SEQUENCE SEQ_CLAIM_REQUESTS START WITH 1 INCREMENT BY 1 NOCACHE");
        }
    }

    private void executeDdl(String sql) {
        jdbcTemplate.execute(sql);
    }
}
