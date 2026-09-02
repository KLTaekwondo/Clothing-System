-- ClothingSystem 初始化数据（建表 + 灌数据一体）
-- 用途：Docker 一键部署时，MySQL 容器首次启动自动执行（挂载到 /docker-entrypoint-initdb.d/）
-- 说明：
--   1. 建表语句与 JPA 实际生成的表结构完全一致（enum 类型、外键、唯一约束），避免后端 ddl-auto=update 冲突
--   2. CREATE TABLE IF NOT EXISTS：表不存在才建
--   3. INSERT IGNORE：已存在的数据自动跳过，重复执行安全
-- 登录账号：管理员 admin001 / 123456，仓库 WH001 / warehouse、WH002 / warehouse

SET NAMES utf8mb4;
START TRANSACTION;

-- ========== 建表（对齐 JPA 实体生成的结构） ==========

-- 管理员表（对应 Admin 实体 + BaseEntity）
CREATE TABLE IF NOT EXISTS t_admin
(
    admin_id    bigint auto_increment primary key,
    create_time datetime(6)                null,
    update_time datetime(6)                null,
    admin_code  varchar(255)               not null,
    password    varchar(255)               not null,
    status      enum ('DISABLE', 'ENABLE') not null,
    username    varchar(255)               not null,
    constraint UK_username unique (username),
    constraint UK_admin_code unique (admin_code)
)
    engine = InnoDB;

-- 仓库表（对应 WareHouse 实体 + BaseEntity）
CREATE TABLE IF NOT EXISTS t_warehouse
(
    ware_house_id       bigint auto_increment primary key,
    create_time         datetime(6)                      null,
    update_time         datetime(6)                      null,
    status              enum ('DISABLE', 'ENABLE')       not null,
    ware_house_code     varchar(255)                     not null,
    ware_house_name     varchar(255)                     not null,
    ware_house_password varchar(255)                     not null,
    check_status        enum ('NO_CHECK', 'UNDER_CHECK') not null,
    constraint UK_ware_house_code unique (ware_house_code),
    constraint UK_ware_house_name unique (ware_house_name)
)
    engine = InnoDB;

-- ========== 灌数据 ==========

-- 1. 管理员。BCrypt 密文对应明文密码：123456
INSERT IGNORE INTO t_admin (admin_code, username, password, status)
VALUES ('admin001', 'System Admin', '$2b$10$Z5eVAh6ANbEH5p2zxf.rLOTLpYqW8MlhH4POuXgFHXchc0.qtegn2', 'ENABLE');

-- 2. 仓库。仓库密码密文对应明文密码：warehouse
INSERT IGNORE INTO t_warehouse (ware_house_code, ware_house_name, status, ware_house_password, check_status)
VALUES
    ('WH001', 'Shanghai WH', 'ENABLE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NO_CHECK'),
    ('WH002', 'Hangzhou WH', 'ENABLE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NO_CHECK');

COMMIT;
