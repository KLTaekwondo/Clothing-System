<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">销售</div>
        <h1 class="page-title-modern">销售开单</h1>
        <p class="page-subtitle-modern">选择仓库、员工和库存 SKU，快速创建销售订单并自动扣减库存。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern" @click="reloadBusinessData">刷新数据</button>
        <button class="btn-modern-primary" @click="submitOrder" :disabled="submitting">{{ submitting ? '提交中...' : '提交订单' }}</button>
      </div>
    </div>

    <section class="checkout-shell">
      <aside class="checkout-side">
        <div class="card-title-row">
          <h2 class="card-title-text">订单信息</h2>
          <span class="card-note-text">必填</span>
        </div>

        <label class="form-group">
          <span class="field-label">仓库</span>
          <select v-model="selectedWareHouseId" class="field-input" @change="handleWarehouseChange" required>
            <option value="">请选择仓库</option>
            <option v-for="warehouse in warehouses" :key="warehouse.wareHouseId" :value="warehouse.wareHouseId">
              {{ warehouse.wareHouseId }} - {{ warehouse.wareHouseName }}
            </option>
          </select>
        </label>

        <label class="form-group">
          <span class="field-label">员工</span>
          <select v-model="selectedEmployeeId" class="field-input" required>
            <option value="">请选择员工</option>
            <option v-for="employee in employees" :key="employee.employeeId" :value="employee.employeeId">
              {{ employee.employeeName }} / {{ employee.role }}
            </option>
          </select>
        </label>

        <label class="form-group">
          <span class="field-label">订单日期</span>
          <input v-model="orderForm.orderDate" class="field-input" type="date" required />
        </label>

        <label class="form-group">
          <span class="field-label">支付方式</span>
          <select v-model="orderForm.paymentMethod" class="field-input">
            <option value="CASH">现金</option>
            <option value="WECHAT">微信</option>
            <option value="ALIPAY">支付宝</option>
            <option value="CARD">银行卡</option>
          </select>
        </label>

        <label class="form-group">
          <span class="field-label">备注</span>
          <input v-model.trim="orderForm.remark" class="field-input" placeholder="可选" />
        </label>

        <div class="order-summary">
          <div class="summary-line">
            <span>商品行数</span>
            <strong>{{ cartItems.length }}</strong>
          </div>
          <div class="summary-line">
            <span>商品数量</span>
            <strong>{{ totalQuantity }}</strong>
          </div>
          <div class="summary-line">
            <span>预估应收</span>
            <strong>¥{{ payableAmount.toFixed(2) }}</strong>
          </div>
        </div>
      </aside>

      <main class="checkout-main">
        <section class="stock-panel">
          <div class="stock-toolbar">
            <div class="toolbar-left">
              <input v-model.trim="scanCode" class="scan-input" placeholder="扫条形码" @keyup.enter="scanBarcode" />
              <input v-model.trim="keyword" class="search-input" placeholder="搜索商品、条码、颜色、尺码" />
              <input v-model.number="addQuantity" class="quantity-input" type="number" min="1" step="1" />
              <input v-model.number="addDiscount" class="quantity-input" type="number" min="0" step="0.01" placeholder="折扣" />
              <button class="btn-modern-soft" @click="addSelectedToCart">加入订单</button>
            </div>
            <span class="toolbar-note">库存 {{ filteredInventoryRows.length }} 条</span>
          </div>
          <CommonTable :table-data="filteredInventoryRows" :columns="inventoryColumns" primaryKey="inventoryId" displayField="productName" @rowClick="selectInventoryRow" />
        </section>

        <section class="cart-panel">
          <div class="card-title-row">
            <h2 class="card-title-text">订单明细</h2>
            <span class="card-note-text">{{ cartItems.length }} 行</span>
          </div>
          <div v-if="cartItems.length === 0" class="empty-state">
            <div class="empty-panel">
              <div class="empty-icon">¥</div>
              <h2 class="empty-title">还没有商品</h2>
              <p class="empty-desc">从上方库存列表选择 SKU 后加入订单。</p>
            </div>
          </div>
          <div v-else class="cart-list">
            <article v-for="item in cartItems" :key="item.skuCode" class="cart-item">
              <div class="cart-copy">
                <strong>{{ item.productName }}</strong>
                <span>{{ item.color }} / {{ item.size }} / {{ item.barcode }}</span>
              </div>
              <div class="cart-controls">
                <input v-model.number="item.quantity" class="cart-input" type="number" min="1" step="1" />
                <input v-model.number="item.discountAmount" class="cart-input" type="number" min="0" step="0.01" />
                <span class="cart-price">¥{{ getLineAmount(item).toFixed(2) }}</span>
                <button class="cart-remove" @click="removeCartItem(item.skuCode)">删除</button>
              </div>
            </article>
          </div>
        </section>
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { useEmployeeapi } from '../utils/axios/apiComposables/useEmployeeapi'
import { useInventoryapi } from '../utils/axios/apiComposables/useInventoryapi'
import { useOrderapi } from '../utils/axios/apiComposables/useOrderapi'
import { errorMessage, successMessage, warningMessage } from '../utils/toastCenter'

