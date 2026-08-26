<template>
    <div class="stock-view-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">查看库存</h2>
                <p class="page-label-desc">按仓库查看全部 SKU 的实时库存数量</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-label">
                    <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                        <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    <span>库存检索</span>
                </div>
                <div class="search-shell">
                    <div class="search-controls">
                        <div class="search-warehouse">
                            <OptionValuePicker
                                v-model="warehousePickerValue"
                                :disabled="warehouseLoading"
                                :options="warehouseOptions"
                                error-message="请从仓库列表中选择有效仓库"
                                placeholder="输入仓库名称或编码筛选"
                                select-placeholder="选择仓库"
                                @update:model-value="onPickWarehouse"
                            />
                        </div>
                        <input
                            v-model="searchCode"
                            class="search-code-input"
                            placeholder="商品编码 / 商品 ID"
                            type="text"
                            @keyup.enter="doSearch"
                        />
                        <button
                            :disabled="loading || !warehouseId || warehouseLoading"
                            class="btn-primary search-button"
                            type="button"
                            @click="doSearch"
                        >{{ loading ? '查询中...' : '查询库存' }}</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="stats-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="stock"/></span>
                <span class="stats-body">
                    <strong>{{ pageInfo.totalElements }}</strong>
                    <span>SKU 总数</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-success"><IconGraphic name="warehouse"/></span>
                <span class="stats-body">
                    <strong>{{ totalStock }}</strong>
                    <span>当前页库存合计</span>
                </span>
            </div>
        </div>

        <div class="stock-table-card">
            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="stockList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="stock"/></div>
                <div class="empty-text">{{ emptyText }}</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>商品</th>
                    <th>SKU</th>
                    <th>规格</th>
                    <th>库存数量</th>
                    <th>状态</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in stockList" :key="item.id">
                    <td>
                        <strong>{{ item.productName || '-' }}</strong>
                        <span class="sub-text">{{ item.productCode || '-' }}</span>
                    </td>
                    <td>
                        <strong>{{ item.skuName || '-' }}</strong>
                        <span class="sub-text"><code>{{ item.skuCode || '-' }}</code></span>
                    </td>
                    <td>{{ formatSpec(item.spec) }}</td>
                    <td>
                        <strong class="stock-quantity" :class="item.stock <= LOW_STOCK ? 'stock-low' : ''">{{ item.stock ?? 0 }}</strong>
                    </td>
                    <td>
                        <span :class="item.stock <= LOW_STOCK ? 'status-badge status-danger' : 'status-badge status-success'">
                            {{ item.stock <= LOW_STOCK ? '低库存' : '正常' }}
                        </span>
                    </td>
                </tr>
                </tbody>
            </table>

            <TablePagination
                v-if="warehouseId && pageInfo.totalPages > 1"
                :loading="loading"
                :page="pageInfo.page"
                :total-elements="pageInfo.totalElements"
                :total-pages="pageInfo.totalPages"
                @change="fetchPage"
            />
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute} from 'vue-router'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../../axios/interface/WareHouseStockInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'
import TablePagination from '../../../component/common/TablePagination.vue'
import IconGraphic from '../../../component/IconGraphic.vue'
import {useToastStore} from '../../../stores/toastStore.js'

const LOW_STOCK = 10
const PAGE_SIZE = 10

const route = useRoute()
const toast = useToastStore()
const warehouses = ref([])
const warehouseId = ref('')
const warehousePickerValue = ref('')
const searchCode = ref('')
const warehouseLoading = ref(false)
const loading = ref(false)
const stockList = ref([])
const searchingSingle = ref(false)

const pageInfo = ref({
    page: 0,
    totalElements: 0,
    totalPages: 0
})

const warehouseOptions = computed(() => warehouses.value.map(item => ({
    id: item.id,
    optionValue: formatWarehouseOption(item)
})))

const totalStock = computed(() => stockList.value.reduce((sum, item) => sum + (item.stock ?? 0), 0))

const emptyText = computed(() => {
    if (!warehouseId.value) return '请先选择仓库'
    if (searchingSingle.value) return '该商品在此仓库暂无库存'
    return '暂无库存数据'
})

onMounted(async () => {
    await loadWarehouses()
    preselectWarehouse()
})

async function loadWarehouses() {
    warehouseLoading.value = true
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    } finally {
        warehouseLoading.value = false
    }
}

