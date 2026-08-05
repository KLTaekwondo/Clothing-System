<template>
    <div class="stock-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">人工库存调整</h2>
                <p class="page-desc">直接调整指定仓库中商品 SKU 的库存数量</p>
            </div>
        </div>

        <div class="card search-panel">
            <div class="search-row">
                <div class="field">
                    <label>仓库</label>
                    <OptionValuePicker
                        v-model="warehousePickerValue"
                        :disabled="warehouseLoading"
                        :options="warehouseOptions"
                        error-message="请从仓库列表中选择有效仓库"
                        placeholder="输入仓库名称或编码筛选"
                        select-placeholder="选择仓库"
                        @update:model-value="syncWarehouseId"
                    />
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
            <div class="product-list-header">
                <span class="product-header-name">商品名称</span>
                <span class="product-header-code">商品编码</span>
                <span class="product-header-price">商品价格</span>
                <span class="product-header-special">是否特价</span>
                <span class="product-header-sku">SKU 数量</span>
                <span class="product-header-action">操作</span>
            </div>
            <div v-for="(item, index) in formList" :key="item.key" class="stock-product-card card">
                <div class="product-form-header" @click="item.expanded = !item.expanded">
                    <div class="product-name-cell">
                        <span class="product-mark"><IconGraphic name="product"/></span>
                        <strong class="product-name">{{ item.product.name }}</strong>
                    </div>
                    <code class="product-code">{{ item.product.code }}</code>
                    <span class="product-price">¥{{ item.product.salePrice ?? '0.00' }}</span>
                    <span :class="item.product.special ? 'special-status' : 'regular-status'" class="product-special">
                        {{ item.product.special ? '是' : '否' }}
                    </span>
                    <span class="product-sku-count">{{ item.skus.length }} 个</span>
                    <div class="product-form-actions">
                        <span v-if="item.hasChanges" class="changed-hint">有修改</span>
                        <button class="btn-danger btn-sm" @click.stop="removeItem(index)">移除</button>
                        <span class="expand-icon">{{ item.expanded ? '▼' : '▶' }}</span>
                    </div>
                </div>

                <div v-show="item.expanded" class="sku-list-wrapper">
                    <div v-if="item.skus.length === 0" class="empty-state compact-empty">
                        <div class="empty-text">该商品暂无可调整的 SKU</div>
                    </div>
                    <div v-else class="sku-list">
                        <div class="sku-list-header">
                            <span class="sku-header-name">SKU 名称</span>
                            <span class="sku-header-code">SKU 编码</span>
                            <span class="sku-header-spec">规格</span>
                            <span class="sku-header-current">当前库存</span>
                            <span class="sku-header-target">调整后库存</span>
                        </div>
                        <div v-for="sku in item.skus" :key="sku.skuId" class="sku-row">
                            <strong class="sku-name">{{ sku.skuName || sku.skuCode }}</strong>
                            <code class="sku-code">{{ sku.skuCode }}</code>
                            <span class="sku-spec">{{ formatSpec(sku.spec) }}</span>
                            <span class="current-stock">{{ sku.originalStock }}</span>
                            <input
                                v-model.number="sku.stock"
                                :class="{ changed: sku.changed }"
                                class="stock-input"
                                min="0"
                                type="number"
                                @input="sku.changed = true; markChanged(item)"
                                @keydown.enter.prevent="focusNextSku($event)"
                            />
                        </div>
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
import OptionValuePicker from '../product/components/OptionValuePicker.vue'

const toast = useToastStore()
const warehouses = ref([])
const warehouseId = ref('')
const warehousePickerValue = ref('')
const warehouseLoading = ref(false)
const searchCode = ref('')
const loading = ref(false)
const savingAll = ref(false)
const formList = ref([])
let keyCounter = 0

const hasAnyChanges = computed(() => formList.value.some(item => item.hasChanges))

const warehouseOptions = computed(() => warehouses.value.map(item => ({
    id: item.id,
    optionValue: formatWarehouseOption(item)
})))

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    warehouseLoading.value = true
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    } finally {
        warehouseLoading.value = false
    }
})

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(() => {
    if (!hasAnyChanges.value) return true
    return window.confirm('当前有未保存的库存修改，确定要离开吗？')
})

