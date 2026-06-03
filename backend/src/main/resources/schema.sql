-- 创建数据库
CREATE DATABASE IF NOT EXISTS bookkeeping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bookkeeping;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    email VARCHAR(100),
    avatar VARCHAR(255) DEFAULT '/avatar/default.jpg',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    icon VARCHAR(50),
    type TINYINT NOT NULL COMMENT '1:收入 2:支出',
    color VARCHAR(20),
    user_id BIGINT COMMENT 'NULL表示系统预设',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 账单表
CREATE TABLE IF NOT EXISTS bill (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    type TINYINT NOT NULL COMMENT '1:收入 2:支出',
    category_id BIGINT NOT NULL,
    payment_account_id BIGINT COMMENT '支付方式/资金账户ID（关联liability表）',
    description VARCHAR(255),
    bill_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (category_id) REFERENCES category(id),
    FOREIGN KEY (payment_account_id) REFERENCES liability(id)
);

-- 预算表
CREATE TABLE IF NOT EXISTS budget (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    month VARCHAR(7) NOT NULL COMMENT '格式: 2024-01',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
);

-- 负债表
CREATE TABLE IF NOT EXISTS liability (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL COMMENT '负债名称，如花呗、信用卡、房贷',
    type TINYINT NOT NULL COMMENT '1:资产 2:负债',
    amount DECIMAL(12,2) NOT NULL COMMENT '总额度/负债金额',
    used_amount DECIMAL(12,2) DEFAULT 0 COMMENT '已使用金额',
    remaining_amount DECIMAL(12,2) DEFAULT 0 COMMENT '剩余额度',
    description VARCHAR(255) COMMENT '描述/备注',
    due_date DATE COMMENT '到期日（可选）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 负债还款记录表
CREATE TABLE IF NOT EXISTS liability_repayment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    liability_id BIGINT NOT NULL COMMENT '关联的负债ID',
    user_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL COMMENT '还款金额',
    description VARCHAR(255) COMMENT '还款说明',
    repayment_date DATE COMMENT '还款日期（可选择）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (liability_id) REFERENCES liability(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 更新现有负债的remaining_amount字段（type=2为负债）
UPDATE liability SET remaining_amount = amount WHERE type = 2 AND remaining_amount IS NULL;

-- 兼职收入表
CREATE TABLE IF NOT EXISTS part_time_job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL COMMENT '收入金额',
    description VARCHAR(255) COMMENT '工作描述/内容',
    work_date DATE NOT NULL COMMENT '工作日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 创建索引
CREATE INDEX idx_bill_user_id ON bill(user_id);
CREATE INDEX idx_bill_date ON bill(bill_date);
CREATE INDEX idx_budget_user_month ON budget(user_id, month);
CREATE INDEX idx_category_type ON category(type);
CREATE INDEX idx_liability_user_id ON liability(user_id);
CREATE INDEX idx_part_time_job_user_id ON part_time_job(user_id);
CREATE INDEX idx_part_time_job_work_date ON part_time_job(work_date);

-- 注意：资金账户由用户自己在账户管理中添加，不再自动预设

-- 插入预设收入分类
INSERT INTO category (name, icon, type, color, user_id) VALUES
('工资', 'money', 1, '#67C23A', NULL),
('奖金', 'trophy', 1, '#E6A23C', NULL),
('投资收益', 'trendCharts', 1, '#409EFF', NULL),
('兼职', 'briefcase', 1, '#909399', NULL),
('其他收入', 'more', 1, '#909399', NULL);

-- 插入预设支出分类
INSERT INTO category (name, icon, type, color, user_id) VALUES
('餐饮', 'food', 2, '#F56C6C', NULL),
('交通', 'bus', 2, '#E6A23C', NULL),
('购物', 'shopping', 2, '#F56C6C', NULL),
('居住', 'house', 2, '#67C23A', NULL),
('医疗', 'medicine', 2, '#F56C6C', NULL),
('教育', 'reading', 2, '#409EFF', NULL),
('娱乐', 'film', 2, '#909399', NULL),
('通讯', 'phone', 2, '#E6A23C', NULL),
('人情', 'user', 2, '#F56C6C', NULL),
('其他支出', 'more', 2, '#909399', NULL);
