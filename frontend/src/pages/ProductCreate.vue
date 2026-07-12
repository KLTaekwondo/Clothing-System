<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">商品</div>
        <h1 class="page-title-modern">新增商品</h1>
        <p class="page-subtitle-modern">创建新的商品信息，并在创建时直接生成颜色尺码 SKU。</p>
      </div>
      <div class="action-row">
        <router-link class="btn-modern-soft" to="/dashboard/products">返回商品管理</router-link>
      </div>
    </div>

    <section class="product-form-shell">
      <form class="product-form" @submit.prevent="submitProduct">
        <section class="form-section">
          <div class="section-header">
            <span class="section-index">01</span>
            <div class="section-copy">
              <h2 class="section-title">基础信息</h2>
              <p class="section-desc">录入商品编码、名称和分类信息，商品简称会根据商品名称自动生成。</p>
            </div>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">商品编码</span>
              <input v-model.trim="formData.productCode" class="field-input" required placeholder="例如：TSHIRT-001" />
            </label>
            <label class="field-group">
              <span class="field-label">商品名称</span>
              <input v-model.trim="formData.productName" class="field-input" required placeholder="例如：基础款短袖" />
            </label>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">商品简称</span>
              <input v-model="formData.productShortName" class="field-input" readonly placeholder="输入商品名称后自动生成" />
            </label>
            <label class="field-group">
              <span class="field-label">商品类别</span>
              <select v-model="formData.productCategory" class="field-input" required>
                <option value="" disabled>请选择类别</option>
                <option v-for="item in optionGroups.categories" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
              </select>
            </label>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">商品子类</span>
              <select v-model="formData.productSubCategory" class="field-input" required>
                <option value="" disabled>请选择子类</option>
                <option v-for="item in optionGroups.subCategories" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
              </select>
            </label>
            <label class="field-group">
              <span class="field-label">产品成分</span>
              <select v-model="formData.productComposition" class="field-input" required>
                <option value="" disabled>请选择成分</option>
                <option v-for="item in optionGroups.compositions" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
              </select>
            </label>
          </div>
        </section>

        <section class="form-section">
          <div class="section-header">
            <span class="section-index">02</span>
            <div class="section-copy">
              <h2 class="section-title">销售属性</h2>
              <p class="section-desc">维护季节、年份、单位、默认价格和上下架状态。</p>
            </div>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">季节</span>
              <select v-model="formData.productSeason" class="field-input" required>
                <option value="" disabled>请选择季节</option>
                <option v-for="item in optionGroups.seasons" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
              </select>
            </label>
            <label class="field-group">
              <span class="field-label">年份</span>
              <input v-model.number="formData.productYear" class="field-input" type="number" min="2000" step="1" required />
            </label>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">单位</span>
              <select v-model="formData.productUnit" class="field-input" required>
                <option value="" disabled>请选择单位</option>
                <option v-for="item in optionGroups.units" :key="item.optionValue" :value="item.optionValue">{{ item.optionLabel }}</option>
              </select>
            </label>
            <label class="field-group">
              <span class="field-label">商品状态</span>
              <select v-model="formData.productStatus" class="field-input">
                <option :value="true">启用</option>
                <option :value="false">禁用</option>
              </select>
            </label>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">默认进货价</span>
              <input v-model.number="formData.productPurchasePrice" class="field-input" type="number" min="0" step="0.01" required />
            </label>
            <label class="field-group">
              <span class="field-label">默认销售价</span>
              <input v-model.number="formData.productSalePrice" class="field-input" type="number" min="0" step="0.01" required />
            </label>
          </div>
          <div class="field-row">
            <label class="field-group">
              <span class="field-label">是否特价</span>
              <select v-model="formData.isSpecialProduct" class="field-input">
                <option :value="true">是</option>
                <option :value="false">否</option>
              </select>
            </label>
            <div class="helper-card">
              <strong>提示</strong>
              <span>这里维护的是商品默认价格，后续单个 SKU 可以设置覆盖价格。</span>
            </div>
          </div>
        </section>

        <section class="form-section">
          <div class="section-header">
            <span class="section-index">03</span>
            <div class="section-copy">
              <h2 class="section-title">SKU 规格</h2>
              <p class="section-desc">选择颜色和尺码后，系统会组合生成 SKU，条码由后端自动生成。</p>
            </div>
          </div>
          <div class="sku-panel">
            <div class="sku-choice-block">
              <span class="field-label">颜色</span>
              <div class="choice-list">
                <label v-for="item in optionGroups.colors" :key="item.optionValue" class="choice-item">
                  <input v-model="formData.skuColors" type="checkbox" :value="item.optionValue" />
                  <span>{{ item.optionLabel }}</span>
                </label>
              </div>
            </div>
            <div class="sku-choice-block">
              <span class="field-label">尺码</span>
              <div class="choice-list">
                <label v-for="item in optionGroups.sizes" :key="item.optionValue" class="choice-item">
                  <input v-model="formData.skuSizes" type="checkbox" :value="item.optionValue" />
                  <span>{{ item.optionLabel }}</span>
                </label>
              </div>
            </div>
            <div class="sku-preview-card">
              <strong>预计生成 {{ skuPreview.length }} 个 SKU</strong>
              <span v-if="skuPreview.length === 0">请选择至少一个颜色和一个尺码。</span>
              <div v-if="skuPreview.length > 0" class="sku-preview-list">
                <span v-for="item in skuPreview" :key="item.key" class="sku-preview-item">{{ item.label }}</span>
              </div>
            </div>
          </div>
        </section>

        <div class="submit-bar">
          <button type="button" class="btn-modern" @click="resetForm">重置</button>
          <button type="submit" class="btn-modern-primary" :disabled="loading">{{ loading ? '创建中...' : '创建商品' }}</button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useProductapi } from '../utils/axios/apiComposables/useProductapi'
