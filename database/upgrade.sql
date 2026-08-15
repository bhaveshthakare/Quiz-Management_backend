-- =============================================================
-- Upgrade script for databases created with the ORIGINAL schema
-- (before the advanced features were added).
-- Run this against your existing quiz_platform database.
-- =============================================================

ALTER TABLE quizzes
    ADD COLUMN thumbnail           VARCHAR(255) NULL AFTER status,
    ADD COLUMN start_date          TIMESTAMP NULL AFTER thumbnail,
    ADD COLUMN end_date            TIMESTAMP NULL AFTER start_date,
    ADD COLUMN negative_marking    BOOLEAN NOT NULL DEFAULT FALSE AFTER end_date,
    ADD COLUMN negative_mark_value DECIMAL(4,2) NOT NULL DEFAULT 0.00 AFTER negative_marking;

ALTER TABLE answers
    ADD COLUMN question_position INT NOT NULL DEFAULT 0 AFTER is_correct,
    ADD COLUMN option_order      TEXT NULL AFTER question_position;