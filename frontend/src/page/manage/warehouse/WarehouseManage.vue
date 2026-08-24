<template>
    <div class="warehouse-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">仓库管理</h2>
                <p class="page-label-desc">管理仓库信息、查看和调拨库存</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>仓库检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <input
                    v-model="searchQuery"
                    class="search-code-input"
                    placeholder="搜索仓库名称或编码"
                    type="text"
                />
                <router-link
                    class="btn-primary search-button"
                    to="/manage/warehouse/add"
                >
                    + 添加仓库
                </router-link>
            </div>
        </div>
            </div>
        </div>

        <div class="card">
            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="filteredList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="warehouse"/></div>
                <div class="empty-text">暂无仓库数据</div>
            </div>

            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>编码</th>
                    <th>仓库名称</th>
                    <th>状态</th>
                    <th>盘点状态</th>
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
                        <span
                            :class="item.checkStatus === CHECK_STATUS.UNDER_CHECK ? 'status-warn' : 'status-ok'"
                            class="status-badge"
                        >
                            {{ checkStatusLabels[item.checkStatus] || item.checkStatus || '-' }}
                        </span>
                    </td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/warehouse/${item.id}`" class="btn-outline btn-sm">详情/编辑
                            </router-link>
                            <router-link :to="`/manage/warehouse/${item.id}/stock`" class="btn-outline btn-sm">库存
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
import {useToastStore} from '../../../stores/toastStore.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import {STATUS, STATUS_LABELS} from '../../../constants/status.js'
import {CHECK_STATUS, CHECK_STATUS_LABELS} from '../../../constants/checkStatus.js'

const router = useRouter()
const toast = useToastStore()
const statusLabels = STATUS_LABELS
const checkStatusLabels = CHECK_STATUS_LABELS

const warehouseList = ref([])
const searchQuery = ref('')
const loading = ref(true)

const filteredList = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    if (!query) return warehouseList.value
    return warehouseList.value.filter(item =>
        item.name?.toLowerCase().includes(query) ||
        item.code?.toLowerCase().includes(query) ||
        String(item.id).includes(query)
    )
})

onMounted(() => fetchList())

async function fetchList() {
    loading.value = true
    try {
        warehouseList.value = await wareHouseInterface.searchList()
    } catch {
        warehouseList.value = []
    } finally {
        loading.value = false
    }
}

// 删除
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

function goDetail(item) {
    router.push(`/manage/warehouse/${item.id}`)
}

function confirmDelete(item) {
    deleteTarget.value = item
    showDelete.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await wareHouseInterface.softDelete(deleteTarget.value.id)
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
.warehouse-manage {
    width: 100%;
    min-width: 0;
}

.warehouse-manage > .card {
    border-radius: 16px;
    box-shadow: var(--shadow);
}

.data-table th,
.data-table td {
    text-align: center;
}

.data-table .actions {
    justify-content: center;
}

@media (max-width: 760px) {
    .warehouse-manage > .card {
        overflow-x: auto;
    }

    .data-table {
        min-width: 760px;
    }
}
</style>
