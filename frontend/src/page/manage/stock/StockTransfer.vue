<template>
    <div class="stock-transfer">
        <div class="page-heading">
            <div>
                <h2 class="page-title">库存转移</h2>
                <p class="page-desc">选择源仓库和目标仓库，查询商品后批量转移库存</p>
            </div>
        </div>

        <div class="search-panel">
            <div class="search-row">
                <div class="warehouse-field">
                    <label>源仓库</label>
                    <OptionValuePicker
                        v-model="sourcePickerValue"
                        :disabled="warehouseLoading"
                        :options="warehouseOptions"
                        error-message="请从仓库列表中选择有效的源仓库"
                        placeholder="输入源仓库名称或编码筛选"
                        select-placeholder="选择源仓库"
                        @update:model-value="syncSourceId"
                    />
                </div>
                <div class="warehouse-field">
                    <label>目标仓库</label>
                    <OptionValuePicker
                        v-model="targetPickerValue"
                        :disabled="warehouseLoading"
                        :options="warehouseOptions"
                        error-message="请从仓库列表中选择有效的目标仓库"
                        placeholder="输入目标仓库名称或编码筛选"
                        select-placeholder="选择目标仓库"
                        @update:model-value="syncTargetId"
                    />
                </div>
                <div class="product-field">
                    <label>商品编码</label>
                    <input
                        v-model="searchCode"
                        placeholder="输入商品编码"
                        type="text"
                        @keyup.enter="searchProduct"
                    />
                </div>
                <button
                    :disabled="searching || !canSearch"
                    class="search-button"
                    type="button"
                    @click="searchProduct"
                >{{ searching ? '查询中...' : '查询并添加' }}</button>
            </div>
            <div
                v-if="sourceId && targetId && sourceId === targetId"
                class="warning-text"
            >源仓库和目标仓库不能相同</div>
        </div>

        <div
            v-if="formList.length === 0"
            class="empty-form-card"
        >
            <div class="empty-state">
                <div class="empty-icon"><IconGraphic name="transfer"/></div>
                <div class="empty-text">选择仓库并查询商品，SKU 会显示在这里</div>
            </div>
        </div>

        <div
            v-if="formList.length > 0"
            class="stock-actions-bar"
        >
            <span class="total-hint">共 {{ formList.length }} 件商品，{{ skuCount }} 个 SKU</span>
            <span
                v-if="hasAnyTransfer"
                class="changed-hint"
            >已填写 {{ selectedSkuCount }} 个 SKU</span>
            <button
                :disabled="transferring || !hasAnyTransfer"
                class="transfer-button"
                type="button"
                @click="transferAll"
            >{{ transferring ? '转移中...' : '确认全部转移' }}</button>
            <button
                class="clear-button"
                type="button"
                @click="clearAll"
            >清空全部</button>
        </div>

        <div
            v-if="formList.length > 0"
            class="transfer-item-list"
        >
            <div class="item-list-header">
                <span class="header-product">商品名称</span>
                <span class="header-code">商品编码</span>
                <span class="header-sku">SKU 数量</span>
                <span class="header-action">操作</span>
            </div>

            <div
                v-for="(item, productIndex) in formList"
                :key="item.key"
                class="product-card"
            >
                <div
                    class="product-header"
                    @click="item.expanded = !item.expanded"
                >
                    <div class="product-name-cell">
                        <span class="product-mark"><IconGraphic name="product"/></span>
                        <strong>{{ item.product.name || '-' }}</strong>
                    </div>
                    <code class="product-code">{{ item.product.code || '-' }}</code>
                    <span class="sku-count">{{ item.skus.length }} 个</span>
                    <div class="product-actions">
                        <button
                            class="remove-product-button"
                            type="button"
                            @click.stop="removeProduct(productIndex)"
                        >移除商品</button>
                        <span class="expand-icon">{{ item.expanded ? '▼' : '▶' }}</span>
                    </div>
                </div>

                <div
                    v-show="item.expanded"
                    class="sku-wrapper"
                >
                    <div class="sku-list-header">
                        <span class="sku-header-name">SKU 名称</span>
                        <span class="sku-header-code">SKU 编码</span>
                        <span class="sku-header-spec">规格</span>
                        <span class="sku-header-stock">源库存</span>
                        <span class="sku-header-quantity">转移数量</span>
                        <span class="sku-header-action">操作</span>
                    </div>
                    <div
                        v-for="sku in item.skus"
                        :key="sku.skuCode"
                        class="sku-row"
                    >
                        <strong class="sku-name">{{ sku.skuName || sku.skuCode }}</strong>
                        <code class="sku-code">{{ sku.skuCode }}</code>
                        <span class="sku-spec">{{ formatSpec(sku.spec) }}</span>
                        <strong class="available-stock">{{ sku.available }}</strong>
                        <input
                            v-model.number="sku.quantity"
                            :class="sku.quantity > 0 ? 'transfer-quantity-filled' : 'transfer-quantity'"
                            :max="sku.available"
                            :placeholder="String(sku.available)"
                            min="0"
                            step="1"
                            type="number"
                            @blur="normalizeQuantity(sku, $event)"
                            @input="updateQuantity(sku, $event)"
                            @keydown.enter.prevent="focusNextSku($event)"
                        />
                        <button
                            class="remove-sku-button"
                            type="button"
                            @click="removeSku(item, sku.skuCode)"
                        >移除</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref, watch} from 'vue'
