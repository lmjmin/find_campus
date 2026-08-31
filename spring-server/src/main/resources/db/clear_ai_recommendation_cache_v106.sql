-- Recalculate AI recommendations with the stricter scoring code.
-- Run this once in SQL Developer if old recommendation scores are still visible.
DELETE FROM AI_RECOMMENDATIONS;
COMMIT;