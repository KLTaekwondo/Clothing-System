<template>
    <div class="stock-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">库存转移</h2>
                <p class="page-desc">选择源仓库和目标仓库，查询商品后批量转移库存</p>
            </div>
        </div>

        <div class="card search-panel">
            <div class="search-row">
                <div class="field"><label>源仓库</label>
                    <select v-model="sourceId" class="wh-select" @change="clearAll">
                        <option disabled value="">请选择源仓库</option>
                        <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
                    </select>
                </div>
                <div class="field"><label>目标仓库</label>
                    <select v-model="targetId" class="wh-select" @change="clearAll">
                        <option disabled value="">请选择目标仓库</option>
                        <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
                    </select>
                </div>
                <div class="field field-product"><label>商品编码</label>
                    <input v-model="searchCode" placeholder="输入商品编码" type="text" @keyup.enter="searchProduct"/>
                </div>
                <button :disabled="searching || !sourceId || !targetId || !searchCode" class="btn-primary search-button"
                        @click="searchProduct">{{ searching ? '查询中...' : '查询并添加' }}
                </button>
            </div>
            <div v-if="sourceId && targetId && sourceId === targetId" class="warning-text">源仓库和目标仓库不能相同
            </div>
        </div>

        <div v-if="formList.length === 0" class="card empty-form-card">
            <div class="empty-state">
                <div class="empty-icon">🔄</div>
                <div class="empty-text">选择仓库并查询商品，SKU 会显示在这里</div>
            </div>
        </div>

        <div v-if="formList.length > 0" class="stock-form-actions-bar">
            <span class="total-hint">共 {{ formList.length }} 件商品</span>
            <span v-if="hasAnyTransfer" class="changed-hint">有未提交的转移</span>
            <button :disabled="transferring || !hasAnyTransfer" class="btn-primary" @click="transferAll">
                {{ transferring ? '转移中...' : '确认全部转移' }}
            </button>
            <button class="btn-outline btn-sm" @click="clearAll">清空全部</button>
        </div>

        <div v-if="formList.length > 0" class="stock-form-list">
            <div v-for="(item, index) in formList" :key="item.key" class="stock-product-card card">
                <div class="product-form-header" @click="item.expanded = !item.expanded">
                    <div class="product-summary">
                        <span class="product-mark">📦</span>
                        <div><strong class="product-name">{{ item.product.name }}</strong><span
                            class="product-code">{{ item.product.code }}</span></div>
                    </div>
                    <div class="product-form-actions">
                        <span v-if="item.hasTransfer" class="changed-hint">有数量</span>
                        <button class="btn-danger btn-sm" @click.stop="removeItem(index)">移除</button>
                        <span class="expand-icon">{{ item.expanded ? '▼' : '▶' }}</span>
                    </div>
                </div>

                <div v-show="item.expanded" class="matrix-wrapper">
                    <div v-if="item.rowHeaders.length === 0" class="empty-state compact-empty">
                        <div class="empty-text">无规格数据</div>
                    </div>
                    <div v-else class="table-wrap">
                        <table class="matrix-table">
                            <thead>
                            <tr>
                                <th class="corner-cell">{{ item.rowLabel }}</th>
                                <th v-for="col in item.colHeaders" :key="col" class="col-header">{{ col }}</th>
                                <th v-if="item.colHeaders.length > 1" class="col-header stock-col">源库存</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr v-for="(row, ri) in item.rowHeaders" :key="ri">
                                <td class="row-header">{{ row }}</td>
                                <td v-for="(col, ci) in item.colHeaders" :key="ci" class="cell-input">
                                    <input v-model.number="item.cells[ri][ci].qty"
                                           :class="{ filled: item.cells[ri][ci].qty > 0 }"
                                           :placeholder="String(item.cells[ri][ci].available)"
                                           class="stock-input" min="0"
                                           type="number"
                                           @input="markTransfer(item)"/>
                                </td>
                                <td v-if="item.colHeaders.length > 1" class="cell-available">
                                    {{ item.cells[ri][0]?.available ?? 0 }}
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <div v-if="item.colHeaders.length === 1" class="inline-available">
                            源库存：<strong>{{ item.cells[0]?.[0]?.available ?? 0 }}</strong>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useToastStore} from '../../stores/toastStore.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../axios/interface/WareHouseStockInterface.js'
