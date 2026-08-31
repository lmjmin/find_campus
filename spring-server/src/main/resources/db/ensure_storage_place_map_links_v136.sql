SET DEFINE OFF;
-- Link real storage places to map locations used by the storage guide page.

UPDATE STORAGE_PLACES
   SET LOCATION_ID = (
       SELECT MIN(LOCATION_ID)
         FROM LOCATIONS
        WHERE USE_YN = 'Y'
          AND (LOCATION_NAME = '8. 학생복지관'
               OR REGEXP_REPLACE(LOCATION_NAME, '^[0-9]+\.\s*', '') = '학생복지관')
   ),
       PHONE = '051-629-0514',
       OPERATING_HOURS = '평일 09:00 ~ 17:00'
 WHERE STORAGE_NAME IN ('학생복지관 2층 학생·장학봉사팀', '학생장학봉사팀 분실물 센터')
    OR STORAGE_NAME LIKE '%학생장학봉사팀%';

UPDATE STORAGE_PLACES
   SET LOCATION_ID = (
       SELECT MIN(LOCATION_ID)
         FROM LOCATIONS
        WHERE USE_YN = 'Y'
          AND (LOCATION_NAME = '10. 중앙도서관'
               OR REGEXP_REPLACE(LOCATION_NAME, '^[0-9]+\.\s*', '') = '중앙도서관')
   ),
       PHONE = '051-629-3118',
       OPERATING_HOURS = '평일 09:00 ~ 21:00'
 WHERE STORAGE_NAME LIKE '%중앙도서관%';

UPDATE STORAGE_PLACES
   SET LOCATION_ID = (
       SELECT MIN(LOCATION_ID)
         FROM LOCATIONS
        WHERE USE_YN = 'Y'
          AND (LOCATION_NAME = '1. 대학본관'
               OR REGEXP_REPLACE(LOCATION_NAME, '^[0-9]+\.\s*', '') = '대학본관')
   ),
       PHONE = '051-629-1000',
       OPERATING_HOURS = '평일 09:00 ~ 17:00'
 WHERE STORAGE_NAME LIKE '%대학본관%';

UPDATE STORAGE_PLACES
   SET LOCATION_ID = NULL
 WHERE STORAGE_NAME LIKE '학과사무실%';

COMMIT;
