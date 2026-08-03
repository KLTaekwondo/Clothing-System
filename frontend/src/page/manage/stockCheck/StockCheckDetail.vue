<template>
    <div class="stock-check-detail">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    @click="goBack"
                >← 返回盘点单</button>
                <div>
                    <h2 class="page-title">库存盘点单详情</h2>
                    <p
                        v-if="checkOrder"
                        class="page-desc"
                    >{{ checkOrder.stockCheckNo }}</p>
                </div>
            </div>
            <div
                v-if="checkOrder"
                class="heading-actions"
            >
                <button
                    v-if="isDraft"
                    :disabled="saving || !warehouseId"
                    class="btn-outline"
                    @click="handleUpdate"
                >{{ saving ? '保存中...' : '保存修改' }}</button>
                <button
                    v-if="isDraft"
                    :disabled="submitting"
                    class="btn-primary"
                    @click="handleCheck"
                >提交审核</button>
                <button
                    v-if="isDraft"
                    :disabled="deleting"
                    class="btn-danger"
                    @click="handleDelete"
                >{{ deleting ? '删除中...' : '删除草稿' }}</button>
                <button
                    v-if="isChecking"
                    class="btn-primary"
                    @click="handleApprove"
                >审批通过</button>
                <button
                    v-if="isChecking"
                    class="btn-danger"
                    @click="handleReject"
                >拒绝</button>
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
                <input
                    v-if="isDraft"
                    v-model="remark"
                    maxlength="100"
                    placeholder="不超过 100 字"
                    type="text"
                    @input="dirty = true"
                />
                <strong v-else>{{ checkOrder.remark || '-' }}</strong>
                <span
                    v-if="isDraft && !warehouseId"
                    class="warehouse-warning"
                >未能匹配仓库编号，当前草稿无法保存修改</span>
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
                            <input
                                v-if="isDraft"
                                v-model.number="item.actualQuantity"
                                class="quantity-input"
                                min="0"
                                step="1"
                                type="number"
                                @input="normalizeQuantity(item)"
                            />
                            <strong v-else>{{ item.actualQuantity ?? 0 }}</strong>
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
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave, useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import stockCheckInterface from '../../../axios/interface/StockCheckInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import {AUDIT_STATUS, AUDIT_STATUS_LABELS} from '../../../constants/auditStatus.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const statusLabels = AUDIT_STATUS_LABELS
const checkOrder = ref(null)
const items = ref([])
const warehouses = ref([])
const warehouseId = ref(null)
const remark = ref('')
const loading = ref(true)
const saving = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const dirty = ref(false)

const isDraft = computed(() => checkOrder.value?.status === AUDIT_STATUS.DRAFT)
const isChecking = computed(() => checkOrder.value?.status === AUDIT_STATUS.CHECKING)
const differenceCount = computed(() => items.value.filter(item => getDiff(item) !== 0).length)

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        const detail = await stockCheckInterface.search(route.params.id)
        checkOrder.value = detail
        items.value = (detail.stockCheckItems || []).map(item => ({...item}))
        remark.value = detail.remark || ''
        if (detail.status === AUDIT_STATUS.DRAFT) {
            try {
                warehouses.value = await wareHouseInterface.searchList()
                warehouseId.value = warehouses.value.find(item => item.code === detail.wareHouseCode)?.id || null
            } catch {
                warehouses.value = []
                warehouseId.value = null
            }
        }
    } catch {
        checkOrder.value = null
    } finally {
        loading.value = false
    }
})

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(() => {
    if (!dirty.value) return true
    return window.confirm('当前盘点修改尚未保存，确定要离开吗？')
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

function normalizeQuantity(item) {
    const value = Number(item.actualQuantity)
    item.actualQuantity = Number.isInteger(value) && value >= 0 ? value : 0
    dirty.value = true
}

function buildDto() {
    return {
        wareHouseId: Number(warehouseId.value),
        remark: remark.value.trim(),
        stockCheckItems: items.value.map(item => ({
            skuCode: item.skuCode,
            actualQuantity: Number(item.actualQuantity)
        }))
    }
}

async function handleUpdate() {
    if (!warehouseId.value) {
        toast.warning('未能匹配盘点仓库，无法保存修改')
        return
    }
    saving.value = true
    try {
        await stockCheckInterface.update(checkOrder.value.id, buildDto())
        checkOrder.value.remark = remark.value.trim()
        dirty.value = false
        toast.success('盘点单草稿已更新')
    } catch {
    } finally {
        saving.value = false
    }
}

async function handleCheck() {
    if (dirty.value) {
        toast.warning('请先保存当前修改，再提交审核')
        return
    }
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
    if (!window.confirm('审批通过后，将按实际数量更新仓库库存。确定继续吗？')) return
    try {
        await stockCheckInterface.approve(checkOrder.value.id)
        checkOrder.value.status = AUDIT_STATUS.APPROVED
        toast.success('盘点单已审核通过，库存已更新')
    } catch {}
}

async function handleReject() {
    if (!window.confirm('确定拒绝该库存盘点单吗？')) return
    try {
        await stockCheckInterface.reject(checkOrder.value.id)
        checkOrder.value.status = AUDIT_STATUS.REJECTED
        toast.success('已拒绝该盘点单')
    } catch {}
}

async function handleDelete() {
    if (!window.confirm(`确定删除盘点单“${checkOrder.value.stockCheckNo}”吗？`)) return
    deleting.value = true
    try {
        await stockCheckInterface.hardDelete(checkOrder.value.id)
        dirty.value = false
        toast.success('盘点单已删除')
        await router.push('/manage/stock-check')
    } catch {
    } finally {
        deleting.value = false
    }
}

function handleBeforeUnload(event) {
    if (!dirty.value) return
    event.preventDefault()
    event.returnValue = ''
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

.page-heading,
.heading-left,
.heading-actions {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 26px;
}

.heading-left,
.heading-actions {
    gap: 10px;
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

.info-card {
    width: calc(33.33% - 10px);
    min-width: 210px;
    min-height: 96px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 8px;
    padding: 18px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border: 1px solid #e3efed;
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.info-card span,
.info-card small {
    color: var(--text-muted);
    font-size: 12px;
}

.info-card strong {
    font-size: 15px;
}

.code-value {
    color: var(--primary);
    font-family: ui-monospace, "SF Mono", Consolas, monospace;
}

.difference-value {
    color: #d97706;
}

.status-pending {
    color: #d97706;
}

.status-warn {
    color: #2563eb;
}

.status-ok {
    color: #16a34a;
}

.status-error {
    color: #dc2626;
}

.remark-card,
.detail-card {
    padding: 20px;
    margin-bottom: 16px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
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

.remark-card input {
    width: 55%;
    height: 40px;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
}

.warehouse-warning {
    color: #dc2626;
    font-size: 12px;
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

.quantity-input {
    width: 90px;
    height: 36px;
    padding: 0 8px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fbfefd;
    text-align: center;
    font-weight: 700;
}

.diff-positive {
    color: #16a34a;
}

.diff-negative {
    color: #dc2626;
}

.diff-zero {
    color: var(--text-secondary);
}

@media (max-width: 900px) {
    .page-heading,
    .remark-card {
        align-items: flex-start;
        flex-direction: column;
    }

    .info-card {
        width: calc(50% - 7px);
    }

    .remark-card input {
        width: 100%;
    }
}
</style>
