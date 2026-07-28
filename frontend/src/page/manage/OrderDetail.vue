<template>
    <div class="detail-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回订单</button>
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
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import orderInterface from '../../axios/interface/OrderInterface.js'
import {ORDER_STATUS_LABELS} from '../../constants/orderStatus.js'
import {PAY_METHOD_LABELS} from '../../constants/payMethod.js'

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
    background: linear-gradient(145deg, #fff, #fbfefd);
    border: 1px solid #e6f0ef;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(15, 118, 110, 0.06);
}

.info-card:nth-child(4n+1) {
    border-top: 3px solid #0d9488;
}

.info-label {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.code-value {
    color: var(--primary);
    font-family: ui-monospace, "SF Mono", Consolas, monospace;
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
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.card-hint {
    font-size: 12px;
    color: var(--text-muted);
}

.data-table td {
    height: 52px;
}
</style>
