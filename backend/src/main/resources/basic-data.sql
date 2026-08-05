-- ClothingSystem 基础初始化数据
-- 管理员账号：koole，仓库账号：z01，登录密码均为：123456。
-- 不包含产品、SKU、库存及任何订单模块数据。

SET NAMES utf8mb4;
START TRANSACTION;

-- BCrypt 密文对应明文密码：123456
SET @password_hash =
    '$2b$10$Z5eVAh6ANbEH5p2zxf.rLOTLpYqW8MlhH4POuXgFHXchc0.qtegn2';

-- 1. 管理员
INSERT INTO t_admin (
    admin_code,
    username,
    password,
    status,
    create_time,
    update_time
)
VALUES (
    'koole',
    'koole',
    @password_hash,
    'ENABLE',
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    password = VALUES(password),
    status = VALUES(status),
    update_time = NOW();

SET @admin_id = (
    SELECT admin_id
    FROM t_admin
    WHERE admin_code = 'koole'
    LIMIT 1
);

-- 2. 仓库
INSERT INTO t_warehouse (
    ware_house_code,
    ware_house_name,
    status,
    ware_house_password,
    check_status,
    admin_id,
    create_time,
    update_time
)
VALUES (
    'z01',
    '中心仓库',
    'ENABLE',
    @password_hash,
    'NO_CHECK',
    @admin_id,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    ware_house_name = VALUES(ware_house_name),
    status = VALUES(status),
    ware_house_password = VALUES(ware_house_password),
    check_status = VALUES(check_status),
    admin_id = VALUES(admin_id),
    update_time = NOW();

SET @warehouse_id = (
    SELECT ware_house_id
    FROM t_warehouse
    WHERE ware_house_code = 'z01'
    LIMIT 1
);

-- 3. 员工
INSERT INTO t_employee (
    employee_code,
    employee_name,
    status,
    ware_house_id,
    create_time,
    update_time
)
VALUES
    (
        'EMP001',
        '张明',
        'ENABLE',
        @warehouse_id,
        NOW(),
        NOW()
    ),
    (
        'EMP002',
        '李雪',
        'ENABLE',
        @warehouse_id,
        NOW(),
        NOW()
    ),
    (
        'EMP003',
        '王强',
        'ENABLE',
        @warehouse_id,
        NOW(),
        NOW()
    ),
    (
        'EMP004',
        '陈静',
        'ENABLE',
        @warehouse_id,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    employee_name = VALUES(employee_name),
    status = VALUES(status),
    ware_house_id = VALUES(ware_house_id),
    update_time = NOW();

-- 4. 供应商
INSERT INTO t_supplier (
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
    (
        'SUP001',
        '广州优衣服饰有限公司',
        '13800001001',
        '主要供应春夏服装',
        'ENABLE',
        @admin_id,
        NOW(),
        NOW()
    ),
    (
        'SUP002',
        '杭州新潮制衣有限公司',
        '13800001002',
        '主要供应秋冬服装',
        'ENABLE',
        @admin_id,
        NOW(),
        NOW()
    ),
    (
        'SUP003',
        '深圳童梦服饰有限公司',
        '13800001003',
        '主要供应儿童服装',
        'ENABLE',
        @admin_id,
        NOW(),
        NOW()
    ),
    (
        'SUP004',
        '上海经典服饰有限公司',
        '13800001004',
        '长期合作供应商',
        'ENABLE',
        @admin_id,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    supplier_name = VALUES(supplier_name),
    contact_phone = VALUES(contact_phone),
    remark = VALUES(remark),
    status = VALUES(status),
    admin_id = VALUES(admin_id),
    update_time = NOW();

COMMIT;

-- 输出脚本数据数量，便于确认执行结果。
SELECT
    (
        SELECT COUNT(*)
        FROM t_admin
        WHERE admin_code = 'koole'
    ) AS admins,
    (
        SELECT COUNT(*)
        FROM t_warehouse
        WHERE ware_house_code = 'z01'
    ) AS warehouses,
    (
        SELECT COUNT(*)
        FROM t_employee
        WHERE ware_house_id = @warehouse_id
    ) AS employees,
    (
        SELECT COUNT(*)
        FROM t_supplier
        WHERE admin_id = @admin_id
    ) AS suppliers;
