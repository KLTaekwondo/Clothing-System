# 后端清洗待办

## 供应商模块

- [x] 调整供应商创建流程，不再信任前端传入的 `adminId`。
- [x] 从 `SupplierCreateDto` 删除 `adminId` 及其校验注解。
- [x] 在 `SupplierService.create()` 中通过 `RequestUser.notNull().getRequestId()` 获取当前登录管理员 ID。
- [x] 根据当前管理员 ID 查询 `Admin` 并绑定到新供应商。

## 销售数据聚合模块

- [X] 编写今日销售、成本、毛利率。
- [X] 编写每月的销售额、成本、毛利率。
- [X] 编写允许用户选择时间计算的销售额、成本、毛利率的接口
- [ ] 编写统计挂单，完成订单的接口，并统计出他们的总价格、成本
- [ ] 编写统计进货各个状态的接口，并统计出他们的总成本

## 权限模块

- [X] 统计出权限模块的权限分布
- [X] 翻新权限模块的接口

---

# 优化型功能待办（全项目扫描整理）

> 按优先级分四档：🔴 数据安全（建议先做）→ 🟡 性能 → 🟢 功能完善 → 🔵 工程质量

## 🔴 数据安全类（容易出数据事故，优先做）

- [X] **库存并发防超卖**：`WareHouseStockService.decreaseStock()` 当前"查库存→判断→扣减"无锁，高并发下可超卖。需加 `SELECT ... FOR UPDATE`（悲观锁）或乐观锁（版本号字段），面试高频点。
- [ ] **`low`同商品重复规格兜底**：`ProductService.create()` 里 `SkuUtil.generateSkuList()` 生成组合后，未检查同一商品下是否已有相同规格的 SKU。
- [ ] **`low`盘点单重复 skuCode 兜底**（见上方盘点模块）。

## 🟡 性能与并发类

- [ ] **`medium`分页排序/条件搜索**：`ProductRepository.findPage`、`OrderRepository.findPage` 等分页查询无条件无排序，无法按名称/状态/时间过滤。可加条件参数（名称模糊、状态、时间区间）+ 排序。
- [ ] **`medium`数据库索引检查**：高频查询字段确认有索引——`t_order_item(sku_id)`、`t_order_item(create_time)`、`t_ware_house_stock(sku_id, ware_house_id)`（已有唯一约束）、`t_stock_record(create_time)` 等。
- [ ] **`medium`订单号生成优化**：`OrderConverter.generateOrderNo()` 用 UUID 前 8 位，量大时存在碰撞可能。可改为"日期+序列号"（如 Redis INCR / 表序列）更稳。

## 🟢 功能完善类

- [X] **前端 Dashboard 接真数据**：`Dashboard.vue` 的财务卡（今日销售额/毛利/客单价）和图表数据仍是写死/未接接口，接上 `/api/dash/*`。
- [ ] **`high`数据导出**：订单流水/库存/报表导出 Excel 或 CSV（EasyExcel 或 CSV+BOM），复用现有查询逻辑。
- [ ] **`low`订单状态机扩展**：`OrderStatusEnum` 仅 DRAFT/COMPLETED，商业系统通常含待付款/已退款/已取消等中间态，按需扩展（考虑到以后使用小程序拓展的情况下）。
- [ ] **`medium`挂单清理**：长期未完成的挂单（DRAFT）可加"超时自动清理/提醒"。

## 🔵 工程质量类

- [ ] **单元测试补齐**：目前 `src/test` 基本为空。至少补：库存扣减（含防超卖）、金额核验、报表聚合、权限归属的核心用例。
- [ ] **配置文件环境隔离**：确认 `application.yml` 区分 dev/prod（数据库、日志级别、CORS 域名、cookie secure 开关）。
- [ ] **日志链路**：业务日志目前散落，可加 traceId/统一日志格式，便于排障。
- [ ] **README/接口文档**：整理一份项目 README（模块结构、启动方式、接口清单），配合 springdoc 已有能力。


## 🟢 急需开发（优先度按照排序顺序进行，越靠前越紧急）

- [X] **小票和条码打印模块**:打印消费小票和商品条码
- [X] **CSV导出模块**:导出订单流水、库存、报表等数据
- [ ] **Redis 黑白名单模块**:用于存储和管理用户黑名单/白名单，用于踢人控制
- [ ] **Spring AI 模块尝试**:用于处理用户的问题，如订单查询、库存查询等
- [ ] **一键导入商品，并生成sku和库存**: 从Excel文件导入商品，自动生成SKU和库存，减少工作量的关键
- [ ] **一键导入进货商品，减少重复性工作**: 从Excel文件导入进货商品，减少重复性工作，提高效率

---

# 数据流转缺失分析（全模块扫查）

> 分析范围：16 Controller / 20 Service / 18 Repository。标记每个模块"数据流断点"—数据存在但无接口输出，或查询链路不完整。
> 不涉及现有 CRUD 本身的好坏，只关注"数据是否流通到前端可看可用"。

## 🔴 最优先（数据存在但完全无入口，每天经营都要看）

