<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">人员</div>
        <h1 class="page-title-modern">员工管理</h1>
        <p class="page-subtitle-modern">管理系统员工账号、角色和所属仓库，让开单、库存操作与人员归属保持一致。</p>
      </div>
      <div class="action-row">
        <button class="btn-modern-primary" @click="openCreateForm">新增员工</button>
        <button class="btn-modern" @click="openEditForm">编辑选中</button>
        <button class="btn-modern-danger" @click="deleteSelected">删除选中</button>
      </div>
    </div>

    <section class="metric-grid">
      <div class="metric-card">
        <div class="metric-label">员工总数</div>
        <div class="metric-value">{{ employeeRows.length }}</div>
        <p class="metric-hint">当前已登记员工</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">启用员工</div>
        <div class="metric-value">{{ enabledCount }}</div>
        <p class="metric-hint">可参与业务操作</p>
      </div>
      <div class="metric-card">
        <div class="metric-label">仓库数量</div>
        <div class="metric-value">{{ warehouses.length }}</div>
        <p class="metric-hint">来自仓库管理模块</p>
      </div>
      <div class="metric-card-accent">
        <div class="metric-label">当前筛选</div>
        <div class="metric-value">{{ filteredRows.length }}</div>
        <p class="metric-hint">表格显示员工数</p>
      </div>
    </section>

    <section class="business-panel">
      <div class="filter-bar">
        <div class="filter-left">
          <input v-model.trim="keyword" class="search-input" placeholder="搜索编号、姓名、电话、角色" />
          <select v-model="warehouseFilter" class="filter-select">
            <option value="ALL">全部仓库</option>
            <option v-for="warehouse in warehouses" :key="warehouse.wareHouseId" :value="warehouse.wareHouseId">
              {{ warehouse.wareHouseName }}
            </option>
          </select>
          <select v-model="statusFilter" class="filter-select">
            <option value="ALL">全部状态</option>
            <option value="true">启用</option>
            <option value="false">禁用</option>
          </select>
        </div>
        <div class="filter-right">当前显示 {{ filteredRows.length }} 条</div>
      </div>

      <div class="content-layout">
        <transition name="slide-left">
          <aside v-if="showForm" class="edit-panel">
            <div class="panel-header">
              <div class="panel-title-block">
                <span class="panel-eyebrow">{{ formMode === 'create' ? 'Create' : 'Edit' }}</span>
                <h2 class="panel-title">{{ formMode === 'create' ? '新增员工' : '编辑员工' }}</h2>
              </div>
              <button class="panel-close" @click="closeForm">×</button>
            </div>
            <form class="panel-form" @submit.prevent="submitForm">
              <label class="form-group">
                <span class="field-label">员工编号</span>
                <input v-model.trim="formData.employeeCode" class="field-input" required placeholder="例如：EMP001" />
              </label>
              <label class="form-group">
                <span class="field-label">员工姓名</span>
                <input v-model.trim="formData.employeeName" class="field-input" required placeholder="请输入姓名" />
              </label>
              <label class="form-group">
                <span class="field-label">手机号</span>
                <input v-model.trim="formData.phone" class="field-input" placeholder="请输入手机号" />
              </label>
              <label class="form-group">
                <span class="field-label">角色</span>
                <select v-model="formData.role" class="field-input" required>
                  <option value="CASHIER">收银员</option>
                  <option value="WAREHOUSE">仓库员</option>
                  <option value="MANAGER">店长</option>
                  <option value="ASSISTANT">导购</option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">所属仓库</span>
                <select v-model="formData.wareHouseId" class="field-input" required>
                  <option value="" disabled>请选择仓库</option>
                  <option v-for="warehouse in warehouses" :key="warehouse.wareHouseId" :value="warehouse.wareHouseId">
                    {{ warehouse.wareHouseName }}
                  </option>
                </select>
              </label>
              <label class="form-group">
                <span class="field-label">状态</span>
                <select v-model="formData.employeeStatus" class="field-input">
                  <option :value="true">启用</option>
                  <option :value="false">禁用</option>
                </select>
              </label>
              <div class="form-actions">
                <button type="button" class="btn-modern" @click="closeForm">取消</button>
                <button type="submit" class="btn-modern-primary" :disabled="loading">{{ loading ? '保存中...' : '保存' }}</button>
              </div>
            </form>
          </aside>
        </transition>

        <div :class="tableAreaClass">
          <CommonTable :table-data="filteredRows" :columns="columns" primaryKey="employeeId" displayField="employeeName" @rowClick="setSelectedRow" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import CommonTable from '../Boards/CommonTable.vue'
