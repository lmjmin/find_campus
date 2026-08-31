-- FindCampus real-mode cleanup
-- SQL Developer 또는 Oracle 접속 도구에서 sunny 계정으로 실행하세요.
-- 제목/물건명/설명에 테스트 표식이 들어간 기존 샘플 게시물만 삭제합니다.

DELETE FROM ITEM_IMAGES
 WHERE (ITEM_TYPE = 'lost' AND ITEM_ID IN (
        SELECT LOST_ID FROM LOST_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ))
    OR (ITEM_TYPE = 'found' AND ITEM_ID IN (
        SELECT FOUND_ID FROM FOUND_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ));

DELETE FROM AI_RECOMMENDATIONS
 WHERE (SOURCE_TYPE = 'lost' AND SOURCE_ID IN (
        SELECT LOST_ID FROM LOST_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ))
    OR (TARGET_TYPE = 'lost' AND TARGET_ID IN (
        SELECT LOST_ID FROM LOST_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ))
    OR (SOURCE_TYPE = 'found' AND SOURCE_ID IN (
        SELECT FOUND_ID FROM FOUND_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ))
    OR (TARGET_TYPE = 'found' AND TARGET_ID IN (
        SELECT FOUND_ID FROM FOUND_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ));

DELETE FROM AI_ITEM_EMBEDDINGS
 WHERE (ITEM_TYPE = 'lost' AND ITEM_ID IN (
        SELECT LOST_ID FROM LOST_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ))
    OR (ITEM_TYPE = 'found' AND ITEM_ID IN (
        SELECT FOUND_ID FROM FOUND_ITEMS
         WHERE TITLE LIKE '%테스트%'
            OR ITEM_NAME LIKE '%테스트%'
            OR TITLE LIKE '%코덱스%'
            OR ITEM_NAME LIKE '%코덱스%'
            OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
            OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0
       ));

DELETE FROM LOST_ITEMS
 WHERE TITLE LIKE '%테스트%'
    OR ITEM_NAME LIKE '%테스트%'
    OR TITLE LIKE '%코덱스%'
    OR ITEM_NAME LIKE '%코덱스%'
    OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
    OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0;

DELETE FROM FOUND_ITEMS
 WHERE TITLE LIKE '%테스트%'
    OR ITEM_NAME LIKE '%테스트%'
    OR TITLE LIKE '%코덱스%'
    OR ITEM_NAME LIKE '%코덱스%'
    OR DBMS_LOB.INSTR(DESCRIPTION, '테스트') > 0
    OR DBMS_LOB.INSTR(DESCRIPTION, '코덱스') > 0;

COMMIT;