<template>
    <div class="order-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">订单管理</h2>
                <p class="page-desc">查看订单状态、支付方式和交易金额</p>
            </div>
            <button class="btn-outline" @click="fetchOrders">↻ 刷新订单</button>
        </div>

        <div class="order-stats">
            <div class="order-stat-card">
                <span class="order-stat-icon">📋</span>
                <span class="order-stat-value">{{ pageInfo.totalElements }}</span>
                <span class="order-stat-label">全部订单</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon completed-icon">✓</span>
                <span class="order-stat-value">{{ completedCount }}</span>
                <span class="order-stat-label">当前页已完成</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon draft-icon">◷</span>
                <span class="order-stat-value">{{ draftCount }}</span>
                <span class="order-stat-label">当前页挂单</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon amount-icon">¥</span>
                <span class="order-stat-value">¥{{ totalAmount.toFixed(2) }}</span>
                <span class="order-stat-label">当前页金额</span>
            </div>
        </div>

        <div class="order-table-card">
            <div class="table-toolbar">
                <div class="filter-tabs">
                    <button :class="{ active: statusFilter === '' }" class="filter-tab" @click="statusFilter = ''">
                        全部
                    </button>
                    <button :class="{ active: statusFilter === ORDER_STATUS.COMPLETED }" class="filter-tab"
                            @click="statusFilter = ORDER_STATUS.COMPLETED">已完成
                    </button>
                    <button :class="{ active: statusFilter === ORDER_STATUS.DRAFT }" class="filter-tab"
                            @click="statusFilter = ORDER_STATUS.DRAFT">挂单
                    </button>
                    <button :class="{ active: statusFilter === ORDER_STATUS.REFUND }" class="filter-tab"
                            @click="statusFilter = ORDER_STATUS.REFUND">已退款
                    </button>
                </div>
                <input v-model="searchQuery" class="order-search" placeholder="搜索订单号、员工或仓库" type="text"/>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="filteredOrders.length === 0" class="empty-state">
                <div class="empty-icon">📋</div>
                <div class="empty-text">暂无订单数据</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>订单编号</th>
                    <th>状态</th>
                    <th>支付方式</th>
                    <th>订单金额</th>
                    <th>实付金额</th>
                    <th>员工</th>
                    <th>仓库</th>
                    <th>备注</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="order in filteredOrders" :key="order.id" style="cursor:pointer" @dblclick="goDetail(order)">
                    <td><code>{{ order.orderNo }}</code></td>
                    <td>
                            <span :class="statusClass(order.orderStatus)" class="status-badge">
                                {{ orderStatusLabels[order.orderStatus] || order.orderStatus || '-' }}
                            </span>
                    </td>
                    <td>{{ payMethodLabels[order.payMethod] || order.payMethod || '-' }}</td>
                    <td>¥{{ order.totalPrice ?? '0.00' }}</td>
                    <td class="actual-price">¥{{ order.actualPrice ?? '0.00' }}</td>
                    <td>{{ order.employeeName || '-' }}</td>
                    <td>{{ order.warehouseName || '-' }}</td>
                    <td class="remark-cell">{{ order.remark || '-' }}</td>
                    <td>
                        <router-link :to="`/manage/order/${order.id}`" class="btn-outline btn-sm">详情</router-link>
                    </td>
                </tr>
                </tbody>
            </table>

            <div class="pagination-bar">
                <span class="page-info">
                    第 {{ pageInfo.page + 1 }} / {{ totalPages }} 页，共 {{ pageInfo.totalElements }} 条
                </span>
                <div class="page-actions">
                    <button
                        :disabled="loading || pageInfo.page <= 0"
                        class="btn-outline btn-sm"
                        @click="changePage(pageInfo.page - 1)"
                    >
                        上一页
                    </button>
                    <button
                        :disabled="loading || pageInfo.page >= totalPages - 1"
                        class="btn-outline btn-sm"
                        @click="changePage(pageInfo.page + 1)"
                    >
                        下一页
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import orderInterface from '../../axios/interface/OrderInterface.js'
import {ORDER_STATUS, ORDER_STATUS_LABELS} from '../../constants/orderStatus.js'
import {PAY_METHOD_LABELS} from '../../constants/payMethod.js'

const router = useRouter()
const orderList = ref([])
const loading = ref(true)
const statusFilter = ref('')
const searchQuery = ref('')
const orderStatusLabels = ORDER_STATUS_LABELS
const payMethodLabels = PAY_METHOD_LABELS
const pageInfo = ref({
    totalElements: 0,
    totalPages: 0,
    page: 0,
    size: 10
})

const totalPages = computed(() => Math.max(pageInfo.value.totalPages || 1, 1))

const filteredOrders = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    return orderList.value.filter(order => {
        const matchesStatus = !statusFilter.value || order.orderStatus === statusFilter.value
        const matchesQuery = !query || [order.orderNo, order.employeeName, order.warehouseName, order.remark]
            .some(value => String(value || '').toLowerCase().includes(query))
        return matchesStatus && matchesQuery
    })
})

const completedCount = computed(() => orderList.value.filter(order => order.orderStatus === ORDER_STATUS.COMPLETED).length)
const draftCount = computed(() => orderList.value.filter(order => order.orderStatus === ORDER_STATUS.DRAFT).length)
const totalAmount = computed(() => orderList.value.reduce((total, order) => total + Number(order.actualPrice || 0), 0))

onMounted(fetchOrders)

async function fetchOrders() {
    loading.value = true
    try {
        const data = await orderInterface.searchPage(pageInfo.value.page, pageInfo.value.size)
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
    fetchOrders()
}

function goDetail(item) {
    router.push(`/manage/order/${item.id}`)
}

function statusClass(status) {
    if (status === ORDER_STATUS.COMPLETED) return 'status-ok'
    if (status === ORDER_STATUS.DRAFT) return 'status-pending'
    if (status === ORDER_STATUS.REFUND) return 'status-error'
    return ''
}
</script>

<style scoped>
.order-manage {
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

.completed-icon {
    background: #dcfce7;
    color: #16a34a;
}

.draft-icon {
    background: #fef3c7;
    color: #d97706;
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

.table-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 10px 0 16px;
    border-bottom: 1px solid var(--border-light);
}

.filter-tabs {
    display: flex;
    gap: 4px;
}

.filter-tab {
    padding: 7px 13px;
    border-radius: 8px;
    color: var(--text-secondary);
    background: transparent;
    font-size: 13px;
}

.filter-tab:hover {
    background: #f1faf8;
    color: var(--primary);
}

.filter-tab.active {
    background: var(--primary-light);
    color: var(--primary);
    font-weight: 700;
}

.order-search {
    width: 260px;
    height: 36px;
}

.order-table-card .data-table {
    min-width: 980px;
}

.order-table-card .data-table td {
    height: 56px;
}

.actual-price {
    color: var(--primary);
    font-weight: 700;
}

.remark-cell {
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-secondary);
}

.pagination-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding-top: 16px;
    border-top: 1px solid var(--border-light);
}

.page-info {
    color: var(--text-muted);
    font-size: 13px;
}

.page-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

@media (max-width: 900px) {
    .page-heading,
    .table-toolbar,
    .pagination-bar {
        align-items: flex-start;
        flex-direction: column;
    }

    .order-stats {
        flex-wrap: wrap;
    }

    .order-stat-card {
        width: calc(50% - 7px);
    }

    .order-search {
        width: 100%;
    }
}
</style>