function syncWarehouseId(value) {
    const selected = warehouses.value.find(item => formatWarehouseOption(item) === value)
    warehouseId.value = selected?.id || ''
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

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
    const skuRecords = skuList.map(sku => ({
        skuId: sku.id,
        skuCode: sku.code,
        skuName: sku.name,
        spec: parseSpec(sku.spec),
        stockId: null,
        originalStock: 0,
        stock: 0,
        changed: false
    }))

    loadStock(product.id, skuRecords)

    return {
        key: ++keyCounter,
        product,
        expanded: true,
        skus: skuRecords,
        hasChanges: false
    }
}

async function loadStock(productId, skuRecords) {
    try {
        const records = await wareHouseStockInterface.searchStock(warehouseId.value, productId)
        if (!Array.isArray(records)) return
        records.forEach(record => {
            const sku = skuRecords.find(item => sameSpec(item.spec, record.spec))
            if (!sku) return
            sku.stockId = record.id
            sku.originalStock = record.stock ?? 0
            sku.stock = record.stock ?? 0
        })
    } catch {
    }
}

function markChanged(item) {
    item.hasChanges = item.skus.some(sku => sku.changed)
}

function focusNextSku(event) {
    const currentInput = event.currentTarget
    const wrapper = currentInput.closest('.sku-list-wrapper')
    if (!wrapper) return
    const inputs = [...wrapper.querySelectorAll('.stock-input')]
    const currentIndex = inputs.indexOf(currentInput)
    const nextInput = inputs[currentIndex + 1]
    if (nextInput) {
        nextInput.focus()
        nextInput.select()
    }
}

function parseSpec(spec) {
    if (!spec) return {}
    if (typeof spec === 'object') return spec
    try {
        return JSON.parse(spec)
    } catch {
        return {}
    }
}

function sameSpec(first, second) {
    return JSON.stringify(sortSpec(first)) === JSON.stringify(sortSpec(second))
}

function sortSpec(spec) {
    return Object.keys(parseSpec(spec)).sort().reduce((result, key) => {
        result[key] = parseSpec(spec)[key]
        return result
    }, {})
}

function formatSpec(spec) {
    const entries = Object.entries(parseSpec(spec))
    if (!entries.length) return '无规格'
    return entries.map(([key, value]) => `${key}：${value}`).join(' / ')
}

