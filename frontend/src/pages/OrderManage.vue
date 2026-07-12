<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">销售</div>
        <h1 class="page-title-modern">订单管理</h1>
        <p class="page-subtitle-modern">按日期和仓库查看销售订单，支持取消订单与整单退货并自动恢复库存。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern" @click="loadOrders">查询订单</button>
        <button class="btn-modern-danger" @click="cancelSelected">取消选中</button>
        <button class="btn-modern-soft" @click="returnSelected">整单退货</button>
      </div>
    </div>

    <section class="metric-grid">
      <div class="metric-card">
        <div class="metric-label">订单数量</div>
        <div class="metric-value">{{ orderRows.length }}</div>
        <p class="metric-hint">当前筛选日期订单</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">已完成</div>
        <div class="metric-value">{{ completedCount }}</div>
        <p class="metric-hint">可取消或退货</p>
      </div>
      <div class="metric-card">
        <div class="metric-label">商品数量</div>
        <div class="metric-value">{{ totalQuantity }}</div>
        <p class="metric-hint">订单明细合计</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">应收金额</div>
        <div class="metric-value">{{ payableText }}</div>
        <p class="metric-hint">当前列表合计</p>
      </div>
    </section>

    <section class="business-panel">
      <div class="filter-bar">
        <div class="filter-left">
          <input v-model="filterDate" class="filter-input" type="date" />
          <select v-model="selectedWareHouseId" class="filter-select">
            <option value="ALL">全部仓库</option>
            <option v-for="warehouse in warehouses" :key="warehouse.wareHouseId" :value="warehouse.wareHouseId">
              {{ warehouse.wareHouseName }}
            </option>
          </select>
          <input v-model.trim="keyword" class="search-input" placeholder="搜索订单号、仓库、员工" />
        </div>
        <div class="filter-right">当前显示 {{ filteredRows.length }} 条</div>
      </div>

      <CommonTable :table-data="filteredRows" :columns="columns" primaryKey="orderId" displayField="orderNo" @rowClick="setSelectedRow" />
    </section>

    <section v-if="selectedRow" class="detail-panel">
      <div class="card-title-row">
        <h2 class="card-title-text">订单明细：{{ selectedRow.orderNo }}</h2>
        <span class="card-note-text">{{ selectedRow.items?.length || 0 }} 行商品</span>
      </div>
      <div class="detail-list">
        <article v-for="item in selectedRow.items || []" :key="item.orderItemId" class="detail-item">
          <div class="detail-copy">
            <strong>{{ item.productName }}</strong>
            <span>{{ item.color }} / {{ item.size }} / {{ item.barcode }}</span>
          </div>
          <div class="detail-values">
            <span>数量 {{ item.quantity }}</span>
            <span>单价 ¥{{ formatMoney(item.unitPrice) }}</span>
            <strong>小计 ¥{{ formatMoney(item.lineAmount) }}</strong>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useOrderapi } from '../utils/axios/apiComposables/useOrderapi'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const orderApi = useOrderapi()
const warehouseApi = useWareHouseapi()
const today = new Date().toISOString().slice(0, 10)
const warehouses = ref([])
const orderRows = ref([])
const selectedRow = ref(null)
const filterDate = ref(today)
const selectedWareHouseId = ref('ALL')
const keyword = ref('')

const columns = ref([
  { Id: 'orderNo', Label: '订单号' },
  { Id: 'orderDate', Label: '日期' },
  { Id: 'wareHouseName', Label: '仓库' },
  { Id: 'employeeName', Label: '员工' },
  { Id: 'stateText', Label: '状态' },
  { Id: 'quantityText', Label: '数量' },
  { Id: 'payableText', Label: '应收' },
  { Id: 'paymentMethodText', Label: '支付方式' }
])

const paymentLabels = {
  CASH: '现金',
  WECHAT: '微信',
  ALIPAY: '支付宝',
  CARD: '银行卡'
}

const statusLabels = {
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  RETURNED: '已退货'
}