// 支持从仓库详情页带 ?warehouseId= 直接定位仓库
function preselectWarehouse() {
    const targetId = Number(route.query.warehouseId)
    if (!targetId) return
    const target = warehouses.value.find(item => Number(item.id) === targetId)
    if (!target) return
    warehouseId.value = target.id
    warehousePickerValue.value = formatWarehouseOption(target)
    fetchPage(0)
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

function onPickWarehouse(value) {
    const selected = warehouses.value.find(item => formatWarehouseOption(item) === value)
    warehouseId.value = selected?.id || ''
    searchCode.value = ''
    searchingSingle.value = false
    stockList.value = []
    pageInfo.value = {page: 0, totalElements: 0, totalPages: 0}
    if (warehouseId.value) {
        fetchPage(0)
    }
}

async function fetchPage(page) {
    if (!warehouseId.value) {
        toast.warning('请先选择仓库')
        return
    }
    searchingSingle.value = false
    loading.value = true
    try {
        const data = await wareHouseStockInterface.searchStockPage(warehouseId.value, page, PAGE_SIZE)
        if (!data) {
            stockList.value = []
            pageInfo.value = {page: 0, totalElements: 0, totalPages: 0}
            return
        }
        stockList.value = data.content || []
        pageInfo.value = {
            page: data.page ?? 0,
            totalElements: data.totalElements ?? 0,
            totalPages: data.totalPages ?? 0
        }
    } catch {
        stockList.value = []
        pageInfo.value = {page: 0, totalElements: 0, totalPages: 0}
    } finally {
        loading.value = false
    }
}

// 空输入查全部分页；输入编码/ID 查单个商品
async function doSearch() {
    if (!warehouseId.value) {
        toast.warning('请先选择仓库')
        return
    }
    const keyword = searchCode.value.trim()
    if (!keyword) {
        fetchPage(0)
        return
    }
    loading.value = true
    try {
        let productId = null
        // 纯数字直接当作商品 ID
        if (/^\d+$/.test(keyword)) {
            productId = Number(keyword)
        } else {
            // 否则按商品编码匹配
            const pageData = await productInterface.searchPage(0, 40)
            const content = pageData?.content || []
            if (pageData && pageData.totalElements > content.length) {
                toast.info('商品数量较多，建议直接输入商品 ID 查询')
            }
            const matched = content.find(item => item.code === keyword)
            if (!matched) {
                toast.info('未找到该编码的商品')
                return
            }
            productId = matched.id
        }
        const records = await wareHouseStockInterface.searchStock(warehouseId.value, productId)
        searchingSingle.value = true
        stockList.value = Array.isArray(records) ? records : []
        pageInfo.value = {
            page: 0,
            totalElements: stockList.value.length,
            totalPages: 1
        }
    } catch {
        stockList.value = []
        pageInfo.value = {page: 0, totalElements: 0, totalPages: 0}
    } finally {
        loading.value = false
    }
}

function formatSpec(spec) {
    if (!spec) return '无规格'
    if (typeof spec === 'string') {
        try {
            spec = JSON.parse(spec)
        } catch {
            return spec
        }
    }
    const entries = Object.entries(spec)
    if (!entries.length) return '无规格'
    return entries.map(([key, value]) => `${key}：${value}`).join(' / ')
}
</script>

<style scoped>
.stock-view-page {
    width: 100%;
    min-width: 0;
}

.stock-view-page .search-shell {
    min-width: min(100%, 700px);
}

.stock-table-card {
    padding: 8px 20px 20px;
    border-radius: 16px;
    border: 1px solid var(--border-light);
    background: var(--bg-card);
    box-shadow: var(--shadow);
}

.stock-view-page .stats-item {
    width: 50%;
}

.stock-quantity {
    font-size: 20px;
    font-weight: 800;
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
    font-variant-numeric: tabular-nums;
    letter-spacing: 0.5px;
}

.stock-low {
    color: var(--error);
    font-weight: 800;
}

.sub-text {
    display: block;
    margin-top: 2px;
    font-size: 12px;
    color: var(--text-muted);
}

.status-danger {
    background: var(--error-light);
    color: var(--error-dark);
}

.status-success {
    background: var(--success-light);
    color: var(--success-dark);
}

@media (max-width: 900px) {
    .stock-table-card {
        overflow-x: auto;
    }

    .stock-table-card .data-table {
        min-width: 640px;
    }
}
</style>