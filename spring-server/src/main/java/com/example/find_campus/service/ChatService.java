package com.example.find_campus.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dto.ChatMessageDto;
import com.example.find_campus.dto.ChatRoomDto;
import com.example.find_campus.dto.ItemViewDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final JdbcTemplate jdbcTemplate;
    private final ItemService itemService;
    private final AppNotificationService appNotificationService;
    private boolean schemaChecked;

    @Transactional
    public Long findOrCreateRoom(String itemType, Long itemId, Long requesterId) {
        ensureSchema();
        requireLogin(requesterId);
        String normalizedType = normalizeItemType(itemType);
        if (itemId == null) {
            throw new IllegalArgumentException("게시글 정보가 필요합니다.");
        }

        ItemViewDto item = "found".equals(normalizedType)
                ? itemService.findFoundItem(itemId)
                : itemService.findLostItem(itemId);
        if (item == null) {
            throw new IllegalArgumentException("게시글을 찾을 수 없습니다.");
        }

        Long ownerId = item.getUserId();
        if (ownerId == null) {
            ownerId = findItemOwnerId(normalizedType, itemId);
        }
        if (ownerId == null) {
            throw new IllegalArgumentException("게시글 작성자를 확인할 수 없습니다.");
        }
        if (ownerId.equals(requesterId)) {
            throw new IllegalArgumentException("본인이 등록한 게시글에는 채팅을 시작할 수 없습니다.");
        }

        Long roomId = findRoomId(normalizedType, itemId, ownerId, requesterId);
        if (roomId == null) {
            roomId = createRoom(normalizedType, itemId, ownerId, requesterId);
            insertRoomUser(roomId, ownerId);
            insertRoomUser(roomId, requesterId);
        } else {
            boolean requesterHadLeft = isRoomUserLeft(roomId, requesterId);
            Long visibleAfterMessageId = currentMaxMessageId(roomId);
            insertRoomUser(roomId, ownerId);
            insertRoomUser(roomId, requesterId);
            if (requesterHadLeft) {
                jdbcTemplate.update("""
                        UPDATE CHAT_ROOM_USERS
                           SET LEFT_YN = 'N',
                               BLOCKED_YN = 'N',
                               VISIBLE_AFTER_MESSAGE_ID = ?
                         WHERE ROOM_ID = ?
                           AND USER_ID = ?
                        """, visibleAfterMessageId, roomId, requesterId);
            } else {
                jdbcTemplate.update("""
                        UPDATE CHAT_ROOM_USERS
                           SET LEFT_YN = 'N',
                               BLOCKED_YN = 'N'
                         WHERE ROOM_ID = ?
                           AND USER_ID = ?
                        """, roomId, requesterId);
            }
        }
        return roomId;
    }

    private Long createRoom(String itemType, Long itemId, Long ownerId, Long requesterId) {
        Long existing = findRoomId(normalizeItemType(itemType), itemId, ownerId, requesterId);
        if (existing != null) {
            return existing;
        }

        Long roomId = nextRoomId();
        try {
            insertRoom(roomId, itemType, itemId, ownerId, requesterId);
            return roomId;
        } catch (DataAccessException e) {
            Long retryExisting = findRoomId(normalizeItemType(itemType), itemId, ownerId, requesterId);
            if (retryExisting != null) {
                return retryExisting;
            }
            roomId = nextRoomId();
            insertRoom(roomId, itemType, itemId, ownerId, requesterId);
            return roomId;
        }
    }

    private void insertRoom(Long roomId, String itemType, Long itemId, Long ownerId, Long requesterId) {
        jdbcTemplate.update("""
                INSERT INTO CHAT_ROOMS
                    (ROOM_ID, ITEM_TYPE, ITEM_ID, OWNER_ID, REQUESTER_ID, CREATED_AT, UPDATED_AT)
                VALUES (?, ?, ?, ?, ?, SYSDATE, SYSDATE)
                """, roomId, itemType, itemId, ownerId, requesterId);
    }

    private Long findItemOwnerId(String itemType, Long itemId) {
        String sql = "found".equals(itemType)
                ? "SELECT USER_ID FROM FOUND_ITEMS WHERE FOUND_ID = ?"
                : "SELECT USER_ID FROM LOST_ITEMS WHERE LOST_ID = ?";
        List<Long> ids = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong(1), itemId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    public List<ChatRoomDto> findRooms(Long userId) {
        ensureSchema();
        requireLogin(userId);
        try {
            return queryRooms(userId, null);
        } catch (DataAccessException e) {
            System.err.println("[chat] 채팅방 목록 조회 실패: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public ChatRoomDto findRoom(Long roomId, Long userId) {
        ensureSchema();
        requireLogin(userId);
        if (roomId == null) {
            return null;
        }
        try {
            List<ChatRoomDto> rooms = queryRooms(userId, roomId);
            return rooms.isEmpty() ? null : rooms.get(0);
        } catch (DataAccessException e) {
            System.err.println("[chat] 채팅방 상세 조회 실패: " + e.getMessage());
            return null;
        }
    }

    @Transactional
    public List<ChatMessageDto> findMessages(Long roomId, Long userId) {
        ensureSchema();
        try {
            requireRoomAccess(roomId, userId);
            List<ChatMessageDto> messages = jdbcTemplate.query("""
                    SELECT M.MESSAGE_ID,
                           M.ROOM_ID,
                           M.SENDER_ID,
                           U.USER_NAME AS SENDER_NAME,
                           M.MESSAGE_CONTENT,
                           M.CREATED_AT,
                           M.READ_YN
                      FROM CHAT_MESSAGES M
                      JOIN USERS U ON U.USER_ID = M.SENDER_ID
                      JOIN CHAT_ROOM_USERS CRU
                        ON CRU.ROOM_ID = M.ROOM_ID
                       AND CRU.USER_ID = ?
                     WHERE M.ROOM_ID = ?
                       AND M.MESSAGE_ID > NVL(CRU.VISIBLE_AFTER_MESSAGE_ID, 0)
                     ORDER BY M.CREATED_AT ASC, M.MESSAGE_ID ASC
                    """, (rs, rowNum) -> mapMessage(rs, userId), userId, roomId);
            jdbcTemplate.update("""
                    UPDATE CHAT_ROOM_USERS
                       SET LAST_READ_AT = SYSDATE
                     WHERE ROOM_ID = ?
                       AND USER_ID = ?
                    """, roomId, userId);
            return messages;
        } catch (DataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Transactional
    public void sendMessage(Long roomId, Long senderId, String messageContent) {
        ensureSchema();
        requireRoomAccess(roomId, senderId);
        if (!StringUtils.hasText(messageContent)) {
            throw new IllegalArgumentException("메시지를 입력해 주세요.");
        }
        if (isCurrentUserBlockedOrLeft(roomId, senderId)) {
            throw new IllegalArgumentException("이 채팅방에서는 메시지를 보낼 수 없습니다.");
        }

        String content = messageContent.trim();
        Long messageId = nextMessageId();
        jdbcTemplate.update("""
                INSERT INTO CHAT_MESSAGES
                    (MESSAGE_ID, ROOM_ID, SENDER_ID, MESSAGE_CONTENT, READ_YN, CREATED_AT)
                VALUES (?, ?, ?, ?, 'N', SYSDATE)
                """, messageId, roomId, senderId, content);
        jdbcTemplate.update("UPDATE CHAT_ROOMS SET UPDATED_AT = SYSDATE WHERE ROOM_ID = ?", roomId);

        Long receiverId = findOtherUserId(roomId, senderId);
        String senderName = findUserName(senderId);
        if (receiverId != null && !isMuted(roomId, receiverId) && !isCurrentUserBlockedOrLeft(roomId, receiverId)) {
            appNotificationService.notifyUser(
                    receiverId,
                    "CHAT_MESSAGE",
                    "새 채팅 메시지가 도착했습니다.",
                    safeText(senderName, "상대방") + "님이 메시지를 보냈습니다.",
                    "chat",
                    roomId);
        }
    }

    @Transactional
    public void setMute(Long roomId, Long userId, boolean mute) {
        ensureSchema();
        requireRoomAccess(roomId, userId);
        jdbcTemplate.update("""
                UPDATE CHAT_ROOM_USERS
                   SET MUTE_YN = ?
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                """, mute ? "Y" : "N", roomId, userId);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long userId) {
        ensureSchema();
        requireRoomAccess(roomId, userId);
        jdbcTemplate.update("""
                UPDATE CHAT_ROOM_USERS
                   SET LEFT_YN = 'Y',
                       VISIBLE_AFTER_MESSAGE_ID = ?
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                """, currentMaxMessageId(roomId), roomId, userId);
    }

    @Transactional
    public void blockRoom(Long roomId, Long userId) {
        ensureSchema();
        requireRoomAccess(roomId, userId);
        jdbcTemplate.update("""
                UPDATE CHAT_ROOM_USERS
                   SET BLOCKED_YN = 'Y',
                       LEFT_YN = 'Y'
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                """, roomId, userId);
    }

    @Transactional
    public void repairRoomAccess(Long roomId, Long userId) {
        ensureSchema();
        requireLogin(userId);
        if (roomId == null) {
            return;
        }

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM CHAT_ROOMS
                 WHERE ROOM_ID = ?
                   AND (OWNER_ID = ? OR REQUESTER_ID = ?)
                """, Integer.class, roomId, userId, userId);
        if (count == null || count == 0) {
            return;
        }

        boolean userHadLeft = isRoomUserLeft(roomId, userId);
        Long visibleAfterMessageId = currentMaxMessageId(roomId);
        insertRoomUser(roomId, userId);
        if (userHadLeft) {
            jdbcTemplate.update("""
                    UPDATE CHAT_ROOM_USERS
                       SET LEFT_YN = 'N',
                           BLOCKED_YN = 'N',
                           VISIBLE_AFTER_MESSAGE_ID = ?
                     WHERE ROOM_ID = ?
                       AND USER_ID = ?
                    """, visibleAfterMessageId, roomId, userId);
        } else {
            jdbcTemplate.update("""
                    UPDATE CHAT_ROOM_USERS
                       SET LEFT_YN = 'N',
                           BLOCKED_YN = 'N'
                     WHERE ROOM_ID = ?
                       AND USER_ID = ?
                    """, roomId, userId);
        }
    }

    private List<ChatRoomDto> queryRooms(Long userId, Long roomId) {
        StringBuilder sql = new StringBuilder("""
                SELECT R.ROOM_ID,
                       R.ITEM_TYPE,
                       R.ITEM_ID,
                       CASE WHEN R.OWNER_ID = ? THEN R.REQUESTER_ID ELSE R.OWNER_ID END AS OTHER_USER_ID,
                       OU.USER_NAME AS OTHER_USER_NAME,
                       OU.STUDENT_NO AS OTHER_STUDENT_NO,
                       NULL AS OTHER_SCHOOL,
                       CASE WHEN R.ITEM_TYPE = 'lost' THEN L.TITLE ELSE F.TITLE END AS ITEM_TITLE,
                       CASE WHEN R.ITEM_TYPE = 'lost' THEN L.ITEM_NAME ELSE F.ITEM_NAME END AS ITEM_NAME,
                       CASE WHEN R.ITEM_TYPE = 'lost' THEN LL.LOCATION_NAME ELSE FL.LOCATION_NAME END AS ITEM_LOCATION_NAME,
                       CASE WHEN R.ITEM_TYPE = 'lost' THEN L.STATUS ELSE F.STATUS END AS ITEM_STATUS,
                       CRU.MUTE_YN,
                       CRU.BLOCKED_YN,
                       CRU.LEFT_YN,
                       CAST(NULL AS VARCHAR2(1000)) AS LAST_MESSAGE,
                       CAST(NULL AS DATE) AS LAST_MESSAGE_AT,
                       0 AS UNREAD_COUNT
                  FROM CHAT_ROOMS R
                  JOIN CHAT_ROOM_USERS CRU ON CRU.ROOM_ID = R.ROOM_ID
                  JOIN USERS OU ON OU.USER_ID = CASE WHEN R.OWNER_ID = ? THEN R.REQUESTER_ID ELSE R.OWNER_ID END
                  LEFT JOIN LOST_ITEMS L ON R.ITEM_TYPE = 'lost' AND L.LOST_ID = R.ITEM_ID
                  LEFT JOIN FOUND_ITEMS F ON R.ITEM_TYPE = 'found' AND F.FOUND_ID = R.ITEM_ID
                  LEFT JOIN LOCATIONS LL ON L.LOST_LOCATION_ID = LL.LOCATION_ID
                  LEFT JOIN LOCATIONS FL ON F.FOUND_LOCATION_ID = FL.LOCATION_ID
                 WHERE CRU.USER_ID = ?
                   AND NVL(CRU.LEFT_YN, 'N') <> 'Y'
                   AND NVL(CRU.BLOCKED_YN, 'N') <> 'Y'
                """);

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);
        params.add(userId);
        if (roomId != null) {
            sql.append(" AND R.ROOM_ID = ?");
            params.add(roomId);
        }
        sql.append(" ORDER BY R.UPDATED_AT DESC, R.ROOM_ID DESC");

        try {
            List<ChatRoomDto> rooms = jdbcTemplate.query(sql.toString(), this::mapRoom, params.toArray());
            enrichRoomPreviews(rooms, userId);
            return rooms;
        } catch (DataAccessException e) {
            System.err.println("[chat] 상세 채팅방 쿼리 실패, 기본 쿼리로 재시도: " + e.getMessage());
            return queryRoomsFallback(userId, roomId);
        }
    }

    private List<ChatRoomDto> queryRoomsFallback(Long userId, Long roomId) {
        StringBuilder sql = new StringBuilder("""
                SELECT R.ROOM_ID,
                       R.ITEM_TYPE,
                       R.ITEM_ID,
                       CASE WHEN R.OWNER_ID = ? THEN R.REQUESTER_ID ELSE R.OWNER_ID END AS OTHER_USER_ID,
                       OU.USER_NAME AS OTHER_USER_NAME,
                       CAST(NULL AS VARCHAR2(100)) AS OTHER_STUDENT_NO,
                       CAST(NULL AS VARCHAR2(100)) AS OTHER_SCHOOL,
                       CASE WHEN R.ITEM_TYPE = 'found' THEN '습득물 게시글' ELSE '분실물 게시글' END AS ITEM_TITLE,
                       CAST(NULL AS VARCHAR2(100)) AS ITEM_NAME,
                       CAST(NULL AS VARCHAR2(200)) AS ITEM_LOCATION_NAME,
                       CAST(NULL AS VARCHAR2(30)) AS ITEM_STATUS,
                       NVL(CRU.MUTE_YN, 'N') AS MUTE_YN,
                       NVL(CRU.BLOCKED_YN, 'N') AS BLOCKED_YN,
                       NVL(CRU.LEFT_YN, 'N') AS LEFT_YN,
                       CAST(NULL AS VARCHAR2(1000)) AS LAST_MESSAGE,
                       CAST(NULL AS DATE) AS LAST_MESSAGE_AT,
                       0 AS UNREAD_COUNT
                  FROM CHAT_ROOMS R
                  JOIN CHAT_ROOM_USERS CRU ON CRU.ROOM_ID = R.ROOM_ID
                  JOIN USERS OU ON OU.USER_ID = CASE WHEN R.OWNER_ID = ? THEN R.REQUESTER_ID ELSE R.OWNER_ID END
                 WHERE CRU.USER_ID = ?
                   AND NVL(CRU.LEFT_YN, 'N') <> 'Y'
                   AND NVL(CRU.BLOCKED_YN, 'N') <> 'Y'
                """);

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.add(userId);
        params.add(userId);
        if (roomId != null) {
            sql.append(" AND R.ROOM_ID = ?");
            params.add(roomId);
        }
        sql.append(" ORDER BY R.UPDATED_AT DESC, R.ROOM_ID DESC");
        List<ChatRoomDto> rooms = jdbcTemplate.query(sql.toString(), this::mapRoom, params.toArray());
        enrichRoomPreviews(rooms, userId);
        return rooms;
    }

    private void enrichRoomPreviews(List<ChatRoomDto> rooms, Long userId) {
        if (rooms == null || rooms.isEmpty()) {
            return;
        }

        for (ChatRoomDto room : rooms) {
            Long roomId = room.getRoomId();
            if (roomId == null) {
                continue;
            }

            try {
                List<java.util.Map<String, Object>> rows = jdbcTemplate.queryForList("""
                        SELECT MESSAGE_CONTENT, CREATED_AT
                          FROM (
                                SELECT M.MESSAGE_CONTENT, M.CREATED_AT, M.MESSAGE_ID
                                  FROM CHAT_MESSAGES M
                                  JOIN CHAT_ROOM_USERS CRU
                                    ON CRU.ROOM_ID = M.ROOM_ID
                                   AND CRU.USER_ID = ?
                                 WHERE M.ROOM_ID = ?
                                   AND M.MESSAGE_ID > NVL(CRU.VISIBLE_AFTER_MESSAGE_ID, 0)
                                 ORDER BY M.CREATED_AT DESC, M.MESSAGE_ID DESC
                               )
                         WHERE ROWNUM = 1
                        """, userId, roomId);
                if (!rows.isEmpty()) {
                    Object content = rows.get(0).get("MESSAGE_CONTENT");
                    Object createdAt = rows.get(0).get("CREATED_AT");
                    if (content instanceof String message && StringUtils.hasText(message)) {
                        room.setLastMessage(message);
                    }
                    if (createdAt instanceof java.util.Date date) {
                        room.setLastMessageAt(date);
                    }
                }
            } catch (DataAccessException ignored) {
            }

            try {
                Integer unreadCount = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*)
                          FROM CHAT_MESSAGES M
                          JOIN CHAT_ROOM_USERS CRU
                            ON CRU.ROOM_ID = M.ROOM_ID
                           AND CRU.USER_ID = ?
                         WHERE M.ROOM_ID = ?
                           AND M.SENDER_ID <> ?
                           AND M.MESSAGE_ID > NVL(CRU.VISIBLE_AFTER_MESSAGE_ID, 0)
                           AND (CRU.LAST_READ_AT IS NULL OR M.CREATED_AT > CRU.LAST_READ_AT)
                        """, Integer.class, userId, roomId, userId);
                room.setUnreadCount(unreadCount == null ? 0 : unreadCount);
            } catch (DataAccessException ignored) {
                room.setUnreadCount(0);
            }
        }
    }


    private ChatRoomDto mapRoom(ResultSet rs, int rowNum) throws SQLException {
        ChatRoomDto dto = new ChatRoomDto();
        dto.setRoomId(rs.getLong("ROOM_ID"));
        dto.setItemType(rs.getString("ITEM_TYPE"));
        dto.setItemId(rs.getLong("ITEM_ID"));
        dto.setOtherUserId(rs.getLong("OTHER_USER_ID"));
        dto.setOtherUserName(safeText(rs.getString("OTHER_USER_NAME"), "사용자"));
        dto.setOtherStudentNo(rs.getString("OTHER_STUDENT_NO"));
        dto.setOtherSchool(rs.getString("OTHER_SCHOOL"));
        dto.setOtherInitial(initial(dto.getOtherUserName()));
        dto.setItemTitle(resolveRoomItemTitle(dto.getItemType(), dto.getItemId(), rs.getString("ITEM_TITLE")));
        dto.setItemName(rs.getString("ITEM_NAME"));
        dto.setItemLocationName(resolveRoomItemLocation(dto.getItemType(), dto.getItemId(), rs.getString("ITEM_LOCATION_NAME")));
        dto.setItemStatus(rs.getString("ITEM_STATUS"));
        dto.setMuteYn(safeFlag(rs.getString("MUTE_YN")));
        dto.setBlockedYn(safeFlag(rs.getString("BLOCKED_YN")));
        dto.setLeftYn(safeFlag(rs.getString("LEFT_YN")));
        dto.setLastMessage(rs.getString("LAST_MESSAGE"));
        dto.setLastMessageAt(rs.getTimestamp("LAST_MESSAGE_AT"));
        dto.setUnreadCount(rs.getInt("UNREAD_COUNT"));
        dto.setItemTypeLabel("found".equals(dto.getItemType()) ? "습득물" : "분실물");
        dto.setItemStatusLabel(statusLabel(dto.getItemStatus(), dto.getItemType()));
        dto.setItemDetailUrl("found".equals(dto.getItemType()) ? "/found/detail/" + dto.getItemId() : "/lost/detail/" + dto.getItemId());
        return dto;
    }


    private String resolveRoomItemTitle(String itemType, Long itemId, String currentTitle) {
        if (StringUtils.hasText(currentTitle)
                && !"습득물 게시글".equals(currentTitle)
                && !"분실물 게시글".equals(currentTitle)
                && !"게시글".equals(currentTitle)) {
            return currentTitle;
        }
        if (itemId == null) {
            return safeText(currentTitle, "게시글");
        }

        List<String> sqls = "found".equalsIgnoreCase(safeText(itemType, ""))
                ? List.of(
                "SELECT TITLE FROM FOUND_ITEMS WHERE FOUND_ID = ?",
                "SELECT ITEM_NAME FROM FOUND_ITEMS WHERE FOUND_ID = ?")
                : List.of(
                "SELECT TITLE FROM LOST_ITEMS WHERE LOST_ID = ?",
                "SELECT ITEM_NAME FROM LOST_ITEMS WHERE LOST_ID = ?");

        for (String sql : sqls) {
            try {
                String title = jdbcTemplate.queryForObject(sql, String.class, itemId);
                if (StringUtils.hasText(title)) {
                    return title;
                }
            } catch (DataAccessException ignored) {
            }
        }
        return safeText(currentTitle, "게시글");
    }


    private String resolveRoomItemLocation(String itemType, Long itemId, String currentLocation) {
        if (StringUtils.hasText(currentLocation)) {
            return currentLocation;
        }
        if (itemId == null) {
            return currentLocation;
        }

        List<String> sqls = "found".equalsIgnoreCase(safeText(itemType, ""))
                ? List.of(
                "SELECT L.LOCATION_NAME FROM FOUND_ITEMS F LEFT JOIN LOCATIONS L ON F.FOUND_LOCATION_ID = L.LOCATION_ID WHERE F.FOUND_ID = ?",
                "SELECT L.LOCATION_NAME FROM FOUND_ITEMS F LEFT JOIN LOCATIONS L ON F.LOCATION_ID = L.LOCATION_ID WHERE F.FOUND_ID = ?",
                "SELECT LOCATION_NAME FROM FOUND_ITEMS WHERE FOUND_ID = ?",
                "SELECT FOUND_LOCATION FROM FOUND_ITEMS WHERE FOUND_ID = ?",
                "SELECT LOCATION FROM FOUND_ITEMS WHERE FOUND_ID = ?")
                : List.of(
                "SELECT L.LOCATION_NAME FROM LOST_ITEMS I LEFT JOIN LOCATIONS L ON I.LOST_LOCATION_ID = L.LOCATION_ID WHERE I.LOST_ID = ?",
                "SELECT L.LOCATION_NAME FROM LOST_ITEMS I LEFT JOIN LOCATIONS L ON I.LOCATION_ID = L.LOCATION_ID WHERE I.LOST_ID = ?",
                "SELECT LOCATION_NAME FROM LOST_ITEMS WHERE LOST_ID = ?",
                "SELECT LOST_LOCATION FROM LOST_ITEMS WHERE LOST_ID = ?",
                "SELECT LOCATION FROM LOST_ITEMS WHERE LOST_ID = ?");

        for (String sql : sqls) {
            try {
                String location = jdbcTemplate.queryForObject(sql, String.class, itemId);
                if (StringUtils.hasText(location)) {
                    return location;
                }
            } catch (DataAccessException ignored) {
            }
        }
        return currentLocation;
    }

    private ChatMessageDto mapMessage(ResultSet rs, Long userId) throws SQLException {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setMessageId(rs.getLong("MESSAGE_ID"));
        dto.setRoomId(rs.getLong("ROOM_ID"));
        dto.setSenderId(rs.getLong("SENDER_ID"));
        dto.setSenderName(safeText(rs.getString("SENDER_NAME"), "사용자"));
        dto.setMessageContent(rs.getString("MESSAGE_CONTENT"));
        dto.setCreatedAt(rs.getTimestamp("CREATED_AT"));
        dto.setReadYn(rs.getString("READ_YN"));
        dto.setMine(dto.getSenderId() != null && dto.getSenderId().equals(userId));
        return dto;
    }

    private Long findRoomId(String itemType, Long itemId, Long ownerId, Long requesterId) {
        List<Long> ids = jdbcTemplate.query("""
                SELECT ROOM_ID
                  FROM CHAT_ROOMS
                 WHERE ITEM_TYPE = ?
                   AND ITEM_ID = ?
                   AND ((OWNER_ID = ? AND REQUESTER_ID = ?)
                        OR (OWNER_ID = ? AND REQUESTER_ID = ?))
                 ORDER BY ROOM_ID DESC
                """, (rs, rowNum) -> rs.getLong(1),
                itemType, itemId, ownerId, requesterId, requesterId, ownerId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private void insertRoomUser(Long roomId, Long userId) {
        try {
            jdbcTemplate.update("""
                    INSERT INTO CHAT_ROOM_USERS
                        (ROOM_ID, USER_ID, MUTE_YN, BLOCKED_YN, LEFT_YN, LAST_READ_AT)
                    VALUES (?, ?, 'N', 'N', 'N', SYSDATE)
                    """, roomId, userId);
        } catch (DataAccessException ignored) {
            jdbcTemplate.update("""
                    UPDATE CHAT_ROOM_USERS
                       SET LEFT_YN = 'N'
                     WHERE ROOM_ID = ?
                       AND USER_ID = ?
                    """, roomId, userId);
        }
    }

    private boolean isRoomUserLeft(Long roomId, Long userId) {
        List<String> flags = jdbcTemplate.query("""
                SELECT NVL(LEFT_YN, 'N')
                  FROM CHAT_ROOM_USERS
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                """, (rs, rowNum) -> rs.getString(1), roomId, userId);
        return !flags.isEmpty() && "Y".equalsIgnoreCase(flags.get(0));
    }

    private Long currentMaxMessageId(Long roomId) {
        Long id = jdbcTemplate.queryForObject("""
                SELECT NVL(MAX(MESSAGE_ID), 0)
                  FROM CHAT_MESSAGES
                 WHERE ROOM_ID = ?
                """, Long.class, roomId);
        return id == null ? 0L : id;
    }


    private Long findOtherUserId(Long roomId, Long userId) {
        List<Long> ids = jdbcTemplate.query("""
                SELECT CASE WHEN OWNER_ID = ? THEN REQUESTER_ID ELSE OWNER_ID END
                  FROM CHAT_ROOMS
                 WHERE ROOM_ID = ?
                """, (rs, rowNum) -> rs.getLong(1), userId, roomId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String findUserName(Long userId) {
        List<String> names = jdbcTemplate.query("""
                SELECT USER_NAME FROM USERS WHERE USER_ID = ?
                """, (rs, rowNum) -> rs.getString(1), userId);
        return names.isEmpty() ? null : names.get(0);
    }

    private boolean isMuted(Long roomId, Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM CHAT_ROOM_USERS
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                   AND NVL(MUTE_YN, 'N') = 'Y'
                """, Integer.class, roomId, userId);
        return count != null && count > 0;
    }

    private boolean isCurrentUserBlockedOrLeft(Long roomId, Long userId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM CHAT_ROOM_USERS
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                   AND (NVL(BLOCKED_YN, 'N') = 'Y' OR NVL(LEFT_YN, 'N') = 'Y')
                """, Integer.class, roomId, userId);
        return count != null && count > 0;
    }

    private void requireRoomAccess(Long roomId, Long userId) {
        requireLogin(userId);
        if (roomId == null) {
            throw new IllegalArgumentException("채팅방 정보가 필요합니다.");
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                  FROM CHAT_ROOM_USERS
                 WHERE ROOM_ID = ?
                   AND USER_ID = ?
                """, Integer.class, roomId, userId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("채팅방에 접근할 수 없습니다.");
        }
    }

    private synchronized void ensureSchema() {
        if (schemaChecked) {
            return;
        }
        ensureTable("CHAT_ROOMS", """
                CREATE TABLE CHAT_ROOMS (
                    ROOM_ID NUMBER PRIMARY KEY,
                    ITEM_TYPE VARCHAR2(20) NOT NULL,
                    ITEM_ID NUMBER NOT NULL,
                    OWNER_ID NUMBER NOT NULL,
                    REQUESTER_ID NUMBER NOT NULL,
                    CREATED_AT DATE DEFAULT SYSDATE NOT NULL,
                    UPDATED_AT DATE DEFAULT SYSDATE NOT NULL,
                    CONSTRAINT UQ_CHAT_ROOMS UNIQUE (ITEM_TYPE, ITEM_ID, OWNER_ID, REQUESTER_ID)
                )
                """);
        ensureTableColumn("CHAT_ROOMS", "CREATED_AT", "CREATED_AT DATE DEFAULT SYSDATE NOT NULL");
        ensureTableColumn("CHAT_ROOMS", "UPDATED_AT", "UPDATED_AT DATE DEFAULT SYSDATE NOT NULL");

        ensureTable("CHAT_ROOM_USERS", """
                CREATE TABLE CHAT_ROOM_USERS (
                    ROOM_ID NUMBER NOT NULL,
                    USER_ID NUMBER NOT NULL,
                    MUTE_YN CHAR(1) DEFAULT 'N' NOT NULL,
                    BLOCKED_YN CHAR(1) DEFAULT 'N' NOT NULL,
                    LEFT_YN CHAR(1) DEFAULT 'N' NOT NULL,
                    LAST_READ_AT DATE,
                    CONSTRAINT PK_CHAT_ROOM_USERS PRIMARY KEY (ROOM_ID, USER_ID)
                )
                """);
        ensureTableColumn("CHAT_ROOM_USERS", "MUTE_YN", "MUTE_YN CHAR(1) DEFAULT 'N' NOT NULL");
        ensureTableColumn("CHAT_ROOM_USERS", "BLOCKED_YN", "BLOCKED_YN CHAR(1) DEFAULT 'N' NOT NULL");
        ensureTableColumn("CHAT_ROOM_USERS", "LEFT_YN", "LEFT_YN CHAR(1) DEFAULT 'N' NOT NULL");
        ensureTableColumn("CHAT_ROOM_USERS", "LAST_READ_AT", "LAST_READ_AT DATE");
        ensureTableColumn("CHAT_ROOM_USERS", "VISIBLE_AFTER_MESSAGE_ID", "VISIBLE_AFTER_MESSAGE_ID NUMBER");

        ensureTable("CHAT_MESSAGES", """
                CREATE TABLE CHAT_MESSAGES (
                    MESSAGE_ID NUMBER PRIMARY KEY,
                    ROOM_ID NUMBER NOT NULL,
                    SENDER_ID NUMBER NOT NULL,
                    MESSAGE_CONTENT VARCHAR2(1000) NOT NULL,
                    READ_YN CHAR(1) DEFAULT 'N' NOT NULL,
                    CREATED_AT DATE DEFAULT SYSDATE NOT NULL
                )
                """);
        ensureTableColumn("CHAT_MESSAGES", "READ_YN", "READ_YN CHAR(1) DEFAULT 'N' NOT NULL");
        ensureTableColumn("CHAT_MESSAGES", "CREATED_AT", "CREATED_AT DATE DEFAULT SYSDATE NOT NULL");

        ensureSequence("SEQ_CHAT_ROOMS");
        ensureSequence("SEQ_CHAT_MESSAGES");
        schemaChecked = true;
    }

    private void ensureTable(String tableName, String ddl) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
        } catch (DataAccessException ignored) {
            try {
                jdbcTemplate.execute(ddl);
            } catch (DataAccessException ignoredToo) {
            }
        }
    }

    private void ensureTableColumn(String tableName, String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
                    Integer.class,
                    tableName,
                    columnName);
            if (count == null || count == 0) {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD (" + ddl + ")");
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void ensureSequence(String sequenceName) {
        try {
            jdbcTemplate.queryForObject("SELECT " + sequenceName + ".NEXTVAL FROM DUAL", Long.class);
        } catch (DataAccessException ignored) {
            try {
                jdbcTemplate.execute("CREATE SEQUENCE " + sequenceName + " START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE");
            } catch (DataAccessException ignoredToo) {
            }
        }
    }

    private Long nextRoomId() {
        Long id = jdbcTemplate.queryForObject("SELECT NVL(MAX(ROOM_ID), 0) + 1 FROM CHAT_ROOMS", Long.class);
        return id == null ? 1L : id;
    }

    private Long nextMessageId() {
        Long id = jdbcTemplate.queryForObject("SELECT NVL(MAX(MESSAGE_ID), 0) + 1 FROM CHAT_MESSAGES", Long.class);
        return id == null ? 1L : id;
    }

    private String normalizeItemType(String itemType) {
        if ("found".equalsIgnoreCase(itemType) || "습득물".equals(itemType)) {
            return "found";
        }
        if ("lost".equalsIgnoreCase(itemType) || "분실물".equals(itemType)) {
            return "lost";
        }
        throw new IllegalArgumentException("채팅을 시작할 게시글 종류를 확인할 수 없습니다.");
    }

    private String statusLabel(String status, String itemType) {
        if ("RETURNED".equalsIgnoreCase(status) || "반환완료".equals(status)) {
            return "반환완료";
        }
        if ("found".equals(itemType)) {
            return "보관중";
        }
        return "접수중";
    }

    private String safeFlag(String value) {
        return "Y".equalsIgnoreCase(value) ? "Y" : "N";
    }

    private String safeText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String initial(String name) {
        if (!StringUtils.hasText(name)) {
            return "유";
        }
        return name.trim().substring(0, 1);
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
    }
}
