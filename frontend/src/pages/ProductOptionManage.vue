<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">商品</div>
        <h1 class="page-title-modern">商品选项管理</h1>
        <p class="page-subtitle-modern">管理分类、季节、单位、成分、颜色和尺码，为商品创建与 SKU 组合提供标准字典。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern-primary" @click="openCreateForm">新增选项</button>
        <button class="btn-modern" @click="openEditForm">编辑选中</button>
        <button class="btn-modern-danger" @click="deleteSelected">删除选中</button>
      </div>
    </div>

    <section class="option-summary">
      <article v-for="item in summaryCards" :key="item.type" :class="item.cardClass" @click="activeType = item.type">
        <span class="summary-label">{{ item.label }}</span>
        <strong class="summary-value">{{ item.count }}</strong>
        <span class="summary-hint">{{ item.hint }}</span>
      </article>
    </section>

    <section class="business-panel">
      <div class="filter-bar">
        <div class="filter-left">
          <span class="filter-label">选项类型</span>
          <select v-model="activeType" class="filter-select">
            <option value="ALL">全部类型</option>
            <option v-for="item in productOptionTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="filter-right">
          <span>当前显示 {{ filteredRows.length }} 条</span>
        </div>
      </div>

      <div class="content-layout">
        <transition name="slide-left">
          <aside v-if="showForm" class="option-form-panel">
            <div class="form-header">
              <div class="form-title-block">
                <span class="form-eyebrow">{{ formMode === 'create' ? 'Create' : 'Edit' }}</span>
                <h2 class="form-title">{{ formMode === 'create' ? '新增商品选项' : '编辑商品选项' }}</h2>
              </div>
              <button class="form-close" @click="closeForm">×</button>
            </div>

            <form class="form-body" @submit.prevent="submitForm">
              <label class="form-group">
                <span class="field-label">选项类型</span>
                <select v-model="formData.optionType" class="field-input" required>
                  <option v-for="item in productOptionTypeOptions" :key="item.value" :value="item.value">
                    {{ item.label }}
                  </option>
                </select>
              </label>

              <label class="form-group">
                <span class="field-label">选项值</span>
                <input v-model.trim="formData.optionValue" class="field-input" placeholder="例如：SPRING 或 RED" required />
              </label>

              <label class="form-group">
                <span class="field-label">显示名称</span>
                <input v-model.trim="formData.optionLabel" class="field-input" placeholder="例如：春季 或 红色" required />
              </label>

              <label class="form-group">
                <span class="field-label">排序</span>
                <input v-model.number="formData.sortOrder" class="field-input" type="number" min="0" step="1" />
              </label>

              <label class="form-group">
                <span class="field-label">状态</span>
                <select v-model="formData.optionStatus" class="field-input">
                  <option :value="true">启用</option>
                  <option :value="false">禁用</option>
                </select>
              </label>

              <div class="form-actions">
                <button type="button" class="btn-modern" @click="closeForm">取消</button>
                <button type="submit" class="btn-modern-primary" :disabled="loading">
                  {{ loading ? '保存中...' : '保存' }}
                </button>
              </div>
            </form>
          </aside>
        </transition>

        <div :class="tableAreaClass">
          <CommonTable :table-data="filteredRows" :columns="columns" primaryKey="productOptionId" displayField="optionLabel" @rowClick="setSelectedRow" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useProductOptionapi } from '../utils/axios/apiComposables/useProductOptionapi'
import { productOptionTypeOptions, productOptionTypeLabels } from '../utils/productOptionTypes'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const optionApi = useProductOptionapi()
const optionRows = ref([])
const selectedRow = ref(null)
const activeType = ref('ALL')
const showForm = ref(false)
const formMode = ref('create')
const loading = ref(false)

const columns = ref([
  { Id: 'productOptionId', Label: 'ID' },
  { Id: 'optionTypeLabel', Label: '类型' },
  { Id: 'optionValue', Label: '选项值' },
  { Id: 'optionLabel', Label: '显示名称' },
  { Id: 'sortOrder', Label: '排序' },
  { Id: 'optionStatus', Label: '状态' }
])

const defaultForm = () => ({
  productOptionId: '',
  optionType: productOptionTypeOptions[0]?.value || '',
  optionValue: '',
  optionLabel: '',
  sortOrder: 0,
  optionStatus: true
})

const formData = reactive(defaultForm())

const tableRows = computed(() => optionRows.value.map(item => ({
  ...item,
  optionTypeLabel: productOptionTypeLabels[item.optionType] || item.optionType
})))

