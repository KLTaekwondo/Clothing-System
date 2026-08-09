<template>
    <div class="stock-check-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">库存盘点</h2>
                <p class="page-desc">记录仓库实际库存，审批后按盘点结果更新系统库存</p>
            </div>
            <router-link
                class="btn-primary"
                to="/manage/stock-check/add"
            >+ 新建盘点单</router-link>
        </div>

        <div class="check-stats">
            <div class="check-stat-card">
                <span class="stat-icon"><IconGraphic name="document"/></span>
                <span class="stat-value">{{ pageInfo.totalElements }}</span>
                <span class="stat-label">全部盘点单</span>
            </div>
            <div class="check-stat-card">
                <span class="stat-icon"><IconGraphic name="clock"/></span>
                <span class="stat-value">{{ draftCount }}</span>
                <span class="stat-label">当前页草稿</span>
            </div>
            <div class="check-stat-card">
                <span class="stat-icon"><IconGraphic name="hourglass"/></span>
                <span class="stat-value">{{ checkingCount }}</span>
                <span class="stat-label">当前页审核中</span>
            </div>
            <div class="check-stat-card">
                <span class="stat-icon"><IconGraphic name="warning"/></span>
                <span class="stat-value">{{ activeCount }}</span>
                <span class="stat-label">当前页进行中</span>
            </div>
        </div>

        <div class="check-table-card">
            <StatusFilterToolbar
                v-model="statusFilter"
                v-model:search="searchQuery"
                :tabs="statusTabs"
                search-placeholder="搜索盘点单号或仓库"
            />

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
import StatusFilterToolbar from '../common/StatusFilterToolbar.vue'
import TablePagination from '../common/TablePagination.vue'
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

.page-heading {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: 26px;
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

.check-stats {
    display: flex;
    gap: 14px;
    margin-bottom: 18px;
    flex-wrap: wrap;
}

.check-stat-card {
    width: calc(25% - 11px);
    min-width: 180px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 17px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border: 1px solid #e3efed;
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.stat-icon {
    width: 38px;
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 11px;
    background: #e8f7f3;
}

.stat-icon :deep(img) {
    width: 22px;
    height: 22px;
}

.stat-value {
    color: var(--text);
    font-size: 20px;
    font-weight: 800;
}

.stat-label {
    margin-left: -4px;
    color: var(--text-muted);
    font-size: 12px;
}

.check-table-card {
    padding: 8px 20px 20px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
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

@media (max-width: 900px) {
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
    }

    .check-stat-card {
        width: calc(50% - 7px);
    }
}
</style>
