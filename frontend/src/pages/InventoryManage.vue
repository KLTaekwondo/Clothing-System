<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">库存</div>
        <h1 class="page-title-modern">库存管理</h1>
        <p class="page-subtitle-modern">按仓库维护 SKU 库存，支持初始化、入库、出库、调整和流水查看。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern-primary" @click="openOperationPanel">库存操作</button>
        <button class="btn-modern" @click="reloadInventory">刷新库存</button>
      </div>
    </div>

    <section class="metric-grid">
      <div class="metric-card">
        <div class="metric-label">仓库数量</div>
        <div class="metric-value">{{ warehouses.length }}</div>
        <p class="metric-hint">来自仓库管理模块</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">库存 SKU</div>
        <div class="metric-value">{{ inventoryRows.length }}</div>
        <p class="metric-hint">当前仓库库存明细</p>
      </div>
      <div class="metric-card">
        <div class="metric-label">低库存</div>
        <div class="metric-value">{{ warningCount }}</div>
        <p class="metric-hint">库存小于等于预警值</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">库存流水</div>
        <div class="metric-value">{{ movementRows.length }}</div>
        <p class="metric-hint">当前仓库操作记录</p>
      </div>
    </section>

    <section class="business-panel">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="filter-label">选择仓库</span>
          <select v-model="selectedWareHouseId" class="filter-select" @change="reloadInventory">
            <option value="">请选择仓库</option>
            <option v-for="warehouse in warehouses" :key="warehouse.wareHouseId" :value="warehouse.wareHouseId">
              {{ warehouse.wareHouseId }} - {{ warehouse.wareHouseName }}
            </option>
          </select>
          <input v-model.trim="keyword" class="search-input" placeholder="搜索商品、颜色、尺码、条码" />
        </div>
        <div class="filter-right">当前显示 {{ filteredInventoryRows.length }} 条</div>
      </div>

      <div class="content-layout">
        <transition name="slide-left">
          <aside v-if="showOperationPanel" class="operation-panel">
            <div class="panel-header">
              <div class="panel-title-block">
                <span class="panel-eyebrow">Stock</span>
                <h2 class="panel-title">库存操作</h2>
              </div>
              <button class="panel-close" @click="closeOperationPanel">×</button>
            </div>
            <form class="panel-form" @submit.prevent="submitStockOperation">
              <label class="form-group">
                <span class="field-label">操作类型</span>
                <select v-model="operationData.type" class="field-input">
                  <option value="init">初始化库存</option>
                  <option value="in">入库</option>
                  <option value="out">出库</option>
                  <option value="adjust">调整库存</option>
                </select>
              </label>

              <label class="form-group">
                <span class="field-label">商品</span>
                <select v-model="selectedProductId" class="field-input" @change="loadSkusByProduct">
                  <option value="">请选择商品</option>
                  <option v-for="product in products" :key="product.productId" :value="product.productId">
                    {{ product.productCode }} - {{ product.productName }}
                  </option>
                </select>
              </label>

              <label class="form-group">
                <span class="field-label">SKU</span>
                <select v-model="operationData.productSkuId" class="field-input" required>
                  <option value="">请选择 SKU</option>
                  <option v-for="sku in skuOptions" :key="sku.productSkuId" :value="sku.productSkuId">
                    {{ sku.productName }} / {{ sku.color }} / {{ sku.size }} / {{ sku.barcode }}
                  </option>
                </select>
              </label>

              <label class="form-group">
                <span class="field-label">数量</span>
                <input v-model.number="operationData.quantity" class="field-input" type="number" min="0" step="1" required />
              </label>

              <label class="form-group">
                <span class="field-label">预警数量</span>
                <input v-model.number="operationData.warningQuantity" class="field-input" type="number" min="0" step="1" />
              </label>

              <label class="form-group">
                <span class="field-label">备注</span>
                <input v-model.trim="operationData.remark" class="field-input" placeholder="可选" />
              </label>

              <div class="operation-tip">
                <strong>说明</strong>
                <span>初始化仅用于仓库中还不存在的 SKU；入库会自动初始化不存在的库存；出库会校验库存数量。</span>
              </div>

              <div class="form-actions">
                <button type="button" class="btn-modern" @click="closeOperationPanel">取消</button>
                <button type="submit" class="btn-modern-primary" :disabled="loading">{{ loading ? '提交中...' : '提交操作' }}</button>
              </div>
            </form>
          </aside>
        </transition>

        <div :class="tableAreaClass">
          <CommonTable :table-data="filteredInventoryRows" :columns="inventoryColumns" primaryKey="inventoryId" displayField="productName" @rowClick="selectInventoryRow" />
        </div>
      </div>
    </section>

    <section class="movement-panel">
      <div class="card-title-row">
        <h2 class="card-title-text">库存流水</h2>
        <span class="card-note-text">按当前仓库显示最近操作</span>
      </div>
      <CommonTable :table-data="movementRows" :columns="movementColumns" primaryKey="movementId" displayField="movementType" />
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { useProductapi } from '../utils/axios/apiComposables/useProductapi'
import { useProductSkuapi } from '../utils/axios/apiComposables/useProductSkuapi'
import { useInventoryapi } from '../utils/axios/apiComposables/useInventoryapi'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const warehouseApi = useWareHouseapi()
const productApi = useProductapi()
const skuApi = useProductSkuapi()
const inventoryApi = useInventoryapi()

