package com.example.find_campus.service;

import java.util.Collections;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.example.find_campus.dao.IInquiryDao;
import com.example.find_campus.dto.InquiryDto;
import com.example.find_campus.dto.NotificationDto;
import com.example.find_campus.dto.ItemSearchDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final IInquiryDao inquiryDao;
    private final JdbcTemplate jdbcTemplate;
    private final AppNotificationService appNotificationService;
    private boolean schemaChecked;

    @Transactional
    public void createInquiry(InquiryDto inquiryDto) {
        ensureSchema();
        if (!StringUtils.hasText(inquiryDto.getInquiryType())
                || !StringUtils.hasText(inquiryDto.getTitle())
                || !StringUtils.hasText(inquiryDto.getContent())) {
            throw new IllegalArgumentException("\uBB38\uC758 \uC720\uD615, \uC81C\uBAA9, \uB0B4\uC6A9\uC744 \uC785\uB825\uD574\uC8FC\uC138\uC694.");
        }

        inquiryDto.setInquiryType(inquiryDto.getInquiryType().trim());
        inquiryDto.setTitle(inquiryDto.getTitle().trim());
        inquiryDto.setContent(inquiryDto.getContent().trim());

        if (StringUtils.hasText(inquiryDto.getReplyEmail())) {
            inquiryDto.setReplyEmail(inquiryDto.getReplyEmail().trim());
        } else {
            inquiryDto.setReplyEmail(null);
        }

        inquiryDto.setStatus("WAITING");
        inquiryDao.insertInquiry(inquiryDto);
        notifyInquiryCreated(inquiryDto);
    }

    public InquiryDto findInquiryById(Long inquiryId) {
        ensureSchema();
        if (inquiryId == null) {
            return null;
        }
        return inquiryDao.findInquiryById(inquiryId);
    }

    public List<InquiryDto> findInquiries(ItemSearchDto searchDto) {
        ensureSchema();
        try {
            return inquiryDao.findInquiries(searchDto);
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }

    public List<InquiryDto> findUserInquiries(Long userId) {
        ensureSchema();
        if (userId == null) {
            return Collections.emptyList();
        }
        try {
            return inquiryDao.findInquiriesByUserId(userId);
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }

    public int countInquiries(ItemSearchDto searchDto) {
        ensureSchema();
        try {
            return inquiryDao.countInquiries(searchDto);
        } catch (DataAccessException e) {
            return 0;
        }
    }

    public int countWaitingInquiries() {
        return countInquiriesByStatus("WAITING");
    }

    public int countAnsweredInquiries() {
        return countInquiriesByStatus("ANSWERED");
    }

    public int countClosedInquiries() {
        return countInquiriesByStatus("CLOSED");
    }

    @Transactional
    public void updateInquiryStatus(Long inquiryId, String status) {
        ensureSchema();
        if (inquiryId == null || !StringUtils.hasText(status)) {
            throw new IllegalArgumentException("\uBB38\uC758 \uBC88\uD638\uC640 \uC0C1\uD0DC\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694.");
        }
        inquiryDao.updateInquiryStatus(inquiryId, status);
    }

    @Transactional
    public void answerInquiry(Long inquiryId, String answerContent, Long answerAdminId) {
        ensureSchema();
        if (inquiryId == null) {
            throw new IllegalArgumentException("\uBB38\uC758 \uBC88\uD638\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694.");
        }
        if (!StringUtils.hasText(answerContent)) {
            throw new IllegalArgumentException("\uB2F5\uBCC0 \uB0B4\uC6A9\uC744 \uC785\uB825\uD574\uC8FC\uC138\uC694.");
        }

        InquiryDto inquiry = inquiryDao.findInquiryById(inquiryId);
        inquiryDao.answerInquiry(inquiryId, answerContent.trim(), answerAdminId);
        notifyInquiryAnswered(inquiry);
    }


    private void notifyInquiryCreated(InquiryDto inquiry) {
        if (inquiry == null || inquiry.getInquiryId() == null) {
            return;
        }

        try {
            String title = StringUtils.hasText(inquiry.getTitle()) ? inquiry.getTitle() : "문의";
            appNotificationService.notifyAdmins(
                    "INQUIRY_RECEIVED",
                    "새 문의가 작성되었습니다.",
                    title + " 문의가 작성되었습니다. 문의 관리에서 내용을 확인하고 답변을 작성해 주세요.",
                    "inquiry",
                    inquiry.getInquiryId());
        } catch (RuntimeException ignored) {
        }
    }

    private void notifyInquiryAnswered(InquiryDto inquiry) {
        if (inquiry == null || inquiry.getUserId() == null) {
            return;
        }

        NotificationDto notification = new NotificationDto();
        notification.setUserId(inquiry.getUserId());
        notification.setTitle("\uBB38\uC758 \uB2F5\uBCC0\uC774 \uB4F1\uB85D\uB418\uC5C8\uC2B5\uB2C8\uB2E4.");
        notification.setMessage("\uAD00\uB9AC\uC790\uAC00 \uBB38\uC758\uC5D0 \uB2F5\uBCC0\uD588\uC2B5\uB2C8\uB2E4. \uBB38\uC758 \uB0B4\uC5ED\uC5D0\uC11C \uB2F5\uBCC0\uC744 \uD655\uC778\uD574 \uC8FC\uC138\uC694.");
        notification.setNotificationType("INQUIRY_ANSWER");
        notification.setTargetType("inquiry");
        notification.setTargetId(inquiry.getInquiryId());
        appNotificationService.createNotification(notification);
    }

    private int countInquiriesByStatus(String status) {
        ensureSchema();
        try {
            return inquiryDao.countInquiriesByStatus(status);
        } catch (DataAccessException e) {
            return 0;
        }
    }

    private synchronized void ensureSchema() {
        if (schemaChecked) {
            return;
        }
        ensureTable();
        ensureColumns();
        ensureSequence();
        schemaChecked = true;
    }

    private void ensureTable() {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM INQUIRIES", Integer.class);
        } catch (DataAccessException ignored) {
            executeDdl("""
                    CREATE TABLE INQUIRIES (
                        INQUIRY_ID NUMBER PRIMARY KEY,
                        USER_ID NUMBER NULL,
                        INQUIRY_TYPE VARCHAR2(30) NOT NULL,
                        TITLE VARCHAR2(200) NOT NULL,
                        CONTENT CLOB NOT NULL,
                        REPLY_EMAIL VARCHAR2(200),
                        STATUS VARCHAR2(30) DEFAULT 'WAITING' NOT NULL,
                        ANSWER_CONTENT CLOB,
                        ANSWERED_AT DATE,
                        ANSWER_ADMIN_ID NUMBER,
                        CREATED_AT DATE DEFAULT SYSDATE NOT NULL,
                        UPDATED_AT DATE
                    )
                    """);
        }
    }

    private void ensureColumns() {
        ensureColumn("INQUIRY_ID", "INQUIRY_ID NUMBER");
        ensureColumn("USER_ID", "USER_ID NUMBER NULL");
        ensureColumn("INQUIRY_TYPE", "INQUIRY_TYPE VARCHAR2(30) DEFAULT 'etc' NOT NULL");
        ensureColumn("TITLE", "TITLE VARCHAR2(200) DEFAULT 'inquiry' NOT NULL");
        ensureColumn("CONTENT", "CONTENT CLOB");
        ensureColumn("REPLY_EMAIL", "REPLY_EMAIL VARCHAR2(200)");
        ensureColumn("STATUS", "STATUS VARCHAR2(30) DEFAULT 'WAITING' NOT NULL");
        ensureColumn("ANSWER_CONTENT", "ANSWER_CONTENT CLOB");
        ensureColumn("ANSWERED_AT", "ANSWERED_AT DATE");
        ensureColumn("ANSWER_ADMIN_ID", "ANSWER_ADMIN_ID NUMBER");
        ensureColumn("CREATED_AT", "CREATED_AT DATE DEFAULT SYSDATE NOT NULL");
        ensureColumn("UPDATED_AT", "UPDATED_AT DATE");
    }

    private void ensureColumn(String columnName, String ddl) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = 'INQUIRIES' AND COLUMN_NAME = ?",
                    Integer.class,
                    columnName);
            if (count == null || count == 0) {
                executeDdl("ALTER TABLE INQUIRIES ADD (" + ddl + ")");
            }
        } catch (DataAccessException ignored) {
        }
    }

    private void ensureSequence() {
        try {
            jdbcTemplate.queryForObject("SELECT SEQ_INQUIRIES.NEXTVAL FROM DUAL", Long.class);
        } catch (DataAccessException ignored) {
            executeDdl("""
                    CREATE SEQUENCE SEQ_INQUIRIES
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
}