<template>
    <div class="stock-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">库存管理</h2>
                <p class="page-desc">查询商品后添加到下方表单，可同时维护多个商品库存</p>
            </div>
        </div>

        <div class="card search-panel">
            <div class="search-row">
                <div class="field">
                    <label>仓库</label>
                    <select v-model="warehouseId" class="wh-select">
                        <option disabled value="">请选择仓库</option>
                        <option v-for="item in warehouses" :key="item.id" :value="item.id">{{ item.name }}</option>
                    </select>
                </div>
                <div class="field field-product">
                    <label>商品编码</label>
                    <input v-model="searchCode" placeholder="输入商品编码" type="text" @keyup.enter="searchProduct"/>
                </div>
                <button :disabled="loading || !warehouseId || !searchCode" class="btn-primary search-button"
                        @click="searchProduct">
                    {{ loading ? '查询中...' : '查询并添加' }}
                </button>
            </div>
        </div>

        <div v-if="formList.length === 0" class="card empty-form-card">
            <div class="empty-state">
                <div class="empty-icon"><IconGraphic name="stock"/></div>
                <div class="empty-text">选择仓库并查询商品，库存表单会显示在这里</div>
            </div>
        </div>

        <div v-if="formList.length > 0" class="stock-form-actions-bar">
            <span class="total-hint">共 {{ formList.length }} 件商品</span>
            <span v-if="hasAnyChanges" class="changed-hint">有未保存的修改</span>
            <button :disabled="savingAll || !hasAnyChanges" class="btn-primary" @click="saveAll">
                {{ savingAll ? '保存中...' : '保存全部库存' }}
            </button>
            <button class="btn-outline btn-sm" @click="clearAll">清空全部</button>
        </div>

        <div v-if="formList.length > 0" class="stock-form-list">
            <div v-for="(item, index) in formList" :key="item.key" class="stock-product-card card">
                <div class="product-form-header" @click="item.expanded = !item.expanded">
                    <div class="product-summary">
                        <span class="product-mark"><IconGraphic name="product"/></span>
                        <div>
                            <strong class="product-name">{{ item.product.name }}</strong>
                            <span class="product-code">{{ item.product.code }}</span>
                        </div>
                    </div>
                    <div class="product-form-actions">
                        <span v-if="item.hasChanges" class="changed-hint">有修改</span>
                        <button class="btn-danger btn-sm" @click.stop="removeItem(index)">移除</button>
                        <span class="expand-icon">{{ item.expanded ? '▼' : '▶' }}</span>
                    </div>
                </div>

                <div v-show="item.expanded" class="matrix-wrapper">
                    <div v-if="item.rowHeaders.length === 0" class="empty-state compact-empty">
                        <div class="empty-text">该商品无规格数据</div>
                    </div>
                    <div v-else class="table-wrap">
                        <table class="matrix-table">
                            <thead>
                            <tr>
                                <th class="corner-cell">{{ item.rowLabel }}</th>
                                <th v-for="col in item.colHeaders" :key="col" class="col-header">{{ col }}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr v-for="(row, ri) in item.rowHeaders" :key="ri">
                                <td class="row-header">{{ row }}</td>
                                <td v-for="(col, ci) in item.colHeaders" :key="ci" class="cell-input">
                                    <input v-model.number="item.cells[ri][ci].stock"
                                           :class="{ changed: item.cells[ri][ci].changed }" class="stock-input"
                                           min="0" type="number"
                                           @input="item.cells[ri][ci].changed = true; markChanged(item)"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../../axios/interface/WareHouseStockInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import {
    buildSkuMatrix,
    createStockBySpec,
    fillMatrixFromStock,
    parseSkuSpecs
} from './utils/skuMatrix.js'

const toast = useToastStore()
const warehouses = ref([])
const warehouseId = ref('')
const searchCode = ref('')
const loading = ref(false)
const savingAll = ref(false)
const formList = ref([])
let keyCounter = 0

const hasAnyChanges = computed(() => formList.value.some(item => item.hasChanges))

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    }
})

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(() => {
    if (!hasAnyChanges.value) return true
    return window.confirm('当前有未保存的库存修改，确定要离开吗？')
})

async function searchProduct() {
    if (!warehouseId.value || !searchCode.value) {
        toast.warning('请选择仓库并输入编码');
        return
    }
    loading.value = true
    try {
        const data = await productInterface.searchPage()
        const all = data.content || []
        const matched = all.find(p => p.code === searchCode.value)
        if (!matched) {
            toast.info('未找到该编码的商品');
            return
        }

        const product = await productInterface.search(matched.id)
        const skuList = await productSkuInterface.searchListByProductId(matched.id)
        if (!skuList.length) {
            toast.info('该商品没有 SKU');
            return
        }

        // 构建矩阵
        const block = buildStockBlock(product, skuList)
        if (block) {
            formList.value.push(block)
            toast.success(`已添加「${product.name}」`)
            searchCode.value = ''
        }
    } catch {
        toast.info('查询失败')
    } finally {
        loading.value = false
    }
}