const filteredRows = computed(() => {
  if (activeType.value === 'ALL') return tableRows.value
  return tableRows.value.filter(item => item.optionType === activeType.value)
})

const tableAreaClass = computed(() => showForm.value ? 'table-area-compressed' : 'table-area')

const summaryCards = computed(() => productOptionTypeOptions.map((item, index) => ({
  type: item.value,
  label: item.label,
  count: optionRows.value.filter(row => row.optionType === item.value).length,
  hint: activeType.value === item.value ? '当前筛选' : '点击筛选',
  cardClass: index % 2 === 0 ? 'summary-card' : 'summary-card-accent'
})))

const resetForm = () => {
  Object.assign(formData, defaultForm())
}

const loadOptions = async () => {
  try {
    const response = await optionApi.findAll()
    optionRows.value = Array.isArray(response) ? response : []
  } catch (error) {
    errorMessage('加载失败', '商品选项数据获取失败')
  }
}

const setSelectedRow = (row) => {
  selectedRow.value = row
}

const openCreateForm = () => {
  formMode.value = 'create'
  resetForm()
  if (activeType.value !== 'ALL') formData.optionType = activeType.value
  showForm.value = true
}

const openEditForm = () => {
  if (!selectedRow.value) {
    warningMessage('请选择数据', '请先在表格中选择要编辑的商品选项')
    return
  }
  formMode.value = 'edit'
  Object.assign(formData, {
    productOptionId: selectedRow.value.productOptionId,
    optionType: selectedRow.value.optionType,
    optionValue: selectedRow.value.optionValue,
    optionLabel: selectedRow.value.optionLabel,
    sortOrder: selectedRow.value.sortOrder ?? 0,
    optionStatus: Boolean(selectedRow.value.optionStatus)
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
    optionType: formData.optionType,
    optionValue: formData.optionValue,
    optionLabel: formData.optionLabel,
    sortOrder: Number(formData.sortOrder) || 0,
    optionStatus: Boolean(formData.optionStatus)
  }
  try {
    if (formMode.value === 'create') {
      await optionApi.create(payload)
    } else {
      await optionApi.update(formData.productOptionId, payload)
    }
    closeForm()
    selectedRow.value = null
    await loadOptions()
  } catch (error) {
    loading.value = false
  }
}

const deleteSelected = async () => {
  if (!selectedRow.value) {
    warningMessage('请选择数据', '请先在表格中选择要删除的商品选项')
    return
  }
  try {
    await optionApi.datadelete(selectedRow.value.productOptionId)
    selectedRow.value = null
    await loadOptions()
  } catch (error) {
    errorMessage('删除失败', '商品选项删除失败，请检查是否被业务数据引用')
  }
}

onMounted(loadOptions)
</script>

<style scoped>
.option-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-bottom: 16px;
}

.summary-card,
.summary-card-accent {
  width: calc(25% - 10.5px);
  min-width: 170px;
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast), border-color var(--transition-fast);
}

.summary-card-accent {
  background: linear-gradient(135deg, var(--surface), var(--primary-50));
}

.summary-card:hover,
.summary-card-accent:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-lighter);
}

.summary-label,
.summary-hint {
  display: block;
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 700;
}

.summary-value {
  display: block;
  margin: 8px 0;
  color: var(--text);
  font-size: 28px;
  letter-spacing: -0.04em;
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

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-secondary);
  font-size: 13px;
}

.filter-label {
  font-weight: 800;
}

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

.option-form-panel {
  position: absolute;
  top: 0;
  right: 0;
  width: 400px;
  z-index: 5;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px;
  border-bottom: 1px solid var(--border-light);
  background: var(--surface-soft);
}

.form-title-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-eyebrow {
  color: var(--primary);
  font-size: 11px;
  font-weight: 820;
  letter-spacing: 0.08em;
}

.form-title {
  color: var(--text);
  font-size: 16px;
  font-weight: 820;
}

.form-close {
  width: 30px;
  height: 30px;
  color: var(--text-muted);
  background: var(--surface);
  border-radius: var(--radius-sm);
  font-size: 20px;
}

.form-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
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
  margin-top: 6px;
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
  .summary-card,
  .summary-card-accent {
    width: calc(50% - 7px);
  }

  .table-area-compressed {
    margin-right: 0;
  }

  .option-form-panel {
    position: relative;
    width: 100%;
    margin-bottom: 14px;
  }
}

@media (max-width: 720px) {
  .summary-card,
  .summary-card-accent {
    width: 100%;
  }

  .filter-bar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