import productInterface from '../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'

const toast = useToastStore()
const warehouses = ref([])
const sourceId = ref('')
const targetId = ref('')
const searchCode = ref('')
const searching = ref(false)
const transferring = ref(false)
const formList = ref([])
let keyCounter = 0

const hasAnyTransfer = computed(() => formList.value.some(item => item.hasTransfer))

onMounted(async () => {
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
    }
})

function clearAll() {
    formList.value = []
}

async function searchProduct() {
    if (!sourceId.value || !targetId.value) {
        toast.warning('请选择源仓库和目标仓库');
        return
    }
    if (sourceId.value === targetId.value) {
        toast.warning('源仓库和目标仓库不能相同');
        return
    }
    if (!searchCode.value) return
    searching.value = true
    try {
        const all = await productInterface.searchList()
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

        const block = buildBlock(product, skuList)
        if (block) {
            formList.value.push(block);
            toast.success(`已添加「${product.name}」`);
            searchCode.value = ''
        }
    } catch {
        toast.info('查询失败')
    } finally {
        searching.value = false
    }
}

function buildBlock(product, skuList) {
    const specs = skuList.map(sku => {
        let spec = sku.spec || '{}'
        try {
            spec = JSON.parse(spec)
        } catch {
            spec = {}
        }
        return {...sku, parsedSpec: spec}
    })
    const allKeys = [...new Set(specs.flatMap(s => Object.keys(s.parsedSpec)))]
    let rowHeaders, colHeaders, rowLabel

    if (allKeys.length <= 1) {
        const key = allKeys[0]
        rowLabel = key || 'SKU'
        rowHeaders = key ? [...new Set(specs.map(s => s.parsedSpec[key] || '未知'))] : specs.map(s => `${s.name} (${s.code})`)
        colHeaders = ['转移数量']
    } else {
        rowLabel = allKeys[0]
        rowHeaders = [...new Set(specs.map(s => s.parsedSpec[allKeys[0]] || '未知'))]
        colHeaders = [...new Set(specs.map(s => s.parsedSpec[allKeys[1]] || '未知'))]
    }

    const cells = rowHeaders.map(rv =>
        colHeaders.map(cv => {
            let sku = null
            if (allKeys.length <= 1) {
                sku = allKeys.length === 0 ? specs[rowHeaders.indexOf(rv)] : specs.find(s => s.parsedSpec[allKeys[0]] === rv)
            } else {
                sku = specs.find(s => s.parsedSpec[allKeys[0]] === rv && s.parsedSpec[allKeys[1]] === cv)
            }
            return {qty: 0, available: 0, skuId: sku?.id || null, skuSpec: sku?.parsedSpec}
        })
    )

    loadStock(product.id, specs, cells, rowHeaders, colHeaders, rowLabel)

    return {key: ++keyCounter, product, expanded: true, rowLabel, rowHeaders, colHeaders, cells, hasTransfer: false}
}

