-- ============================================================
-- SkillSwap Database Initialization Script
-- Student Skill Exchange Platform
-- Compatible with MySQL 8.0+
-- ============================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS skillswap
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE skillswap;

-- Drop tables in reverse order of foreign key dependency if re-initializing
DROP TABLE IF EXISTS skill_exchange_requests;
DROP TABLE IF EXISTS skills;
DROP TABLE IF EXISTS students;

-- ============================================================
-- Table: STUDENTS
-- ============================================================
CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_info VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_student_name_not_empty CHECK (CHAR_LENGTH(TRIM(student_name)) > 0),
    CONSTRAINT chk_email_not_empty CHECK (CHAR_LENGTH(TRIM(email)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Table: SKILLS
-- ============================================================
CREATE TABLE skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    skill_type ENUM('TEACH', 'LEARN') NOT NULL,
    proficiency_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skills_student
        FOREIGN KEY (student_id) REFERENCES students(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_skill_name_not_empty CHECK (CHAR_LENGTH(TRIM(skill_name)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for fast lookup and smart matching
CREATE INDEX idx_skills_student_id ON skills(student_id);
CREATE INDEX idx_skills_name ON skills(skill_name);
CREATE INDEX idx_skills_type ON skills(skill_type);
CREATE INDEX idx_skills_matching ON skills(skill_name, skill_type);

-- ============================================================
-- Table: SKILL_EXCHANGE_REQUESTS
-- ============================================================
CREATE TABLE skill_exchange_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    receiver_id INT NOT NULL,
    offered_skill_id INT NOT NULL,
    wanted_skill_id INT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' NOT NULL,
    CONSTRAINT fk_requests_requester
        FOREIGN KEY (requester_id) REFERENCES students(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_requests_receiver
        FOREIGN KEY (receiver_id) REFERENCES students(student_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_requests_offered_skill
        FOREIGN KEY (offered_skill_id) REFERENCES skills(skill_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_requests_wanted_skill
        FOREIGN KEY (wanted_skill_id) REFERENCES skills(skill_id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for requests
CREATE INDEX idx_requests_requester ON skill_exchange_requests(requester_id);
CREATE INDEX idx_requests_receiver ON skill_exchange_requests(receiver_id);
CREATE INDEX idx_requests_status ON skill_exchange_requests(status);
CREATE INDEX idx_requests_active_check ON skill_exchange_requests(requester_id, receiver_id, offered_skill_id, wanted_skill_id, status);

-- ============================================================
-- Sample Demo Data
-- Demonstrates reciprocal skill matching:
-- Arun: Teaches Java, Wants to Learn UI Design
-- Priya: Teaches UI Design, Wants to Learn Java
-- ============================================================
INSERT INTO students (student_id, student_name, email, contact_info) VALUES
(1, 'Arun Kumar', 'arun.kumar@example.com', '+91-9876543210 / LinkedIn: @arunkumar'),
(2, 'Priya Sharma', 'priya.sharma@example.com', '+91-9876543211 / Discord: @priyaui'),
(3, 'Sneha Patel', 'sneha.patel@example.com', '+91-9876543212 / GitHub: @snehap'),
(4, 'Rahul Verma', 'rahul.verma@example.com', '+91-9876543213 / Telegram: @rahulv');

INSERT INTO skills (skill_id, student_id, skill_name, skill_type, proficiency_level, description) VALUES
-- Arun's skills
(1, 1, 'Java', 'TEACH', 'ADVANCED', 'Core Java, OOP, Collections, Multithreading & JDBC'),
(2, 1, 'UI Design', 'LEARN', 'BEGINNER', 'Wants to learn Figma, color theory and component systems'),

-- Priya's skills (Mutual Match with Arun!)
(3, 2, 'UI Design', 'TEACH', 'ADVANCED', 'Figma expert, Wireframing, UX heuristics, Design tokens'),
(4, 2, 'Java', 'LEARN', 'BEGINNER', 'Wants to master backend development and OOP concepts'),

-- Sneha's skills
(5, 3, 'Python', 'TEACH', 'INTERMEDIATE', 'Python 3, Pandas, NumPy, Data Analysis'),
(6, 3, 'Machine Learning', 'LEARN', 'BEGINNER', 'Wants to learn Scikit-Learn and Neural Networks'),

-- Rahul's skills (Mutual Match with Sneha!)
(7, 4, 'Machine Learning', 'TEACH', 'ADVANCED', 'Supervised learning, deep learning architectures'),
(8, 4, 'Python', 'LEARN', 'INTERMEDIATE', 'Looking to brush up advanced asynchronous Python');

-- Optional demo exchange request between Arun and Priya
INSERT INTO skill_exchange_requests (request_id, requester_id, receiver_id, offered_skill_id, wanted_skill_id, status) VALUES
(1, 1, 2, 1, 3, 'PENDING');