import { useProductOptionapi } from '../utils/axios/apiComposables/useProductOptionapi'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const router = useRouter()
const productApi = useProductapi()
const optionApi = useProductOptionapi()
const currentYear = new Date().getFullYear()
const loading = ref(false)

const optionGroups = reactive({
  categories: [],
  subCategories: [],
  seasons: [],
  units: [],
  compositions: [],
  colors: [],
  sizes: []
})

const buildShortName = (productName) => (productName || '').trim().replace(/\s+/g, '')
const labelOf = (items, value) => items.find(item => item.optionValue === value)?.optionLabel || value

const defaultForm = () => ({
  productCode: '',
  productName: '',
  productShortName: '',
  productCategory: '',
  productSubCategory: '',
  productSeason: '',
  productYear: currentYear,
  productUnit: '',
  productPurchasePrice: 0,
  productSalePrice: 0,
  productStatus: true,
  isSpecialProduct: false,
  productComposition: '',
  skuColors: [],
  skuSizes: []
})

const formData = reactive(defaultForm())

const skuPreview = computed(() => {
  const rows = []
  formData.skuColors.forEach(color => {
    formData.skuSizes.forEach(size => {
      rows.push({
        key: `${color}-${size}`,
        color,
        size,
        label: `${labelOf(optionGroups.colors, color)} / ${labelOf(optionGroups.sizes, size)}`
      })
    })
  })
  return rows
})

watch(() => formData.productName, (productName) => {
  formData.productShortName = buildShortName(productName)
})

const resetForm = () => {
  Object.assign(formData, defaultForm())
  applyDefaultOptions()
}

const applyDefaultOptions = () => {
  formData.productCategory = optionGroups.categories[0]?.optionValue || ''
  formData.productSubCategory = optionGroups.subCategories[0]?.optionValue || ''
  formData.productSeason = optionGroups.seasons[0]?.optionValue || ''
  formData.productUnit = optionGroups.units[0]?.optionValue || ''
  formData.productComposition = optionGroups.compositions[0]?.optionValue || ''
}

const loadOptionGroups = async () => {
  try {
    const response = await optionApi.grouped(true)
    optionGroups.categories = response?.categories || []
    optionGroups.subCategories = response?.subCategories || []
    optionGroups.seasons = response?.seasons || []
    optionGroups.units = response?.units || []
    optionGroups.compositions = response?.compositions || []
    optionGroups.colors = response?.colors || []
    optionGroups.sizes = response?.sizes || []
    applyDefaultOptions()
  } catch (error) {
    errorMessage('选项加载失败', '请先检查商品选项接口是否可用')
  }
}

