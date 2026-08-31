package com.example.find_campus.service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.INotificationDao;
import com.example.find_campus.dto.NotificationDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppNotificationService {

    private final INotificationDao notificationDao;
    private final JdbcTemplate jdbcTemplate;
    private static final String ADMIN_NOTIFICATION_LOGIN_ID = "admin_notifications";
    private boolean schemaChecked;

    public List<NotificationDto> findNotifications(Long userId) {
        ensureSchema();
        requireLogin(userId);
        return notificationDao.findNotificationsByUserId(userId);
    }

    public int countUnread(Long userId) {
        ensureSchema();
        requireLogin(userId);
        return notificationDao.countUnreadNotifications(userId);
    }

    @Transactional
    public Long createNotification(NotificationDto dto) {
        ensureSchema();
        if (dto.getUserId() == null) {
            throw new IllegalArgumentException("알림을 받을 사용자가 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getTitle())) {
            throw new IllegalArgumentException("알림 제목이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getMessage())) {
            throw new IllegalArgumentException("알림 내용이 필요합니다.");
        }
        if (!StringUtils.hasText(dto.getNotificationType())) {
            dto.setNotificationType("SYSTEM");
        }
        dto.setReadYn("N");

        if (notificationDao.insertNotification(dto) != 1) {
            throw new IllegalStateException("알림을 저장할 수 없습니다.");
        }
        return dto.getNotificationId();
    }


    public List<NotificationDto> findAdminNotifications() {
        ensureSchema();
        return notificationDao.findAdminNotifications();
    }

    public int countAdminUnread() {
        ensureSchema();
        return notificationDao.countUnreadAdminNotifications();
    }

    @Transactional
    public void notifyUser(Long userId, String type, String title, String message, String targetType, Long targetId) {
        if (userId == null) {
            return;
        }
        NotificationDto notification = new NotificationDto();
        notification.setUserId(userId);
        notification.setNotificationType(StringUtils.hasText(type) ? type : "SYSTEM");
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        createNotification(notification);
    }

    @Transactional
    public void notifyAdmins(String type, String title, String message, String targetType, Long targetId) {
        ensureSchema();

        Long adminNotificationUserId = ensureAdminNotificationUserId();
        if (adminNotificationUserId != null) {
            try {
                notifyUser(adminNotificationUserId, type, title, message, targetType, targetId);
                return;
            } catch (RuntimeException ignored) {
            }
        }

        for (Long adminId : findAdminUserIds()) {
            try {
                notifyUser(adminId, type, title, message, targetType, targetId);
            } catch (RuntimeException ignored) {
            }
        }
    }

    private Long ensureAdminNotificationUserId() {
        try {
            List<Long> existing = jdbcTemplate.queryForList(
                    "SELECT USER_ID FROM USERS WHERE LOGIN_ID = ?",
                    Long.class,
                    ADMIN_NOTIFICATION_LOGIN_ID);
            if (!existing.isEmpty()) {
                Long userId = existing.get(0);
                jdbcTemplate.update(
                        "UPDATE USERS SET ROLE = 'ADMIN', STATUS = 'ACTIVE', USER_NAME = '관리자 알림함', UPDATED_AT = SYSDATE WHERE USER_ID = ?",
                        userId);
                return userId;
            }

            Long userId = jdbcTemplate.queryForObject("SELECT SEQ_USERS.NEXTVAL FROM DUAL", Long.class);
            jdbcTemplate.update(
                    "INSERT INTO USERS (USER_ID, LOGIN_ID, PASSWORD, USER_NAME, STUDENT_NO, PHONE, EMAIL, ROLE, STATUS, CREATED_AT) "
                            + "VALUES (?, ?, '{noop}admin-notification-only', '관리자 알림함', 'ADMIN-NOTI', '051-000-0000', "
                            + "'admin_notifications@findcampus.local', 'ADMIN', 'ACTIVE', SYSDATE)",
                    userId,
                    ADMIN_NOTIFICATION_LOGIN_ID);
            return userId;
        } catch (DataAccessException ignored) {
            return null;
        }
    }

    private List<Long> findAdminUserIds() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT USER_ID FROM USERS WHERE (UPPER(NVL(ROLE, 'USER')) = 'ADMIN' OR LOWER(NVL(LOGIN_ID, '-')) IN ('admin', 'administrator') OR USER_NAME = '관리자') AND NVL(STATUS, 'ACTIVE') <> 'SUSPENDED'",
                    Long.class);
        } catch (DataAccessException ignored) {
            return List.of();
        }
    }

    @Transactional
    public String openAdminNotification(Long notificationId) {
        ensureSchema();
        if (notificationId == null) {
            return "/admin/notifications";
        }

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT TARGET_TYPE, TARGET_ID FROM NOTIFICATIONS WHERE NOTIFICATION_ID = ?",
                notificationId);
        if (rows.isEmpty()) {
            return "/admin/notifications";
        }

        jdbcTemplate.update("UPDATE NOTIFICATIONS SET READ_YN = 'Y' WHERE NOTIFICATION_ID = ?", notificationId);

        Map<String, Object> row = rows.get(0);
        String targetType = value(row.get("TARGET_TYPE"));
        Long targetId = numberValue(row.get("TARGET_ID"));

        if ("inquiry".equalsIgnoreCase(targetType) && targetId != null) {
            return "/admin/inquiries/" + targetId;
        }
        if ("report".equalsIgnoreCase(targetType)) {
            return "/admin/reports";
        }
        if ("claim".equalsIgnoreCase(targetType)) {
            return "/admin/found?status=CLAIM_WAITING";
        }
        return "/admin/notifications";
    }

    @Transactional
    public void markRead(Long notificationId, Long userId) {
        ensureSchema();
        requireLogin(userId);
        if (notificationId == null) {
            throw new IllegalArgumentException("알림 ID가 필요합니다.");
        }
        notificationDao.markNotificationRead(notificationId, userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        ensureSchema();
        requireLogin(userId);
        notificationDao.markAllNotificationsRead(userId);
    }

    private synchronized void ensureSchema() {
        if (schemaChecked) {
            return;
        }
        ensureTable();
        ensureColumn("TARGET_TYPE", "TARGET_TYPE VARCHAR2(30)");
        ensureColumn("TARGET_ID", "TARGET_ID NUMBER");
        ensureSequence();
        schemaChecked = true;
    }

    private void ensureTable() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM NOTIFICATIONS", Integer.class);
        } catch (DataAccessException ignored) {
            executeDdl("""
                    CREATE TABLE NOTIFICATIONS (
                        NOTIFICATION_ID NUMBER PRIMARY KEY,
                        USER_ID NUMBER NOT NULL,
                        TITLE VARCHAR2(200) NOT NULL,
                        MESSAGE VARCHAR2(1000) NOT NULL,
                        NOTIFICATION_TYPE VARCHAR2(50) DEFAULT 'SYSTEM' NOT NULL,
                        TARGET_TYPE VARCHAR2(30),
                        TARGET_ID NUMBER,
                        READ_YN CHAR(1) DEFAULT 'N' NOT NULL,
                        CREATED_AT DATE DEFAULT SYSDATE NOT NULL
                    )
                    """);
        }
    }

    private void ensureColumn(String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'NOTIFICATIONS' AND COLUMN_NAME = ?",
                    Integer.class,
                    columnName);
            if (count == null || count == 0) {
                executeDdl("ALTER TABLE NOTIFICATIONS ADD (" + ddl + ")");
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void ensureSequence() {
        try {
            jdbcTemplate.queryForObject("SELECT SEQ_NOTIFICATIONS.NEXTVAL FROM DUAL", Long.class);
        } catch (DataAccessException ignored) {
            executeDdl("""
                    CREATE SEQUENCE SEQ_NOTIFICATIONS
                        START WITH 1
                        INCREMENT BY 1
                        NOCACHE
                        NOCYCLE
                    """);
        }
    }

    private void executeDdl(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ignored) {
        }
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Long numberValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }
}