const formatMoney = (value) => Number(value || 0).toFixed(2)
const getOrderQuantity = (order) => (order.items || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0)

const tableRows = computed(() => orderRows.value.map(item => ({
  ...item,
  stateText: statusLabels[item.orderStatus] || item.orderStatus || '-',
  quantityText: getOrderQuantity(item),
  payableText: `¥${formatMoney(item.payableAmount)}`,
  paymentMethodText: paymentLabels[item.paymentMethod] || item.paymentMethod || '-'
})))

const filteredRows = computed(() => tableRows.value.filter(item => {
  const text = `${item.orderNo} ${item.wareHouseName} ${item.employeeName} ${item.stateText}`.toLowerCase()
  return !keyword.value || text.includes(keyword.value.toLowerCase())
}))

const completedCount = computed(() => orderRows.value.filter(item => item.orderStatus === 'COMPLETED').length)
const totalQuantity = computed(() => orderRows.value.reduce((sum, order) => sum + getOrderQuantity(order), 0))
const payableText = computed(() => `¥${formatMoney(orderRows.value.reduce((sum, order) => sum + Number(order.payableAmount || 0), 0))}`)

const loadWarehouses = async () => {
  const response = await warehouseApi.getall()
  warehouses.value = Array.isArray(response) ? response : []
}

const loadOrders = async () => {
  if (!filterDate.value) {
    warningMessage('请选择日期', '订单查询需要指定日期')
    return
  }
  try {
    const response = selectedWareHouseId.value === 'ALL'
      ? await orderApi.findByDate(filterDate.value)
      : await orderApi.findByDateAndWarehouse(filterDate.value, selectedWareHouseId.value)
    orderRows.value = Array.isArray(response) ? response : []
    selectedRow.value = null
  } catch (error) {
    errorMessage('订单加载失败', '请检查订单接口是否可用')
  }
}

const setSelectedRow = (row) => {
  selectedRow.value = row
}

const ensureCompletedOrderSelected = () => {
  if (!selectedRow.value) {
    warningMessage('请选择订单', '请先在表格中选择订单')
    return false
  }
  if (selectedRow.value.orderStatus !== 'COMPLETED') {
    warningMessage('状态不允许', '只有已完成订单才能取消或退货')
    return false
  }
  return true
}

const cancelSelected = async () => {
  if (!ensureCompletedOrderSelected()) return
  try {
    await orderApi.cancel(selectedRow.value.orderId)
    await loadOrders()
  } catch (error) {
    errorMessage('取消失败', '订单取消失败')
  }
}

const returnSelected = async () => {
  if (!ensureCompletedOrderSelected()) return
  try {
    await orderApi.returnOrder(selectedRow.value.orderId)
    await loadOrders()
  } catch (error) {
    errorMessage('退货失败', '订单退货失败')
  }
}

onMounted(async () => {
  try {
    await loadWarehouses()
    await loadOrders()
  } catch (error) {
    errorMessage('初始化失败', '订单管理初始化数据加载失败')
  }
})
</script>

<style scoped>
.business-panel,
.detail-panel {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.business-panel {
  margin-top: 16px;
}

.detail-panel {
  margin-top: 16px;
}

.filter-bar,
.filter-left,
.filter-right,
.detail-item,
.detail-values {
  display: flex;
}

.filter-bar,
.detail-item {
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.filter-left,
.detail-values {
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-right {
  color: var(--text-secondary);
  font-size: 13px;
}

.filter-bar {
  margin-bottom: 14px;
}

.filter-input,
.filter-select,
.search-input {
  height: 38px;
  padding: 0 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--text);
  outline: none;
}

.search-input {
  width: 240px;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.detail-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-copy strong {
  color: var(--text);
  font-size: 14px;
}

.detail-copy span,
.detail-values span {
  color: var(--text-secondary);
  font-size: 12px;
}

.detail-values strong {
  color: var(--primary);
  font-size: 13px;
}

@media (max-width: 720px) {
  .filter-bar,
  .detail-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-input,
  .filter-select,
  .search-input {
    width: 100%;
  }
}
</style>
