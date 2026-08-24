<template>
    <div class="import-order-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">采购订单</h2>
                <p class="page-label-desc">管理采购入库和采购退货单据</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>采购单检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <StatusFilterToolbar
                    v-model="statusFilter"
                    v-model:search="searchQuery"
                    :tabs="statusTabs"
                    search-placeholder="搜索单号或供应商"
                />
                <router-link
                    class="btn-primary search-button"
                    to="/manage/import-order/add"
                >+ 新建采购单</router-link>
            </div>
        </div>
            </div>
        </div>

        <div class="order-stats">
            <div class="order-stat-card">
                <span class="order-stat-icon"><IconGraphic name="order"/></span>
                <span class="stats-body">
<span class="order-stat-value">{{ pageInfo.totalElements }}</span>

                <span class="order-stat-label">全部订单</span>
</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon draft-icon"><IconGraphic name="clock"/></span>
                <span class="stats-body">
<span class="order-stat-value">{{ draftCount }}</span>

                <span class="order-stat-label">当前页草稿</span>
</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon checking-icon"><IconGraphic name="hourglass"/></span>
                <span class="stats-body">
<span class="order-stat-value">{{ checkingCount }}</span>

                <span class="order-stat-label">当前页审核中</span>
</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon amount-icon">¥</span>
                <span class="stats-body">
<span class="order-stat-value">¥{{ totalAmount.toFixed(2) }}</span>

                <span class="order-stat-label">当前页金额</span>
