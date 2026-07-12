<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">商品</div>
        <h1 class="page-title-modern">SKU 管理</h1>
        <p class="page-subtitle-modern">按商品、颜色、尺码生成库存单位，并为库存与销售开单提供条码数据。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern-primary" @click="openCreateForm">新增 SKU</button>
        <button class="btn-modern" @click="openEditForm">编辑选中</button>
        <button class="btn-modern-danger" @click="deleteSelected">删除选中</button>
      </div>
    </div>

    <section class="sku-metrics">
      <article class="metric-card">
        <div class="metric-label">商品数量</div>
        <div class="metric-value">{{ productRows.length }}</div>
        <p class="metric-hint">来自商品管理模块</p>
      </article>
      <article class="metric-card-accent">
        <div class="metric-label">当前 SKU</div>
        <div class="metric-value">{{ skuRows.length }}</div>
        <p class="metric-hint">按选中商品加载</p>
      </article>
      <article class="metric-card">
        <div class="metric-label">颜色选项</div>
        <div class="metric-value">{{ optionGroups.colors.length }}</div>
        <p class="metric-hint">启用的颜色字典</p>
      </article>
      <article class="metric-card-accent">
        <div class="metric-label">尺码选项</div>
        <div class="metric-value">{{ optionGroups.sizes.length }}</div>
        <p class="metric-hint">启用的尺码字典</p>
      </article>
    </section>

    <section class="business-panel">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="filter-label">选择商品</span>
          <select v-model="selectedProductId" class="filter-select" @change="loadSkus">
            <option value="">请选择商品</option>
            <option v-for="product in productRows" :key="product.productId" :value="product.productId">
              {{ product.productCode }} - {{ product.productName }}
            </option>
          </select>
          <input v-model.trim="keyword" class="search-input" placeholder="搜索条码、颜色、尺码" />
        </div>
        <div class="filter-right">当前显示 {{ filteredRows.length }} 条</div>
      </div>

      <div class="batch-panel">
        <div class="batch-copy">
          <strong>批量生成 SKU</strong>
          <span>选择商品后，可按颜色和尺码组合自动生成 SKU，条码前缀默认使用商品编码。</span>
        </div>
        <div class="batch-form">
          <input v-model.trim="batchData.barcodePrefix" class="batch-input" placeholder="条码前缀，可不填" />
          <button class="btn-modern-soft" @click="batchCreateSkus" :disabled="batchLoading">{{ batchLoading ? '生成中...' : '批量生成' }}</button>
        </div>
        <div class="choice-area">
          <div class="choice-group">
            <span class="choice-title">颜色</span>
            <label v-for="item in optionGroups.colors" :key="item.optionValue" class="choice-item">
              <input v-model="batchData.colors" type="checkbox" :value="item.optionValue" />
              <span>{{ item.optionLabel }}</span>
            </label>
          </div>
          <div class="choice-group">
            <span class="choice-title">尺码</span>
            <label v-for="item in optionGroups.sizes" :key="item.optionValue" class="choice-item">
              <input v-model="batchData.sizes" type="checkbox" :value="item.optionValue" />
              <span>{{ item.optionLabel }}</span>
            </label>
          </div>
        </div>
      </div>

      <div class="content-layout">
        <transition name="slide-left">
          <aside v-if="showForm" class="sku-form-panel">
            <div class="panel-header">
              <div class="panel-title-block">
                <span class="panel-eyebrow">{{ formMode === 'create' ? 'Create' : 'Edit' }}</span>
                <h2 class="panel-title">{{ formMode === 'create' ? '新增 SKU' : '编辑 SKU' }}</h2>
              </div>
              <button class="panel-close" @click="closeForm">×</button>
            </div>
            <form class="panel-form" @submit.prevent="submitForm">
              <label class="form-group">
                <span class="field-label">商品</span>
                <select v-model="formData.productId" class="field-input" required>
                  <option v-for="product in productRows" :key="product.productId" :value="product.productId">
                    {{ product.productCode }} - {{ product.productName }}
                  </option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">颜色</span>
                <select v-model="formData.color" class="field-input" required>
                  <option v-for="item in optionGroups.colors" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">尺码</span>
                <select v-model="formData.size" class="field-input" required>
                  <option v-for="item in optionGroups.sizes" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">条码</span>
                <input v-model.trim="formData.barcode" class="field-input" required placeholder="请输入唯一条码" />
              </label>
              <div class="form-actions">
                <button type="button" class="btn-modern" @click="closeForm">取消</button>
                <button type="submit" class="btn-modern-primary" :disabled="loading">{{ loading ? '保存中...' : '保存' }}</button>
              </div>
            </form>
          </aside>
        </transition>

        <div :class="tableAreaClass">
          <CommonTable :table-data="filteredRows" :columns="columns" primaryKey="productSkuId" displayField="barcode" @rowClick="setSelectedRow" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useProductapi } from '../utils/axios/apiComposables/useProductapi'