### 1️⃣ 分页库存列表

**现状**：`GET /api/stock/search/{warehouseId}/{productId}` 只能查**单个商品**某个仓库的库存，没法一次看"整个仓库所有SKU的库存"。

**需要**：`GET /api/stock/page?warehouseId=&page=&size=`
- WareHouseStockRepository: `findByWarehouseId(warehouseId, pageable)` 分页查某仓库所有库存
- WareHouseStockService: `findPageByWarehouseId(warehouseId, pageable)`
- WareHouseStockController: `GET /api/stock/page`
- 前端即可展示"库存清单"页面，也可复用为导出库存CSV

### 2️⃣ 会员消费记录

**现状**：`GET /api/member/search/{phone}` 只能查会员基本信息（手机号、积分），**看不到这个会员买过什么、买了多少、花了多少钱**。服装店熟客来了问"上次那条裤子还有吗"，查不到。

**需要**：`GET /api/member/{memberPhone}/orders?page=&size=`
- OrderRepository: 按手机号查已完成的订单列表
- MemberService: `searchOrders(memberPhone, pageable)` 或 MemberController 新增
- 返回订单号、时间、商品列表、金额。现有数据完整，只需一个查询

## 🟡 第二优先（数据存在但缺少聚合缺口，管理决策用）

### 3️⃣ 进货统计（按状态汇总成本）

**现状**：进货单 CRUD 全了，但无法一眼看到"本月草稿/待审核/通过/拒绝的各有多少单，总成本是多少"。

**需要**：`GET /api/import-order/summary?start=&end=`
- ImportOrderRepository: `GROUP BY status, SUM(totalPrice)` 按状态聚合
- ImportOrderService: 新增统计方法
- ImportOrderController: 新增端点
- 已有 TODO (L16)，但优先度应高于"自定义"类报表

### 4️⃣ 销售统计（按状态汇总金额）

**现状**：订单有 DRAFT 和 COMPLETED 两种状态，目前只做了已完成订单的报表（销售额/成本/毛利），**没有区分挂单 vs 完成订单各自的合计金额和成本**。

**需要**：`GET /api/order/summary?start=&end=`
- OrderRepository: `GROUP BY status, SUM(actualPrice/totalPrice), COUNT(*)`
- OrderService: 新增统计方法
- OrderController: 新增端点
- 已有 TODO (L15)，补充明确

### 5️⃣ 调拨统计

**现状**：调拨单走完审核流程就结束了，没有调拨量/频率的统计。

**需要**：`GET /api/transfer-order/summary?start=&end=`
- 按状态汇总调拨单数、总调拨商品数、总金额
- 如果商品数在 TransferOrderItem 里，要 SUM(quantity)

### 6️⃣ 盘点统计

**现状**：盘点后差异情况（盘盈/盘亏）仅记录在每行明细中，没有汇总口径。

**需要**：`GET /api/stockCheck/summary?start=&end=`
- 按状态汇总盘点单数、总盘盈量、总盘亏量、已处理/未处理

## 🟢 第三优先（基础数据没有详情查看链）

### 7️⃣ 库存流水按来源过滤

**现状**：`GET /api/stock-record/page` 不分来源全部列出，无法筛选只看"销售扣减"或"进货入库"。

**需要**：`GET /api/stock-record/page?sourceType=&start=&end=`
- StockRecordRepository: 加条件查询（sourceType, changeType, 时间范围）
- 改动小，但查看流水时高效得多

### 8️⃣ 供应商进货历史

**现状**：`GET /api/supplier/search/{code}` 只看供应商信息，没法看到"这个供应商供过哪些货、多少钱"。

**需要**：`GET /api/supplier/{id}/import-orders?page=&size=`
- ImportOrderRepository: 按供应商ID查进货单
- 复用已有 ImportOrderInfo，只加一个查询

### 9️⃣ 员工销售业绩

**现状**：员工 CRUD + 核验都有，但不能看"这个员工做了多少单、卖了多少钱"。

**需要**：`GET /api/employee/{id}/sales-summary?start=&end=`
- OrderRepository: `COUNT(*) + SUM(actualPrice) WHERE employeeId`
- 统计某员工在时间范围内的完成订单数和总销售额

## 缺失汇总表

| 模块 | 缺失接口 | 数据来源 | TODO已有 |
|------|---------|---------|---------|
| **库存** | 分页库存列表 | WareHouseStock | ❌ 未列入 |
| **会员** | 会员购买记录 | Order | ❌ 未列入 |
| **进货** | 按状态汇总成本 | ImportOrder | ✅ L16 |
| **订单** | 按状态汇总金额 | Order | ✅ L15 |
| **调拨** | 调拨统计 | TransferOrder | ❌ 未列入 |
| **盘点** | 盘点差异统计 | StockCheck | ❌ 未列入 |
| **库存流水** | 按来源筛选 | StockRecord | ❌ 未列入 |
| **供应商** | 供应商进货历史 | ImportOrder | ❌ 未列入 |
| **员工** | 员工销售业绩 | Order | ❌ 未列入 |
