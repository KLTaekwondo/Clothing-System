<template>
    <div class="transfer-order-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">调拨订单</h2>
                <p class="page-desc">管理库存调拨单据，审核后自动扣减源仓库并增加目标仓库</p>
            </div>
            <router-link class="btn-primary" to="/manage/stock/transfer">+ 新建调拨单</router-link>
        </div>

        <div class="order-stats">
            <div class="order-stat-card">
                <span class="order-stat-icon"><IconGraphic name="order"/></span>
                <span class="order-stat-value">{{ pageInfo.totalElements }}</span>
                <span class="order-stat-label">全部订单</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon draft-icon"><IconGraphic name="clock"/></span>
                <span class="order-stat-value">{{ draftCount }}</span>
                <span class="order-stat-label">当前页草稿</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon checking-icon"><IconGraphic name="hourglass"/></span>
                <span class="order-stat-value">{{ checkingCount }}</span>
                <span class="order-stat-label">当前页审核中</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon amount-icon">¥</span>
                <span class="order-stat-value">¥{{ totalPrice.toFixed(2) }}</span>
                <span class="order-stat-label">当前页金额</span>
            </div>
        </div>

        <div class="order-table-card">
            <StatusFilterToolbar
                v-model="statusFilter"
                v-model:search="searchQuery"
                :tabs="statusTabs"
                search-placeholder="搜索单号或仓库"
            />

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="filteredOrders.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="transfer"/></div>
                <div class="empty-text">暂无调拨订单</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>订单编号</th>
                    <th>源仓库</th>
                    <th>目标仓库</th>
                    <th>总金额</th>
                    <th>状态</th>
                    <th>创建时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in filteredOrders" :key="item.id">
                    <td><code>{{ item.transferOrderNo }}</code></td>
                    <td>{{ item.sourceWareHouseName || '-' }}</td>
                    <td>{{ item.targetWareHouseName || '-' }}</td>
                    <td class="price-cell">¥{{ item.totalPrice ?? '0.00' }}</td>
                    <td>
                        <span :class="statusClass(item.status)" class="status-badge">
                            {{ statusLabels[item.status] || item.status || '-' }}
                        </span>
                    </td>
                    <td class="time-cell">{{ item.createTime || '-' }}</td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/transfer-order/${item.id}`" class="btn-outline btn-sm">详情</router-link>
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
            :title="`确定要删除调拨单“${deleteTarget?.transferOrderNo || ''}”吗？`"
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
import transferOrderInterface from '../../../axios/interface/TransferOrderInterface.js'
import DeleteConfirmDialog from '../../../component/DeleteConfirmDialog.vue'
import StatusFilterToolbar from '../common/StatusFilterToolbar.vue'
import TablePagination from '../common/TablePagination.vue'
import {AUDIT_STATUS, AUDIT_STATUS_LABELS, AUDIT_STATUS_TABS} from '../../../constants/auditStatus.js'

const router = useRouter()
const toast = useToastStore()
const confirmStore = useConfirmStore()
const statusLabels = AUDIT_STATUS_LABELS
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
        const matchesQuery = !query || [item.transferOrderNo, item.sourceWareHouseName, item.targetWareHouseName]
            .some(value => String(value || '').toLowerCase().includes(query))
        return matchesStatus && matchesQuery
    })
})

const draftCount = computed(() => orderList.value.filter(item => item.status === AUDIT_STATUS.DRAFT).length)
const checkingCount = computed(() => orderList.value.filter(item => item.status === AUDIT_STATUS.CHECKING).length)
const totalPrice = computed(() => orderList.value.reduce((sum, item) => sum + Number(item.totalPrice || 0), 0))

onMounted(fetchList)

async function fetchList() {
    loading.value = true
    try {
        const data = await transferOrderInterface.searchPage(pageInfo.value.page, pageInfo.value.size)
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
        await transferOrderInterface.check(item.id)
        toast.success('调拨单已提交，等待审核')
        await fetchList()
    } catch {}
}

async function handleApprove(item) {
    const confirmed = await confirmStore.confirm({
        title: '通过调拨单？',
        message: `调拨单“${item.transferOrderNo}”通过后，将变更源仓库和目标仓库库存。`,
        confirmText: '确认通过'
    })
    if (!confirmed) return
    try {
        await transferOrderInterface.approve(item.id)
        toast.success('调拨单已审核通过，库存已变更')
        await fetchList()
    } catch {}
}

async function handleReject(item) {
    try {
        await transferOrderInterface.reject(item.id)
        toast.success('已拒绝该调拨单')
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
        await transferOrderInterface.hardDelete(deleteTarget.value.id)
        toast.success('调拨单已删除')
        showDeleteConfirm.value = false
        await fetchList()
    } catch {} finally {
        deleting.value = false
    }
}
</script>

<style scoped>
.transfer-order-manage {
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

.order-stats {
    display: flex;
    gap: 14px;
    margin-bottom: 18px;
    flex-wrap: wrap;
}

.order-stat-card {
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

.order-stat-icon {
    width: 38px;
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 11px;
    background: #e8f7f3;
    color: var(--primary);
    font-size: 21px;
    font-weight: 800;
}

.draft-icon {
    background: #fef3c7;
    color: #d97706;
}

.checking-icon {
    background: #e8f0fe;
    color: #3b82f6;
}

.amount-icon {
    background: #e8f0fe;
    color: #3b82f6;
}

.order-stat-value {
    color: var(--text);
    font-size: 20px;
    font-weight: 800;
    line-height: 1.1;
}

.order-stat-label {
    margin-left: -4px;
    color: var(--text-muted);
    font-size: 12px;
}

.order-table-card {
    padding: 8px 20px 20px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
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
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
    }

    .order-stats {
        flex-wrap: wrap;
    }

    .order-stat-card {
        width: calc(50% - 7px);
    }
}
</style>
