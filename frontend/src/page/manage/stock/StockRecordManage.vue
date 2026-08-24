<template>
    <div class="stock-record-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">库存记录</h2>
                <p class="page-label-desc">查看库存变动来源、商品 SKU、仓库和操作人</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>记录检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <div class="filter-tabs">
                    <button
                        :class="changeTypeFilter === '' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="changeTypeFilter = ''"
                    >全部</button>
                    <button
                        :class="changeTypeFilter === 'in' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="changeTypeFilter = 'in'"
                    >入库</button>
                    <button
                        :class="changeTypeFilter === 'out' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="changeTypeFilter = 'out'"
                    >出库</button>
                    <button
                        :class="changeTypeFilter === 'adjust' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="changeTypeFilter = 'adjust'"
                    >调整</button>
                    <button
                        :class="changeTypeFilter === 'check' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="changeTypeFilter = 'check'"
                    >盘点</button>
                </div>
                <input
                    v-model="searchQuery"
                    class="search-code-input"
                    placeholder="搜索单号、商品、SKU、仓库、操作人"
                    type="text"
                />
                <button
                    class="btn-outline search-button"
                    type="button"
                    @click="fetchRecords"
                >↻ 刷新记录</button>
            </div>
        </div>
            </div>
        </div>

        <div class="stats-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="document"/></span>
                <span class="stats-body">
                    <strong>{{ pageInfo.totalElements }}</strong>
                    <span>全部记录</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-success"><IconGraphic name="check"/></span>
                <span class="stats-body">
                    <strong>{{ inCount }}</strong>
                    <span>当前页入库</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-error"><IconGraphic name="trash"/></span>
                <span class="stats-body">
                    <strong>{{ outCount }}</strong>
                    <span>当前页出库</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-warning"><IconGraphic name="transfer"/></span>
                <span class="stats-body">
                    <strong>{{ adjustCount }}</strong>
                    <span>当前页调整</span>
                </span>
            </div>
        </div>

        <div class="record-table-card">
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
import TablePagination from '../../../component/common/TablePagination.vue'

const changeTypeLabels = {
    SALE_OUT: '销售出库',
    SALE_RETURN: '销售退货',
    IMPORT_IN: '采购入库',
    IMPORT_RETURN: '采购退货',
    TRANSFER_IN: '调入',
    TRANSFER_OUT: '调出',
    MANUAL_ADJUST: '手动操作',
    STOCK_CHECK: '盘点'
}

const sourceTypeLabels = {
    ORDER: '销售订单',
    IMPORT_ORDER: '采购订单',
    TRANSFER_ORDER: '调拨订单',
    MANUAL_ADJUST: '手动操作',
    STOCK_CHECK: '盘点'
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
    if (changeType === 'STOCK_CHECK') return 'check'
    return 'adjust'
}

function changeTypeClass(changeType) {
    const direction = getChangeDirection(changeType)
    if (direction === 'in') return 'status-ok'
    if (direction === 'out') return 'status-error'
    if (direction === 'check') return 'status-warn'
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

.record-table-card {
    padding: 8px 20px 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: var(--shadow);
    overflow-x: auto;
}

.record-table-card .data-table {
    min-width: 1180px;
}

.record-table-card .data-table th,
.record-table-card .data-table td {
    text-align: center;
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
    color: var(--success-dark);
    font-weight: 800;
}

.quantity-out {
    color: var(--error-dark);
    font-weight: 800;
}

.quantity-normal {
    color: var(--text-secondary);
    font-weight: 800;
}

@media (max-width: 900px) {
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }
}
</style>
