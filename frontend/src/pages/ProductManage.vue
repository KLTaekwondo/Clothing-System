<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">商品</div>
        <h1 class="page-title-modern">商品管理</h1>
        <p class="page-subtitle-modern">管理商品基础资料、分类信息和上架状态，为 SKU 与库存模块提供数据基础。</p>
      </div>
      <div class="action-row">
        <router-link class="btn-modern-primary" to="/dashboard/products/create">新增商品</router-link>
        <router-link class="btn-modern-soft" to="/dashboard/products/options">管理选项</router-link>
        <button class="btn-modern" @click="openEditForm">编辑选中</button>
        <button class="btn-modern-danger" @click="deleteSelected">删除选中</button>
      </div>
    </div>

    <section class="product-metrics">
      <article v-for="item in metricCards" :key="item.label" :class="item.cardClass">
        <span class="metric-label">{{ item.label }}</span>
        <strong class="metric-value">{{ item.value }}</strong>
        <span class="metric-hint">{{ item.hint }}</span>
      </article>
    </section>

    <section class="business-panel">
      <div class="filter-bar">
        <div class="filter-left">
          <input v-model.trim="keyword" class="search-input" placeholder="搜索编码、名称、类别" />
          <select v-model="statusFilter" class="filter-select">
            <option value="ALL">全部状态</option>
            <option value="true">启用</option>
            <option value="false">禁用</option>
          </select>
          <select v-model="categoryFilter" class="filter-select">
            <option value="ALL">全部类别</option>
            <option v-for="item in optionGroups.categories" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
          </select>
        </div>
        <div class="filter-right">当前显示 {{ filteredRows.length }} 条</div>
      </div>

      <div class="content-layout">
        <transition name="slide-left">
          <aside v-if="showEditPanel" class="edit-panel">
            <div class="panel-header">
              <div class="panel-title-block">
                <span class="panel-eyebrow">Edit</span>
                <h2 class="panel-title">编辑商品</h2>
              </div>
              <button class="panel-close" @click="closeEditForm">×</button>
            </div>
            <form class="panel-form" @submit.prevent="submitEdit">
              <label class="form-group">
                <span class="field-label">商品编码</span>
                <input v-model.trim="editData.productCode" class="field-input" required />
              </label>
              <label class="form-group">
                <span class="field-label">商品名称</span>
                <input v-model.trim="editData.productName" class="field-input" required />
              </label>
              <label class="form-group">
                <span class="field-label">商品简称</span>
                <input v-model.trim="editData.productShortName" class="field-input" />
              </label>
              <label class="form-group">
                <span class="field-label">类别</span>
                <select v-model="editData.productCategory" class="field-input" required>
                  <option v-for="item in optionGroups.categories" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">子类</span>
                <select v-model="editData.productSubCategory" class="field-input" required>
                  <option v-for="item in optionGroups.subCategories" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">季节</span>
                <select v-model="editData.productSeason" class="field-input" required>
                  <option v-for="item in optionGroups.seasons" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">年份</span>
                <input v-model.number="editData.productYear" class="field-input" type="number" min="2000" step="1" required />
              </label>
              <label class="form-group">
                <span class="field-label">单位</span>
                <select v-model="editData.productUnit" class="field-input" required>
                  <option v-for="item in optionGroups.units" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">产品成分</span>
                <select v-model="editData.productComposition" class="field-input" required>
                  <option v-for="item in optionGroups.compositions" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">进货价</span>
                <input v-model.number="editData.productPurchasePrice" class="field-input" type="number" min="0" step="0.01" required />
              </label>
              <label class="form-group">
                <span class="field-label">销售价</span>
                <input v-model.number="editData.productSalePrice" class="field-input" type="number" min="0" step="0.01" required />
              </label>
              <label class="form-group">
                <span class="field-label">商品状态</span>
                <select v-model="editData.productStatus" class="field-input">
                  <option :value="true">启用</option>
                  <option :value="false">禁用</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">是否特价</span>
                <select v-model="editData.isSpecialProduct" class="field-input">
                  <option :value="true">是</option>
                  <option :value="false">否</option>
                </select>
              </label>
              <div class="form-actions">
                <button type="button" class="btn-modern" @click="closeEditForm">取消</button>
                <button type="submit" class="btn-modern-primary" :disabled="loading">{{ loading ? '保存中...' : '保存修改' }}</button>
              </div>
            </form>
          </aside>
        </transition>

        <div :class="tableAreaClass">
          <CommonTable :table-data="filteredRows" :columns="columns" primaryKey="productId" displayField="productName" @rowClick="setSelectedRow" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useProductapi } from '../utils/axios/apiComposables/useProductapi'
