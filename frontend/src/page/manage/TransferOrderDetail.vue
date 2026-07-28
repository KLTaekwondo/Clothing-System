<template>
    <div class="detail-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回调拨单</button>
                <div>
                    <h2 class="page-title">调拨订单详情</h2>
                    <p v-if="order" class="page-desc">{{ order.transferOrderNo }}</p>
                </div>
            </div>
            <div v-if="order" class="heading-actions">
                <button v-if="order.status === AUDIT_STATUS.DRAFT" class="btn-primary" @click="handleCheck">提交</button>
                <button v-if="order.status === AUDIT_STATUS.CHECKING" class="btn-primary" @click="handleApprove">通过</button>
                <button v-if="order.status === AUDIT_STATUS.CHECKING" class="btn-danger" @click="handleReject">拒绝</button>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!order" class="empty-state">
            <div class="empty-icon"><IconGraphic name="transfer"/></div>
            <div class="empty-text">调拨订单不存在</div>
        </div>
        <template v-else>
            <div class="info-grid">
                <div class="info-card"><span class="info-label">订单编号</span><strong class="code-value">{{ order.transferOrderNo }}</strong></div>
                <div class="info-card"><span class="info-label">状态</span><span :class="statusClass(order.status)" class="status-badge">{{ statusLabels[order.status] || order.status }}</span></div>
                <div class="info-card"><span class="info-label">源仓库</span><strong>{{ order.sourceWareHouseName || '-' }}</strong></div>
                <div class="info-card"><span class="info-label">目标仓库</span><strong>{{ order.targetWareHouseName || '-' }}</strong></div>
                <div class="info-card"><span class="info-label">总金额</span><strong class="price-value">¥{{ order.totalPrice ?? '0.00' }}</strong></div>
                <div class="info-card"><span class="info-label">创建时间</span><strong>{{ order.createTime || '-' }}</strong></div>
                <div class="info-card" style="width:calc(50% - 7px);min-width:190px"><span class="info-label">备注</span><strong class="remark-text">{{ order.remark || '-' }}</strong></div>
            </div>

            <div class="card">
                <div class="card-header" style="margin-bottom:0">
                    <span class="card-title">调拨商品</span>
                    <span class="card-hint">{{ order.items?.length || 0 }} 项</span>
                </div>
                <table v-if="order.items?.length" class="data-table">
                    <thead>
                    <tr>
                        <th>商品名称</th>
                        <th>SKU 名称</th>
                        <th>单价</th>
                        <th>数量</th>
                        <th>小计</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="item in order.items" :key="item.id">
                        <td>{{ item.productName || '-' }}</td>
                        <td>{{ item.skuName || '-' }}</td>
                        <td>¥{{ item.price ?? '0.00' }}</td>
                        <td>{{ item.quantity ?? 0 }}</td>
                        <td><strong>¥{{ item.totalPrice ?? '0.00' }}</strong></td>
                    </tr>
                    </tbody>
                </table>
                <div v-else class="empty-state" style="padding:20px">
                    <div class="empty-icon"><IconGraphic name="product"/></div>
                    <div class="empty-text">无调拨商品</div>
                </div>
            </div>
        </template>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import transferOrderInterface from '../../axios/interface/TransferOrderInterface.js'
import {AUDIT_STATUS, AUDIT_STATUS_LABELS} from '../../constants/auditStatus.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const statusLabels = AUDIT_STATUS_LABELS

const order = ref(null)
const loading = ref(true)

onMounted(async () => {
    try {
        order.value = await transferOrderInterface.search(route.params.id)
    } catch {
        order.value = null
    } finally {
        loading.value = false
    }
})

function statusClass(s) {
    if (s === AUDIT_STATUS.DRAFT) return 'status-pending'
    if (s === AUDIT_STATUS.CHECKING) return 'status-warn'
    if (s === AUDIT_STATUS.APPROVED) return 'status-ok'
    if (s === AUDIT_STATUS.REJECTED) return 'status-error'
    return ''
}

async function handleCheck() {
    try {
        await transferOrderInterface.check(order.value.id)
        toast.success('调拨单已提交，等待审核')
        order.value.status = AUDIT_STATUS.CHECKING
    } catch {}
}

async function handleApprove() {
    try {
        await transferOrderInterface.approve(order.value.id)
        toast.success('调拨单已审核通过，库存已变更')
        order.value.status = AUDIT_STATUS.APPROVED
    } catch {}
}

async function handleReject() {
    try {
        await transferOrderInterface.reject(order.value.id)
        toast.success('已拒绝该调拨单')
        order.value.status = AUDIT_STATUS.REJECTED
    } catch {}
}

function goBack() {
    router.push('/manage/transfer-order')
}
</script>

<style scoped>
.detail-page {
    width: 100%;
    min-width: 0;
}

.page-heading, .heading-left {
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

.heading-actions {
    display: flex;
    gap: 8px;
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

.card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 14px;
    border-bottom: 1px solid var(--border-light);
}

.card-title {
    font-size: 16px;
    font-weight: 700;
}

.card-hint {
    font-size: 12px;
    color: var(--text-muted);
}

.data-table td {
    height: 52px;
}
</style>