import {useToastStore} from '../../../stores/toastStore.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../../axios/interface/WareHouseStockInterface.js'
import transferOrderInterface from '../../../axios/interface/TransferOrderInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'

const toast = useToastStore()
const transferDraftStorageKey = 'clothing_stock_transfer_draft'
const warehouses = ref([])
const sourceId = ref('')
const targetId = ref('')
const sourcePickerValue = ref('')
const targetPickerValue = ref('')
const warehouseLoading = ref(false)
const searchCode = ref('')
const searching = ref(false)
const transferring = ref(false)
const formList = ref([])
let keyCounter = 0

const warehouseOptions = computed(() => warehouses.value.map(warehouse => ({
    id: warehouse.id,
    optionValue: formatWarehouseOption(warehouse)
})))
const canSearch = computed(() => {
    return Boolean(sourceId.value && targetId.value && searchCode.value.trim())
})
const skuCount = computed(() => {
    return formList.value.reduce((count, item) => count + item.skus.length, 0)
})
const selectedSkuCount = computed(() => {
    return formList.value.reduce((count, item) => {
        return count + item.skus.filter(sku => Number(sku.quantity) > 0).length
    }, 0)
})
const hasAnyTransfer = computed(() => selectedSkuCount.value > 0)

watch(
    [sourceId, targetId, sourcePickerValue, targetPickerValue, searchCode, formList],
    () => saveDraft(),
    {deep: true}
)

onMounted(async () => {
    restoreDraft()
    warehouseLoading.value = true
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    } finally {
        warehouseLoading.value = false
    }
})

function syncSourceId(value) {
    const selected = warehouses.value.find(warehouse => formatWarehouseOption(warehouse) === value)
    sourceId.value = selected?.id || ''
    clearAll()
}

