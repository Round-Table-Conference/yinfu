-- 银龄心伴数据库初始化脚本
-- 数据库: MySQL 5.7

CREATE DATABASE IF NOT EXISTS yinfu_app DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE yinfu_app;

-- 用户表
DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    real_name VARCHAR(50) COMMENT '真实姓名',
    age INT COMMENT '年龄',
    gender VARCHAR(10) COMMENT '性别',
    blood_type VARCHAR(10) COMMENT '血型',
    allergy VARCHAR(200) COMMENT '过敏史',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 紧急联系人表
DROP TABLE IF EXISTS t_emergency_contact;
CREATE TABLE t_emergency_contact (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '联系人ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    relationship VARCHAR(50) COMMENT '与用户关系',
    is_primary TINYINT DEFAULT 0 COMMENT '是否为主要联系人',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='紧急联系人表';

-- 用药提醒表
DROP TABLE IF EXISTS t_medication_reminder;
CREATE TABLE t_medication_reminder (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '提醒ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '药物名称',
    dosage VARCHAR(50) NOT NULL COMMENT '剂量',
    time VARCHAR(20) NOT NULL COMMENT '服药时间',
    frequency VARCHAR(50) NOT NULL COMMENT '服用频率',
    remark VARCHAR(200) COMMENT '备注',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用',
    is_taken TINYINT DEFAULT 0 COMMENT '是否已服用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药提醒表';

-- 用药记录表
DROP TABLE IF EXISTS t_medication_record;
CREATE TABLE t_medication_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    reminder_id BIGINT NOT NULL COMMENT '提醒ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    taken_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '实际服药时间',
    status VARCHAR(20) DEFAULT 'taken' COMMENT '状态（taken已服用/missed漏服）',
    INDEX idx_reminder_id (reminder_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (reminder_id) REFERENCES t_medication_reminder(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用药记录表';

-- 健康档案表
DROP TABLE IF EXISTS t_health_record;
CREATE TABLE t_health_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_type VARCHAR(50) NOT NULL COMMENT '记录类型（blood_pressure血糖/heart_rate心率/temperature体温）',
    value VARCHAR(50) NOT NULL COMMENT '测量值',
    unit VARCHAR(20) NOT NULL COMMENT '单位',
    measured_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '测量时间',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_record_type (record_type),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康档案表';

-- 病史记录表
DROP TABLE IF EXISTS t_medical_history;
CREATE TABLE t_medical_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '病史ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    disease_name VARCHAR(100) NOT NULL COMMENT '疾病名称',
    diagnosis_date DATE COMMENT '诊断日期',
    current_status VARCHAR(50) COMMENT '当前状态',
    description TEXT COMMENT '详细描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病史记录表';

-- SOS求救记录表
DROP TABLE IF EXISTS t_sos_record;
CREATE TABLE t_sos_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '求救ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    latitude DECIMAL(10,6) COMMENT '纬度',
    longitude DECIMAL(10,6) COMMENT '经度',
    address VARCHAR(500) COMMENT '地址',
    status VARCHAR(20) DEFAULT 'triggered' COMMENT '状态（triggered已触发/handled已处理/cancelled已取消）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SOS求救记录表';

-- 插入测试用户（密码是123456的BCrypt加密）
INSERT INTO t_user (username, password, phone, real_name, age, gender, blood_type, allergy) VALUES
('test', '$2a$10$AiOM69ygLu1l0dP5KzWjSOpc4NXzCyc8VD0haG/3LNqjJ/cBW0N2S', '13800138000', '测试用户', 65, '男', 'O型', '无');

-- 插入测试紧急联系人
INSERT INTO t_emergency_contact (user_id, name, phone, relationship, is_primary) VALUES
(1, '张三', '13900139000', '子女', 1);

-- 插入测试用药提醒
INSERT INTO t_medication_reminder (user_id, name, dosage, time, frequency, remark) VALUES
(1, '降压药', '1片', '08:00', '每天', '饭后服用'),
(1, '阿司匹林', '1片', '20:00', '每天', '睡前服用'),
(1, '钙片', '2片', '12:00', '每天', '随餐服用');

-- 插入测试健康记录
INSERT INTO t_health_record (user_id, record_type, value, unit, remark) VALUES
(1, 'blood_pressure', '120/80', 'mmHg', '正常'),
(1, 'heart_rate', '72', '次/分', '正常'),
(1, 'blood_sugar', '5.6', 'mmol/L', '正常'),
(1, 'temperature', '36.5', '°C', '正常');

-- 插入测试病史
INSERT INTO t_medical_history (user_id, disease_name, diagnosis_date, current_status, description) VALUES
(1, '高血压', '2020-05-01', '稳定', '规律服用降压药，血压控制良好');