async function loadStock(productId, specs, cells, rowHeaders, colHeaders, rowLabel) {
    try {
        const records = await wareHouseStockInterface.searchStock(sourceId.value, productId)
        if (!Array.isArray(records)) return
        const stockBySpec = {}
        records.forEach(r => {
            stockBySpec[JSON.stringify(r.spec || {})] = r.stock ?? 0
        })

        if (rowLabel === 'SKU' || (colHeaders.length === 1 && colHeaders[0] === '转移数量')) {
            rowHeaders.forEach((rv, ri) => {
                if (specs[ri]) {
                    const found = stockBySpec[JSON.stringify(specs[ri].parsedSpec)];
                    if (found !== undefined) {
                        cells[ri][0].available = found
                    }
                }
            })
        } else {
            rowHeaders.forEach((rv, ri) => {
                colHeaders.forEach((cv, ci) => {
                    const sku = specs.find(s => Object.keys(s.parsedSpec).length >= 2 && s.parsedSpec[rowLabel] === rv && s.parsedSpec[Object.keys(s.parsedSpec).find(k => k !== rowLabel)] === cv)
                    if (sku) {
                        const found = stockBySpec[JSON.stringify(sku.parsedSpec)];
                        if (found !== undefined) {
                            cells[ri][ci].available = found
                        }
                    }
                })
            })
        }
    } catch {
    }
}

function markTransfer(item) {
    item.hasTransfer = item.cells.some(row => row.some(c => c.qty > 0))
}

async function transferAll() {
    const allItems = []
    formList.value.forEach(item => {
        item.cells.forEach(row => row.forEach(cell => {
            if (cell.qty > 0 && cell.skuId != null) {
                allItems.push({
                    sourceWarehouseId: Number(sourceId.value),
                    targetWarehouseId: Number(targetId.value),
                    skuId: cell.skuId,
                    stock: cell.qty
                })
            }
        }))
    })
    if (!allItems.length) {
        toast.info('没有需要转移的数量');
        return
    }
    transferring.value = true
    const failed = []
    let count = 0
    for (const item of allItems) {
        try {
            await wareHouseStockInterface.transferStock(item);
            count++
        } catch {
            failed.push(item)
        }
    }
    if (count > 0) {
        toast.success(`转移完成：成功 ${count} 个，失败 ${failed.length} 个`)
        if (failed.length === 0) {
            formList.value = []
        } else {
            // 标记失败的单元格不清除，只清除成功项
            formList.value.forEach(item => {
                item.cells.forEach(row => row.forEach(cell => {
                    if (cell.qty > 0 && cell.skuId != null && !failed.some(f => f.skuId === cell.skuId && f.stock === cell.qty)) {
                        cell.qty = 0
                    }
                }))
                item.hasTransfer = item.cells.some(row => row.some(c => c.qty > 0))
            })
        }
    } else {
        toast.error('转移全部失败，请检查库存是否充足')
    }
    transferring.value = false
}

function removeItem(index) {
    formList.value.splice(index, 1)
}
</script>

<style scoped>
.stock-manage {
    width: 100%;
    min-width: 0;
}

.page-heading {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 24px;
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

.search-panel {
    padding: 20px;
    margin-bottom: 16px;
    border-radius: 16px;
}

.search-row {
    display: flex;
    gap: 14px;
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
    width: 200px;
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

.warning-text {
    color: #dc2626;
    font-size: 13px;
    margin-top: 8px;
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
    padding: 14px 20px;
    cursor: pointer;
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

.product-code {
    font-size: 12px;
    color: var(--text-secondary);
}

.product-form-actions {
    display: flex;
    align-items: center;
    gap: 8px;
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

.stock-input {
    width: 68px;
    height: 34px;
    padding: 0;
    border: 1px solid #dceae7;
    border-radius: 8px;
    text-align: center;
    font-weight: 600;
    font-size: 14px;
    background: #fbfefd;
}

.stock-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.stock-input.filled {
    background: #fef3c7;
    border-color: #f59e0b;
    font-weight: 700;
}

.matrix-table tbody tr:hover td {
    background: #f4fbfa;
}

.cell-available {
    color: var(--text-muted);
    font-size: 13px;
    padding: 6px 10px !important;
}

.inline-available {
    text-align: right;
    padding: 8px 4px 0;
    font-size: 13px;
    color: var(--text-muted);
}

@media (max-width: 900px) {
    .search-row {
        flex-direction: column;
        align-items: stretch;
    }

    .wh-select, .search-row input {
        width: 100%;
    }
}
</style>
