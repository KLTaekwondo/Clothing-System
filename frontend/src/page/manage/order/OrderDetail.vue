<template>
    <div class="detail-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回订单</button>
                <button
                    v-if="order"
                    class="btn-secondary"
                    @click="printReceipt"
                >🖨️ 打印小票</button>
                <div>
                    <h2 class="page-title">订单详情</h2>
                    <p v-if="order" class="page-desc">{{ order.orderNo }}</p>
                </div>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!order" class="empty-state">
            <div class="empty-icon"><IconGraphic name="order"/></div>
            <div class="empty-text">订单不存在</div>
        </div>
        <template v-else>
            <div class="info-grid">
                <div class="info-card"><span class="info-label">订单编号</span><strong
                    class="code-value">{{ order.orderNo }}</strong></div>
                <div class="info-card"><span class="info-label">订单状态</span><span :class="statusClass(order.orderStatus)"
                                                                                     class="status-badge">{{
                        orderStatusLabels[order.orderStatus] || order.orderStatus
                    }}</span></div>
                <div class="info-card"><span class="info-label">支付方式</span><strong>{{
                        payMethodLabels[order.payMethod] || order.payMethod || '-'
                    }}</strong></div>
                <div class="info-card"><span class="info-label">订单金额</span><strong>¥{{
                        order.totalPrice ?? '0.00'
                    }}</strong></div>
                <div class="info-card"><span class="info-label">实付金额</span><strong
                    class="price-value">¥{{ order.actualPrice ?? '0.00' }}</strong></div>
                <div class="info-card"><span class="info-label">员工</span><strong>{{
                        order.employeeName || '-'
                    }}</strong></div>
                <div class="info-card"><span class="info-label">仓库</span><strong>{{
                        order.warehouseName || '-'
                    }}</strong></div>
                <div class="info-card"><span class="info-label">备注</span><strong
                    class="remark-text">{{ order.remark || '-' }}</strong></div>
            </div>

            <div class="card">
                <div class="card-header" style="margin-bottom:0">
                    <span class="card-title">订单商品</span>
                    <span class="card-hint">{{ order.items?.length || 0 }} 项</span>
                </div>
                <table v-if="order.items?.length" class="data-table">
                    <thead>
                    <tr>
                        <th>商品名称</th>
                        <th>SKU 名称</th>
                        <th>单价</th>
                        <th>数量</th>
                        <th>折扣</th>
                        <th>小计</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="item in order.items" :key="item.itemId">
                        <td>{{ item.productName }}</td>
                        <td>{{ item.skuName }}</td>
                        <td>¥{{ item.unitPrice }}</td>
                        <td>{{ item.quantity }}</td>
                        <td>{{ (item.discount * 100).toFixed(0) }}%</td>
                        <td><strong>¥{{ item.actualPrice }}</strong></td>
                    </tr>
                    </tbody>
                </table>
                <div v-else class="empty-state" style="padding:20px">
                    <div class="empty-icon"><IconGraphic name="product"/></div>
                    <div class="empty-text">无商品明细</div>
                </div>
            </div>
        </template>
    </div>
<ReceiptPreview
        :visible="showReceipt"
        :order-no="order?.orderNo || ''"
        :items="receiptItems"
        :total-amount="order?.totalPrice || 0"
        :actual-amount="order?.actualPrice || 0"
        :discount-amount="receiptDiscount"
        :employee-name="order?.employeeName || ''"
        :warehouse-name="order?.warehouseName || ''"
        :pay-method="receiptPayMethod"
        :member-phone="order?.memberPhone || ''"
        :remark="order?.remark || ''"
        :shop-name="'阳光服装店'"
        @close="showReceipt = false"
    />
</template>

<script setup>
import {computed, nextTick, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import orderInterface from '../../../axios/interface/OrderInterface.js'
import {ORDER_STATUS_LABELS} from '../../../constants/orderStatus.js'
import {PAY_METHOD_LABELS} from '../../../constants/payMethod.js'
import ReceiptPreview from '../../../component/checkout/ReceiptPreview.vue'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const loading = ref(true)
const orderStatusLabels = ORDER_STATUS_LABELS
const payMethodLabels = PAY_METHOD_LABELS

onMounted(async () => {
    try {
        order.value = await orderInterface.search(route.params.id)
    } catch {
        order.value = null
    } finally {
        loading.value = false
    }
})

function statusClass(s) {
    if (s === 'COMPLETED') return 'status-ok'
    if (s === 'DRAFT') return 'status-pending'
    if (s === 'REFUND') return 'status-error'
    return ''
}

const showReceipt = ref(false)
const receiptItems = ref([])
const receiptPayMethod = ref('')

const receiptDiscount = computed(() => {
    if (!order.value) return 0
    const total = Number(order.value.totalPrice || 0)
    const actual = Number(order.value.actualPrice || 0)
    return Math.max(0, total - actual)
})

async function printReceipt() {
    if (!order.value) return
    receiptPayMethod.value = payMethodLabels[order.value.payMethod] || order.value.payMethod || ''
    receiptItems.value = (order.value.items || []).map(item => ({
        ...item,
        direction: item.direction || 'IN'
    }))
    showReceipt.value = true
    await nextTick()
    window.print()
}

function goBack() {
    router.push('/manage/order')
}
</script>

<style scoped>
.detail-page {
    width: 100%;
    min-width: 0;
}

.page-heading,
.heading-left {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 28px;
}

.heading-left {
    gap: 12px;
}

.btn-secondary {
    height: 36px;
    padding: 0 14px;
    border: 1px solid var(--border);
    border-radius: 8px;
    background: var(--bg-card);
    color: var(--primary-dark);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.btn-secondary:hover {
    background: var(--bg-hover);
    border-color: var(--primary);
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

.info-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 22px;
}

.info-card {
    width: calc(25% - 11px);
    min-width: 190px;
    min-height: 96px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 10px;
    padding: 18px;
    background: linear-gradient(145deg, var(--bg-card), var(--bg-subtle));
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow);
}

.info-card:nth-child(4n+1) {
    border-top: 3px solid var(--primary);
}

.price-value {
    color: var(--primary);
    font-size: 20px;
    font-weight: 700;
}

.remark-text {
    font-size: 13px;
}

.card {
    padding: 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow);
}

.card-hint {
    font-size: 12px;
    color: var(--text-muted);
}

.data-table td {
    height: 52px;
}

@media (max-width: 900px) {
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }

    .card {
        overflow-x: auto;
    }

    .data-table {
        min-width: 820px;
    }
}

@media (max-width: 560px) {
    .info-card {
        width: 100%;
        min-width: 0;
    }
}</style>