import { useProductSkuapi } from '../utils/axios/apiComposables/useProductSkuapi'
import { useProductOptionapi } from '../utils/axios/apiComposables/useProductOptionapi'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const productApi = useProductapi()
const skuApi = useProductSkuapi()
const optionApi = useProductOptionapi()
const productRows = ref([])
const skuRows = ref([])
const selectedRow = ref(null)
const selectedProductId = ref('')
const keyword = ref('')
const showForm = ref(false)
const formMode = ref('create')
const loading = ref(false)
const batchLoading = ref(false)

const optionGroups = reactive({ colors: [], sizes: [] })
const batchData = reactive({ colors: [], sizes: [], barcodePrefix: '' })
const formData = reactive({ productSkuId: '', productId: '', color: '', size: '', barcode: '' })

const columns = ref([
  { Id: 'productSkuId', Label: 'SKU ID' },
  { Id: 'productName', Label: '商品名称' },
  { Id: 'colorLabel', Label: '颜色' },
  { Id: 'sizeLabel', Label: '尺码' },
  { Id: 'barcode', Label: '条码' }
])

const tableAreaClass = computed(() => showForm.value ? 'table-area-compressed' : 'table-area')
const labelOf = (items, value) => items.find(item => item.optionValue === value)?.optionLabel || value || '-'

const tableRows = computed(() => skuRows.value.map(item => ({
  ...item,
  colorLabel: labelOf(optionGroups.colors, item.color),
  sizeLabel: labelOf(optionGroups.sizes, item.size)
})))

const filteredRows = computed(() => tableRows.value.filter(item => {
  const text = `${item.productName} ${item.colorLabel} ${item.sizeLabel} ${item.barcode}`.toLowerCase()
  return !keyword.value || text.includes(keyword.value.toLowerCase())
}))

const resetForm = () => {
  Object.assign(formData, {
    productSkuId: '',
    productId: selectedProductId.value || productRows.value[0]?.productId || '',
    color: optionGroups.colors[0]?.optionValue || '',
    size: optionGroups.sizes[0]?.optionValue || '',
    barcode: ''
  })
}

const loadProducts = async () => {
  const response = await productApi.getAllProducts()
  productRows.value = Array.isArray(response) ? response : []
  if (!selectedProductId.value && productRows.value.length > 0) {
    selectedProductId.value = String(productRows.value[0].productId)
  }
}

const loadOptions = async () => {
  const response = await optionApi.grouped(true)
  optionGroups.colors = response?.colors || []
  optionGroups.sizes = response?.sizes || []
}

const loadSkus = async () => {
  selectedRow.value = null
  if (!selectedProductId.value) {
    skuRows.value = []
    return
  }
  try {
    const response = await skuApi.productFind(selectedProductId.value)
    skuRows.value = Array.isArray(response) ? response : []
  } catch (error) {
    errorMessage('SKU 加载失败', '请检查商品 SKU 接口')
  }
}

const setSelectedRow = (row) => {
  selectedRow.value = row
}

