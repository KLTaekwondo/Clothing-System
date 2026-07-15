<template>
    <div class="employee-manage">
        <div class="card">
            <div class="card-header">
                <span class="card-title">员工列表</span>
                <div class="header-actions">
                    <div class="search-bar">
                        <input
                            v-model="searchQuery"
                            type="text"
                            placeholder="搜索员工姓名..."
                            @input="filterList"
                        />
                    </div>
                    <button class="btn-primary" @click="openAdd">+ 添加员工</button>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="filteredList.length === 0" class="empty-state">
                <div class="empty-icon">👤</div>
                <div class="empty-text">暂无员工数据</div>
            </div>

            <table v-else class="data-table">
                <thead>
                    <tr>
                        <th>编码</th>
                        <th>姓名</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in filteredList" :key="item.id">
                        <td><code>{{ item.code }}</code></td>
                        <td><strong>{{ item.name }}</strong></td>
                        <td>
                            <span class="status-badge" :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'">
                                {{ statusLabels[item.status] || item.status || '-' }}
                            </span>
                        </td>
                        <td>
                            <div class="actions">
                                <button class="btn-outline btn-sm" @click="openEdit(item)">编辑</button>
                                <button class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- 新增/编辑弹窗 -->
        <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
            <div class="modal-content">
                <div class="modal-header">
                    <span class="modal-title">{{ isEditing ? '编辑员工' : '添加员工' }}</span>
                    <button class="modal-close" @click="closeForm">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleSubmit">
                    <div class="form-row">
                        <div class="form-group">
                            <label>员工编码</label>
                            <input v-model="form.code" type="text" minlength="2" maxlength="10" placeholder="请输入员工编码" required />
                        </div>
                        <div class="form-group">
                            <label>员工姓名</label>
                            <input v-model="form.name" type="text" minlength="2" maxlength="10" placeholder="请输入员工姓名" required />
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>所属仓库</label>
                            <select v-model.number="form.wareHouseId" required>
                                <option value="" disabled>请选择仓库</option>
                                <option v-for="warehouse in warehouses" :key="warehouse.id" :value="warehouse.id">
                                    {{ warehouse.name }}
                                </option>
                            </select>
                        </div>
                        <div v-if="isEditing" class="form-group">
                            <label>状态</label>
                            <select v-model="form.status" required>
                                <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                            </select>
                        </div>
                    </div>
                </form>
                <div class="modal-footer">
                    <button type="button" class="btn-outline" @click="closeForm">取消</button>
                    <button type="button" class="btn-primary" @click="handleSubmit" :disabled="submitting">
                        {{ submitting ? '提交中...' : '确认' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 删除确认 -->
        <div v-if="showDelete" class="modal-overlay" @click.self="showDelete = false">
            <div class="modal-content" style="min-width: 360px;">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon">⚠️</div>
                        <div class="confirm-msg">确定要删除"{{ deleteTarget?.name }}"吗？</div>
                        <div class="confirm-hint">此操作不可恢复</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn-outline" @click="showDelete = false">取消</button>
                    <button class="btn-danger" @click="handleDelete" :disabled="deleting">
                        {{ deleting ? '删除中...' : '确认删除' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToastStore } from '../../stores/toastStore.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import { STATUS, STATUS_LABELS, STATUS_OPTIONS } from '../../constants/status.js'

const toast = useToastStore()
const statusLabels = STATUS_LABELS
const statusOptions = STATUS_OPTIONS

const employeeList = ref([])
const warehouses = ref([])
const searchQuery = ref('')
const loading = ref(true)

const filteredList = computed(() => {
    if (!searchQuery.value) return employeeList.value
    const q = searchQuery.value.toLowerCase()
    return employeeList.value.filter(e =>
        e.name?.toLowerCase().includes(q) ||
        e.code?.toLowerCase().includes(q) ||
        String(e.id).includes(q)
    )
})

onMounted(async () => {
    await Promise.all([fetchList(), fetchWarehouses()])
})

async function fetchWarehouses() {
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    }
}

async function fetchList() {
    loading.value = true
    try {
        employeeList.value = await employeeInterface.searchList()
    } catch {
        employeeList.value = []
    } finally {
        loading.value = false
    }
}

function filterList() {}

// 表单
const showForm = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const form = ref({
    code: '',
    name: '',
    wareHouseId: '',
    status: STATUS.ENABLE
})

function openAdd() {
    isEditing.value = false
    form.value = { code: '', name: '', wareHouseId: '', status: STATUS.ENABLE }
    showForm.value = true
}

function openEdit(item) {
    isEditing.value = true
    form.value = {
        id: item.id,
        code: item.code || '',
        name: item.name || '',
        wareHouseId: item.wareHouseId || '',
        status: item.status || STATUS.ENABLE
    }
    showForm.value = true
}

function closeForm() {
    showForm.value = false
}

async function handleSubmit() {
    if (!form.value.code || !form.value.name || !form.value.wareHouseId) return
    submitting.value = true
    try {
        const payload = {
            code: form.value.code,
            name: form.value.name,
            wareHouseId: Number(form.value.wareHouseId)
        }
        if (isEditing.value) {
            payload.status = form.value.status
        }
        if (isEditing.value) {
            await employeeInterface.update(form.value.id, payload)
            toast.success('员工信息更新成功')
        } else {
            await employeeInterface.create(payload)
            toast.success('员工添加成功')
        }
        closeForm()
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 核验
async function handleVerify(item) {
    try {
        await employeeInterface.verify(item.id)
        toast.success('员工核验成功')
        await fetchList()
    } catch {
        // 拦截器已处理
    }
}

// 删除
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

function confirmDelete(item) {
    deleteTarget.value = item
    showDelete.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await employeeInterface.softDelete(deleteTarget.value.id)
        toast.success('员工已删除')
        showDelete.value = false
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        deleting.value = false
    }
}
</script>

<style scoped>
.employee-manage {
    max-width: 1100px;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.status-badge {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 12px;
    font-size: var(--font-sm);
    font-weight: 500;
}

.status-ok {
    background: var(--success-light);
    color: #16a34a;
}

.status-pending {
    background: var(--warning-light);
    color: #d97706;
}
</style>
