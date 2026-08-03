-- ClothingSystem 基础开发数据
-- 不包含管理员、商品、SKU、库存及任何订单模块数据。
-- 仓库与供应商关联数据库中现有的第一个管理员，执行前请确保至少存在一名管理员。

SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 获取现有管理员，本脚本不创建管理员
SET @admin_id = (
    SELECT admin_id
    FROM t_admin
    ORDER BY admin_id
    LIMIT 1
);

-- 2. 仓库测试数据
-- BCrypt 密文对应明文密码：password
INSERT IGNORE INTO t_warehouse (
    ware_house_code,
    ware_house_name,
    status,
    ware_house_password,
    check_status,
    admin_id,
    create_time,
    update_time
)
VALUES
    (
        'WH001',
        '总仓库',
        'ENABLE',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'NO_CHECK',
        @admin_id,
        NOW(),
        NOW()
    ),
    (
        'WH002',
        '城东仓库',
        'ENABLE',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'NO_CHECK',
        @admin_id,
        NOW(),
        NOW()
    ),
    (
        'WH003',
        '城西仓库',
        'ENABLE',
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'NO_CHECK',
        @admin_id,
        NOW(),
        NOW()
    );

-- 3. 获取仓库 ID
SET @warehouse_1_id = (
    SELECT ware_house_id
    FROM t_warehouse
    WHERE ware_house_code = 'WH001'
    LIMIT 1
);

SET @warehouse_2_id = (
    SELECT ware_house_id
    FROM t_warehouse
    WHERE ware_house_code = 'WH002'
    LIMIT 1
);

SET @warehouse_3_id = (
    SELECT ware_house_id
    FROM t_warehouse
    WHERE ware_house_code = 'WH003'
    LIMIT 1
);

-- 4. 员工测试数据
INSERT IGNORE INTO t_employee (
    employee_code,
    employee_name,
    status,
    ware_house_id,
    create_time,
    update_time
)
VALUES
    ('EMP001', '张明', 'ENABLE', @warehouse_1_id, NOW(), NOW()),
    ('EMP002', '李雪', 'ENABLE', @warehouse_1_id, NOW(), NOW()),
    ('EMP003', '王强', 'ENABLE', @warehouse_2_id, NOW(), NOW()),
    ('EMP004', '陈静', 'ENABLE', @warehouse_2_id, NOW(), NOW()),
    ('EMP005', '赵磊', 'ENABLE', @warehouse_3_id, NOW(), NOW()),
    ('EMP006', '周婷', 'DISABLE', @warehouse_3_id, NOW(), NOW());

-- 5. 供应商测试数据
INSERT IGNORE INTO t_supplier (
    supplier_code,
    supplier_name,
    contact_phone,
    remark,
    status,
    admin_id,
    create_time,
    update_time
)
VALUES
    ('SUP001', '广州优衣服饰有限公司', '13800001001', '主要供应春夏季服装', 'ENABLE', @admin_id, NOW(), NOW()),
    ('SUP002', '杭州新潮服装有限公司', '13800001002', '主要供应秋冬季服装', 'ENABLE', @admin_id, NOW(), NOW()),
    ('SUP003', '深圳童梦服饰有限公司', '13800001003', '主要供应儿童服装', 'ENABLE', @admin_id, NOW(), NOW()),
    ('SUP004', '上海经典制衣有限公司', '13800001004', '长期合作供应商', 'ENABLE', @admin_id, NOW(), NOW()),
    ('SUP005', '北京华美服饰有限公司', '13800001005', '当前暂停合作', 'DISABLE', @admin_id, NOW(), NOW());

-- 6. 商品选项值测试数据
-- option_type 使用 Java 枚举名称，而不是枚举中的小写 code。
INSERT IGNORE INTO t_product_option_value (
    option_type,
    option_value,
    create_time,
    update_time
)
VALUES
    ('COLOR', '黑色', NOW(), NOW()),
    ('COLOR', '白色', NOW(), NOW()),
    ('COLOR', '红色', NOW(), NOW()),
    ('COLOR', '蓝色', NOW(), NOW()),
    ('COLOR', '灰色', NOW(), NOW()),
    ('COLOR', '卡其色', NOW(), NOW()),
    ('SIZE', '110', NOW(), NOW()),
    ('SIZE', '120', NOW(), NOW()),
    ('SIZE', '130', NOW(), NOW()),
    ('SIZE', '140', NOW(), NOW()),
    ('SIZE', '150', NOW(), NOW()),
    ('SIZE', '160', NOW(), NOW()),
    ('SIZE', 'S', NOW(), NOW()),
    ('SIZE', 'M', NOW(), NOW()),
    ('SIZE', 'L', NOW(), NOW()),
    ('SIZE', 'XL', NOW(), NOW()),
    ('TYPE', '男童', NOW(), NOW()),
    ('TYPE', '女童', NOW(), NOW()),
    ('TYPE', '男大童', NOW(), NOW()),
    ('TYPE', '女大童', NOW(), NOW()),
    ('TYPE', '男装', NOW(), NOW()),
    ('TYPE', '女装', NOW(), NOW()),
    ('CATEGORY', 'T恤', NOW(), NOW()),
    ('CATEGORY', '衬衫', NOW(), NOW()),
    ('CATEGORY', '卫衣', NOW(), NOW()),
    ('CATEGORY', '外套', NOW(), NOW()),
    ('CATEGORY', '长裤', NOW(), NOW()),
    ('CATEGORY', '短裤', NOW(), NOW()),
    ('CATEGORY', '连衣裙', NOW(), NOW()),
    ('UNIT', '件', NOW(), NOW()),
    ('UNIT', '条', NOW(), NOW()),
    ('UNIT', '套', NOW(), NOW()),
    ('COMPOSITION', '纯棉', NOW(), NOW()),
    ('COMPOSITION', '聚酯纤维', NOW(), NOW()),
    ('COMPOSITION', '棉麻', NOW(), NOW()),
    ('COMPOSITION', '羊毛', NOW(), NOW()),
    ('COMPOSITION', '锦纶', NOW(), NOW()),
    ('YEAR', '2024', NOW(), NOW()),
    ('YEAR', '2025', NOW(), NOW()),
    ('YEAR', '2026', NOW(), NOW());

COMMIT;

-- 执行后输出基础测试数据数量，便于检查。
SELECT
    (SELECT COUNT(*) FROM t_warehouse WHERE ware_house_code IN ('WH001', 'WH002', 'WH003')) AS warehouses,
    (SELECT COUNT(*) FROM t_employee WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003', 'EMP004', 'EMP005', 'EMP006')) AS employees,
    (SELECT COUNT(*) FROM t_supplier WHERE supplier_code IN ('SUP001', 'SUP002', 'SUP003', 'SUP004', 'SUP005')) AS suppliers,
    (SELECT COUNT(*) FROM t_product_option_value) AS option_values;
