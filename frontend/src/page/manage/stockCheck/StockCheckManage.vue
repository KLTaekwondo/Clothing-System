<template>
    <div class="stock-check-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">库存盘点</h2>
                <p class="page-label-desc">记录仓库实际库存，审批后按盘点结果更新系统库存</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>盘点单检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <StatusFilterToolbar
                    v-model="statusFilter"
                    v-model:search="searchQuery"
                    :tabs="statusTabs"
                    search-placeholder="搜索盘点单号或仓库"
                />
                <router-link
                    class="btn-primary search-button"
                    to="/manage/stock-check/add"
                >+ 新建盘点单</router-link>
            </div>
        </div>
            </div>
        </div>

        <div class="stats-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="document"/></span>
                <span class="stats-body">
                    <strong>{{ pageInfo.totalElements }}</strong>
                    <span>全部盘点单</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="clock"/></span>
                <span class="stats-body">
                    <strong>{{ draftCount }}</strong>
                    <span>当前页草稿</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="hourglass"/></span>
                <span class="stats-body">
                    <strong>{{ checkingCount }}</strong>
                    <span>当前页审核中</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="warning"/></span>
                <span class="stats-body">
                    <strong>{{ activeCount }}</strong>
                    <span>当前页进行中</span>
                </span>
            </div>
        </div>

        <div class="check-table-card">
            <div
                v-if="loading"
                class="loading-overlay"
            >
                <div class="loading-spinner"></div>
            </div>
            <div
                v-else-if="filteredChecks.length === 0"
                class="empty-state"
            >
                <div class="empty-icon"><IconGraphic name="document"/></div>
                <div class="empty-text">暂无库存盘点单</div>
            </div>
            <table
                v-else
                class="data-table"
            >
                <thead>
                <tr>
                    <th>盘点单号</th>
                    <th>仓库</th>
                    <th>状态</th>
                    <th>备注</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="item in filteredChecks"
                    :key="item.id"
                >
                    <td><code>{{ item.stockCheckNo }}</code></td>
                    <td>
                        <strong>{{ item.wareHouseName || '-' }}</strong>
                        <span class="sub-text">{{ item.wareHouseCode || '-' }}</span>
                    </td>
                    <td>
                        <span
                            :class="statusClass(item.status)"
                            class="status-badge"
                        >{{ statusLabels[item.status] || item.status || '-' }}</span>
                    </td>
                    <td class="remark-cell">{{ item.remark || '-' }}</td>
                    <td class="time-cell">{{ item.createTime || '-' }}</td>
                    <td>
                        <div class="actions">
                            <router-link
                                :to="`/manage/stock-check/${item.id}`"
                                class="btn-outline"
                            >详情</router-link>
                            <router-link
                                v-if="item.status === AUDIT_STATUS.DRAFT"
                                :to="`/manage/stock-check/${item.id}/edit`"
                                class="btn-outline"
                            >编辑</router-link>
                            <button
                                v-if="item.status === AUDIT_STATUS.DRAFT"
                                class="btn-primary"
                                @click="handleCheck(item)"
                            >提交</button>
                            <button
                                v-if="item.status === AUDIT_STATUS.DRAFT"
                                class="btn-danger"
                                @click="confirmDelete(item)"
                            >删除</button>
                            <button
                                v-if="item.status === AUDIT_STATUS.CHECKING"
                                class="btn-primary"
                                @click="handleApprove(item)"
                            >通过</button>
                            <button
                                v-if="item.status === AUDIT_STATUS.CHECKING"
                                class="btn-danger"
                                @click="handleReject(item)"
                            >拒绝</button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>

            <TablePagination
                :loading="loading"
                :page="pageInfo.page"
                :total-elements="pageInfo.totalElements"
                :total-pages="totalPages"
                @change="changePage"
            />
        </div>

        <DeleteConfirmDialog
            :loading="deleting"
            :title="`确定删除盘点单“${deleteTarget?.stockCheckNo || ''}”吗？`"
            :visible="showDeleteConfirm"
            hint="仅草稿状态的盘点单可以删除"
            @cancel="closeDeleteConfirm"
            @confirm="handleDelete"
        />
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useToastStore} from '../../../stores/toastStore.js'
import {useConfirmStore} from '../../../stores/confirmStore.js'
import stockCheckInterface from '../../../axios/interface/StockCheckInterface.js'
import DeleteConfirmDialog from '../../../component/DeleteConfirmDialog.vue'
import StatusFilterToolbar from '../../../component/common/StatusFilterToolbar.vue'
import TablePagination from '../../../component/common/TablePagination.vue'
import {AUDIT_STATUS, AUDIT_STATUS_LABELS, AUDIT_STATUS_TABS} from '../../../constants/auditStatus.js'

