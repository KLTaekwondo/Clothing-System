<template>
    <div class="draft-page">
        <header class="draft-header">
            <div class="header-left">
                <button
                    class="back-button"
                    @click="goCheckout"
                >← 返回收银台</button>
                <div>
                    <h1>挂单查询</h1>
                    <p>查看当前仓库尚未完成的收银订单</p>
                </div>
            </div>
            <div class="header-actions">
                <span>共 {{ pageInfo.totalElements }} 笔挂单</span>
                <button
                    :disabled="loading"
                    class="refresh-button"
                    @click="fetchDrafts"
                >{{ loading ? '刷新中...' : '刷新' }}</button>
            </div>
        </header>

        <section class="draft-toolbar">
            <div class="search-box">
                <IconGraphic name="search"/>
                <input
                    v-model="searchQuery"
                    placeholder="搜索订单编号、员工或备注"
                    type="text"
                />
            </div>
            <div class="summary-list">
                <span>当前页 {{ draftList.length }} 笔</span>
                <strong>实付合计 ¥{{ pageActualAmount }}</strong>
            </div>
        </section>

        <div
            v-if="loading"
            class="loading-state"
        >
            <div class="loading-spinner"></div>
            <span>正在读取挂单...</span>
        </div>

        <div
            v-else-if="filteredDrafts.length === 0"
            class="empty-panel"
        >
            <IconGraphic name="order"/>
            <strong>{{ draftList.length ? '没有符合条件的挂单' : '当前仓库暂无挂单' }}</strong>
            <span>{{ draftList.length ? '可以修改关键词重新搜索' : '在收银台添加商品后即可挂单' }}</span>
            <button
                v-if="!draftList.length"
                class="checkout-link"
                @click="goCheckout"
            >返回收银台</button>
        </div>

        <div
            v-else
            class="draft-list"
        >
            <article
                v-for="order in filteredDrafts"
                :key="order.id"
                class="draft-card"
            >
                <div class="card-heading">
                    <div class="order-identity">
                        <span>挂单编号</span>
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
                        <span>原始金额</span>
                        <strong>¥{{ formatMoney(order.totalPrice) }}</strong>
                    </div>
                </div>

                <div class="order-meta">
                    <span>{{ order.createTime || '-' }}</span>
                    <p>{{ order.remark || '无备注' }}</p>
                </div>

                <div class="card-actions">
                    <button
                        class="delete-button"
                        @click="confirmDelete(order)"
                    >删除挂单</button>
                    <button
                        class="restore-button"
                        @click="restoreOrder(order)"
                    >取单继续</button>
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
            v-if="deleteTarget"
            class="confirm-overlay"
            @click.self="closeDeleteConfirm"
        >
            <div class="confirm-dialog">
                <div class="confirm-content">
                    <IconGraphic name="warning"/>
                    <strong>删除这笔挂单？</strong>
                    <span>{{ deleteTarget.orderNo }}</span>
                    <p>删除后无法恢复，订单中的商品不会结算。</p>
                </div>
                <div class="confirm-actions">
                    <button
                        class="cancel-button"
                        @click="closeDeleteConfirm"
                    >取消</button>
                    <button
                        :disabled="deleting"
                        class="confirm-delete-button"
                        @click="deleteDraft"
                    >{{ deleting ? '删除中...' : '确认删除' }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import orderInterface from '../../axios/interface/OrderInterface.js'
import {PAY_METHOD_LABELS} from '../../constants/payMethod.js'
import IconGraphic from '../../component/IconGraphic.vue'
import TablePagination from '../manage/common/TablePagination.vue'

const router = useRouter()
const draftList = ref([])
const searchQuery = ref('')
const loading = ref(true)
const deleting = ref(false)
const deleteTarget = ref(null)
const pageInfo = ref({
    totalElements: 0,
    totalPages: 0,
    page: 0,
    size: 9
})

const totalPages = computed(() => Math.max(pageInfo.value.totalPages || 1, 1))
const pageActualAmount = computed(() => {
    return draftList.value.reduce((sum, order) => sum + Number(order.actualPrice || 0), 0).toFixed(2)
})

const filteredDrafts = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    if (!query) return draftList.value
    return draftList.value.filter(order => {
        return [order.orderNo, order.employeeName, order.remark]
            .some(value => String(value || '').toLowerCase().includes(query))
    })
})

