-- ClothingSystem 演示数据
-- 用途：为本地开发环境补齐一套完整的管理员、仓库、员工、商品、SKU、库存和订单数据。
-- 说明：脚本不会清空现有数据；重复执行时，具有唯一编码的数据会被忽略，库存和订单项不会重复插入。
-- 登录账号：admin001 / admin，仓库账号：WH001 / warehouse、WH002 / warehouse。

SET NAMES utf8mb4;
START TRANSACTION;

-- 1. 管理员。BCrypt 密文对应明文密码：password
INSERT IGNORE INTO t_admin (admin_code, username, password, status)
VALUES ('admin001', '系统管理员', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ENABLE');

SELECT admin_id INTO @demo_admin_id
FROM t_admin
WHERE admin_code = 'admin001'
LIMIT 1;

-- 2. 仓库。仓库密码密文对应明文密码：warehouse
INSERT IGNORE INTO t_warehouse (ware_house_code, ware_house_name, status, ware_house_password, admin_id)
VALUES
    ('WH001', '上海总仓', 'ENABLE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', @demo_admin_id),
    ('WH002', '杭州分仓', 'ENABLE', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', @demo_admin_id);

SELECT ware_house_id INTO @demo_wh1_id
FROM t_warehouse
WHERE ware_house_code = 'WH001'
LIMIT 1;

SELECT ware_house_id INTO @demo_wh2_id
FROM t_warehouse
WHERE ware_house_code = 'WH002'
LIMIT 1;

-- 3. 员工
INSERT IGNORE INTO t_employee (employee_code, employee_name, status, ware_house_id)
VALUES
    ('EMP001', '张晓琳', 'ENABLE', @demo_wh1_id),
    ('EMP002', '李文博', 'ENABLE', @demo_wh1_id),
    ('EMP003', '王雅静', 'ENABLE', @demo_wh2_id);

SELECT employee_id INTO @demo_emp1_id
FROM t_employee
WHERE employee_code = 'EMP001'
LIMIT 1;

SELECT employee_id INTO @demo_emp2_id
FROM t_employee
WHERE employee_code = 'EMP002'
LIMIT 1;

SELECT employee_id INTO @demo_emp3_id
FROM t_employee
WHERE employee_code = 'EMP003'
LIMIT 1;

-- 4. 商品选项
INSERT IGNORE INTO t_product_option_value (option_type, option_value)
VALUES
    ('COLOR', '黑色'),
    ('COLOR', '白色'),
    ('COLOR', '蓝色'),
    ('COLOR', '卡其色'),
    ('COLOR', '红色'),
    ('SIZE', 'M'),
    ('SIZE', 'L'),
    ('SIZE', 'XL'),
    ('TYPE', '男装'),
    ('TYPE', '女装'),
    ('CATEGORY', 'T恤'),
    ('CATEGORY', '休闲裤'),
    ('CATEGORY', '夹克'),
    ('UNIT', '件'),
    ('COMPOSITION', '棉');

-- 5. 商品
INSERT IGNORE INTO t_product
    (product_code, product_name, season, status, import_price, sale_price, special)
VALUES
    ('P001', '基础圆领T恤', 'SUMMER', 'ENABLE', 65.00, 129.00, 0),
    ('P002', '舒适休闲裤', 'AUTUMN', 'ENABLE', 110.00, 199.00, 0),
    ('P003', '轻薄连帽夹克', 'SPRING', 'ENABLE', 168.00, 299.00, 1);

SELECT product_id INTO @demo_product1_id
FROM t_product
WHERE product_code = 'P001'
LIMIT 1;

SELECT product_id INTO @demo_product2_id
FROM t_product
WHERE product_code = 'P002'
LIMIT 1;

SELECT product_id INTO @demo_product3_id
FROM t_product
WHERE product_code = 'P003'
LIMIT 1;

-- 6. SKU。sku_code 是系统扫码和订单接口使用的唯一编码。
INSERT IGNORE INTO t_product_sku
    (sku_name, sku_code, spec_attributes, status, product_id)
VALUES
    ('基础圆领T恤-红色-M', '6900000000011', '{"color":"红色","size":"M"}', 'ENABLE', @demo_product1_id),
    ('基础圆领T恤-红色-L', '6900000000028', '{"color":"红色","size":"L"}', 'ENABLE', @demo_product1_id),
    ('基础圆领T恤-蓝色-M', '6900000000035', '{"color":"蓝色","size":"M"}', 'ENABLE', @demo_product1_id),
    ('舒适休闲裤-黑色-M', '6900000000042', '{"color":"黑色","size":"M"}', 'ENABLE', @demo_product2_id),
    ('舒适休闲裤-黑色-L', '6900000000059', '{"color":"黑色","size":"L"}', 'ENABLE', @demo_product2_id),
    ('轻薄连帽夹克-卡其色-M', '6900000000066', '{"color":"卡其色","size":"M"}', 'ENABLE', @demo_product3_id),
    ('轻薄连帽夹克-卡其色-L', '6900000000073', '{"color":"卡其色","size":"L"}', 'ENABLE', @demo_product3_id);

SELECT sku_id INTO @demo_sku1_id FROM t_product_sku WHERE sku_code = '6900000000011' LIMIT 1;
SELECT sku_id INTO @demo_sku2_id FROM t_product_sku WHERE sku_code = '6900000000028' LIMIT 1;
SELECT sku_id INTO @demo_sku3_id FROM t_product_sku WHERE sku_code = '6900000000035' LIMIT 1;
SELECT sku_id INTO @demo_sku4_id FROM t_product_sku WHERE sku_code = '6900000000042' LIMIT 1;
SELECT sku_id INTO @demo_sku5_id FROM t_product_sku WHERE sku_code = '6900000000059' LIMIT 1;
SELECT sku_id INTO @demo_sku6_id FROM t_product_sku WHERE sku_code = '6900000000066' LIMIT 1;
SELECT sku_id INTO @demo_sku7_id FROM t_product_sku WHERE sku_code = '6900000000073' LIMIT 1;

-- 7. 仓库库存。使用 INSERT IGNORE，重复执行不会覆盖已有的实际库存。
INSERT IGNORE INTO t_ware_house_stock (sku_id, ware_house_id, stock)
VALUES
    (@demo_sku1_id, @demo_wh1_id, 24),
    (@demo_sku2_id, @demo_wh1_id, 20),
    (@demo_sku3_id, @demo_wh1_id, 18),
    (@demo_sku4_id, @demo_wh1_id, 15),
    (@demo_sku5_id, @demo_wh1_id, 12),
    (@demo_sku6_id, @demo_wh1_id, 10),
    (@demo_sku7_id, @demo_wh1_id, 8),
    (@demo_sku1_id, @demo_wh2_id, 16),
    (@demo_sku2_id, @demo_wh2_id, 14),
    (@demo_sku3_id, @demo_wh2_id, 12),
    (@demo_sku4_id, @demo_wh2_id, 10),
    (@demo_sku5_id, @demo_wh2_id, 9),
    (@demo_sku6_id, @demo_wh2_id, 7),
    (@demo_sku7_id, @demo_wh2_id, 6);

-- 8. 示例订单
INSERT IGNORE INTO t_order
    (order_no, pay_method, status, total_price, actual_price, remark, employee_id, warehouse_id)
VALUES
    ('202607150001', 'ALIPAY', 'COMPLETED', 328.00, 328.00, '门店日常销售', @demo_emp1_id, @demo_wh1_id),
    ('202607150002', 'WECHAT', 'DRAFT', 299.00, 269.10, '顾客暂存订单', @demo_emp2_id, @demo_wh1_id),
    ('202607150003', 'CARD', 'REFUND', 129.00, 129.00, '顾客退货', @demo_emp3_id, @demo_wh2_id);

SELECT order_id INTO @demo_order1_id
FROM t_order
WHERE order_no = '202607150001'
LIMIT 1;

SELECT order_id INTO @demo_order2_id
FROM t_order
WHERE order_no = '202607150002'
LIMIT 1;

SELECT order_id INTO @demo_order3_id
FROM t_order
WHERE order_no = '202607150003'
LIMIT 1;

-- 9. 订单明细。订单表没有明细唯一约束，因此通过订单号、SKU 和商品名称避免重复插入。
INSERT INTO t_order_item
    (sku_id, product_name, sku_name, unit_price, quantity, discount, total_price, actual_price, order_id)
SELECT @demo_sku1_id, '基础圆领T恤', '基础圆领T恤-红色-M', 129.00, 1, 1.00, 129.00, 129.00, @demo_order1_id
WHERE NOT EXISTS (
    SELECT 1 FROM t_order_item
    WHERE order_id = @demo_order1_id AND sku_id = @demo_sku1_id
);

INSERT INTO t_order_item
    (sku_id, product_name, sku_name, unit_price, quantity, discount, total_price, actual_price, order_id)
SELECT @demo_sku4_id, '舒适休闲裤', '舒适休闲裤-黑色-M', 199.00, 1, 1.00, 199.00, 199.00, @demo_order1_id
WHERE NOT EXISTS (
    SELECT 1 FROM t_order_item
    WHERE order_id = @demo_order1_id AND sku_id = @demo_sku4_id
);

INSERT INTO t_order_item
    (sku_id, product_name, sku_name, unit_price, quantity, discount, total_price, actual_price, order_id)
SELECT @demo_sku7_id, '轻薄连帽夹克', '轻薄连帽夹克-卡其色-L', 299.00, 1, 0.90, 299.00, 269.10, @demo_order2_id
WHERE NOT EXISTS (
    SELECT 1 FROM t_order_item
    WHERE order_id = @demo_order2_id AND sku_id = @demo_sku7_id
);

INSERT INTO t_order_item
    (sku_id, product_name, sku_name, unit_price, quantity, discount, total_price, actual_price, order_id)
SELECT @demo_sku2_id, '基础圆领T恤', '基础圆领T恤-红色-L', 129.00, 1, 1.00, 129.00, 129.00, @demo_order3_id
WHERE NOT EXISTS (
    SELECT 1 FROM t_order_item
    WHERE order_id = @demo_order3_id AND sku_id = @demo_sku2_id
);

COMMIT;

-- 执行后输出本次演示数据的关键数量，便于检查。
SELECT
    (SELECT COUNT(*) FROM t_admin WHERE admin_code = 'admin001') AS admins,
    (SELECT COUNT(*) FROM t_warehouse WHERE ware_house_code IN ('WH001', 'WH002')) AS warehouses,
    (SELECT COUNT(*) FROM t_employee WHERE employee_code IN ('EMP001', 'EMP002', 'EMP003')) AS employees,
    (SELECT COUNT(*) FROM t_product WHERE product_code IN ('P001', 'P002', 'P003')) AS products,
    (SELECT COUNT(*) FROM t_product_sku WHERE sku_code LIKE '69000000000%') AS skus,
    (SELECT COUNT(*) FROM t_order WHERE order_no LIKE '20260715000%') AS orders;
