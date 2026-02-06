--1. 用户表：存储系统用户信息
CREATE TABLE IF NOT EXISTS t_user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,          -- 用户ID，主键，自动增长
    username TEXT UNIQUE NOT NULL,                 -- 用户名，唯一，不能为空
    password TEXT NOT NULL,                        -- 密码，不能为空
    nickname TEXT,                                 -- 昵称，可以为空
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建时间，默认当前时间
);
--2. 账单分类表：存储收入和支出分类
CREATE TABLE IF NOT EXISTS t_bill_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,          -- 分类ID，主键，自动增长
    name TEXT NOT NULL UNIQUE,                     -- 分类名称，不能为空，唯一
    type INTEGER NOT NULL                          -- 分类类型：1-收入，2-支出
);
--3. 账单表：存储用户的记账记录
CREATE TABLE IF NOT EXISTS t_bill (
    id INTEGER PRIMARY KEY AUTOINCREMENT,          -- 账单ID，主键，自动增长
    user_id INTEGER NOT NULL,                      -- 用户ID，外键关联t_user表
    category_id INTEGER NOT NULL,                  -- 分类ID，外键关联t_bill_category表
    amount REAL NOT NULL,                          -- 金额，不能为空
    remark TEXT,                                   -- 备注，可以为空
    bill_time TIMESTAMP NOT NULL,                  -- 记账时间，不能为空
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间

-- 外键约束：确保数据完整性
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE, -- 用户删除时级联删除账单
    FOREIGN KEY (category_id) REFERENCES t_bill_category(id) -- 分类ID必须有效
    );
--4. 插入默认账单分类数据
-- 注意：使用INSERT OR IGNORE防止重复插入
INSERT OR IGNORE INTO t_bill_category (name, type) VALUES
-- 支出分类（type=2）
('餐饮', 2),      -- 餐饮消费
('交通', 2),      -- 交通费用
('购物', 2),      -- 购物消费
('娱乐', 2),      -- 娱乐消费
('住房', 2),      -- 住房相关费用
-- 收入分类（type=1）
('工资', 1),      -- 工资收入
('奖金', 1),      -- 奖金收入
('兼职', 1),      -- 兼职收入
('投资', 1),      -- 投资收益
('其他', 2);      -- 其他支出
--5. 插入默认管理员用户
-- 用户名：admin，密码：admin123
INSERT OR IGNORE INTO t_user (username, password, nickname) VALUES
('admin', 'admin123', '系统管理员');
-- 结束脚本
PRAGMA foreign_keys = ON; -- 启用外键约束