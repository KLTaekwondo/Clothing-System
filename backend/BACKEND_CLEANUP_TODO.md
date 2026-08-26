# 后端清洗待办（收尾 + 周计划）

## ✅ 已完成的里程碑

- [x] 供应商信任边界（`adminId` → `RequestUser` 取当前管理员）
- [x] 销售报表聚合（今日/每月/自定义时间：销售额、成本、毛利率）
- [x] 权限模块（权限分布统计 + 接口翻新）
- [x] 库存并发防超卖（加锁）
- [x] 前端 Dashboard 接真数据
- [x] 小票/条码打印模块、CSV 导出模块
- [x] 选项值删除前引用检查（`existsBySpecExactValue`）

---

## 🔥 两天收尾冲刺（数据安全 + 面试亮点 + 交付物）

### 第 1 天：数据安全兜底

- [x] **同商品重复规格兜底**：`ProductSkuService.createFromProduct()` 保存前按 `productId + skuName` 查重，`ProductSkuRepository` 加 `existsByProductIdAndSkuName`
- [x] **盘点单重复 skuCode 兜底**：`StockCheckItemService.createList()` 用 `Set<String>` 去重
- [x] 核对选项值删除引用检查是否覆盖更新场景（改值后旧快照失联问题）

### 第 2 天上午：统计接口 + 单元测试

- [ ] **进货按状态汇总接口**：`ImportOrderRepository` 加 `GROUP BY status` 聚合（COUNT、SUM totalAmount）+ Service + Controller
  - ~~挂单/完成订单汇总接口~~：已砍——完成订单统计已有 `sumTotalAmountByTime` 覆盖，挂单金额无经营价值（挂单不产生收入、不占库存）
- [ ] **单元测试最小集**：
  - [ ] 库存扣减防超卖：库存 5 → 扣 3 成功 → 再扣 3 抛"库存不足" → 最终库存 2
  - [ ] 订单金额校验：前端传的金额与后端逐项计算不一致 → 拒绝

### 第 2 天下午：交付物

- [ ] **后端 README.md**：技术栈、启动方式、模块结构、演示账号
- [ ] **全量验证**：`mvn test` + `npm run build` 全绿
- [ ] **小步提交**（按模块拆 2-3 个 commit）

### ⚡ 白拿清单（每项 5 分钟）

- [ ] `application.properties` 加 `spring.jpa.open-in-view: false`（消启动 WARN）
- [ ] `application-prod.properties` 关闭 swagger：`springdoc.api-docs.enabled=false`、`springdoc.swagger-ui.enabled=false`
- [ ] 修正本文件历史遗留标注（CSV 导出矛盾标记）

---

## 📅 周计划（收尾后慢慢弄）

### 第 1 周：查询与性能

- [ ] 分页条件搜索：Product / Order / StockRecord 的 `findPage` 加名称/状态/时间过滤 + 排序
- [ ] 订单号生成优化：UUID 前 8 位 → 日期 + 序列号（Redis INCR / 表序列）
- [ ] 数据库索引检查：`t_order_item(sku_id)`、`t_order_item(create_time)`、`t_stock_record(create_time)`

### 第 2 周：工程化

- [ ] 日志链路：traceId + 统一日志格式
- [ ] 单元测试持续补齐（金额核验、报表聚合、权限归属）
- [ ] 配置文件环境隔离复查（dev/prod 已分，检查遗留项）

### 第 3 周：功能模块

- [ ] Redis 黑白名单模块（用户黑/白名单、踢人控制）
- [ ] 订单状态机扩展（待付款/已退款/已取消）——按需
- [ ] 挂单超时清理/提醒——按需
- [ ] 商品图片上传（本地存储：`Product` 加 `imageUrl` 字段 + `FileController` 上传接口 + 前端 `file input`，不引 OSS）

### 第 4 周：智能化

- [ ] Spring AI 模块尝试（订单查询、库存查询等）

### 第 5 周：一键导入

- [ ] 一键导入商品，自动生成 SKU 和库存（Excel）
- [ ] 一键导入进货商品（Excel）

### 长期（随时维护）

- [ ] README 持续维护
- [ ] 新增功能同步更新接口文档（springdoc /v3/api-docs）
- [ ] 代码规范复查（`flex: 1`、多 class 名等历史遗留）