import { useProductOptionapi } from '../utils/axios/apiComposables/useProductOptionapi'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const productApi = useProductapi()
const optionApi = useProductOptionapi()
const productRows = ref([])
const selectedRow = ref(null)
const keyword = ref('')
const statusFilter = ref('ALL')
const categoryFilter = ref('ALL')
const showEditPanel = ref(false)
const loading = ref(false)

const optionGroups = reactive({
  categories: [],
  subCategories: [],
  seasons: [],
  units: [],
  compositions: []
})

const columns = ref([
  { Id: 'productId', Label: 'ID' },
  { Id: 'productCode', Label: '编码' },
  { Id: 'productName', Label: '名称' },
  { Id: 'categoryLabel', Label: '类别' },
  { Id: 'subCategoryLabel', Label: '子类' },
  { Id: 'salePriceText', Label: '销售价' },
  { Id: 'productStatus', Label: '状态' },
  { Id: 'isSpecialText', Label: '特价' }
])

const defaultEdit = () => ({
  productId: '',
  productCode: '',
  productName: '',
  productShortName: '',
  productCategory: '',
  productSubCategory: '',
  productSeason: '',
  productYear: new Date().getFullYear(),
  productUnit: '',
  productPurchasePrice: 0,
  productSalePrice: 0,
  productStatus: true,
  isSpecialProduct: false,
  productComposition: ''
})

const editData = reactive(defaultEdit())

const tableAreaClass = computed(() => showEditPanel.value ? 'table-area-compressed' : 'table-area')

const makeOptionLabel = (items, value) => items.find(item => item.optionValue === value)?.optionLabel || value || '-'

const tableRows = computed(() => productRows.value.map(item => ({
  ...item,
  categoryLabel: makeOptionLabel(optionGroups.categories, item.productCategory),
  subCategoryLabel: makeOptionLabel(optionGroups.subCategories, item.productSubCategory),
  salePriceText: `¥${Number(item.productSalePrice || 0).toFixed(2)}`,
  isSpecialText: item.isSpecialProduct ? '是' : '否'
})))

const filteredRows = computed(() => tableRows.value.filter(item => {
  const text = `${item.productCode} ${item.productName} ${item.categoryLabel} ${item.subCategoryLabel}`.toLowerCase()
  const keywordMatched = !keyword.value || text.includes(keyword.value.toLowerCase())
  const statusMatched = statusFilter.value === 'ALL' || String(item.productStatus) === statusFilter.value
  const categoryMatched = categoryFilter.value === 'ALL' || item.productCategory === categoryFilter.value
  return keywordMatched && statusMatched && categoryMatched
}))

const metricCards = computed(() => [
  { label: '商品总数', value: productRows.value.length, hint: '当前系统商品档案', cardClass: 'metric-card' },
  { label: '启用商品', value: productRows.value.filter(item => item.productStatus).length, hint: '可用于销售开单', cardClass: 'metric-card-accent' },
  { label: '特价商品', value: productRows.value.filter(item => item.isSpecialProduct).length, hint: '需重点关注毛利', cardClass: 'metric-card' },
  { label: '商品类别', value: optionGroups.categories.length, hint: '来自商品选项', cardClass: 'metric-card-accent' }
])

