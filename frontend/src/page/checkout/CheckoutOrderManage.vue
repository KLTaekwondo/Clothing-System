<template>
    <div class="orders-page">
        <header class="orders-header">
            <div class="header-left">
                <button
                    class="back-button"
                    @click="goCheckout"
                >← 返回收银台</button>
                <div>
                    <h1>订单查询</h1>
                    <p>查看当前仓库已完成的收银订单</p>
                </div>
            </div>
            <div class="header-actions">
                <span>共 {{ pageInfo.totalElements }} 笔订单</span>
                <button
                    :disabled="loading"
                    class="refresh-button"
                    @click="fetchOrders"
                >{{ loading ? '刷新中...' : '刷新' }}</button>
            </div>
        </header>

        <section class="orders-toolbar">
            <div class="search-box">
                <IconGraphic name="search"/>
                <input
                    v-model="searchQuery"
                    placeholder="搜索订单编号、员工、会员或备注"
                    type="text"
                />
            </div>
            <div class="date-range">
                <label>开始日期</label>
                <input
                    v-model="startDate"
                    type="date"
                    @change="applyDateFilter"
                />
                <span>至</span>
                <input
                    v-model="endDate"
                    type="date"
                    @change="applyDateFilter"
                />
            </div>
            <div class="summary-list">
                <span>当前页 {{ orderList.length }} 笔</span>
                <strong>实付合计 ¥{{ pageActualAmount }}</strong>
            </div>
        </section>

        <div
            v-if="loading"
            class="loading-state"
        >
            <div class="loading-spinner"></div>
            <span>正在读取订单...</span>
        </div>

        <div
            v-else-if="filteredOrders.length === 0"
            class="empty-panel"
        >
            <IconGraphic name="order"/>
            <strong>{{ orderList.length ? '没有符合条件的订单' : '当前仓库暂无已完成订单' }}</strong>
            <span>{{ orderList.length ? '可以修改关键词重新搜索' : '在收银台完成收款后即可看到订单记录' }}</span>
            <button
                v-if="!orderList.length"
                class="checkout-link"
                @click="goCheckout"
            >返回收银台</button>
        </div>

        <div
            v-else
            class="orders-list"
        >
            <article
                v-for="order in filteredOrders"
                :key="order.id"
                class="order-card"
            >
                <div class="card-heading">
                    <div class="order-identity">
                        <span>订单编号</span>
                        <code>{{ order.orderNo || '-' }}</code>
                    </div>
                    <strong class="order-amount">¥{{ formatMoney(order.actualPrice) }}</strong>
                </div>

                <div class="order-details">
                    <div class="detail-item">
                        <span>收银员工</span>
                        <strong>{{ order.employeeName || '未指定员工' }}</strong>
                    </div>
                    <div class="detail-item">
                        <span>支付方式</span>
                        <strong>{{ payMethodLabel(order.payMethod) }}</strong>
                    </div>
                    <div class="detail-item">
                        <span>业务方向</span>
                        <strong>{{ directionLabel(order.direction) }}</strong>
                    </div>
                    <div class="detail-item">
                        <span>会员手机号</span>
                        <strong>{{ order.memberPhone || '无会员' }}</strong>
                    </div>
                </div>

                <div class="order-meta">
                    <span>{{ order.createTime || '-' }}</span>
                    <p>{{ order.remark || '无备注' }}</p>
                </div>

                <div class="card-actions">
                    <button
                        class="detail-button"
                        @click="openDetail(order)"
                    >查看详情</button>
                </div>
            </article>
        </div>

        <TablePagination
            v-if="pageInfo.totalElements > 0"
            :loading="loading"
            :page="pageInfo.page"
            :total-elements="pageInfo.totalElements"
            :total-pages="totalPages"
            @change="changePage"
        />

        <div
            v-if="detailTarget"
            class="detail-overlay"
            @click.self="closeDetail"
        >
            <div class="detail-dialog">
                <div class="detail-heading">
                    <div class="detail-title">
                        <strong>订单详情</strong>
                        <code>{{ detailTarget.orderNo }}</code>
                    </div>
                    <button
                        class="detail-close"
                        @click="closeDetail"
                    >×</button>
                </div>

                <div
                    v-if="detailLoading"
                    class="detail-loading"
                >
                    <div class="loading-spinner"></div>
                    <span>正在读取明细...</span>
                </div>
                <div
                    v-else-if="detailItems.length === 0"
                    class="detail-empty"
                >无商品明细</div>
                <div
                    v-else
                    class="detail-table-wrap"
                >
                    <table class="detail-table">
                        <thead>
                        <tr>
                            <th>类型</th>
                            <th>商品</th>
                            <th>SKU</th>
                            <th>单价</th>
                            <th>数量</th>
                            <th>折扣</th>
                            <th>小计</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr
                            v-for="item in detailItems"
                            :key="item.id"
                        >
                            <td>{{ item.direction === 'OUT' ? '退货' : '销售' }}</td>
                            <td>{{ item.productName || '-' }}</td>
                            <td>{{ item.skuName || item.skuCode || '-' }}</td>
                            <td>¥{{ formatMoney(item.unitPrice) }}</td>
                            <td>{{ item.quantity }}</td>
                            <td>{{ formatDiscount(item.discount) }}</td>
                            <td><strong>¥{{ formatMoney(item.actualPrice ?? item.totalPrice) }}</strong></td>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import orderInterface from '../../axios/interface/OrderInterface.js'
