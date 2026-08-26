<template>
    <div class="detail-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">员工详情</h2>
                <p class="page-label-desc">{{ editing ? '在卡片上直接修改员工信息' : '查看员工基本信息和状态' }}</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            @click="goBack"
                        >← 返回员工</button>
                        <template v-if="employee">
                            <template v-if="!editing">
                                <button
                                    class="btn-primary search-button"
                                    @click="startEdit"
                                >编辑员工</button>
                            </template>
                            <template v-else>
                                <button
                                    :disabled="saving"
                                    class="btn-outline search-button"
                                    @click="cancelEdit"
                                >取消</button>
                                <button
                                    :disabled="saving"
                                    class="btn-primary search-button"
                                    @click="saveEdit"
                                >{{ saving ? '保存中...' : '保存修改' }}</button>
                            </template>
                        </template>
                    </div>
                </div>
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
            <div class="detail-card">
                <span>所属仓库</span>
                <select v-if="editing" v-model.number="form.wareHouseId" class="card-select">
                    <option value="">请选择仓库</option>
                    <option v-for="item in warehouses" :key="item.id" :value="item.id">{{ item.name }}</option>
                </select>
                <div v-else class="warehouse-value">
                    <strong>{{ employee.wareHouseName || '-' }}</strong>
                    <span class="sub-text"><code>{{ employee.wareHouseCode || '' }}</code></span>
                </div>
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
import {useToastStore} from '../../../stores/toastStore.js'
import employeeInterface from '../../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import {STATUS, STATUS_LABELS, STATUS_OPTIONS} from '../../../constants/status.js'

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





.detail-card > span:first-child {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.warehouse-value {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.sub-text {
    font-size: 12px;
    color: var(--text-muted);
}










</style>