import { useEmployeeapi } from '../utils/axios/apiComposables/useEmployeeapi'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { errorMessage, warningMessage } from '../utils/toastCenter'

const employeeApi = useEmployeeapi()
const warehouseApi = useWareHouseapi()
const employeeRows = ref([])
const warehouses = ref([])
const selectedRow = ref(null)
const keyword = ref('')
const warehouseFilter = ref('ALL')
const statusFilter = ref('ALL')
const showForm = ref(false)
const formMode = ref('create')
const loading = ref(false)

const columns = ref([
  { Id: 'employeeId', Label: 'ID' },
  { Id: 'employeeCode', Label: '员工编号' },
  { Id: 'employeeName', Label: '姓名' },
  { Id: 'phone', Label: '电话' },
  { Id: 'roleLabel', Label: '角色' },
  { Id: 'wareHouseName', Label: '仓库' },
  { Id: 'employeeStatus', Label: '状态' }
])

const roleLabels = {
  CASHIER: '收银员',
  WAREHOUSE: '仓库员',
  MANAGER: '店长',
  ASSISTANT: '导购'
}

const defaultForm = () => ({
  employeeId: '',
  employeeCode: '',
  employeeName: '',
  phone: '',
  role: 'CASHIER',
  employeeStatus: true,
  wareHouseId: warehouses.value[0]?.wareHouseId || ''
})

const formData = reactive(defaultForm())
const tableAreaClass = computed(() => showForm.value ? 'table-area-compressed' : 'table-area')
const enabledCount = computed(() => employeeRows.value.filter(item => item.employeeStatus).length)

const tableRows = computed(() => employeeRows.value.map(item => ({
  ...item,
  roleLabel: roleLabels[item.role] || item.role || '-'
})))

const filteredRows = computed(() => tableRows.value.filter(item => {
  const text = `${item.employeeCode} ${item.employeeName} ${item.phone} ${item.roleLabel} ${item.wareHouseName}`.toLowerCase()
  const keywordMatched = !keyword.value || text.includes(keyword.value.toLowerCase())
  const warehouseMatched = warehouseFilter.value === 'ALL' || item.wareHouseId === warehouseFilter.value
  const statusMatched = statusFilter.value === 'ALL' || String(item.employeeStatus) === statusFilter.value
  return keywordMatched && warehouseMatched && statusMatched
}))

const loadWarehouses = async () => {
  const response = await warehouseApi.getall()
  warehouses.value = Array.isArray(response) ? response : []
}

const loadEmployees = async () => {
  const response = await employeeApi.findAll()
  employeeRows.value = Array.isArray(response) ? response : []
}

const setSelectedRow = (row) => {
  selectedRow.value = row
}

const openCreateForm = () => {
  formMode.value = 'create'
  Object.assign(formData, defaultForm())
  showForm.value = true
}

const openEditForm = () => {
  if (!selectedRow.value) {
    warningMessage('请选择员工', '请先在表格中选择要编辑的员工')
    return
  }
  formMode.value = 'edit'
  Object.assign(formData, defaultForm(), selectedRow.value)
  showForm.value = true
}

const closeForm = () => {
  showForm.value = false
  loading.value = false
}

const buildPayload = () => ({
  employeeCode: formData.employeeCode,
  employeeName: formData.employeeName,
  phone: formData.phone,
  role: formData.role,
  employeeStatus: Boolean(formData.employeeStatus),
  wareHouseId: formData.wareHouseId
})

const submitForm = async () => {
  loading.value = true
  try {
    if (formMode.value === 'create') {
      await employeeApi.create(buildPayload())
    } else {
      await employeeApi.update(formData.employeeId, buildPayload())
    }
    closeForm()
    selectedRow.value = null
    await loadEmployees()
  } catch (error) {
    loading.value = false
  }
}

const deleteSelected = async () => {
  if (!selectedRow.value) {
    warningMessage('请选择员工', '请先在表格中选择要删除的员工')
    return
  }
  try {
    await employeeApi.datadelete(selectedRow.value.employeeId)
    selectedRow.value = null
    await loadEmployees()
  } catch (error) {
    errorMessage('删除失败', '员工删除失败，请检查是否存在关联订单')
  }
}

onMounted(async () => {
  try {
    await loadWarehouses()
    await loadEmployees()
  } catch (error) {
    errorMessage('初始化失败', '员工管理初始化数据加载失败')
  }
})
</script>

<style scoped>
.business-panel {
  padding: 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
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

.edit-panel {
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

  .edit-panel {
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

  .search-input,
  .filter-select {
    width: 100%;
  }
}
</style>
