<template>
    <div class="stock-check-detail">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">库存盘点单详情</h2>
                <p class="page-label-desc">{{ checkOrder ? checkOrder.stockCheckNo : '加载中...' }}</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            @click="goBack"
                        >← 返回盘点单</button>
                        <template v-if="checkOrder">
                            <router-link
                                v-if="isDraft"
                                class="btn-outline search-button"
                                :to="`/manage/stock-check/${checkOrder.id}/edit`"
                            >编辑草稿</router-link>
                            <button
                                v-if="isDraft"
                                :disabled="submitting"
                                class="btn-primary search-button"
                                @click="handleCheck"
                            >提交审核</button>
                            <button
                                v-if="isDraft"
                                :disabled="deleting"
                                class="btn-danger search-button"
                                @click="handleDelete"
                            >{{ deleting ? '删除中...' : '删除草稿' }}</button>
                            <button
                                v-if="isChecking"
                                class="btn-primary search-button"
                                @click="handleApprove"
                            >审批通过</button>
                            <button
                                v-if="isChecking"
                                class="btn-danger search-button"
                                @click="handleReject"
                            >拒绝</button>
                        </template>
                    </div>
                </div>
            </div>
        </div>

        <div
            v-if="loading"
            class="loading-overlay"
        >
            <div class="loading-spinner"></div>
        </div>
        <div
            v-else-if="!checkOrder"
            class="empty-state"
        >
            <div class="empty-icon"><IconGraphic name="document"/></div>
            <div class="empty-text">库存盘点单不存在</div>
        </div>
        <template v-else>
            <div class="info-list">
                <div class="info-card">
                    <span>盘点单号</span>
                    <strong class="code-value">{{ checkOrder.stockCheckNo }}</strong>
                </div>
                <div class="info-card">
                    <span>状态</span>
                    <strong :class="statusClass(checkOrder.status)">{{ statusLabels[checkOrder.status] || checkOrder.status }}</strong>
                </div>
                <div class="info-card">
                    <span>盘点仓库</span>
                    <strong>{{ checkOrder.wareHouseName || '-' }}</strong>
                    <small>{{ checkOrder.wareHouseCode || '-' }}</small>
                </div>
                <div class="info-card">
                    <span>差异项目</span>
                    <strong :class="{ 'difference-value': differenceCount > 0 }">{{ differenceCount }} 项</strong>
                </div>
                <div class="info-card">
                    <span>创建时间</span>
                    <strong>{{ checkOrder.createTime || '-' }}</strong>
                </div>
                <div class="info-card">
                    <span>更新时间</span>
                    <strong>{{ checkOrder.updateTime || '-' }}</strong>
                </div>
            </div>

            <div class="remark-card">
                <label>备注</label>
                <strong>{{ checkOrder.remark || '-' }}</strong>
            </div>

            <div class="detail-card">
                <div class="card-header">
                    <strong>盘点明细</strong>
                    <span>共 {{ items.length }} 个 SKU</span>
                </div>
                <div
                    v-if="items.length === 0"
                    class="empty-state"
                >
                    <div class="empty-icon"><IconGraphic name="stock"/></div>
                    <div class="empty-text">暂无盘点明细</div>
                </div>
                <table
                    v-else
                    class="data-table"
                >
                    <thead>
                    <tr>
                        <th>商品</th>
                        <th>SKU</th>
                        <th>系统数量</th>
                        <th>实际数量</th>
                        <th>差异数量</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr
                        v-for="item in items"
                        :key="item.id || item.skuCode"
                    >
                        <td>
                            <strong>{{ item.productName || '-' }}</strong>
                            <span class="sub-text">{{ item.productCode || '-' }}</span>
                        </td>
                        <td>
                            <strong>{{ item.skuName || '-' }}</strong>
                            <span class="sub-text">{{ item.skuCode || '-' }}</span>
                        </td>
                        <td>{{ item.systemQuantity ?? 0 }}</td>
                        <td>
                            <strong>{{ item.actualQuantity ?? 0 }}</strong>
                        </td>
                        <td>
                            <strong :class="diffClass(item)">{{ formatDiff(getDiff(item)) }}</strong>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </template>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import {useConfirmStore} from '../../../stores/confirmStore.js'
import stockCheckInterface from '../../../axios/interface/StockCheckInterface.js'
import {AUDIT_STATUS, AUDIT_STATUS_LABELS} from '../../../constants/auditStatus.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const confirmStore = useConfirmStore()
const statusLabels = AUDIT_STATUS_LABELS
const checkOrder = ref(null)
const items = ref([])
const loading = ref(true)
const submitting = ref(false)
const deleting = ref(false)