const loadOptions = async () => {
  try {
    const response = await optionApi.grouped(true)
    optionGroups.categories = response?.categories || []
    optionGroups.subCategories = response?.subCategories || []
    optionGroups.seasons = response?.seasons || []
    optionGroups.units = response?.units || []
    optionGroups.compositions = response?.compositions || []
  } catch (error) {
    errorMessage('选项加载失败', '商品选项获取失败')
  }
}

const loadProducts = async () => {
  try {
    const response = await productApi.getAllProducts()
    productRows.value = Array.isArray(response) ? response : []
  } catch (error) {
    errorMessage('商品加载失败', '商品列表获取失败')
  }
}

const setSelectedRow = (row) => {
  selectedRow.value = row
}

const openEditForm = () => {
  if (!selectedRow.value) {
    warningMessage('请选择商品', '请先在表格中选择要编辑的商品')
    return
  }
  Object.assign(editData, defaultEdit(), selectedRow.value)
  editData.productPurchasePrice = Number(editData.productPurchasePrice || 0)
  editData.productSalePrice = Number(editData.productSalePrice || 0)
  showEditPanel.value = true
}

const closeEditForm = () => {
  showEditPanel.value = false
  loading.value = false
}

const buildPayload = () => ({
  productCode: editData.productCode,
  productName: editData.productName,
  productShortName: editData.productShortName,
  productCategory: editData.productCategory,
  productSubCategory: editData.productSubCategory,
  productSeason: editData.productSeason,
  productYear: Number(editData.productYear),
  productUnit: editData.productUnit,
  productPurchasePrice: Number(editData.productPurchasePrice),
  productSalePrice: Number(editData.productSalePrice),
  productStatus: Boolean(editData.productStatus),
  isSpecialProduct: Boolean(editData.isSpecialProduct),
  productComposition: editData.productComposition
})

const submitEdit = async () => {
  loading.value = true
  try {
    await productApi.updateProduct(editData.productId, buildPayload())
    closeEditForm()
    selectedRow.value = null
    await loadProducts()
  } catch (error) {
    loading.value = false
  }
}

const deleteSelected = async () => {
  if (!selectedRow.value) {
    warningMessage('请选择商品', '请先在表格中选择要删除的商品')
    return
  }
  try {
    await productApi.deleteProduct(selectedRow.value.productId)
    selectedRow.value = null
    await loadProducts()
  } catch (error) {
    errorMessage('删除失败', '商品删除失败，请检查是否存在关联 SKU')
  }
}

onMounted(async () => {
  await loadOptions()
  await loadProducts()
})
</script>

<style scoped>
.product-metrics {
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

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.filter-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-right {
  color: var(--text-secondary);
  font-size: 13px;
}

.search-input,
.filter-select,
.field-input {
  height: 38px;
  padding: 0 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--text);
  outline: none;
}

.search-input {
  width: 220px;
}

.content-layout {
  position: relative;
  min-height: 420px;
}

.table-area,
.table-area-compressed {
  transition: margin-right var(--transition-base);
}

.table-area-compressed {
  margin-right: 420px;
}

.edit-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 400px;
  max-height: 620px;
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

.panel-title-block {
  display: flex;
  flex-direction: column;
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
  display: flex;
  flex-direction: column;
  gap: 13px;
  max-height: 542px;
  padding: 18px;
  overflow-y: auto;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.field-label {
  color: var(--text);
  font-size: 13px;
  font-weight: 760;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 4px;
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

  .edit-panel {
    position: relative;
    width: 100%;
    max-height: none;
    margin-bottom: 14px;
  }

  .panel-form {
    max-height: none;
  }
}

@media (max-width: 720px) {
  .filter-bar {
    align-items: flex-start;
    flex-direction: column;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }
}
</style>