async function saveAll() {
    savingAll.value = true
    try {
        const allUpdates = []
        formList.value.forEach(item => {
            item.skus.forEach(sku => {
                if (sku.changed && sku.stockId != null) {
                    allUpdates.push({stockId: sku.stockId, stock: sku.stock})
                }
            })
        })
        if (!allUpdates.length) {
            toast.info('没有需要保存的修改');
            return
        }
        await wareHouseStockInterface.batchUpdateStock(allUpdates)
        // 后端已返回提示
        formList.value.forEach(item => {
            item.skus.forEach(sku => {
                sku.changed = false
                sku.originalStock = sku.stock
            })
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
    width: calc(100% - 300px);
    font-size: 14px;
    color: var(--text-secondary);
}

.stock-form-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.product-list-header,
.product-form-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 20px;
    padding-left: 20px;
}

.product-list-header {
    padding-top: 8px;
    padding-bottom: 8px;
    color: #47615e;
    font-size: 12px;
    font-weight: 700;
    background: #dcebe8;
    border: 1px solid #c5ddd8;
    border-radius: 9px;
    box-shadow: 0 2px 5px rgba(22, 83, 78, 0.1);
}

.product-form-header {
    padding-top: 12px;
    padding-bottom: 12px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
    border-bottom: 1px solid var(--border-light);
}

.product-header-name,
.product-name-cell {
    width: 200px;
}

.product-header-code,
.product-code {
    width: calc(100% - 648px);
}

.product-header-price,
.product-header-special,
.product-header-sku,
.product-price,
.product-special,
.product-sku-count {
    width: 88px;
    text-align: center;
}

.product-header-action,
.product-form-actions {
    width: 124px;
    text-align: center;
}

.product-header-name,
.product-header-code,
.product-header-price,
.product-header-special,
.product-header-sku,
.product-header-action,
.product-name-cell,
.product-code,
.product-price,
.product-special,
.product-sku-count,
.product-form-actions {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 10px;
    border-right: 1px solid #b8d2cd;
}

.product-header-action,
.product-form-actions {
    border-right: none;
}

.stock-product-card {
    padding: 0;
    overflow: hidden;
    border-radius: 16px;
}

.product-form-header:hover {
    background: #f4fbfa;
}

.product-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.product-mark {
    font-size: 20px;
    flex-shrink: 0;
}

.product-name {
    overflow: hidden;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-code {
    overflow: hidden;
    color: var(--primary);
    font-size: 14px;
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-price {
    color: var(--text);
    font-size: 13px;
    font-weight: 700;
}

.product-sku-count {
    color: var(--text-secondary);
    font-size: 13px;
}

.product-special {
    display: inline-block;
    padding: 3px 8px;
    border-radius: 9px;
    font-size: 12px;
    font-weight: 700;
}

.special-status {
    background: #dcfce7;
    color: #15803d;
}

.regular-status {
    background: #f1f5f9;
    color: #64748b;
}

.product-form-actions {
    display: flex;
    align-items: center;
    justify-content: center;
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

.sku-list-wrapper {
    padding: 10px 14px 12px;
    background: #f8fcfb;
    box-shadow: inset 0 3px 8px rgba(22, 83, 78, 0.06);
}

.sku-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.sku-list-header,
.sku-row {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 12px;
    padding-left: 12px;
}

.sku-list-header {
    padding-top: 8px;
    padding-bottom: 8px;
    color: #47615e;
    font-size: 12px;
    font-weight: 700;
    background: #dcebe8;
    border: 1px solid #c5ddd8;
    border-radius: 8px;
    box-shadow: 0 2px 5px rgba(22, 83, 78, 0.1);
}

.sku-row {
    padding-top: 9px;
    padding-bottom: 9px;
    border: 1px solid #e1eeeb;
    border-radius: 8px;
    background: #ffffff;
    box-shadow: 0 2px 6px rgba(22, 83, 78, 0.07);
}

.sku-header-name,
.sku-name {
    width: 150px;
}

.sku-header-code,
.sku-code {
    width: 170px;
}

.sku-header-spec,
.sku-spec {
    width: calc(100% - 542px);
}

.sku-header-current,
.current-stock {
    width: 80px;
    text-align: center;
}

.sku-header-target,
.stock-input {
    width: 94px;
}

.sku-header-name,
.sku-header-code,
.sku-header-spec,
.sku-header-current,
.sku-header-target,
.sku-name,
.sku-code,
.sku-spec,
.current-stock {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 8px;
    border-right: 1px solid #c5ddd8;
}

.sku-header-target,
.stock-input {
    text-align: center;
}

.sku-header-target,
.stock-input {
    border-right: none;
}

.sku-name,
.sku-code,
.sku-spec {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.sku-name {
    font-size: 13px;
}

.sku-code {
    color: var(--primary);
    font-size: 12px;
}

.sku-spec {
    color: var(--text-secondary);
    font-size: 12px;
}

.current-stock {
    color: var(--text-secondary);
    font-size: 12px;
}

.stock-input {
    width: 94px;
    height: 32px;
    box-sizing: border-box;
    padding: 0 6px;
    border: 1px solid #dceae7;
    border-radius: 7px;
    text-align: center;
    font-weight: 700;
    font-size: 14px;
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

@media (max-width: 900px) {
    .search-row {
        align-items: stretch;
        flex-direction: column;
    }

    .field,
    .wh-select,
    .search-row input,
    .search-button {
        width: 100%;
    }

    .product-form-header {
        align-items: flex-start;
        flex-wrap: wrap;
    }

    .product-name-cell,
    .product-code,
    .product-price,
    .product-special,
    .product-sku-count,
    .product-form-actions {
        width: auto;
        padding: 0;
        border-right: none;
    }

    .product-name-cell {
        width: 100%;
    }

    .product-form-actions {
        justify-content: flex-start;
    }

    .product-list-header {
        display: none;
    }

    .sku-list-header {
        display: none;
    }

    .sku-row {
        align-items: flex-start;
        flex-wrap: wrap;
        gap: 8px;
    }

    .sku-name,
    .sku-code,
    .sku-spec,
    .current-stock,
    .stock-input {
        width: auto;
        padding: 0;
        border-right: none;
    }

    .sku-name {
        width: 100%;
    }

    .current-stock {
        width: auto;
        text-align: left;
    }

    .stock-input {
        width: 94px;
    }
}

@media (max-width: 560px) {
    .search-panel,
    .sku-list-wrapper {
        padding: 14px;
    }

    .stock-form-actions-bar {
        flex-direction: column;
    }

    .stock-form-actions-bar button {
        width: 100%;
    }

    .sku-info {
        align-items: flex-start;
        flex-direction: column;
        gap: 4px;
    }
}
</style>
