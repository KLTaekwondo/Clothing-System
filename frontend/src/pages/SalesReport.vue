<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">报表</div>
        <h1 class="page-title-modern">销售报表</h1>
        <p class="page-subtitle-modern">按日期和仓库查看销售汇总，快速掌握订单量、销售件数和应收金额。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern-primary" @click="loadReport">查询报表</button>
      </div>
    </div>

    <section class="report-filter">
      <div class="filter-left">
        <label class="filter-group">
          <span class="field-label">日期</span>
          <input v-model="filterDate" class="field-input" type="date" />
        </label>
        <label class="filter-group">
          <span class="field-label">仓库</span>
          <select v-model="selectedWareHouseId" class="field-input">
            <option value="ALL">全部仓库</option>
            <option v-for="warehouse in warehouses" :key="warehouse.wareHouseId" :value="warehouse.wareHouseId">
              {{ warehouse.wareHouseName }}
            </option>
          </select>
        </label>
      </div>
      <div class="report-note">日期格式使用 yyyy-MM-dd，仓库可选全部或单仓库。</div>
    </section>

    <section class="metric-grid">
      <div class="metric-card">
        <div class="metric-label">订单数</div>
        <div class="metric-value">{{ reportData.orderCount ?? 0 }}</div>
        <p class="metric-hint">已完成订单数量</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">销售件数</div>
        <div class="metric-value">{{ reportData.salesQuantity ?? 0 }}</div>
        <p class="metric-hint">订单明细数量合计</p>
      </div>
      <div class="metric-card">
        <div class="metric-label">销售总额</div>
        <div class="metric-value">¥{{ formatMoney(reportData.totalAmount) }}</div>
        <p class="metric-hint">折扣前金额</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">应收金额</div>
        <div class="metric-value">¥{{ formatMoney(reportData.payableAmount) }}</div>
        <p class="metric-hint">折扣后金额</p>
      </div>
    </section>

    <section class="report-panel">
      <div class="card-title-row">
        <h2 class="card-title-text">销售汇总</h2>
        <span class="card-note-text">{{ reportData.date || filterDate }}</span>
      </div>
      <div class="report-lines">
        <div class="report-line">
          <span>统计日期</span>
          <strong>{{ reportData.date || filterDate }}</strong>
        </div>
        <div class="report-line">
          <span>仓库</span>
          <strong>{{ selectedWarehouseName }}</strong>
        </div>
        <div class="report-line">
          <span>折扣金额</span>
          <strong>¥{{ formatMoney(reportData.discountAmount) }}</strong>
        </div>
        <div class="report-line">
          <span>客单价估算</span>
          <strong>¥{{ averageOrderAmount }}</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { warningMessage } from '../utils/toastCenter'

const warehouseApi = useWareHouseapi()
const today = new Date().toISOString().slice(0, 10)
const warehouses = ref([])
const filterDate = ref(today)
const selectedWareHouseId = ref('ALL')
const reportData = reactive({
  date: today,
  wareHouseId: '',
  orderCount: 0,
  salesQuantity: 0,
  totalAmount: 0,
  discountAmount: 0,
  payableAmount: 0
})

const formatMoney = (value) => Number(value || 0).toFixed(2)

const selectedWarehouseName = computed(() => {
  if (selectedWareHouseId.value === 'ALL') return '全部仓库'
  const warehouse = warehouses.value.find(item => item.wareHouseId === selectedWareHouseId.value)
  return warehouse?.wareHouseName || selectedWareHouseId.value
})

const averageOrderAmount = computed(() => {
  const count = Number(reportData.orderCount || 0)
  if (count <= 0) return '0.00'
  return formatMoney(Number(reportData.payableAmount || 0) / count)
})

const loadWarehouses = async () => {
  const response = await warehouseApi.getall()
  warehouses.value = Array.isArray(response) ? response : []
}

const loadReport = async () => {
  warningMessage('报表功能暂不可用', '销售报表接口尚未接入后端')
}

onMounted(async () => {
  try {
    await loadWarehouses()
  } catch (error) {
    warningMessage('初始化失败', '销售报表初始化数据加载失败')
  }
})
</script>

<style scoped>
.report-filter,
.report-panel {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.report-filter {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}

.filter-left,
.report-lines {
  display: flex;
}

.filter-left {
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.field-label {
  color: var(--text);
  font-size: 13px;
  font-weight: 760;
}

.field-input {
  height: 38px;
  min-width: 180px;
  padding: 0 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--text);
  outline: none;
}

.report-note {
  color: var(--text-secondary);
  font-size: 12px;
}

.report-panel {
  margin-top: 16px;
}

.report-lines {
  flex-wrap: wrap;
  gap: 12px;
}

.report-line {
  width: calc(25% - 9px);
  min-width: 180px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

.report-line span,
.report-line strong {
  display: block;
}

.report-line span {
  color: var(--text-secondary);
  font-size: 12px;
  margin-bottom: 8px;
}

.report-line strong {
  color: var(--text);
  font-size: 18px;
}

@media (max-width: 720px) {
  .report-filter {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-group,
  .field-input,
  .report-line {
    width: 100%;
  }
}
</style>
