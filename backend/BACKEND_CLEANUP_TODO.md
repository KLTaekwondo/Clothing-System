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