const warehouses = ref([])
const products = ref([])
const skuOptions = ref([])
const inventoryRows = ref([])
const movementRows = ref([])
const selectedWareHouseId = ref('')
const selectedProductId = ref('')
const keyword = ref('')
const showOperationPanel = ref(false)
const loading = ref(false)

const operationData = reactive({
  type: 'in',
  productSkuId: '',
  quantity: 1,
  warningQuantity: 0,
  remark: ''
})

const inventoryColumns = ref([
  { Id: 'inventoryId', Label: '库存ID' },
  { Id: 'productName', Label: '商品' },
  { Id: 'color', Label: '颜色' },
  { Id: 'size', Label: '尺码' },
  { Id: 'barcode', Label: '条码' },
  { Id: 'quantity', Label: '库存' },
  { Id: 'lockedQuantity', Label: '锁定' },
  { Id: 'warningQuantity', Label: '预警' }
])

const movementColumns = ref([
  { Id: 'createTime', Label: '时间' },
  { Id: 'movementType', Label: '类型' },
  { Id: 'productName', Label: '商品' },
  { Id: 'color', Label: '颜色' },
  { Id: 'size', Label: '尺码' },
  { Id: 'quantityChange', Label: '变动' },
  { Id: 'beforeQuantity', Label: '前库存' },
  { Id: 'afterQuantity', Label: '后库存' },
  { Id: 'remark', Label: '备注' }
])

const tableAreaClass = computed(() => showOperationPanel.value ? 'table-area-compressed' : 'table-area')
const warningCount = computed(() => inventoryRows.value.filter(item => Number(item.quantity) <= Number(item.warningQuantity)).length)

const filteredInventoryRows = computed(() => inventoryRows.value.filter(item => {
  const text = `${item.productName} ${item.color} ${item.size} ${item.barcode}`.toLowerCase()
  return !keyword.value || text.includes(keyword.value.toLowerCase())
}))

const loadBaseData = async () => {
  const warehouseResponse = await warehouseApi.getall()
  warehouses.value = Array.isArray(warehouseResponse) ? warehouseResponse : []
  const productResponse = await productApi.getAllProducts()
  products.value = Array.isArray(productResponse) ? productResponse : []
  if (!selectedWareHouseId.value && warehouses.value.length > 0) {
    selectedWareHouseId.value = warehouses.value[0].wareHouseId
  }
}

const reloadInventory = async () => {
  if (!selectedWareHouseId.value) {
    inventoryRows.value = []
    movementRows.value = []
    return
  }
  try {
    const inventoryResponse = await inventoryApi.search(selectedWareHouseId.value, '')
    inventoryRows.value = Array.isArray(inventoryResponse) ? inventoryResponse : []
    movementRows.value = []
  } catch (error) {
    errorMessage('库存加载失败', '请检查库存接口是否可用')
  }
}

