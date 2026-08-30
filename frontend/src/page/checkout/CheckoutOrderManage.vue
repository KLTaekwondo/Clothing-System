<template>
    <div class="orders-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">订单查询</h2>
                <p class="page-label-desc">查看当前仓库已完成的收银订单</p>
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
                </div>
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            type="button"
                            @click="goCheckout"
                        >← 返回收银台</button>
                        <input
                            v-model="searchQuery"
                            class="search-code-input"
                            placeholder="搜索订单编号、员工、会员或备注"
                            type="text"
                        />
                        <input
                            v-model="startDate"
                            class="date-input"
                            type="date"
                            @change="applyDateFilter"
                        />
                        <span class="date-sep">至</span>
                        <input
                            v-model="endDate"
                            class="date-input"
                            type="date"
                            @change="applyDateFilter"
                        />
                        <select
                            v-model="filterEmployeeId"
                            class="employee-select"
                            @change="onEmployeeFilter"
                        >
                            <option value="">全部员工</option>
                            <option
                                v-for="employee in employees"
                                :key="employee.id"
                                :value="employee.id"
                            >{{ employee.name }}</option>
                        </select>
                        <button
                            :disabled="loading"
                            class="btn-outline search-button"
                            type="button"
                            @click="fetchOrders"
                        >{{ loading ? '刷新中...' : '↻ 刷新' }}</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="view-tabs">
            <button
                :class="activeView === 'stats' ? 'view-tab-active' : 'view-tab'"
                type="button"
                @click="switchToStats"
            >支付统计</button>
            <button
                :class="activeView === 'list' ? 'view-tab-active' : 'view-tab'"
                type="button"
                @click="activeView = 'list'"
            >订单列表</button>
        </div>

        <template v-if="activeView === 'list'">
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
                            type="button"
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
        </template>

        <template v-if="activeView === 'stats'">
            <div class="stats-total-card">
                <span class="stats-total-icon"><img :src="moneyIcon" alt=""/></span>
                <span class="stats-total-body">
                    <span class="stats-total-label">区间实付总额</span>
                    <strong class="stats-total-value">¥{{ totalAmount }}</strong>
                </span>
                <span class="stats-total-meta">{{ startDate }} 至 {{ endDate }}</span>
            </div>

            <div
                v-if="statsLoading"
                class="loading-state"
            >
                <div class="loading-spinner"></div>
                <span>正在统计...</span>
            </div>
            <div
                v-else-if="Number(totalAmount) <= 0"
                class="empty-panel"
            >
                <IconGraphic name="order"/>
                <strong>该区间暂无支付统计</strong>
                <span>没有符合所选日期的已完成订单</span>
            </div>
            <div
                v-else
                class="pay-stats-grid"
            >
                <div
                    v-for="item in payStats"
                    :key="item.payMethod"
                    class="pay-stat-card"
                >
                    <span class="pay-stat-icon"><img :src="payMethodIcon(item.payMethod)" alt=""/></span>
                    <span class="pay-stat-body">
                        <strong>¥{{ formatMoney(item.amount) }}</strong>
                        <span>{{ item.label }}</span>
                    </span>
                </div>
            </div>
        </template>

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
                            <td>
                                <strong>{{ item.productName || '-' }}</strong>
                                <span class="sub-text">{{ item.productCode || '-' }}</span>
                            </td>
                            <td>
                                <strong>{{ item.skuName || '-' }}</strong>
                                <span class="sub-text"><code>{{ item.skuCode || '-' }}</code></span>
                            </td>
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
import orderDashInterface from '../../axios/interface/OrderDashInterface.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import {PAY_METHOD, PAY_METHOD_LABELS} from '../../constants/payMethod.js'
import IconGraphic from '../../component/IconGraphic.vue'
import TablePagination from '../../component/common/TablePagination.vue'
import moneyIcon from '../../assets/icons/money.svg'
import cashIcon from '../../assets/icons/cash.svg'
import bankCardIcon from '../../assets/icons/bank-card.svg'
import alipayIcon from '../../assets/icons/alipay.svg'
import wechatIcon from '../../assets/icons/wechat.svg'
import douyinIcon from '../../assets/icons/douyin.svg'

const router = useRouter()
const toast = useToastStore()
const activeView = ref('stats')
const orderList = ref([])
const searchQuery = ref('')
const startDate = ref(getToday())
const endDate = ref(getToday())
// 员工筛选：'' = 全部员工（传 null），其他 = 员工 id
const employees = ref([])
const filterEmployeeId = ref('')
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

// ── 支付统计状态 ──
const statsLoading = ref(false)
const payStats = ref([])
const totalAmount = ref('0.00')

