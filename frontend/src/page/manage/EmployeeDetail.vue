<template>
    <div class="detail-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回员工</button>
                <div>
                    <h2 class="page-title">员工详情</h2>
                    <p v-if="!editing" class="page-desc">查看员工基本信息和状态</p>
                    <p v-else class="page-desc">在卡片上直接修改员工信息</p>
                </div>
            </div>
            <div v-if="employee" class="heading-actions">
                <template v-if="!editing">
                    <button class="btn-primary" @click="startEdit">编辑员工</button>
                </template>
                <template v-else>
                    <button :disabled="saving" class="btn-outline" @click="cancelEdit">取消</button>
                    <button :disabled="saving" class="btn-primary" @click="saveEdit">{{
                            saving ? '保存中...' : '保存修改'
                        }}
                    </button>
                </template>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!employee" class="empty-state">
            <div class="empty-icon"><IconGraphic name="user"/></div>
            <div class="empty-text">员工不存在</div>
        </div>
        <div v-else class="detail-grid">
            <div class="detail-card">
                <span>员工编码</span>
                <input v-if="editing" v-model="form.code" class="card-input" maxlength="10" minlength="2" type="text"/>
                <strong v-else class="code-value">{{ employee.code }}</strong>
            </div>
            <div class="detail-card">
                <span>员工姓名</span>
                <input v-if="editing" v-model="form.name" class="card-input" maxlength="10" minlength="2" type="text"/>
                <strong v-else>{{ employee.name }}</strong>
            </div>
            <div class="detail-card">
                <span>状态</span>
                <select v-if="editing" v-model="form.status" class="card-select">
                    <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{
                            item.label
                        }}
                    </option>
                </select>
                <span v-else :class="employee.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                      class="status-badge">{{
                        statusLabels[employee.status] || employee.status
                    }}</span>
            </div>
            <div v-if="editing" class="detail-card">
                <span>所属仓库</span>
                <select v-model.number="form.wareHouseId" class="card-select">
                    <option value="">请选择仓库</option>
                    <option v-for="item in warehouses" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
            </div>
            <div class="detail-card">
                <span>创建时间</span>
                <strong>{{ employee.createTime || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span>更新时间</span>
                <strong>{{ employee.updateTime || '-' }}</strong>
            </div>
        </div>
    </div>
</template>
<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import {STATUS, STATUS_LABELS, STATUS_OPTIONS} from '../../constants/status.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const statusLabels = STATUS_LABELS
const statusOptions = STATUS_OPTIONS
const employee = ref(null)
const warehouses = ref([])
const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const form = ref({})
const formBackup = ref(null)

onMounted(async () => {
    try {
        const [emp, list] = await Promise.all([
            employeeInterface.search(route.params.id),
            wareHouseInterface.searchList()
        ])
        employee.value = emp
        warehouses.value = list
    } catch {
        employee.value = null
    } finally {
        loading.value = false
    }
})

function startEdit() {
    const e = employee.value
    form.value = {code: e.code || '', name: e.name || '', status: e.status || STATUS.ENABLE, wareHouseId: ''}
    formBackup.value = {...form.value}
    editing.value = true
}

function cancelEdit() {
    form.value = {...formBackup.value}
    editing.value = false
    formBackup.value = null
}

async function saveEdit() {
    if (!form.value.code || !form.value.name || !form.value.wareHouseId) {
        toast.warning('请填写完整信息');
        return
    }
    saving.value = true
    try {
        await employeeInterface.update(employee.value.id, {
            code: form.value.code,
            name: form.value.name,
            status: form.value.status,
            wareHouseId: Number(form.value.wareHouseId)
        })
        employee.value = await employeeInterface.search(route.params.id)
        editing.value = false
        formBackup.value = null
    } catch {
    } finally {
        saving.value = false
    }
}

function goBack() {
    router.push('/manage/employee')
}
</script>

<style scoped>
.detail-page {
    width: 100%;
    min-width: 0;
}

.page-heading, .heading-left, .heading-actions {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 28px;
}

.heading-left, .heading-actions {
    gap: 12px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
    letter-spacing: -0.5px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.detail-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
}

.detail-card {
    width: calc(33.33% - 10px);
    min-width: 210px;
    min-height: 112px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 12px;
    padding: 20px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border: 1px solid #e3efed;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.detail-card > span:first-child {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.code-value {
    color: var(--primary);
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

.card-input {
    width: 100%;
    height: 38px;
    padding: 0 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fff;
    font-size: 14px;
    outline: none;
}

.card-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.card-select {
    width: 100%;
    height: 38px;
    padding: 0 24px 0 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fff;
    font-size: 14px;
    appearance: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364807e' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 8px center;
}

.card-select:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}
</style>
