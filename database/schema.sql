-- =============================================================
-- Quiz Management & Online Assessment Platform - MySQL schema
-- Run against a FRESH database.
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------- Users ----------
CREATE TABLE IF NOT EXISTS `user` (
    `id`         INT          NOT NULL AUTO_INCREMENT,
    `user_name`  VARCHAR(100) NOT NULL,
    `email`      VARCHAR(150) NOT NULL,
    `password`   VARCHAR(255) NOT NULL,
    `role`       ENUM('ADMIN','STUDENT') NOT NULL DEFAULT 'STUDENT',
    `status`     ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Categories ----------
CREATE TABLE IF NOT EXISTS `categories` (
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NULL,
    `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Quizzes ----------
CREATE TABLE IF NOT EXISTS `quizzes` (
    `id`                  INT      NOT NULL AUTO_INCREMENT,
    `title`               VARCHAR(150) NOT NULL,
    `description`         TEXT     NULL,
    `category_id`         INT      NOT NULL,
    `difficulty`          ENUM('EASY','INTERMEDIATE','HARD') NOT NULL DEFAULT 'EASY',
    `duration`            INT      NOT NULL,
    `passing_score`       INT      NOT NULL DEFAULT 60,
    `max_attempts`        INT      NOT NULL DEFAULT 1,
    `status`              ENUM('DRAFT','PUBLISHED','UNPUBLISHED') NOT NULL DEFAULT 'DRAFT',
    `thumbnail`           VARCHAR(255) NULL,
    `start_date`          TIMESTAMP NULL,
    `end_date`            TIMESTAMP NULL,
    `negative_marking`    BOOLEAN  NOT NULL DEFAULT FALSE,
    `negative_mark_value` DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    `created_at`          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_quiz_category` (`category_id`),
    KEY `idx_quiz_status` (`status`),
    CONSTRAINT `fk_quiz_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Questions ----------
CREATE TABLE IF NOT EXISTS `questions` (
    `id`            INT      NOT NULL AUTO_INCREMENT,
    `quiz_id`       INT      NOT NULL,
    `question_text` TEXT     NOT NULL,
    `marks`         INT      NOT NULL DEFAULT 1,
    `explanation`   TEXT     NULL,
    `difficulty`    ENUM('EASY','INTERMEDIATE','HARD') NOT NULL DEFAULT 'EASY',
    `created_at`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_question_quiz` (`quiz_id`),
    CONSTRAINT `fk_question_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Options ----------
CREATE TABLE IF NOT EXISTS `options` (
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `question_id` INT          NOT NULL,
    `option_text` VARCHAR(255) NOT NULL,
    `is_correct`  BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`),
    KEY `idx_option_question` (`question_id`),
    CONSTRAINT `fk_option_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Attempts ----------
CREATE TABLE IF NOT EXISTS `attempts` (
    `id`                INT          NOT NULL AUTO_INCREMENT,
    `quiz_id`           INT          NOT NULL,
    `user_id`           INT          NOT NULL,
    `score`             INT          NOT NULL DEFAULT 0,
    `percentage`        DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    `correct_answers`   INT          NOT NULL DEFAULT 0,
    `incorrect_answers` INT          NOT NULL DEFAULT 0,
    `unanswered`        INT          NOT NULL DEFAULT 0,
    `time_taken`        INT          NULL,
    `status`            ENUM('IN_PROGRESS','PASSED','FAILED') NOT NULL DEFAULT 'IN_PROGRESS',
    `started_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at`      TIMESTAMP    NULL,
    PRIMARY KEY (`id`),
    KEY `idx_attempt_quiz` (`quiz_id`),
    KEY `idx_attempt_user` (`user_id`),
    CONSTRAINT `fk_attempt_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`),
    CONSTRAINT `fk_attempt_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Answers ----------
CREATE TABLE IF NOT EXISTS `answers` (
    `id`                INT  NOT NULL AUTO_INCREMENT,
    `attempt_id`        INT  NOT NULL,
    `question_id`       INT  NOT NULL,
    `selected_option_id` INT NULL,
    `is_correct`        BOOLEAN NOT NULL DEFAULT FALSE,
    `question_position` INT  NOT NULL DEFAULT 0,
    `option_order`      TEXT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_answer_attempt` (`attempt_id`),
    KEY `idx_answer_question` (`question_id`),
    KEY `idx_answer_option` (`selected_option_id`),
    CONSTRAINT `fk_answer_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `attempts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Password reset tokens ----------
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
    `id`         INT          NOT NULL AUTO_INCREMENT,
    `user_id`    INT          NOT NULL,
    `token`      VARCHAR(255) NOT NULL,
    `expires_at` TIMESTAMP    NOT NULL,
    `used`       BOOLEAN      NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_reset_token` (`token`),
    CONSTRAINT `fk_reset_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Certificates ----------
CREATE TABLE IF NOT EXISTS `certificates` (
    `id`              INT          NOT NULL AUTO_INCREMENT,
    `attempt_id`      INT          NOT NULL,
    `user_id`         INT          NOT NULL,
    `quiz_id`         INT          NOT NULL,
    `certificate_url` VARCHAR(255) NOT NULL,
    `issued_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_cert_attempt` (`attempt_id`),
    CONSTRAINT `fk_cert_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `attempts` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cert_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_cert_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Notifications ----------
CREATE TABLE IF NOT EXISTS `quiz_notifications` (
    `id`      INT          NOT NULL AUTO_INCREMENT,
    `user_id` INT          NOT NULL,
    `type`    ENUM('QUIZ_COMPLETION','RESULT','CERTIFICATE') NOT NULL,
    `message` VARCHAR(255) NOT NULL,
    `is_read` BOOLEAN      NOT NULL DEFAULT FALSE,
    `sent_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_notif_user` (`user_id`),
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;