function syncTargetId(value) {
    const selected = warehouses.value.find(warehouse => formatWarehouseOption(warehouse) === value)
    targetId.value = selected?.id || ''
    clearAll()
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

function saveDraft() {
    if (!sourceId.value && !targetId.value && formList.value.length === 0 && !searchCode.value) {
        sessionStorage.removeItem(transferDraftStorageKey)
        return
    }
    sessionStorage.setItem(transferDraftStorageKey, JSON.stringify({
        sourceId: sourceId.value,
        targetId: targetId.value,
        sourcePickerValue: sourcePickerValue.value,
        targetPickerValue: targetPickerValue.value,
        searchCode: searchCode.value,
        formList: formList.value
    }))
}

function restoreDraft() {
    try {
        const saved = JSON.parse(sessionStorage.getItem(transferDraftStorageKey) || 'null')
        if (!saved || !Array.isArray(saved.formList)) return
        sourceId.value = saved.sourceId || ''
        targetId.value = saved.targetId || ''
        sourcePickerValue.value = saved.sourcePickerValue || ''
        targetPickerValue.value = saved.targetPickerValue || ''
        searchCode.value = saved.searchCode || ''
        formList.value = saved.formList
        keyCounter = formList.value.reduce((max, item) => Math.max(max, Number(item.key) || 0), 0)
        toast.info('已恢复上次未完成的调货内容')
    } catch {
        sessionStorage.removeItem(transferDraftStorageKey)
    }
}

function clearAll() {
    formList.value = []
    sessionStorage.removeItem(transferDraftStorageKey)
}

async function searchProduct() {
    const code = searchCode.value.trim()
    if (!sourceId.value || !targetId.value) {
        toast.warning('请选择源仓库和目标仓库')
        return
    }
    if (sourceId.value === targetId.value) {
        toast.warning('源仓库和目标仓库不能相同')
        return
    }
    if (!code) return
    searching.value = true
    try {
        const data = await productInterface.searchPage()
        const matched = (data.content || []).find(product => product.code === code)
        if (!matched) {
            toast.info('未找到该编码的商品')
            return
        }
        if (formList.value.some(item => item.product.id === matched.id)) {
            toast.info('该商品已添加')
            searchCode.value = ''
            return
        }
        const product = await productInterface.search(matched.id)
        const skuList = await productSkuInterface.searchListByProductId(matched.id)
        if (!skuList.length) {
            toast.info('该商品没有 SKU')
            return
        }
        const block = await buildBlock(product, skuList)
        formList.value.push(block)
        toast.success(`已添加「${product.name}」的 ${block.skus.length} 个 SKU`)
        searchCode.value = ''
    } catch {
        toast.info('查询失败')
    } finally {
        searching.value = false
    }
}

async function buildBlock(product, skuList) {
    const skuRecords = skuList.map(sku => ({
        skuId: sku.id,
        skuCode: sku.code,
        skuName: sku.name,
        spec: parseSpec(sku.spec),
        available: 0,
        quantity: 0
    }))
    await loadStock(product.id, skuRecords)
    return {
        key: ++keyCounter,
        product,
        expanded: true,
        skus: skuRecords
    }
}

async function loadStock(productId, skuRecords) {
    try {
        const records = await wareHouseStockInterface.searchStock(sourceId.value, productId)
        records.forEach(record => {
            const sku = skuRecords.find(item => sameSpec(item.spec, record.spec))
            if (sku) sku.available = Number(record.stock || 0)
        })
    } catch {
    }
}

function updateQuantity(sku, event) {
    const value = Number(event.target.value)
    if (!Number.isFinite(value)) {
        sku.quantity = 0
        return
    }
    sku.quantity = Math.min(Math.max(Math.floor(value), 0), sku.available)
    if (value > sku.available) {
        event.target.value = String(sku.available)
        toast.warning(`转移数量不能超过源库存 ${sku.available}`)
    }
}

function normalizeQuantity(sku, event) {
    const quantity = Math.min(Math.max(Math.floor(Number(sku.quantity) || 0), 0), sku.available)
    sku.quantity = quantity
    event.target.value = String(quantity)
}

function focusNextSku(event) {
    const wrapper = event.currentTarget.closest('.sku-wrapper')
    if (!wrapper) return
    const inputs = [...wrapper.querySelectorAll('.transfer-quantity, .transfer-quantity-filled')]
    const currentIndex = inputs.indexOf(event.currentTarget)
    const nextInput = inputs[currentIndex + 1]
    if (nextInput) {
        nextInput.focus()
        nextInput.select()
    }
}

function removeProduct(index) {
    formList.value.splice(index, 1)
}

function removeSku(item, skuCode) {
    const index = item.skus.findIndex(sku => sku.skuCode === skuCode)
    if (index !== -1) item.skus.splice(index, 1)
    if (!item.skus.length) {
        const productIndex = formList.value.findIndex(product => product.key === item.key)
        if (productIndex !== -1) formList.value.splice(productIndex, 1)
    }
}

async function transferAll() {
    if (sourceId.value === targetId.value) {
        toast.warning('源仓库和目标仓库不能相同')
        return
    }
    const transferOrderItems = []
    let totalPrice = 0
    for (const item of formList.value) {
        for (const sku of item.skus) {
            const quantity = Number(sku.quantity || 0)
            if (quantity > sku.available) {
                toast.warning(`${sku.skuName || sku.skuCode} 的转移数量超过源库存`)
                return
            }
            if (quantity > 0) {
                transferOrderItems.push({
                    skuCode: sku.skuCode,
                    quantity
                })
                totalPrice += Number(item.product.importPrice || 0) * quantity
            }
        }
    }
    if (!transferOrderItems.length) {
        toast.info('没有需要转移的数量')
        return
    }
    transferring.value = true
    try {
        await transferOrderInterface.create({
            sourceWareHouseId: Number(sourceId.value),
            targetWareHouseId: Number(targetId.value),
            transferOrderItems,
            totalPrice: totalPrice.toFixed(2),
            remark: '库存转移页创建'
        })
        toast.success(`调拨单已保存草稿，共 ${transferOrderItems.length} 个 SKU，请到调拨订单列表提交审核`)
        formList.value = []
        sessionStorage.removeItem(transferDraftStorageKey)
    } catch {
        toast.error('创建调拨单失败，请检查库存是否充足')
    } finally {
        transferring.value = false
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
    const parsed = parseSpec(spec)
    return Object.keys(parsed).sort().reduce((result, key) => {
        result[key] = parsed[key]
        return result
    }, {})
}

function formatSpec(spec) {
    const entries = Object.entries(parseSpec(spec))
    if (!entries.length) return '无规格'
    return entries.map(([key, value]) => `${key}：${value}`).join(' / ')
}
</script>

<style scoped>
.stock-transfer {
    width: 100%;
    min-width: 0;
}

.page-heading {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: 24px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.search-panel,
.empty-form-card {
    padding: 20px;
    margin-bottom: 16px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.search-row {
    display: flex;
    align-items: flex-end;
    gap: 14px;
    flex-wrap: wrap;
}

.field,
.product-field,
.warehouse-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.field label,
.product-field label,
.warehouse-field label {
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
}

.warehouse-field {
    width: 360px;
}

.product-field {
    width: 320px;
}

.product-field input {
    width: 100%;
    height: 40px;
    box-sizing: border-box;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
}

.search-button,
.transfer-button {
    height: 40px;
    padding: 0 16px;
    border: 1px solid var(--primary);
    border-radius: 9px;
    color: #fff;
    background: var(--primary);
    font-weight: 600;
    cursor: pointer;
}

.transfer-button {
    margin-left: auto;
}

.search-button:disabled,
.transfer-button:disabled {
    cursor: not-allowed;
    opacity: 0.55;
}

.warning-text {
    margin-top: 8px;
    color: #dc2626;
    font-size: 13px;
}

.stock-actions-bar {
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
    width: calc(100% - 440px);
    color: var(--text-secondary);
    font-size: 14px;
}

.changed-hint {
    color: #d97706;
    font-size: 13px;
    font-weight: 700;
}

.clear-button {
    height: 40px;
    padding: 0 14px;
    border: 1px solid #c8ded9;
    border-radius: 9px;
    color: var(--text-secondary);
    background: #fff;
    cursor: pointer;
}

.clear-button:hover {
    border-color: var(--primary);
    color: var(--primary);
    background: #f1faf8;
}

.transfer-item-list {
    width: 100%;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.item-list-header,
.product-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-right: 20px;
    padding-left: 20px;
}

.item-list-header {
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

.header-product,
.product-name-cell {
    width: 220px;
}

.header-code,
.product-code {
    width: calc(100% - 520px);
}

.header-sku,
.sku-count {
    width: 88px;
    text-align: center;
}

.header-action,
.product-actions {
    width: 120px;
    text-align: center;
}

.header-product,
.header-code,
.header-sku,
.header-action,
.product-name-cell,
.product-code,
.sku-count,
.product-actions {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 10px;
    border-right: 1px solid #b8d2cd;
}

.header-action,
.product-actions {
    border-right: none;
}

.product-card {
    overflow: hidden;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.product-header {
    min-height: 58px;
    cursor: pointer;
    user-select: none;
    transition: background 0.2s;
}

.product-header:hover {
    background: #f4fbfa;
}

.product-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.product-name-cell strong,
.product-code {
    overflow: hidden;
    font-size: 14px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.product-mark {
    flex-shrink: 0;
    font-size: 19px;
}

.product-code {
    color: var(--primary);
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

.sku-count {
    color: var(--text-secondary);
    font-size: 13px;
}

.product-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.expand-icon {
    width: 16px;
    color: var(--text-muted);
    font-size: 12px;
    text-align: center;
}

.remove-product-button,
.remove-sku-button {
    border: 1px solid #fecaca;
    border-radius: 7px;
    color: #dc2626;
    background: #fff;
    cursor: pointer;
}

.remove-product-button {
    padding: 5px 8px;
    font-size: 11px;
}

.remove-sku-button {
    width: 64px;
    padding: 5px 7px;
    font-size: 11px;
    text-align: center;
}

.remove-product-button:hover,
.remove-sku-button:hover {
    border-color: #f87171;
    background: #fef2f2;
}

.sku-wrapper {
    padding: 10px 14px 12px;
    background: #f8fcfb;
    box-shadow: inset 0 3px 8px rgba(22, 83, 78, 0.06);
}

.sku-list-header,
.sku-row {
    display: flex;
    align-items: center;
    gap: 10px;
    padding-right: 10px;
    padding-left: 10px;
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
}

.sku-row {
    min-height: 52px;
    margin-top: 6px;
    border: 1px solid #e1eeeb;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 2px 6px rgba(22, 83, 78, 0.07);
}

.sku-header-name,
.sku-name {
    width: 150px;
}

.sku-header-code,
.sku-code {
    width: 160px;
}

.sku-header-spec,
.sku-spec {
    width: calc(100% - 566px);
}

.sku-header-stock,
.available-stock,
.sku-header-quantity,
.transfer-quantity,
.transfer-quantity-filled {
    width: 86px;
    text-align: center;
}

.sku-header-action,
.remove-sku-button {
    width: 64px;
    text-align: center;
}

.sku-header-name,
.sku-header-code,
.sku-header-spec,
.sku-header-stock,
.sku-header-quantity,
.sku-header-action,
.sku-name,
.sku-code,
.sku-spec,
.available-stock {
    box-sizing: border-box;
    min-width: 0;
    padding: 0 7px;
    border-right: 1px solid #c5ddd8;
}

.sku-header-action,
.remove-sku-button {
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

.available-stock {
    color: var(--text);
    font-size: 13px;
}

.transfer-quantity,
.transfer-quantity-filled {
    height: 32px;
    box-sizing: border-box;
    padding: 0 5px;
    border: 1px solid #dceae7;
    border-radius: 7px;
    background: #fbfefd;
    font-size: 14px;
    font-weight: 700;
}

.transfer-quantity-filled {
    border-color: #f59e0b;
    background: #fef3c7;
}

.transfer-quantity:focus,
.transfer-quantity-filled:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

@media (max-width: 900px) {
    .search-row,
    .stock-actions-bar {
        align-items: stretch;
        flex-direction: column;
    }

    .field,
    .product-field,
    .warehouse-field {
        width: 100%;
    }

    .item-list-header {
        display: none;
    }

    .product-header {
        align-items: flex-start;
        flex-wrap: wrap;
        padding-top: 12px;
        padding-bottom: 12px;
    }

    .product-name-cell {
        width: 100%;
        padding: 0;
        border-right: none;
    }

    .product-code,
    .sku-count,
    .product-actions {
        width: auto;
        padding: 0;
        border-right: none;
    }

    .sku-list-header {
        display: none;
    }

    .sku-row {
        align-items: flex-start;
        flex-wrap: wrap;
        gap: 8px;
        padding-top: 10px;
        padding-bottom: 10px;
    }

    .sku-name,
    .sku-code,
    .sku-spec,
    .available-stock,
    .transfer-quantity,
    .transfer-quantity-filled,
    .remove-sku-button {
        width: auto;
        padding: 0;
        border-right: none;
        text-align: left;
    }

    .sku-name {
        width: 100%;
    }

    .transfer-quantity,
    .transfer-quantity-filled {
        width: 86px;
        padding: 0 5px;
        text-align: center;
    }

    .stock-actions-bar button {
        width: 100%;
    }

    .total-hint {
        width: 100%;
    }
}
</style>
