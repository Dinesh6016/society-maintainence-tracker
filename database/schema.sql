-- Society Maintenance Tracker - Database Schema
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS society_tracker CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE society_tracker;

CREATE TABLE IF NOT EXISTS users (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100)  NOT NULL,
    email            VARCHAR(150)  NOT NULL UNIQUE,
    password         VARCHAR(255)  NOT NULL,
    phone            VARCHAR(20),
    apartment_number VARCHAR(20),
    role             VARCHAR(10)   NOT NULL DEFAULT 'RESIDENT',
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role  (role)
);

CREATE TABLE IF NOT EXISTS complaints (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_id      BIGINT        NOT NULL,
    category         VARCHAR(20)   NOT NULL,
    title            VARCHAR(200)  NOT NULL,
    description      TEXT          NOT NULL,
    photo_url        VARCHAR(500),
    photo_public_id  VARCHAR(200),
    status           VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    priority         VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM',
    is_overdue       BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at      DATETIME,
    FOREIGN KEY (resident_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_complaints_resident  (resident_id),
    INDEX idx_complaints_status    (status),
    INDEX idx_complaints_category  (category),
    INDEX idx_complaints_priority  (priority),
    INDEX idx_complaints_overdue   (is_overdue),
    INDEX idx_complaints_created   (created_at)
);

CREATE TABLE IF NOT EXISTS complaint_status_history (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT       NOT NULL,
    old_status   VARCHAR(20),
    new_status   VARCHAR(20)  NOT NULL,
    changed_by   BIGINT       NOT NULL,
    changed_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note         TEXT,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by)   REFERENCES users(id),
    INDEX idx_status_history_complaint (complaint_id)
);

CREATE TABLE IF NOT EXISTS complaint_priority_history (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT       NOT NULL,
    old_priority VARCHAR(10),
    new_priority VARCHAR(10)  NOT NULL,
    changed_by   BIGINT       NOT NULL,
    changed_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note         TEXT,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by)   REFERENCES users(id),
    INDEX idx_priority_history_complaint (complaint_id)
);

CREATE TABLE IF NOT EXISTS notices (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    title      VARCHAR(200) NOT NULL,
    content    TEXT         NOT NULL,
    important  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_by BIGINT       NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_notices_important (important),
    INDEX idx_notices_created   (created_at)
);