function buildStockBlock(product, skuList) {
    const specs = parseSkuSpecs(skuList)
    const matrix = buildSkuMatrix(specs, '库存', sku => ({
        stock: 0,
        changed: false,
        stockId: null,
        skuSpec: sku?.parsedSpec || null
    }))

    loadStock(product.id, matrix)

    return {
        key: ++keyCounter,
        product,
        expanded: true,
        rowLabel: matrix.rowLabel,
        rowHeaders: matrix.rowHeaders,
        colHeaders: matrix.colHeaders,
        cells: matrix.cells,
        hasChanges: false
    }
}

async function loadStock(productId, matrix) {
    try {
        const records = await wareHouseStockInterface.searchStock(warehouseId.value, productId)
        if (!Array.isArray(records)) return
        const stockBySpec = createStockBySpec(records, record => ({
            stockId: record.id,
            stock: record.stock ?? 0
        }))
        fillMatrixFromStock(matrix, stockBySpec, (cell, stock) => {
            cell.stock = stock.stock
            cell.stockId = stock.stockId
        })
    } catch {
    }
}

function markChanged(item) {
    item.hasChanges = item.cells.some(row => row.some(c => c.changed))
}

async function saveAll() {
    savingAll.value = true
    try {
        const allUpdates = []
        formList.value.forEach(item => {
            item.cells.forEach(row => row.forEach(cell => {
                if (cell.changed && cell.stockId != null) {
                    allUpdates.push({stockId: cell.stockId, stock: cell.stock})
                }
            }))
        })
        if (!allUpdates.length) {
            toast.info('没有需要保存的修改');
            return
        }
        await wareHouseStockInterface.batchUpdateStock(allUpdates)
        // 后端已返回提示
        formList.value.forEach(item => {
            item.cells.forEach(row => row.forEach(c => {
                c.changed = false
            }))
            item.hasChanges = false
        })
    } catch {
    } finally {
        savingAll.value = false
    }
}

function clearAll() {
    if (hasAnyChanges.value && !window.confirm('当前有未保存的修改，确定要清空全部吗？')) return
    formList.value = []
}

function removeItem(index) {
    const item = formList.value[index]
    if (item?.hasChanges && !window.confirm(`「${item.product.name}」有未保存的修改，确定要移除吗？`)) return
    formList.value.splice(index, 1)
}

function handleBeforeUnload(event) {
    if (!hasAnyChanges.value) return
    event.preventDefault()
    event.returnValue = ''
}
</script>

<style scoped>
.stock-manage {
    width: 100%;
    min-width: 0;
}

.page-heading {
    margin-bottom: 24px;
}

.page-heading .page-title {
    margin-bottom: 6px;
    font-size: 26px;
    letter-spacing: -0.5px;
}

.page-heading .page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.search-panel {
    padding: 20px;
    margin-bottom: 16px;
    border-radius: 16px;
}

.search-row {
    display: flex;
    gap: 16px;
    align-items: flex-end;
    flex-wrap: wrap;
}

.field {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.field label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
}

.wh-select {
    width: 220px;
    height: 40px;
    padding: 0 28px 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
    appearance: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364807e' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 10px center;
}

.search-row input {
    width: 320px;
    height: 40px;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
}

.search-button {
    height: 40px;
}

.empty-form-card {
    border-radius: 16px;
}

.stock-form-actions-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    margin-bottom: 14px;
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.total-hint {
    flex: 1;
    font-size: 14px;
    color: var(--text-secondary);
}

.stock-form-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.stock-product-card {
    padding: 0;
    overflow: hidden;
    border-radius: 16px;
}

.product-form-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
    border-bottom: 1px solid var(--border-light);
}

.product-form-header:hover {
    background: #f4fbfa;
}

.product-summary {
    display: flex;
    align-items: center;
    gap: 12px;
}

.product-mark {
    font-size: 22px;
}

.product-name {
    font-size: 15px;
}

.product-code {
    font-size: 12px;
    color: var(--text-secondary);
}

.product-form-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

.expand-icon {
    font-size: 12px;
    color: var(--text-muted);
    width: 16px;
    text-align: center;
}

.changed-hint {
    font-size: 11px;
    color: #d97706;
    font-weight: 600;
}

.matrix-wrapper {
    padding: 12px 20px 16px;
}

.table-wrap {
    min-width: 480px;
    overflow-x: auto;
}

.matrix-table {
    width: 100%;
    border-collapse: collapse;
}

.matrix-table th {
    padding: 8px 14px;
    text-align: center;
    font-weight: 700;
    color: var(--text-secondary);
    background: #fafafa;
    border-bottom: 2px solid #dceae7;
    white-space: nowrap;
}

.corner-cell {
    text-align: left;
    min-width: 90px;
}

.col-header {
    min-width: 82px;
}

.matrix-table td {
    padding: 6px;
    border-bottom: 1px solid #edf4f2;
    text-align: center;
}

.row-header {
    font-weight: 700;
    color: var(--text);
    text-align: left;
    padding: 6px 14px;
}

.cell-input {
    padding: 3px 5px !important;
}

.stock-input {
    width: 72px;
    height: 36px;
    padding: 0;
    border: 1px solid #dceae7;
    border-radius: 8px;
    text-align: center;
    font-weight: 700;
    font-size: 15px;
    background: #fbfefd;
    transition: border-color 0.2s, background 0.2s;
}

.stock-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.stock-input.changed {
    background: #fffbeb;
    border-color: #f59e0b;
}

.matrix-table tbody tr:hover td {
    background: #f4fbfa;
}
</style>