import {PAY_METHOD_LABELS} from '../../constants/payMethod.js'
import IconGraphic from '../../component/IconGraphic.vue'
import TablePagination from '../../component/common/TablePagination.vue'

const router = useRouter()
const toast = useToastStore()
const orderList = ref([])
const searchQuery = ref('')
const startDate = ref(getToday())
const endDate = ref(getToday())
const loading = ref(true)
const detailTarget = ref(null)
const detailLoading = ref(false)
const detailItems = ref([])
const pageInfo = ref({
    totalElements: 0,
    totalPages: 0,
    page: 0,
    size: 9
})

const totalPages = computed(() => Math.max(pageInfo.value.totalPages || 1, 1))
const pageActualAmount = computed(() => {
    return orderList.value.reduce((sum, order) => sum + Number(order.actualPrice || 0), 0).toFixed(2)
})

const filteredOrders = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    if (!query) return orderList.value
    return orderList.value.filter(order => {
        return [order.orderNo, order.employeeName, order.memberPhone, order.remark]
            .some(value => String(value || '').toLowerCase().includes(query))
    })
})

onMounted(fetchOrders)

async function fetchOrders() {
    const startTime = `${startDate.value}T00:00:00`
    const endTime = `${endDate.value}T23:59:59`
    loading.value = true
    try {
        const data = await orderInterface.searchCurrentCompletePage(pageInfo.value.page, pageInfo.value.size, startTime, endTime)
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

// 返回今天的 yyyy-MM-dd
function getToday() {
    const now = new Date()
    const pad = n => String(n).padStart(2, '0')
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
}

// 日期范围变化后重新查询，回到第一页
function applyDateFilter() {
    if (!startDate.value || !endDate.value) return
    if (startDate.value > endDate.value) {
        toast.warning('开始日期不能晚于结束日期')
        return
    }
    pageInfo.value.page = 0
    fetchOrders()
}

function changePage(page) {
    pageInfo.value.page = page
    searchQuery.value = ''
    fetchOrders()
}

function formatMoney(value) {
    return Number(value || 0).toFixed(2)
}

function payMethodLabel(value) {
    return PAY_METHOD_LABELS[value] || value || '未选择'
}

function directionLabel(value) {
    if (value === 'OUT') return '退货'
    if (value === 'IN') return '销售'
    return '混合订单'
}

function formatDiscount(value) {
    const discount = Number(value || 1)
    if (discount >= 1) return '原价'
    return `${(discount * 10).toFixed(1)} 折`
}

async function openDetail(order) {
    detailTarget.value = order
    detailLoading.value = true
    detailItems.value = []
    try {
        const detail = await orderInterface.search(order.id)
        detailItems.value = detail.items || []
    } catch {
        detailItems.value = []
    } finally {
        detailLoading.value = false
    }
}

function closeDetail() {
    detailTarget.value = null
    detailItems.value = []
}

function goCheckout() {
    router.push('/checkout')
}
</script>

<style scoped>
.orders-page {
    width: 100%;
    min-height: 100vh;
    padding: 28px 34px;
    background: #f3f8f7;
}

.orders-header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 20px;
    margin-bottom: 20px;
}

.header-left,
.header-actions {
    display: flex;
    align-items: center;
    gap: 14px;
}

.header-left h1 {
    margin-bottom: 4px;
    color: #244743;
    font-size: 26px;
}

.header-left p,
.header-actions span {
    color: #78908d;
    font-size: 13px;
}

.back-button,
.refresh-button,
.checkout-link,
.detail-close {
    height: 36px;
    padding: 0 14px;
    color: #0f766e;
    background: #fff;
    border: 1px solid #b9ded7;
    border-radius: 8px;
    font-weight: 700;
}

.back-button:hover,
.refresh-button:hover,
.checkout-link:hover,
.detail-close:hover {
    background: #e9f8f5;
    border-color: #0d9488;
}

.orders-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    flex-wrap: wrap;
    padding: 12px 14px;
    margin-bottom: 16px;
    background: #fff;
    border: 1px solid #dceae7;
    border-radius: 10px;
    box-shadow: 0 5px 18px rgba(22, 83, 78, 0.05);
}