</span>
            </div>
        </div>

        <div class="order-table-card">
            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="filteredOrders.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="order"/></div>
                <div class="empty-text">暂无采购订单</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>订单编号</th>
                    <th>供应商</th>
                    <th>仓库</th>
                    <th>方向</th>
                    <th>总金额</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in filteredOrders" :key="item.id">
                    <td><code>{{ item.importOrderNo }}</code></td>
                    <td>{{ item.supplierName || '-' }}</td>
                    <td>{{ item.wareHouseName || '-' }}</td>
                    <td>{{ directionLabels[item.direction] || item.direction || '-' }}</td>
                    <td class="price-cell">¥{{ item.totalAmount ?? '0.00' }}</td>
                    <td>
                        <span :class="statusClass(item.status)" class="status-badge">
                            {{ statusLabels[item.status] || item.status || '-' }}
                        </span>
                    </td>
                    <td class="time-cell">{{ item.createTime || '-' }}</td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/import-order/${item.id}`" class="btn-outline btn-sm">详情</router-link>
                            <router-link
                                v-if="item.status === AUDIT_STATUS.DRAFT"
                                :to="`/manage/import-order/${item.id}/edit`"
                                class="btn-outline btn-sm"
                            >编辑</router-link>
                            <button v-if="item.status === AUDIT_STATUS.DRAFT" class="btn-primary btn-sm" @click="handleCheck(item)">提交</button>
                            <button v-if="item.status === AUDIT_STATUS.DRAFT" class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                            <button v-if="item.status === AUDIT_STATUS.CHECKING" class="btn-primary btn-sm" @click="handleApprove(item)">通过</button>
                            <button v-if="item.status === AUDIT_STATUS.CHECKING" class="btn-danger btn-sm" @click="handleReject(item)">拒绝</button>
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
            :title="`确定要删除采购单“${deleteTarget?.importOrderNo || ''}”吗？`"
            :visible="showDeleteConfirm"
            hint="仅草稿状态的订单可以删除"
            @cancel="showDeleteConfirm = false"
            @confirm="handleDelete"
        />
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import {useConfirmStore} from '../../../stores/confirmStore.js'
import importOrderInterface from '../../../axios/interface/ImportOrderInterface.js'
import DeleteConfirmDialog from '../../../component/DeleteConfirmDialog.vue'
import StatusFilterToolbar from '../../../component/common/StatusFilterToolbar.vue'
import TablePagination from '../../../component/common/TablePagination.vue'
import {AUDIT_STATUS, AUDIT_STATUS_LABELS, AUDIT_STATUS_TABS, DIRECTION_LABELS} from '../../../constants/auditStatus.js'

const router = useRouter()
const toast = useToastStore()
const confirmStore = useConfirmStore()
const statusLabels = AUDIT_STATUS_LABELS
const directionLabels = DIRECTION_LABELS
const statusTabs = AUDIT_STATUS_TABS

const orderList = ref([])
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

const filteredOrders = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    return orderList.value.filter(item => {
        const matchesStatus = !statusFilter.value || item.status === statusFilter.value
        const matchesQuery = !query || [item.importOrderNo, item.supplierName]
            .some(value => String(value || '').toLowerCase().includes(query))
        return matchesStatus && matchesQuery
    })
})

const draftCount = computed(() => orderList.value.filter(item => item.status === AUDIT_STATUS.DRAFT).length)
const checkingCount = computed(() => orderList.value.filter(item => item.status === AUDIT_STATUS.CHECKING).length)
const totalAmount = computed(() => orderList.value.reduce((sum, item) => sum + Number(item.totalAmount || 0), 0))

onMounted(fetchList)

async function fetchList() {
    loading.value = true
    try {
        const data = await importOrderInterface.searchPage(pageInfo.value.page, pageInfo.value.size)
        orderList.value = data.content || []
        pageInfo.value = {
            totalElements: data.totalElements || 0,
            totalPages: data.totalPages || 0,
            page: data.page || 0,
            size: data.size || pageInfo.value.size
        }
    } catch {
        orderList.value = []
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
        await importOrderInterface.check(item.id)
        toast.success('订单已提交，等待审核')
        await fetchList()
    } catch {}
}

async function handleApprove(item) {
    const confirmed = await confirmStore.confirm({
        title: '通过采购单？',
        message: `采购单“${item.importOrderNo}”通过后将变更库存。`,
        confirmText: '确认通过'
    })
    if (!confirmed) return
    try {
        await importOrderInterface.approve(item.id)
        toast.success('采购单已审核通过')
        await fetchList()
    } catch {}
}

async function handleReject(item) {
    try {
        await importOrderInterface.reject(item.id)
        toast.success('已拒绝该采购单')
        await fetchList()
    } catch {}
}

function confirmDelete(item) {
    deleteTarget.value = item
    showDeleteConfirm.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await importOrderInterface.hardDelete(deleteTarget.value.id)
        toast.success('采购单已删除')
        showDeleteConfirm.value = false
        await fetchList()
    } catch {} finally {
        deleting.value = false
    }
}
</script>

<style scoped>
.import-order-manage {
    width: 100%;
    min-width: 0;
}

.order-stats {
    display: flex;
    align-items: stretch;
    margin-bottom: 18px;
    border: 1px solid var(--border);
    border-radius: var(--radius-xl);
    background: var(--bg-card);
    box-shadow: var(--shadow);
    overflow: hidden;
}

.order-stat-card {
    width: 25%;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px 20px;
    background: var(--bg-card);
    border-right: 1px solid var(--border-light);
    transition: background 0.2s;
}

.order-stat-card:last-child {
    border-right: none;
    background: linear-gradient(145deg, var(--bg-card), var(--bg-subtle));
}

.order-stat-card:last-child .order-stat-value {
    color: var(--primary-dark);
    font-size: 26px;
}

.order-stat-card:hover {
    background: var(--bg-hover);
}

.order-stat-card:last-child:hover {
    background: linear-gradient(145deg, var(--bg-hover), var(--bg-subtle));
}

.order-stat-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: var(--primary-light);
    color: var(--primary-dark);
    font-size: 16px;
    font-weight: 800;
}

.completed-icon {
    background: var(--success-light);
    color: var(--success-dark);
}

.draft-icon {
    background: var(--warning-light);
    color: var(--warning-dark);
}

.checking-icon {
    background: var(--info-light);
    color: var(--info-dark);
}

.amount-icon {
    background: var(--primary-light);
    color: var(--primary-dark);
    font-size: 15px;
}

.order-stat-value {
    color: var(--text);
    font-size: 22px;
    font-weight: 800;
    line-height: 1.15;
    font-family: var(--mono);
    font-variant-numeric: tabular-nums;
    white-space: nowrap;
}

.order-stat-label {
    margin-top: 3px;
    color: var(--text-muted);
    font-size: 12px;
    white-space: nowrap;
}

.order-table-card {
    padding: 8px 20px 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: var(--shadow);
    overflow-x: auto;
}

.order-table-card .data-table {
    min-width: 980px;
}

.data-table td {
    height: 56px;
}

.price-cell {
    color: var(--primary);
    font-weight: 700;
    font-family: var(--mono);
    font-variant-numeric: tabular-nums;
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

.actions .btn-sm {
    font-size: 12px;
    padding: 4px 10px;
}

@media (max-width: 900px) {
    .order-stats {
        flex-wrap: wrap;
    }

    .order-stat-card {
        width: 50%;
        border-right: none;
    }

    .order-stat-card:nth-child(odd) {
        border-right: 1px solid var(--border-light);
    }

    .order-stat-card:nth-child(-n+2) {
        border-bottom: 1px solid var(--border-light);
    }

    .order-stat-card:nth-child(n+3) {
        border-bottom: none;
    }
}

@media (max-width: 560px) {
    .order-stat-card {
        width: 100%;
        border-right: none;
    }

    .order-stat-card:nth-child(-n+3) {
        border-bottom: 1px solid var(--border-light);
    }

    .order-stat-card:last-child {
        border-bottom: none;
    }
}
</style>
