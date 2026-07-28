<template>
    <div class="employee-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">员工管理</h2>
                <p class="page-desc">管理员工信息、分配仓库和查看状态</p>
            </div>
            <router-link class="btn-primary" to="/manage/employee/add">+ 添加员工</router-link>
        </div>
        <div class="card">
            <div class="card-header">
                <span class="card-title">员工列表</span>
                <div class="header-actions">
                    <div class="search-bar">
                        <input
                            v-model="searchQuery"
                            placeholder="搜索员工姓名..."
                            type="text"
                            @input="filterList"
                        />
                    </div>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="filteredList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="user"/></div>
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
                <tr v-for="item in filteredList" :key="item.id" style="cursor:pointer" @dblclick="goDetail(item)">
                    <td><code>{{ item.code }}</code></td>
                    <td><strong>{{ item.name }}</strong></td>
                    <td>
                            <span :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                                  class="status-badge">
                                {{ statusLabels[item.status] || item.status || '-' }}
                            </span>
                    </td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/employee/${item.id}`" class="btn-outline btn-sm">详情/编辑
                            </router-link>
                            <button class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <!-- 删除确认 -->
        <div v-if="showDelete" class="modal-overlay" @click.self="showDelete = false">
            <div class="modal-content" style="min-width: 360px;">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon"><IconGraphic name="warning"/></div>
                        <div class="confirm-msg">确定要删除"{{ deleteTarget?.name }}"吗？</div>
                        <div class="confirm-hint">此操作不可恢复</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn-outline" @click="showDelete = false">取消</button>
                    <button :disabled="deleting" class="btn-danger" @click="handleDelete">
                        {{ deleting ? '删除中...' : '确认删除' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import {STATUS, STATUS_LABELS} from '../../constants/status.js'

const router = useRouter()
const toast = useToastStore()
const statusLabels = STATUS_LABELS

const employeeList = ref([])
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

onMounted(fetchList)

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

function filterList() {
}

function goDetail(item) {
    router.push(`/manage/employee/${item.id}`)
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
        // 后端已返回提示
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
    width: 100%;
    min-width: 0;
}

.page-heading {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 24px;
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

.employee-manage > .card {
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
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

.status-error {
    background: var(--error-light);
    color: #dc2626;
}

@media (max-width: 760px) {
    .header-actions {
        align-items: stretch;
        flex-direction: column;
    }

    .header-actions .search-bar,
    .header-actions .search-bar input {
        width: 100%;
    }
}
</style>