const isDraft = computed(() => checkOrder.value?.status === AUDIT_STATUS.DRAFT)
const isChecking = computed(() => checkOrder.value?.status === AUDIT_STATUS.CHECKING)
const differenceCount = computed(() => items.value.filter(item => getDiff(item) !== 0).length)

onMounted(async () => {
    try {
        const detail = await stockCheckInterface.search(route.params.id)
        checkOrder.value = detail
        items.value = (detail.stockCheckItems || []).map(item => ({...item}))
    } catch {
        checkOrder.value = null
    } finally {
        loading.value = false
    }
})

function statusClass(status) {
    if (status === AUDIT_STATUS.DRAFT) return 'status-pending'
    if (status === AUDIT_STATUS.CHECKING) return 'status-warn'
    if (status === AUDIT_STATUS.APPROVED) return 'status-ok'
    if (status === AUDIT_STATUS.REJECTED) return 'status-error'
    return ''
}

function getDiff(item) {
    return Number(item.actualQuantity || 0) - Number(item.systemQuantity || 0)
}

function diffClass(item) {
    const value = getDiff(item)
    if (value > 0) return 'diff-positive'
    if (value < 0) return 'diff-negative'
    return 'diff-zero'
}

function formatDiff(value) {
    if (value > 0) return `+${value}`
    return String(value)
}

async function handleCheck() {
    submitting.value = true
    try {
        await stockCheckInterface.check(checkOrder.value.id)
        checkOrder.value.status = AUDIT_STATUS.CHECKING
        toast.success('盘点单已提交，等待审核')
    } catch {
    } finally {
        submitting.value = false
    }
}

async function handleApprove() {
    const confirmed = await confirmStore.confirm({
        title: '通过库存盘点单？',
        message: '审批通过后，将按实际数量更新仓库库存。',
        confirmText: '确认通过'
    })
    if (!confirmed) return
    try {
        await stockCheckInterface.approve(checkOrder.value.id)
        checkOrder.value.status = AUDIT_STATUS.APPROVED
        toast.success('盘点单已审核通过，库存已更新')
    } catch {}
}

async function handleReject() {
    const confirmed = await confirmStore.confirm({
        title: '拒绝库存盘点单？',
        message: '拒绝后该盘点单将不能继续审核。',
        confirmText: '确认拒绝',
        danger: true
    })
    if (!confirmed) return
    try {
        await stockCheckInterface.reject(checkOrder.value.id)
        checkOrder.value.status = AUDIT_STATUS.REJECTED
        toast.success('已拒绝该盘点单')
    } catch {}
}

async function handleDelete() {
    const confirmed = await confirmStore.confirm({
        title: '删除库存盘点单？',
        message: `确定删除盘点单“${checkOrder.value.stockCheckNo}”吗？删除后无法恢复。`,
        confirmText: '确认删除',
        danger: true
    })
    if (!confirmed) return
    deleting.value = true
    try {
        await stockCheckInterface.hardDelete(checkOrder.value.id)
        toast.success('盘点单已删除')
        await router.push('/manage/stock-check')
    } catch {
    } finally {
        deleting.value = false
    }
}

function goBack() {
    router.push('/manage/stock-check')
}
</script>

<style scoped>
.stock-check-detail {
    width: 100%;
    min-width: 0;
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

.info-list {
    display: flex;
    gap: 14px;
    margin-bottom: 16px;
    flex-wrap: wrap;
}

.info-card span,
.info-card small {
    color: var(--text-muted);
    font-size: 12px;
}

.info-card strong {
    font-size: 15px;
}

.difference-value {
    color: var(--warning-dark);
}

.status-pending {
    color: var(--warning-dark);
}

.status-warn {
    color: var(--info-dark);
}

.status-ok {
    color: var(--success-dark);
}

.status-error {
    color: var(--error-dark);
}

.remark-card,
.detail-card {
    padding: 20px;
    margin-bottom: 16px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow);
}

.remark-card {
    display: flex;
    align-items: center;
    gap: 14px;
}

.remark-card label {
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 700;
}

.card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-bottom: 14px;
    border-bottom: 1px solid var(--border-light);
}

.card-header span {
    color: var(--text-muted);
    font-size: 12px;
}

.data-table td {
    height: 58px;
}

.sub-text {
    display: block;
    margin-top: 4px;
    color: var(--text-muted);
    font-size: 12px;
}

@media (max-width: 900px) {
    .remark-card {
        align-items: flex-start;
        flex-direction: column;
    }

    .info-card {
        width: calc(50% - 7px);
    }
}</style>