const loadSkusByProduct = async () => {
  operationData.productSkuId = ''
  if (!selectedProductId.value) {
    skuOptions.value = []
    return
  }
  try {
    const response = await skuApi.productFind(selectedProductId.value)
    skuOptions.value = Array.isArray(response) ? response : []
  } catch (error) {
    errorMessage('SKU 加载失败', '请检查商品 SKU 接口')
  }
}

const openOperationPanel = () => {
  if (!selectedWareHouseId.value) {
    warningMessage('请选择仓库', '请先选择需要操作库存的仓库')
    return
  }
  showOperationPanel.value = true
}

const closeOperationPanel = () => {
  showOperationPanel.value = false
  loading.value = false
}

const selectInventoryRow = (row) => {
  if (!row) return
  operationData.productSkuId = row.productSkuId
  operationData.warningQuantity = row.warningQuantity ?? 0
}

const buildPayload = () => ({
  wareHouseId: selectedWareHouseId.value,
  productSkuId: Number(operationData.productSkuId),
  quantity: Number(operationData.quantity),
  warningQuantity: Number(operationData.warningQuantity) || 0,
  remark: operationData.remark
})

const submitStockOperation = async () => {
  if (!selectedWareHouseId.value) {
    warningMessage('请选择仓库', '请先选择仓库')
    return
  }
  loading.value = true
  try {
    const payload = buildPayload()
    if (operationData.type === 'transfer') {
      await inventoryApi.transfer(payload)
    } else {
      await inventoryApi.batchUpdate([{
        stockId: payload.productSkuId,
        stock: payload.quantity
      }])
    }
    closeOperationPanel()
    await reloadInventory()
  } catch (error) {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await loadBaseData()
    await reloadInventory()
  } catch (error) {
    errorMessage('初始化失败', '库存管理初始化数据加载失败')
  }
})
</script>

<style scoped>
.business-panel,
.movement-panel {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.business-panel {
  margin-top: 16px;
}

.movement-panel {
  margin-top: 16px;
}

.filter-bar,
.filter-left,
.filter-right,
.form-actions {
  display: flex;
}

.filter-bar {
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.filter-left {
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-label,
.filter-right {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 760;
}

.filter-select,
.search-input,
.field-input {
  height: 38px;
  padding: 0 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--text);
  outline: none;
}

.filter-select {
  min-width: 240px;
}

.search-input {
  width: 240px;
}

.content-layout {
  position: relative;
  min-height: 360px;
}

.table-area,
.table-area-compressed {
  transition: margin-right var(--transition-base);
}

.table-area-compressed {
  margin-right: 420px;
}

.operation-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 400px;
  z-index: 5;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-bottom: 1px solid var(--border-light);
  background: var(--surface-soft);
}

.panel-title-block,
.panel-form,
.form-group,
.operation-tip {
  display: flex;
  flex-direction: column;
}

.panel-title-block {
  gap: 4px;
}

.panel-eyebrow {
  color: var(--primary);
  font-size: 11px;
  font-weight: 820;
  letter-spacing: 0.08em;
}

.panel-title {
  color: var(--text);
  font-size: 16px;
  font-weight: 820;
}

.panel-close {
  width: 30px;
  height: 30px;
  color: var(--text-muted);
  background: var(--surface);
  border-radius: var(--radius-sm);
  font-size: 20px;
}

.panel-form {
  gap: 14px;
  padding: 18px;
}

.form-group {
  gap: 7px;
}

.field-label {
  color: var(--text);
  font-size: 13px;
  font-weight: 760;
}

.operation-tip {
  gap: 5px;
  padding: 12px;
  color: var(--text-secondary);
  background: var(--primary-50);
  border: 1px solid var(--primary-lighter);
  border-radius: var(--radius-md);
  font-size: 12px;
  line-height: 1.6;
}

.operation-tip strong {
  color: var(--primary);
}

.form-actions {
  gap: 10px;
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all var(--transition-base);
}

.slide-left-enter-from,
.slide-left-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

@media (max-width: 1180px) {
  .table-area-compressed {
    margin-right: 0;
  }

  .operation-panel {
    position: relative;
    width: 100%;
    margin-bottom: 14px;
  }
}

@media (max-width: 720px) {
  .filter-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-select,
  .search-input {
    width: 100%;
  }
}
</style>