const toast = useToastStore()
const confirmStore = useConfirmStore()
const statusLabels = AUDIT_STATUS_LABELS
const statusTabs = AUDIT_STATUS_TABS
const checkList = ref([])
const loading = ref(true)
const statusFilter = ref('')
const searchQuery = ref('')
const showDeleteConfirm = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
const pageInfo = ref({
    totalElements: 0,
    totalPages: 0,
    page: 0,
    size: 10
})

const totalPages = computed(() => Math.max(pageInfo.value.totalPages || 1, 1))
const draftCount = computed(() => checkList.value.filter(item => item.status === AUDIT_STATUS.DRAFT).length)
const checkingCount = computed(() => checkList.value.filter(item => item.status === AUDIT_STATUS.CHECKING).length)
const activeCount = computed(() => draftCount.value + checkingCount.value)

const filteredChecks = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    return checkList.value.filter(item => {
        const matchesStatus = !statusFilter.value || item.status === statusFilter.value
        const matchesQuery = !query || [
            item.stockCheckNo,
            item.wareHouseName,
            item.wareHouseCode
        ].some(value => String(value || '').toLowerCase().includes(query))
        return matchesStatus && matchesQuery
    })
})

onMounted(fetchList)

async function fetchList() {
    loading.value = true
    try {
        const data = await stockCheckInterface.searchPage(pageInfo.value.page, pageInfo.value.size)
        checkList.value = data.content || []
        pageInfo.value = {
            totalElements: data.totalElements || 0,
            totalPages: data.totalPages || 0,
            page: data.page || 0,
            size: data.size || pageInfo.value.size
        }
    } catch {
        checkList.value = []
    } finally {
        loading.value = false
    }
}

function changePage(page) {
    pageInfo.value.page = page
    fetchList()
}

function statusClass(status) {
    if (status === AUDIT_STATUS.DRAFT) return 'status-pending'
    if (status === AUDIT_STATUS.CHECKING) return 'status-warn'
    if (status === AUDIT_STATUS.APPROVED) return 'status-ok'
    if (status === AUDIT_STATUS.REJECTED) return 'status-error'
    return ''
}

async function handleCheck(item) {
    try {
        await stockCheckInterface.check(item.id)
        toast.success('盘点单已提交，等待审核')
        await fetchList()
    } catch {}
}

async function handleApprove(item) {
    const confirmed = await confirmStore.confirm({
        title: '通过库存盘点单？',
        message: `盘点单“${item.stockCheckNo}”通过后，将按实际数量更新仓库库存。`,
        confirmText: '确认通过'
    })
    if (!confirmed) return
    try {
        await stockCheckInterface.approve(item.id)
        toast.success('盘点单已审核通过，库存已更新')
        await fetchList()
    } catch {}
}

async function handleReject(item) {
    try {
        await stockCheckInterface.reject(item.id)
        toast.success('已拒绝该盘点单')
        await fetchList()
    } catch {}
}

function confirmDelete(item) {
    deleteTarget.value = item
    showDeleteConfirm.value = true
}

function closeDeleteConfirm() {
    showDeleteConfirm.value = false
    deleteTarget.value = null
}

async function handleDelete() {
    if (!deleteTarget.value) return
    deleting.value = true
    try {
        await stockCheckInterface.hardDelete(deleteTarget.value.id)
        toast.success('盘点单已删除')
        closeDeleteConfirm()
        if (checkList.value.length === 1 && pageInfo.value.page > 0) {
            pageInfo.value.page--
        }
        await fetchList()
    } catch {} finally {
        deleting.value = false
    }
}
</script>

<style scoped>
.stock-check-manage {
    width: 100%;
    min-width: 0;
}

/* 统计使用全局 stats-summary */

.check-table-card {
    padding: 8px 20px 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: var(--shadow);
    overflow-x: auto;
}

.check-table-card .data-table {
    min-width: 980px;
}

.sub-text {
    display: block;
    margin-top: 4px;
    color: var(--text-muted);
    font-size: 12px;
}

.remark-cell {
    max-width: 240px;
    color: var(--text-secondary);
}

.time-cell {
    color: var(--text-muted);
    font-size: 13px;
}

.actions {
    display: flex;
    gap: 6px;
    flex-wrap: nowrap;
}

.actions .btn-outline,
.actions .btn-primary,
.actions .btn-danger {
    padding: 4px 10px;
    font-size: 12px;
}
</style>