.search-box {
    width: 420px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.search-box :deep(img) {
    width: 20px;
    height: 20px;
}

.search-box input {
    width: 100%;
    height: 36px;
    border: none;
    box-shadow: none;
}

.date-range {
    display: flex;
    align-items: center;
    gap: 8px;
}

.date-range label,
.date-range span {
    color: #78908d;
    font-size: 13px;
}

.date-range input {
    height: 36px;
    padding: 0 8px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fbfefd;
    font-size: 13px;
}

.summary-list {
    display: flex;
    align-items: center;
    gap: 18px;
    color: #78908d;
    font-size: 13px;
}

.summary-list strong {
    color: #d97706;
    font-size: 15px;
}

.loading-state,
.empty-panel {
    min-height: 360px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: #78908d;
    background: #fff;
    border: 1px solid #dceae7;
    border-radius: 12px;
}

.empty-panel :deep(img) {
    width: 52px;
    height: 52px;
}

.empty-panel strong {
    color: #285f5a;
    font-size: 17px;
}

.empty-panel span {
    font-size: 13px;
}

.orders-list {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.order-card {
    width: calc(33.33% - 10px);
    min-width: 300px;
    padding: 18px;
    background: #fff;
    border: 1px solid #dceae7;
    border-radius: 12px;
    box-shadow: 0 6px 20px rgba(22, 83, 78, 0.06);
    transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.order-card:hover {
    border-color: #9fd8cf;
    box-shadow: 0 10px 26px rgba(22, 83, 78, 0.11);
    transform: translateY(-2px);
}

.card-heading {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 13px;
    border-bottom: 1px solid #edf4f2;
}

.order-identity {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.order-identity span {
    color: #94a3b8;
    font-size: 11px;
}

.order-identity code {
    overflow: hidden;
    color: #0f766e;
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.order-amount {
    color: #d97706;
    font-size: 21px;
    font-variant-numeric: tabular-nums;
}

.order-details {
    display: flex;
    gap: 12px;
    padding: 15px 0;
    flex-wrap: wrap;
}

.detail-item {
    width: calc(50% - 6px);
    display: flex;
    flex-direction: column;
    gap: 3px;
}

.detail-item span {
    color: #94a3b8;
    font-size: 11px;
}

.detail-item strong {
    overflow: hidden;
    color: #475569;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.order-meta {
    min-height: 58px;
    padding: 10px 12px;
    color: #78908d;
    background: #f7fbfa;
    border-radius: 8px;
}

.order-meta span {
    font-size: 11px;
}

.order-meta p {
    margin-top: 4px;
    overflow: hidden;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.card-actions {
    display: flex;
    justify-content: flex-end;
    padding-top: 14px;
}

.detail-button {
    height: 34px;
    padding: 0 14px;
    color: #fff;
    background: #0d9488;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 700;
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.2);
}

.detail-button:hover {
    background: #0f766e;
    transform: translateY(-1px);
}

.detail-overlay {
    position: fixed;
    inset: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    background: rgba(15, 23, 42, 0.38);
}

.detail-dialog {
    width: 760px;
    max-width: 100%;
    overflow: hidden;
    background: #fff;
    border-radius: 14px;
    box-shadow: 0 22px 60px rgba(15, 23, 42, 0.22);
}

.detail-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 16px 20px;
    border-bottom: 1px solid #edf4f2;
}

.detail-title {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.detail-title strong {
    color: #285f5a;
    font-size: 15px;
}

.detail-title code {
    overflow: hidden;
    color: #0f766e;
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.detail-close {
    width: 36px;
    height: 36px;
    padding: 0;
    color: #78908d;
    font-size: 20px;
    line-height: 1;
}

.detail-loading,
.detail-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    min-height: 220px;
    color: #78908d;
    font-size: 13px;
}

.detail-table-wrap {
    overflow-x: auto;
    padding: 4px 0;
}

.detail-table {
    width: 100%;
    border-collapse: collapse;
}

.detail-table th {
    padding: 11px 14px;
    color: #285f5a;
    background: #eff8f6;
    font-size: 12px;
    font-weight: 700;
    text-align: left;
    white-space: nowrap;
}

.detail-table td {
    padding: 11px 14px;
    color: #475569;
    border-top: 1px solid #edf4f2;
    font-size: 13px;
    white-space: nowrap;
}

.detail-table td strong {
    color: #0d9488;
}

@media (max-width: 1050px) {
    .order-card {
        width: calc(50% - 7px);
    }
}

@media (max-width: 720px) {
    .orders-page {
        padding: 20px;
    }

    .orders-header,
    .orders-toolbar {
        align-items: flex-start;
        flex-direction: column;
    }

    .header-left {
        align-items: flex-start;
        flex-direction: column;
    }

    .search-box,
    .order-card {
        width: 100%;
        min-width: 0;
    }

    .date-range {
        flex-wrap: wrap;
    }
}
</style>
