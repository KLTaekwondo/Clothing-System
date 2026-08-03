<template>
    <div class="stock-record-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">库存记录</h2>
                <p class="page-desc">查看库存变动来源、商品 SKU、仓库和操作人</p>
            </div>
            <button class="btn-outline" @click="fetchRecords">↻ 刷新记录</button>
        </div>

        <div class="record-stats">
            <div class="record-stat-card">
                <span class="record-stat-icon"><IconGraphic name="document"/></span>
                <span class="record-stat-value">{{ pageInfo.totalElements }}</span>
                <span class="record-stat-label">全部记录</span>
            </div>
            <div class="record-stat-card">
                <span class="record-stat-icon in-icon">入</span>
                <span class="record-stat-value">{{ inCount }}</span>
                <span class="record-stat-label">当前页入库</span>
            </div>
            <div class="record-stat-card">
                <span class="record-stat-icon out-icon">出</span>
                <span class="record-stat-value">{{ outCount }}</span>
                <span class="record-stat-label">当前页出库</span>
            </div>
            <div class="record-stat-card">
                <span class="record-stat-icon adjust-icon">调</span>
                <span class="record-stat-value">{{ adjustCount }}</span>
                <span class="record-stat-label">当前页调整</span>
            </div>
        </div>

        <div class="record-table-card">
            <div class="table-toolbar">
                <div class="filter-tabs">
                    <button
                        :class="{ active: changeTypeFilter === '' }"
                        class="filter-tab"
                        @click="changeTypeFilter = ''"
                    >
                        全部
                    </button>
                    <button
                        :class="{ active: changeTypeFilter === 'in' }"
                        class="filter-tab"
                        @click="changeTypeFilter = 'in'"
                    >
                        入库
                    </button>
                    <button
                        :class="{ active: changeTypeFilter === 'out' }"
                        class="filter-tab"
                        @click="changeTypeFilter = 'out'"
                    >
                        出库
                    </button>
                    <button
                        :class="{ active: changeTypeFilter === 'adjust' }"
                        class="filter-tab"
                        @click="changeTypeFilter = 'adjust'"
                    >
                        调整
                    </button>
                </div>
                <input
                    v-model="searchQuery"
                    class="record-search"
                    placeholder="搜索单号、商品、SKU、仓库、操作人"
                    type="text"
                />
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="filteredRecords.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="document"/></div>
                <div class="empty-text">暂无库存记录</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>时间</th>
                    <th>来源单号</th>
                    <th>来源类型</th>
                    <th>变动类型</th>
                    <th>商品</th>
                    <th>SKU</th>
                    <th>仓库</th>
                    <th>变动数量</th>
                    <th>库存变化</th>
                    <th>操作人</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in filteredRecords" :key="item.id">
                    <td>{{ item.createTime || '-' }}</td>
                    <td><code>{{ item.sourceNo || '-' }}</code></td>
                    <td>{{ sourceTypeLabels[item.sourceType] || item.sourceType || '-' }}</td>
                    <td>
                        <span :class="changeTypeClass(item.changeType)" class="status-badge">
                            {{ changeTypeLabels[item.changeType] || item.changeType || '-' }}
                        </span>
                    </td>
                    <td>
                        <strong>{{ item.productName || '-' }}</strong>
                        <span class="sub-text">{{ item.productCode || '-' }}</span>
                    </td>
                    <td>
                        <strong>{{ item.skuName || '-' }}</strong>
                        <span class="sub-text">{{ item.skuCode || '-' }}</span>
                    </td>
                    <td>
                        <strong>{{ item.wareHouseName || '-' }}</strong>
                        <span class="sub-text">{{ item.wareHouseCode || '-' }}</span>
                    </td>
                    <td :class="quantityClass(item.changeQuantity)">
                        {{ formatQuantity(item.changeQuantity) }}
                    </td>
                    <td>{{ item.beforeQuantity ?? '-' }} → {{ item.afterQuantity ?? '-' }}</td>
                    <td>
                        <strong>{{ item.operationName || '-' }}</strong>
                        <span class="sub-text">{{ item.operationRole || item.operationCode || '-' }}</span>
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
import stockRecordInterface from '../../../axios/interface/StockRecordInterface.js'
import TablePagination from '../common/TablePagination.vue'

const changeTypeLabels = {
    SALE_OUT: '销售出库',
    SALE_RETURN: '销售退货',
    IMPORT_IN: '采购入库',
    IMPORT_RETURN: '采购退货',
    TRANSFER_IN: '调入',
    TRANSFER_OUT: '调出',
    MANUAL_ADJUST: '手动操作'
}

