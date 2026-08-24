<template>
    <div class="order-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">订单管理</h2>
                <p class="page-label-desc">查看订单状态、支付方式和交易金额</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>订单检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <div class="filter-tabs">
                    <button
                        :class="statusFilter === '' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="statusFilter = ''"
                    >全部</button>
                    <button
                        :class="statusFilter === ORDER_STATUS.COMPLETED ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="statusFilter = ORDER_STATUS.COMPLETED"
                    >已完成</button>
                    <button
                        :class="statusFilter === ORDER_STATUS.DRAFT ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="statusFilter = ORDER_STATUS.DRAFT"
                    >挂单</button>
                    <button
                        :class="statusFilter === ORDER_STATUS.REFUND ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="statusFilter = ORDER_STATUS.REFUND"
                    >已退款</button>
                </div>
                <input
                    v-model="searchQuery"
                    class="search-code-input"
                    placeholder="搜索订单号、员工或仓库"
                    type="text"
                />
                <button
                    class="btn-outline search-button"
                    type="button"
                    @click="fetchOrders"
                >↻ 刷新订单</button>
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
                <span class="order-stat-icon completed-icon"><IconGraphic name="check"/></span>
                <span class="stats-body">
<span class="order-stat-value">{{ completedCount }}</span>

                <span class="order-stat-label">当前页已完成</span>
</span>
            </div>
            <div class="order-stat-card">
                <span class="order-stat-icon draft-icon"><IconGraphic name="clock"/></span>
                <span class="stats-body">
<span class="order-stat-value">{{ draftCount }}</span>

                <span class="order-stat-label">当前页挂单</span>
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

            <TablePagination
                :loading="loading"
                :page="pageInfo.page"
                :total-elements="pageInfo.totalElements"
                :total-pages="totalPages"
                @change="changePage"
            />
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import orderInterface from '../../../axios/interface/OrderInterface.js'
import TablePagination from '../../../component/common/TablePagination.vue'
import {ORDER_STATUS, ORDER_STATUS_LABELS} from '../../../constants/orderStatus.js'
import {PAY_METHOD_LABELS} from '../../../constants/payMethod.js'

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

.order-table-card .data-table td {
    height: 56px;
}

.actual-price {
    color: var(--primary);
    font-weight: 700;
    font-family: var(--mono);
    font-variant-numeric: tabular-nums;
}

.remark-cell {
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-secondary);
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