// 后端 /dash/order/checkout/custom 返回的支付方式字段映射（固定五张卡，后端除 sumAmount 外均为大驼峰）
const PAY_STAT_FIELDS = [
    {field: 'CashAmount', payMethod: PAY_METHOD.CASH, label: '现金'},
    {field: 'CardAmount', payMethod: PAY_METHOD.CARD, label: '信用卡'},
    {field: 'AlipayAmount', payMethod: PAY_METHOD.ALIPAY, label: '支付宝'},
    {field: 'WeChatAmount', payMethod: PAY_METHOD.WECHAT, label: '微信'},
    {field: 'TikTokWriteOffAmount', payMethod: PAY_METHOD.TIKTOK_WRITE_OFF, label: '抖音核销'}
]

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

onMounted(() => {
    fetchOrders()
    fetchPayStats()
    loadEmployees()
})

// 当前仓库可用员工（收银端 employee/verify 列表）
async function loadEmployees() {
    try {
        employees.value = await employeeInterface.verifyList()
    } catch {
        employees.value = []
    }
}

// 员工筛选变化：回到第一页重新查询（'' 传 null = 全部）
function onEmployeeFilter() {
    pageInfo.value.page = 0
    fetchOrders()
}

async function fetchOrders() {
    const startTime = `${startDate.value}T00:00:00`
    const endTime = `${endDate.value}T23:59:59`
    loading.value = true
    try {
        const data = await orderInterface.searchCurrentCompletePage(pageInfo.value.page, pageInfo.value.size, startTime, endTime, filterEmployeeId.value || null)
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
    fetchPayStats()
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

function payMethodIcon(value) {
    const map = {
        [PAY_METHOD.CASH]: cashIcon,
        [PAY_METHOD.CARD]: bankCardIcon,
        [PAY_METHOD.ALIPAY]: alipayIcon,
        [PAY_METHOD.WECHAT]: wechatIcon,
        [PAY_METHOD.TIKTOK_WRITE_OFF]: douyinIcon
    }
    return map[value] || moneyIcon
}

// 切到支付统计：每次进入都重新拉取（日期可能已变化）
async function switchToStats() {
    activeView.value = 'stats'
    await fetchPayStats()
}

// 统计：直接调后端 /dash/order/checkout/custom（按支付方式汇总，一次请求，不再循环翻页）
async function fetchPayStats() {
    // 后端区间为 [start, end)，结束日要加一天才能包含所选结束日当天
    const dashEndDate = nextDay(endDate.value)
    statsLoading.value = true
    try {
        const data = await orderDashInterface.checkoutCustomDash(startDate.value, dashEndDate)
        payStats.value = PAY_STAT_FIELDS.map(item => ({
            payMethod: item.payMethod,
            label: item.label,
            amount: Number(data[item.field] || 0)
        }))
        totalAmount.value = Number(data.sumAmount || 0).toFixed(2)
    } catch {
        payStats.value = []
        totalAmount.value = '0.00'
    } finally {
        statsLoading.value = false
    }
}

// 返回 yyyy-MM-dd 的次日
function nextDay(dateStr) {
    const date = new Date(`${dateStr}T00:00:00`)
    date.setDate(date.getDate() + 1)
    const pad = n => String(n).padStart(2, '0')
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
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
    min-width: 0;
    padding: 28px 34px;
    background: var(--bg-body);
}

/* ── 搜索区（复用全局 page-toolbar / search-shell） ── */
.date-input {
    width: 148px;
    height: 40px;
    padding: 0 14px;
    border: 1px solid var(--border-strong);
    border-radius: 999px;
    background: var(--bg-card);
    font-size: 13px;
    color: var(--text);
    outline: none;
    transition: var(--transition);
    cursor: pointer;
}

.date-input:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

/* 员工筛选下拉（胶囊样式，与日期控件一致） */
.employee-select {
    width: 128px;
    height: 40px;
    padding: 0 14px;
    border: 1px solid var(--border-strong);
    border-radius: 999px;
    background: var(--bg-card);
    font-size: 13px;
    color: var(--text);
    outline: none;
    transition: var(--transition);
    cursor: pointer;
}

.employee-select:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.date-sep {
    color: var(--text-muted);
    font-size: 13px;
}

/* ── 卡片列表 ── */
.orders-list {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.order-card {
    width: calc(33.33% - 10px);
    min-width: 300px;
    padding: 18px;
    background: var(--bg-card);
    border: 1px solid var(--border);
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
    border-bottom: 1px solid var(--border-light);
}

.order-identity {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.order-identity span {
    color: var(--text-muted);
    font-size: 11px;
}

.order-identity code {
    overflow: hidden;
    color: var(--primary-dark);
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.order-amount {
    color: var(--warning-dark);
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
    color: var(--text-muted);
    font-size: 11px;
}

.detail-item strong {
    overflow: hidden;
    color: var(--text-secondary);
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.order-meta {
    min-height: 58px;
    padding: 10px 12px;
    color: var(--text-secondary);
    background: var(--bg-subtle);
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
    color: var(--text-invert);
    background: var(--primary);
    border-radius: 999px;
    font-size: 12px;
    font-weight: 700;
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.2);
    cursor: pointer;
}

.detail-button:hover {
    background: var(--primary-dark);
    transform: translateY(-1px);
}

.loading-state,
.empty-panel {
    min-height: 360px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 12px;
    color: var(--text-secondary);
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: 12px;
}

.empty-panel :deep(img) {
    width: 52px;
    height: 52px;
}

.empty-panel strong {
    color: var(--primary-dark);
    font-size: 17px;
}

.empty-panel span {
    font-size: 13px;
}

.checkout-link {
    height: 36px;
    padding: 0 14px;
    color: var(--primary-dark);
    background: var(--bg-card);
    border: 1px solid var(--border-hover);
    border-radius: 999px;
    font-weight: 700;
    cursor: pointer;
}

.checkout-link:hover {
    background: var(--bg-hover);
    border-color: var(--primary);
}

/* ── 详情弹窗 ── */
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
    background: var(--bg-card);
    border-radius: 14px;
    box-shadow: 0 22px 60px rgba(15, 23, 42, 0.22);
}

.detail-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 16px 20px;
    border-bottom: 1px solid var(--border-light);
}

.detail-title {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 10px;
}

.detail-title strong {
    color: var(--primary-dark);
    font-size: 15px;
}

.detail-title code {
    overflow: hidden;
    color: var(--primary-dark);
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.detail-close {
    width: 36px;
    height: 36px;
    padding: 0;
    color: var(--text-secondary);
    font-size: 20px;
    line-height: 1;
    border: 1px solid var(--border-hover);
    border-radius: 999px;
    background: var(--bg-card);
    cursor: pointer;
}

.detail-close:hover {
    background: var(--bg-hover);
    border-color: var(--primary);
}

.detail-loading,
.detail-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 10px;
    min-height: 220px;
    color: var(--text-secondary);
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
    color: var(--primary-dark);
    background: #eff8f6;
    font-size: 12px;
    font-weight: 700;
    text-align: left;
    white-space: nowrap;
}

.detail-table td {
    padding: 11px 14px;
    color: var(--text-secondary);
    border-top: 1px solid var(--border-light);
    font-size: 13px;
    white-space: nowrap;
}

.detail-table td strong {
    color: var(--primary);
}

.sub-text {
    display: block;
    margin-top: 2px;
    color: var(--text-muted);
    font-size: 11px;
}

/* ── 视图切换 Tabs ── */
.view-tabs {
    display: flex;
    gap: 4px;
    margin-bottom: 16px;
    border-bottom: 1px solid var(--border-light);
}

.view-tab,
.view-tab-active {
    padding: 10px 20px;
    border: none;
    border-bottom: 2px solid transparent;
    background: none;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: color 0.2s, border-color 0.2s;
}

.view-tab:hover {
    color: var(--primary);
}

.view-tab-active {
    color: var(--primary);
    border-bottom-color: var(--primary);
}

/* ── 支付统计 ── */
.stats-total-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 22px 24px;
    margin-bottom: 16px;
    background: linear-gradient(135deg, var(--primary), #14b8a6);
    border-radius: 16px;
    color: var(--text-invert);
    box-shadow: 0 10px 24px rgba(13, 148, 136, 0.18);
}

.stats-total-icon {
    width: 48px;
    height: 48px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.18);
    font-size: 24px;
}

.stats-total-icon :deep(img) {
    width: 24px;
    height: 24px;
    filter: invert(1);
}

.stats-total-body {
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.stats-total-label {
    font-size: 12px;
    opacity: 0.85;
}

.stats-total-value {
    font-size: 30px;
    font-family: var(--mono);
    font-variant-numeric: tabular-nums;
    line-height: 1.1;
}

.stats-total-meta {
    margin-left: auto;
    font-size: 12px;
    opacity: 0.85;
}

.pay-stats-grid {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.pay-stat-card {
    width: calc(25% - 11px);
    min-width: 200px;
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: var(--shadow);
}

.pay-stat-icon {
    width: 42px;
    height: 42px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 999px;
    background: var(--primary-light);
}

.pay-stat-icon :deep(img) {
    width: 20px;
    height: 20px;
}

.pay-stat-body {
    display: flex;
    flex-direction: column;
    gap: 3px;
}

.pay-stat-body strong {
    font-size: 18px;
    font-family: var(--mono);
    font-variant-numeric: tabular-nums;
    color: var(--text);
}

.pay-stat-body span {
    font-size: 12px;
    color: var(--text-muted);
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

    .order-card {
        width: 100%;
        min-width: 0;
    }

    .pay-stat-card {
        width: calc(50% - 7px);
        min-width: 0;
    }

    .stats-total-meta {
        display: none;
    }
}
</style>