const warehouseApi = useWareHouseapi()
const employeeApi = useEmployeeapi()
const inventoryApi = useInventoryapi()
const orderApi = useOrderapi()

const today = new Date().toISOString().slice(0, 10)
const warehouses = ref([])
const employees = ref([])
const inventoryRows = ref([])
const selectedWareHouseId = ref('')
const selectedEmployeeId = ref('')
const selectedInventory = ref(null)
const keyword = ref('')
const scanCode = ref('')
const addQuantity = ref(1)
const addDiscount = ref(0)
const cartItems = ref([])
const submitting = ref(false)

const orderForm = reactive({
  orderDate: today,
  paymentMethod: 'CASH',
  remark: ''
})

const inventoryColumns = ref([
  { Id: 'productName', Label: '商品' },
  { Id: 'color', Label: '颜色' },
  { Id: 'size', Label: '尺码' },
  { Id: 'barcode', Label: '条码' },
  { Id: 'quantity', Label: '可售库存' },
  { Id: 'priceText', Label: '销售价' }
])

const tableRows = computed(() => inventoryRows.value.map(item => {
  const spec = item.spec || {}
  return {
    ...item,
    productName: item.productName,
    color: spec.color || '',
    size: spec.size || '',
    barcode: item.skuCode,
    unitPrice: Number(item.productSalePrice || 0),
    priceText: `¥${Number(item.productSalePrice || 0).toFixed(2)}`
  }
}))

const filteredInventoryRows = computed(() => tableRows.value.filter(item => {
  const hasStock = Number(item.quantity) > 0
  const text = `${item.productName} ${item.color} ${item.size} ${item.barcode}`.toLowerCase()
  return hasStock && (!keyword.value || text.includes(keyword.value.toLowerCase()))
}))

const totalQuantity = computed(() => cartItems.value.reduce((sum, item) => sum + Number(item.quantity || 0), 0))
const payableAmount = computed(() => cartItems.value.reduce((sum, item) => sum + getLineAmount(item), 0))

const getLineAmount = (item) => {
  const total = Number(item.unitPrice || 0) * Number(item.quantity || 0)
  return Math.max(total - Number(item.discountAmount || 0), 0)
}

const loadWarehouses = async () => {
  const response = await warehouseApi.getall()
  warehouses.value = Array.isArray(response) ? response : []
  if (!selectedWareHouseId.value && warehouses.value.length > 0) selectedWareHouseId.value = warehouses.value[0].wareHouseId
}

const loadWarehouseBusiness = async () => {
  if (!selectedWareHouseId.value) {
    employees.value = []
    inventoryRows.value = []
    return
  }
  try {
    const empResponse = await employeeApi.findAll()
    employees.value = Array.isArray(empResponse) ? empResponse : []
    if (!employees.value.some(item => String(item.employeeId || item.id) === String(selectedEmployeeId.value))) {
      selectedEmployeeId.value = employees.value[0]?.employeeId || employees.value[0]?.id || ''
    }
  } catch (e) {
    employees.value = []
  }
  try {
    const invResponse = await inventoryApi.search(selectedWareHouseId.value, '')
    inventoryRows.value = Array.isArray(invResponse) ? invResponse : []
  } catch (e) {
    inventoryRows.value = []
  }
}

const reloadBusinessData = async () => {
  try {
    await loadWarehouseBusiness()
  } catch (error) {
    errorMessage('加载失败', '销售开单数据加载失败')
  }
}

const handleWarehouseChange = async () => {
  cartItems.value = []
  selectedInventory.value = null
  await loadWarehouseBusiness()
}

const selectInventoryRow = (row) => {
  selectedInventory.value = row
}