const validateOptions = () => {
  const requiredGroups = [
    optionGroups.categories,
    optionGroups.subCategories,
    optionGroups.seasons,
    optionGroups.units,
    optionGroups.compositions,
    optionGroups.colors,
    optionGroups.sizes
  ]
  if (requiredGroups.some(group => group.length === 0)) {
    warningMessage('选项不完整', '请先在商品选项管理中维护并启用分类、季节、单位、成分、颜色和尺码')
    return false
  }
  return true
}

const validateSku = () => {
  if (formData.skuColors.length === 0 || formData.skuSizes.length === 0) {
    warningMessage('请选择 SKU 规格', '请至少选择一个颜色和一个尺码')
    return false
  }
  return true
}

const buildProductSkus = () => skuPreview.value.map(item => ({
  color: item.color,
  size: item.size,
  skuStatus: true
}))

const buildPayload = () => ({
  productCode: formData.productCode,
  productName: formData.productName,
  productShortName: formData.productShortName || buildShortName(formData.productName),
  productCategory: formData.productCategory,
  productSubCategory: formData.productSubCategory,
  productSeason: formData.productSeason,
  productYear: Number(formData.productYear),
  productUnit: formData.productUnit,
  productPurchasePrice: Number(formData.productPurchasePrice),
  productSalePrice: Number(formData.productSalePrice),
  productStatus: Boolean(formData.productStatus),
  isSpecialProduct: Boolean(formData.isSpecialProduct),
  productComposition: formData.productComposition,
  productSkus: buildProductSkus()
})

const submitProduct = async () => {
  if (!validateOptions() || !validateSku()) return
  loading.value = true
  try {
    await productApi.addProduct(buildPayload())
    await router.push('/dashboard/products')
  } catch (error) {
    loading.value = false
  }
}

onMounted(loadOptionGroups)
</script>

<style scoped>
.product-form-shell {
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.product-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-section {
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: var(--surface);
}

.section-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.section-index {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  color: var(--primary);
  background: var(--primary-50);
  border-radius: 14px;
  font-weight: 820;
  flex-shrink: 0;
}

.section-copy {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.section-title {
  color: var(--text);
  font-size: 17px;
  font-weight: 820;
}

.section-desc {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.field-row {
  display: flex;
  gap: 14px;
  margin-bottom: 14px;
}

.field-row:last-child {
  margin-bottom: 0;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 7px;
  width: calc(50% - 7px);
}

.field-label {
  color: var(--text);
  font-size: 13px;
  font-weight: 760;
}

.field-input {
  height: 40px;
  padding: 0 12px;
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface-soft);
  color: var(--text);
  outline: none;
}

.field-input:focus {
  border-color: var(--primary);
  background: var(--surface);
  box-shadow: 0 0 0 4px rgba(97, 87, 245, 0.12);
}

.field-input[readonly] {
  color: var(--text-secondary);
  cursor: default;
}

.helper-card {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  width: calc(50% - 7px);
  min-height: 40px;
  padding: 12px;
  color: var(--text-secondary);
  background: var(--primary-50);
  border: 1px solid var(--primary-lighter);
  border-radius: var(--radius-md);
  font-size: 12px;
}

.helper-card strong {
  color: var(--primary);
}

.sku-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sku-choice-block {
  display: flex;
  flex-direction: column;
  gap: 9px;
}

.choice-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.choice-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 11px;
  border: 1px solid var(--border);
  border-radius: 999px;
  background: var(--surface-soft);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
}

.choice-item:has(input:checked) {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-50);
}

.sku-preview-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface-soft);
  color: var(--text-secondary);
  font-size: 13px;
}

.sku-preview-card strong {
  color: var(--text);
}

.sku-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sku-preview-item {
  padding: 6px 10px;
  border-radius: 999px;
  background: var(--surface);
  color: var(--text-secondary);
  border: 1px solid var(--border-light);
  font-size: 12px;
}

.submit-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 760px) {
  .field-row {
    flex-direction: column;
  }

  .field-group,
  .helper-card {
    width: 100%;
  }
}
</style>