const sourceTypeLabels = {
    ORDER: '销售订单',
    IMPORT_ORDER: '采购订单',
    TRANSFER_ORDER: '调拨订单',
    MANUAL_ADJUST: '手动操作'
}

const recordList = ref([])
const loading = ref(true)
const searchQuery = ref('')
const changeTypeFilter = ref('')
const pageInfo = ref({
    totalElements: 0,
    totalPages: 0,
    page: 0,
    size: 10
})

const totalPages = computed(() => Math.max(pageInfo.value.totalPages || 1, 1))

const filteredRecords = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    return recordList.value.filter(item => {
        const matchesType = !changeTypeFilter.value || getChangeDirection(item.changeType) === changeTypeFilter.value
        const matchesQuery = !query || [
            item.sourceNo,
            item.productCode,
            item.productName,
            item.skuCode,
            item.skuName,
            item.wareHouseCode,
            item.wareHouseName,
            item.operationCode,
            item.operationName,
            item.operationRole
        ].some(value => String(value || '').toLowerCase().includes(query))
        return matchesType && matchesQuery
    })
})

const inCount = computed(() => recordList.value.filter(item => getChangeDirection(item.changeType) === 'in').length)
const outCount = computed(() => recordList.value.filter(item => getChangeDirection(item.changeType) === 'out').length)
const adjustCount = computed(() => recordList.value.filter(item => getChangeDirection(item.changeType) === 'adjust').length)

onMounted(fetchRecords)

async function fetchRecords() {
    loading.value = true
    try {
        const data = await stockRecordInterface.searchPage(pageInfo.value.page, pageInfo.value.size)
        recordList.value = data.content || []
        pageInfo.value = {
            totalElements: data.totalElements || 0,
            totalPages: data.totalPages || 0,
            page: data.page || 0,
            size: data.size || pageInfo.value.size
        }
    } catch {
        recordList.value = []
    } finally {
        loading.value = false
    }
}

function changePage(page) {
    pageInfo.value.page = page
    fetchRecords()
}

function getChangeDirection(changeType) {
    if (['IMPORT_IN', 'SALE_RETURN', 'TRANSFER_IN'].includes(changeType)) return 'in'
    if (['SALE_OUT', 'IMPORT_RETURN', 'TRANSFER_OUT'].includes(changeType)) return 'out'
    return 'adjust'
}

function changeTypeClass(changeType) {
    const direction = getChangeDirection(changeType)
    if (direction === 'in') return 'status-ok'
    if (direction === 'out') return 'status-error'
    return 'status-pending'
}

function quantityClass(quantity) {
    const value = Number(quantity || 0)
    if (value > 0) return 'quantity-in'
    if (value < 0) return 'quantity-out'
    return 'quantity-normal'
}

function formatQuantity(quantity) {
    const value = Number(quantity || 0)
    if (value > 0) return `+${value}`
    return String(value)
}
</script>

<style scoped>
.stock-record-manage {
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

.record-stats {
    display: flex;
    gap: 14px;
    margin-bottom: 18px;
    flex-wrap: wrap;
}

.record-stat-card {
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

.record-stat-icon {
    width: 38px;
    height: 38px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 11px;
    background: #e8f7f3;
    color: var(--primary);
    font-size: 19px;
    font-weight: 800;
}

.in-icon {
    background: #dcfce7;
    color: #16a34a;
}

.out-icon {
    background: #fee2e2;
    color: #dc2626;
}

.adjust-icon {
    background: #fef3c7;
    color: #d97706;
}

.record-stat-value {
    color: var(--text);
    font-size: 20px;
    font-weight: 800;
    line-height: 1.1;
}

.record-stat-label {
    margin-left: -4px;
    color: var(--text-muted);
    font-size: 12px;
}

.record-table-card {
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
    flex-wrap: wrap;
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

.record-search {
    width: 320px;
    height: 36px;
}

.record-table-card .data-table {
    min-width: 1180px;
}

.record-table-card .data-table td {
    height: 58px;
}

.sub-text {
    display: block;
    margin-top: 4px;
    color: var(--text-muted);
    font-size: 12px;
}

.quantity-in {
    color: #16a34a;
    font-weight: 800;
}

.quantity-out {
    color: #dc2626;
    font-weight: 800;
}

.quantity-normal {
    color: var(--text-secondary);
    font-weight: 800;
}

@media (max-width: 900px) {
    .page-heading,
    .table-toolbar {
        align-items: flex-start;
        flex-direction: column;
    }

    .record-stat-card {
        width: calc(50% - 7px);
    }

    .record-search {
        width: 100%;
    }
}
</style>