const addSelectedToCart = () => {
  if (!selectedInventory.value) {
    warningMessage('请选择 SKU', '请先从库存列表中选择要销售的 SKU')
    return
  }
  const quantity = Number(addQuantity.value || 1)
  if (quantity <= 0) {
    warningMessage('数量错误', '销售数量必须大于 0')
    return
  }
  if (quantity > Number(selectedInventory.value.quantity || 0)) {
    warningMessage('库存不足', '销售数量不能超过当前库存')
    return
  }
  const existed = cartItems.value.find(item => item.skuCode === selectedInventory.value.skuCode)
  if (existed) {
    existed.quantity += quantity
    existed.discountAmount += Number(addDiscount.value || 0)
    return
  }
  cartItems.value.push({
    skuCode: selectedInventory.value.skuCode,
    productName: selectedInventory.value.productName,
    color: selectedInventory.value.color,
    size: selectedInventory.value.size,
    barcode: selectedInventory.value.skuCode,
    quantity,
    unitPrice: Number(selectedInventory.value.unitPrice || 0),
    discountAmount: Number(addDiscount.value || 0)
  })
}

const scanBarcode = () => {
  const code = scanCode.value.trim()
  if (!code) return
  const match = tableRows.value.find(item => item.skuCode === code)
  if (!match) {
    warningMessage('未找到', `条形码 "${code}" 在当前仓库库存中未找到`)
    scanCode.value = ''
    return
  }
  selectedInventory.value = match
  addSelectedToCart()
  scanCode.value = ''
}

const removeCartItem = (code) => {
  cartItems.value = cartItems.value.filter(item => item.skuCode !== code)
}

const submitOrder = async () => {
  if (!selectedWareHouseId.value || !selectedEmployeeId.value) {
    warningMessage('信息不完整', '请选择仓库和员工')
    return
  }
  if (cartItems.value.length === 0) {
    warningMessage('订单为空', '请至少添加一个 SKU')
    return
  }
  submitting.value = true
  try {
    await orderApi.create({
      wareHouseId: selectedWareHouseId.value,
      employeeId: Number(selectedEmployeeId.value),
      orderDate: orderForm.orderDate,
      paymentMethod: orderForm.paymentMethod,
      remark: orderForm.remark,
      items: cartItems.value.map(item => ({
        skuCode: item.skuCode,
        quantity: Number(item.quantity),
        unitPrice: Number(item.unitPrice || 0),
        discountAmount: Number(item.discountAmount || 0)
      }))
    })
    successMessage('开单成功', '订单已创建并扣减库存')
    cartItems.value = []
    await reloadBusinessData()
  } catch (error) {
    errorMessage('开单失败', '请检查库存、员工和商品信息')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  try {
    await loadWarehouses()
    await reloadBusinessData()
  } catch (error) {
    errorMessage('初始化失败', '销售开单初始化数据加载失败')
  }
})
</script>

<style scoped>
.checkout-shell {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.checkout-side,
.stock-panel,
.cart-panel {
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.checkout-side {
  width: 320px;
  flex-shrink: 0;
  padding: 16px;
}

.checkout-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: calc(100% - 336px);
}

.stock-panel,
.cart-panel {
  padding: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
  margin-bottom: 14px;
}

.field-label {
  color: var(--text);
  font-size: 13px;
  font-weight: 760;
}

.field-input,
.search-input,
.scan-input,
.quantity-input,
.cart-input {
  height: 38px;
  padding: 0 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--text);
  outline: none;
}

.order-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
  padding: 14px;
  border-radius: var(--radius-lg);
  background: var(--primary-50);
  border: 1px solid var(--primary-lighter);
}

.summary-line,
.stock-toolbar,
.toolbar-left,
.cart-item,
.cart-controls {
  display: flex;
}

.summary-line,
.stock-toolbar,
.cart-item {
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.summary-line span,
.toolbar-note {
  color: var(--text-secondary);
  font-size: 13px;
}

.toolbar-left,
.cart-controls {
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.stock-toolbar {
  margin-bottom: 14px;
}

.search-input {
  width: 260px;
}

.quantity-input,
.cart-input {
  width: 96px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cart-item {
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
}

.cart-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.cart-copy strong {
  color: var(--text);
  font-size: 14px;
}

.cart-copy span {
  color: var(--text-secondary);
  font-size: 12px;
}

.cart-price {
  color: var(--primary);
  font-weight: 820;
  min-width: 82px;
}

.cart-remove {
  height: 32px;
  padding: 0 10px;
  border-radius: var(--radius-sm);
  color: var(--danger);
  background: var(--danger-light);
}

@media (max-width: 1120px) {
  .checkout-shell {
    flex-direction: column;
  }

  .checkout-side,
  .checkout-main {
    width: 100%;
  }
}

@media (max-width: 720px) {
  .stock-toolbar,
  .cart-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .search-input,
  .quantity-input,
  .cart-input {
    width: 100%;
  }
}
</style>