const openCreateForm = () => {
  if (!selectedProductId.value) {
    warningMessage('请选择商品', '请先选择要维护 SKU 的商品')
    return
  }
  formMode.value = 'create'
  resetForm()
  showForm.value = true
}

const openEditForm = () => {
  if (!selectedRow.value) {
    warningMessage('请选择 SKU', '请先在表格中选择要编辑的 SKU')
    return
  }
  formMode.value = 'edit'
  Object.assign(formData, {
    productSkuId: selectedRow.value.productSkuId,
    productId: selectedRow.value.productId,
    color: selectedRow.value.color,
    size: selectedRow.value.size,
    barcode: selectedRow.value.barcode
  })
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  loading.value = false
}

const submitForm = async () => {
  loading.value = true
  const payload = {
    productId: Number(formData.productId),
    color: formData.color,
    size: formData.size,
    barcode: formData.barcode
  }
  try {
    if (formMode.value === 'create') {
      await skuApi.create(payload)
      selectedProductId.value = String(payload.productId)
    } else {
      await skuApi.update(formData.productSkuId, payload)
    }
    closeForm()
    await loadSkus()
  } catch (error) {
    loading.value = false
  }
}

const deleteSelected = async () => {
  if (!selectedRow.value) {
    warningMessage('请选择 SKU', '请先在表格中选择要删除的 SKU')
    return
  }
  try {
    await skuApi.datadelete(selectedRow.value.productSkuId)
    selectedRow.value = null
    await loadSkus()
  } catch (error) {
    errorMessage('删除失败', 'SKU 删除失败，请检查是否已被库存或订单引用')
  }
}

const batchCreateSkus = async () => {
  if (!selectedProductId.value) {
    warningMessage('请选择商品', '请先选择商品')
    return
  }
  if (batchData.colors.length === 0 || batchData.sizes.length === 0) {
    warningMessage('请选择组合', '请至少选择一个颜色和一个尺码')
    return
  }
  batchLoading.value = true
  try {
    await skuApi.batchCreate({
      productId: Number(selectedProductId.value),
      colors: batchData.colors,
      sizes: batchData.sizes,
      barcodePrefix: batchData.barcodePrefix,
      skipExisting: true
    })
    batchData.colors = []
    batchData.sizes = []
    await loadSkus()
  } catch (error) {
    errorMessage('批量生成失败', '请检查条码前缀、颜色尺码组合是否重复')
  } finally {
    batchLoading.value = false
  }
}

onMounted(async () => {
  try {
    await loadOptions()
    await loadProducts()
    await loadSkus()
  } catch (error) {
    errorMessage('初始化失败', 'SKU 管理初始化数据加载失败')
  }
})
</script>

<style scoped>
.sku-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.business-panel {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.filter-bar,
.batch-panel,
.choice-area,
.filter-left,
.filter-right,
.batch-form,
.choice-group,
.choice-item,
.form-actions {
  display: flex;
}

.filter-bar,
.batch-panel {
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.filter-left,
.batch-form,
.choice-area {
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-right,
.filter-label,
.choice-title {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 760;
}

.filter-select,
.search-input,
.batch-input,
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
  min-width: 260px;
}

.search-input,
.batch-input {
  width: 220px;
}

.batch-panel {
  align-items: flex-start;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

.batch-copy {
  display: flex;
  flex-direction: column;
  gap: 5px;
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.batch-copy strong {
  color: var(--text);
  font-size: 14px;
}

.choice-area {
  padding: 12px;
  margin-bottom: 14px;
  border-radius: var(--radius-lg);
  background: var(--surface-soft);
}

.choice-group {
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.choice-item {
  align-items: center;
  gap: 5px;
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  font-size: 12px;
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

.sku-form-panel {
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
.form-group {
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

  .sku-form-panel {
    position: relative;
    width: 100%;
    margin-bottom: 14px;
  }
}

@media (max-width: 720px) {
  .filter-bar,
  .batch-panel {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-select,
  .search-input,
  .batch-input {
    width: 100%;
  }
}
</style>