onMounted(fetchDrafts)

async function fetchDrafts() {
    loading.value = true
    try {
        const data = await orderInterface.searchCurrentDraftPage(pageInfo.value.page, pageInfo.value.size)
        draftList.value = data.content || []
        pageInfo.value = {
            totalElements: data.totalElements || 0,
            totalPages: data.totalPages || 0,
            page: data.page || 0,
            size: data.size || pageInfo.value.size
        }
    } catch {
        draftList.value = []
    } finally {
        loading.value = false
    }
}

function changePage(page) {
    pageInfo.value.page = page
    searchQuery.value = ''
    fetchDrafts()
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

function restoreOrder(order) {
    router.push({
        path: '/checkout',
        query: {
            draftId: order.id
        }
    })
}

function confirmDelete(order) {
    deleteTarget.value = order
}

function closeDeleteConfirm() {
    deleteTarget.value = null
}

async function deleteDraft() {
    if (!deleteTarget.value) return
    deleting.value = true
    try {
        await orderInterface.hardDelete(deleteTarget.value.id)
        closeDeleteConfirm()
        if (draftList.value.length === 1 && pageInfo.value.page > 0) {
            pageInfo.value.page--
        }
        await fetchDrafts()
    } catch {
    } finally {
        deleting.value = false
    }
}

function goCheckout() {
    router.push('/checkout')
}
</script>

<style scoped>
.draft-page {
    width: 100%;
    min-height: 100vh;
    padding: 28px 34px;
    background: #f3f8f7;
}

.draft-header {
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
.cancel-button {
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
.cancel-button:hover {
    background: #e9f8f5;
    border-color: #0d9488;
}

.draft-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 18px;
    padding: 12px 14px;
    margin-bottom: 16px;
    background: #fff;
    border: 1px solid #dceae7;
    border-radius: 10px;
    box-shadow: 0 5px 18px rgba(22, 83, 78, 0.05);
}

.search-box {
    width: 390px;
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

.draft-list {
    display: flex;
    gap: 14px;
    flex-wrap: wrap;
}

.draft-card {
    width: calc(33.33% - 10px);
    min-width: 300px;
    padding: 18px;
    background: #fff;
    border: 1px solid #dceae7;
    border-radius: 12px;
    box-shadow: 0 6px 20px rgba(22, 83, 78, 0.06);
    transition: border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.draft-card:hover {
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

.card-actions,
.confirm-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 14px;
}

.delete-button,
.restore-button,
.confirm-delete-button {
    height: 34px;
    padding: 0 14px;
    border-radius: 8px;
    font-size: 12px;
    font-weight: 700;
}

.delete-button {
    color: #dc2626;
    background: #fff;
    border: 1px solid #fecaca;
}

.delete-button:hover {
    background: #fef2f2;
}

.restore-button {
    color: #fff;
    background: #0d9488;
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.2);
}

.restore-button:hover {
    background: #0f766e;
    transform: translateY(-1px);
}

.confirm-overlay {
    position: fixed;
    inset: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(15, 23, 42, 0.38);
}

.confirm-dialog {
    width: 400px;
    overflow: hidden;
    background: #fff;
    border-radius: 14px;
    box-shadow: 0 22px 60px rgba(15, 23, 42, 0.22);
}

.confirm-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 9px;
    padding: 26px 24px 18px;
    text-align: center;
}

.confirm-content :deep(img) {
    width: 44px;
    height: 44px;
}

.confirm-content span,
.confirm-content p {
    color: #78908d;
    font-size: 12px;
}

.confirm-actions {
    padding: 13px 18px;
    background: #f7fbfa;
    border-top: 1px solid #edf4f2;
}

.confirm-delete-button {
    color: #fff;
    background: #dc2626;
}

.confirm-delete-button:hover {
    background: #b91c1c;
}

@media (max-width: 1050px) {
    .draft-card {
        width: calc(50% - 7px);
    }
}

@media (max-width: 720px) {
    .draft-page {
        padding: 20px;
    }

    .draft-header,
    .draft-toolbar {
        align-items: flex-start;
        flex-direction: column;
    }

    .header-left {
        align-items: flex-start;
        flex-direction: column;
    }

    .search-box,
    .draft-card {
        width: 100%;
        min-width: 0;
    }
}
</style>
