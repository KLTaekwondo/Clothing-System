<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">基础设置</div>
        <h1 class="page-title-modern">新增仓库</h1>
        <p class="page-subtitle-modern">创建新的仓库资料，后续可在仓库管理中维护状态、员工和库存关联。</p>
      </div>
      <div class="action-row">
        <router-link class="btn-modern-soft" to="/dashboard/warehouses">返回仓库管理</router-link>
      </div>
    </div>

    <section class="warehouse-form-shell">
      <form class="warehouse-form" @submit.prevent="submitWarehouse">
        <section class="form-section">
          <div class="section-header">
            <span class="section-index">01</span>
            <div class="section-copy">
              <h2 class="section-title">仓库信息</h2>
              <p class="section-desc">填写仓库编码、名称和启用状态，用于库存、员工和销售模块统一引用。</p>
            </div>
          </div>

          <div class="field-row">
            <label class="field-group">
              <span class="field-label">仓库ID</span>
              <input v-model.trim="formData.wareHouseId" class="field-input" required placeholder="例如：WH-001" />
            </label>
            <label class="field-group">
              <span class="field-label">仓库名称</span>
              <input v-model.trim="formData.wareHouseName" class="field-input" required placeholder="例如：总仓" />
            </label>
          </div>

          <div class="field-row">
            <label class="field-group">
              <span class="field-label">仓库状态</span>
              <select v-model="formData.wareHouseStatus" class="field-input">
                <option :value="true">启用</option>
                <option :value="false">禁用</option>
              </select>
            </label>
          </div>

          <div class="helper-card">
            <strong>提示</strong>
            <span>仓库ID创建后会作为库存、员工和销售数据的关联标识，请尽量使用稳定且易识别的编码。</span>
          </div>
        </section>

        <div class="submit-bar">
          <button type="button" class="btn-modern" @click="resetForm">重置</button>
          <button type="submit" class="btn-modern-primary" :disabled="loading">{{ loading ? '创建中...' : '创建仓库' }}</button>
        </div>
      </form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useWareHouseapi } from '../utils/axios/apiComposables/useWareHouseapi'
import { warningMessage } from '../utils/toastCenter'

const router = useRouter()
const warehouseApi = useWareHouseapi()
const loading = ref(false)

const defaultForm = () => ({
  wareHouseId: '',
  wareHouseName: '',
  wareHouseStatus: true
})

const formData = reactive(defaultForm())

const resetForm = () => {
  Object.assign(formData, defaultForm())
}

const normalizeStatus = (status) => status === true || status === 'true'

const validateForm = () => {
  if (!formData.wareHouseId.trim() || !formData.wareHouseName.trim()) {
    warningMessage('信息不完整', '请填写仓库ID和仓库名称')
    return false
  }
  return true
}

const buildPayload = () => ({
  wareHouseId: formData.wareHouseId.trim(),
  wareHouseName: formData.wareHouseName.trim(),
  wareHouseStatus: normalizeStatus(formData.wareHouseStatus)
})

const submitWarehouse = async () => {
  if (!validateForm()) return
  loading.value = true
  try {
    await warehouseApi.create(buildPayload())
    await router.push('/dashboard/warehouses')
  } catch (error) {
    loading.value = false
  }
}
</script>

<style scoped>
.warehouse-form-shell {
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: var(--radius-xl);
  background: var(--surface-glass);
  box-shadow: var(--shadow-sm);
}

.warehouse-form {
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

.helper-card {
  display: flex;
  flex-direction: column;
  gap: 5px;
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

.submit-bar {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 760px) {
  .field-row {
    flex-direction: column;
  }

  .field-group {
    width: 100%;
  }
}
</style>
