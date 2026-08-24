<template>
    <div class="stock-check-add">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    @click="goBack"
                >← 返回盘点单</button>
                <div>
                    <h2 class="page-title">新建库存盘点单</h2>
                    <p class="page-desc">选择仓库并录入 SKU 的实际库存数量</p>
                </div>
            </div>
        </div>

        <div class="form-card">
            <div class="form-row">
                <div class="field">
                    <label>盘点仓库 <span class="required">*</span></label>
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
                <div class="remark-field">
                    <label>备注</label>
                    <input
                        v-model="form.remark"
                        maxlength="100"
                        placeholder="不超过 100 字"
                        type="text"
                    />
                </div>
            </div>
        </div>

        <div class="search-card">
            <div class="search-field">
                <label>商品或 SKU 编码</label>
                <div class="search-control">
                    <input
                        v-model="searchCode"
                        :disabled="!form.wareHouseId"
                        placeholder="输入编码后按回车"
                        type="text"
                        @keyup.enter="searchSku"
                    />
                    <button
                        :disabled="searching || !form.wareHouseId || !searchCode.trim()"
                        class="search-button"
                        type="button"
                        @click="searchSku"
                    >{{ searching ? '查询中...' : '查询 SKU' }}</button>
                </div>
            </div>

            <div
                v-if="candidates.length > 0"
                class="candidate-list"
            >
                <button
                    v-for="candidate in candidates"
                    :key="candidate.code"
                    class="candidate-item"
                    @click="addSku(candidate)"
                >
                    <strong>{{ candidate.name || candidate.code }}</strong>
                    <code>{{ candidate.code }}</code>
                    <span>{{ formatSpec(candidate.spec) }}</span>
                </button>
            </div>
        </div>

        <div
            v-if="items.length > 0"
            class="action-bar"
        >
            <span>共 {{ items.length }} 个 SKU</span>
            <strong :class="{ 'has-difference': differenceCount > 0 }">
                {{ differenceCount > 0 ? `${differenceCount} 项存在差异` : '盘点数量无差异' }}
            </strong>
            <button
                :disabled="saving"
                class="btn-primary"
                @click="handleSave"
            >{{ saving ? '保存中...' : '保存草稿' }}</button>
            <button
                class="btn-outline"
                @click="clearItems"
            >清空全部</button>
        </div>

        <div
            v-if="items.length === 0"
            class="empty-card"
        >
            <div class="empty-state">
                <div class="empty-icon"><IconGraphic name="stock"/></div>
                <div class="empty-text">选择仓库并查询 SKU 后开始盘点</div>
            </div>
        </div>

        <div
            v-else
            class="item-list"
        >
            <StockCheckItemList
                :items="items"
                @remove="removeItem"
                @update-quantity="updateQuantity"
            />
        </div>
    </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import {useConfirmStore} from '../../../stores/confirmStore.js'
import stockCheckInterface from '../../../axios/interface/StockCheckInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../../axios/interface/WareHouseStockInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import StockCheckItemList from '../../../component/stockCheck/StockCheckItemList.vue'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'
import usePageDraft from '../../../composables/usePageDraft.js'

const router = useRouter()
const toast = useToastStore()
const confirmStore = useConfirmStore()
const warehouses = ref([])
const warehousePickerValue = ref('')
const warehouseLoading = ref(false)
const form = ref({
    wareHouseId: '',
    remark: ''
})
const searchCode = ref('')
const searching = ref(false)
const saving = ref(false)
const candidates = ref([])
const items = ref([])
const saved = ref(false)
const previousWarehouseId = ref('')
const stockCheckDraft = usePageDraft(
    'clothing_manage_stock_check_add',
    () => ({
        form: form.value,
        warehousePickerValue: warehousePickerValue.value,
        searchCode: searchCode.value,
        items: items.value,
        previousWarehouseId: previousWarehouseId.value
    }),
    draft => {
        form.value = {...form.value, ...(draft.form || {})}
        warehousePickerValue.value = draft.warehousePickerValue || ''
        searchCode.value = draft.searchCode || ''
        items.value = Array.isArray(draft.items) ? draft.items : []
        previousWarehouseId.value = draft.previousWarehouseId || form.value.wareHouseId
    },
    {
        saved: () => saving.value || saved.value
    }
)

const warehouseOptions = computed(() => warehouses.value
    .filter(warehouse => warehouse.checkStatus !== 'UNDER_CHECK')
    .map(warehouse => ({
        id: warehouse.id,
        optionValue: formatWarehouseOption(warehouse)
    })))

const differenceCount = computed(() => {
    return items.value.filter(item => {
        return Number(item.actualQuantity) !== Number(item.systemQuantity)
    }).length
})

const hasUnsavedContent = computed(() => {
    return !saved.value && (items.value.length > 0 || Boolean(form.value.remark.trim()))
})

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

function syncWarehouseId(value) {
    const selected = warehouses.value.find(warehouse => formatWarehouseOption(warehouse) === value)
    if (!selected) {
        if (!items.value.length) form.value.wareHouseId = ''
        return
    }
    if (selected.id === previousWarehouseId.value) return
    handleWarehouseChange(selected)
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(async () => {
    if (!hasUnsavedContent.value) return true
    return await confirmStore.confirm({
        title: '放弃未保存的盘点？',
        message: '当前盘点内容尚未保存，离开后修改将丢失。',
        confirmText: '确定离开',
        danger: true
    })
})

async function searchSku() {
    const code = searchCode.value.trim()
    if (!form.value.wareHouseId || !code) return
    searching.value = true
    candidates.value = []
    try {
        const data = await productSkuInterface.scan(code)
        const results = Array.isArray(data) ? data : data ? [data] : []
        if (!results.length) return
        const exact = results.find(item => item.code === code)
        if (exact || results.length === 1) {
            await addSku(exact || results[0])
            return
        }
        candidates.value = results
    } catch {
    } finally {
        searching.value = false
    }
}

async function addSku(sku) {
    if (items.value.some(item => item.skuCode === sku.code)) {
        toast.info('该 SKU 已添加')
        candidates.value = []
        searchCode.value = ''
        return
    }
    searching.value = true
    try {
        const product = await findProductByCode(sku.productCode)
        if (!product) {
            toast.warning('未找到该 SKU 对应的商品信息')
            return
        }
        const stocks = await wareHouseStockInterface.searchStock(form.value.wareHouseId, product.id)
        const stock = stocks.find(record => sameSpec(record.spec, sku.spec))
        if (!stock) {
            toast.warning('当前仓库没有该 SKU 的库存记录')
            return
        }
        items.value.push({
            skuCode: sku.code,
            name: sku.name,
            spec: sku.spec,
            productName: sku.productName,
            productCode: sku.productCode,
            systemQuantity: Number(stock.stock || 0),
            actualQuantity: Number(stock.stock || 0)
        })
        saved.value = false
        candidates.value = []
        searchCode.value = ''
    } catch {
    } finally {
        searching.value = false
    }
}

async function findProductByCode(code) {
    let page = 0
    let totalPages = 1
    while (page < totalPages) {
        const data = await productInterface.searchPage(page, 40)
        const matched = (data.content || []).find(product => product.code === code)
        if (matched) return matched
        totalPages = data.totalPages || 0
        page++
    }
    return null
}

function sameSpec(stockSpec, skuSpec) {
    return normalizeSpec(stockSpec) === normalizeSpec(skuSpec)
}

function normalizeSpec(spec) {
    let parsed = spec || {}
    if (typeof parsed === 'string') {
        try {
            parsed = JSON.parse(parsed)
        } catch {
            return parsed
        }
    }
    const sorted = Object.keys(parsed).sort().reduce((result, key) => {
        result[key] = parsed[key]
        return result
    }, {})
    return JSON.stringify(sorted)
}

function formatSpec(spec) {
    let parsed = spec || {}
    if (typeof parsed === 'string') {
        try {
            parsed = JSON.parse(parsed)
        } catch {
            return parsed
        }
    }
    const entries = Object.entries(parsed)
    if (!entries.length) return '无规格'
    return entries.map(([key, value]) => `${key}：${value}`).join(' / ')
}

function updateQuantity(skuCode, quantity) {
    const item = items.value.find(value => value.skuCode === skuCode)
    if (item) item.actualQuantity = quantity
}

function removeItem(skuCode) {
    const index = items.value.findIndex(item => item.skuCode === skuCode)
    if (index !== -1) items.value.splice(index, 1)
}

async function clearItems() {
    if (!items.value.length) return
    const confirmed = await confirmStore.confirm({
        title: '清空盘点项？',
        message: '清空后当前已录入的盘点 SKU 将被移除。',
        confirmText: '确定清空',
        danger: true
    })
    if (!confirmed) return
    items.value = []
    candidates.value = []
}

async function handleWarehouseChange(selectedWarehouse) {
    candidates.value = []
    searchCode.value = ''
    if (!items.value.length) {
        form.value.wareHouseId = selectedWarehouse.id
        previousWarehouseId.value = selectedWarehouse.id
        return
    }
    const confirmed = await confirmStore.confirm({
        title: '切换盘点仓库？',
        message: '切换仓库会清空当前全部盘点项。',
        confirmText: '切换并清空',
        danger: true
    })
    if (confirmed) {
        form.value.wareHouseId = selectedWarehouse.id
        items.value = []
        previousWarehouseId.value = selectedWarehouse.id
        return
    }
    const previousWarehouse = warehouses.value.find(warehouse => warehouse.id === previousWarehouseId.value)
    warehousePickerValue.value = previousWarehouse ? formatWarehouseOption(previousWarehouse) : ''
}

async function handleSave() {
    if (!form.value.wareHouseId) {
        toast.warning('请选择盘点仓库')
        return
    }
    if (!items.value.length) {
        toast.warning('请至少添加一个盘点项')
        return
    }
    saving.value = true
    try {
        await stockCheckInterface.create({
            wareHouseId: Number(form.value.wareHouseId),
            remark: form.value.remark.trim(),
            stockCheckItems: items.value.map(item => ({
                skuCode: item.skuCode,
                actualQuantity: Number(item.actualQuantity)
            }))
        })
        stockCheckDraft.clear()
        saved.value = true
        toast.success('库存盘点单草稿已保存')
        await router.push('/manage/stock-check')
    } catch {
    } finally {
        saving.value = false
    }
}

function handleBeforeUnload(event) {
    if (!hasUnsavedContent.value) return
    event.preventDefault()
    event.returnValue = ''
}

function goBack() {
    router.push('/manage/stock-check')
}
</script>

<style scoped>
.stock-check-add {
    width: 100%;
    min-width: 0;
}

.form-card,
.search-card,
.empty-card {
    padding: 20px;
    margin-bottom: 16px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow);
}

.form-row {
    display: flex;
    align-items: flex-end;
    gap: 16px;
    flex-wrap: wrap;
}

.field,
.remark-field,
.search-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.field label,
.remark-field label,
.search-field label {
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
}

.required {
    color: var(--error-dark);
}

.remark-field input,
.search-control input {
    height: 40px;
    padding: 0 12px;
    border: 1px solid var(--border);
    border-radius: 10px;
    background: var(--bg-subtle);
}

.remark-field {
    width: 45%;
    min-width: 280px;
}

.remark-field input {
    width: 100%;
}

.search-field {
    width: 432px;
}

.search-control {
    width: 432px;
    display: flex;
    align-items: stretch;
}

.search-control input {
    width: 320px;
    border-right: 0;
    border-radius: 10px 0 0 10px;
}

.search-button {
    width: 112px;
    height: 40px;
    padding: 0 14px;
    border: 1px solid var(--primary);
    border-radius: 0 10px 10px 0;
    color: var(--text-invert);
    background: var(--primary);
    font-weight: 600;
    cursor: pointer;
}

.search-button:disabled {
    cursor: not-allowed;
    opacity: 0.55;
}

.candidate-list {
    display: flex;
    gap: 8px;
    padding-top: 16px;
    flex-wrap: wrap;
}

.candidate-item {
    width: 240px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
    padding: 12px 14px;
    color: var(--text);
    background: #f7fcfb;
    border: 1px solid var(--border);
    border-radius: 10px;
    text-align: left;
}

.candidate-item:hover {
    border-color: var(--primary);
    background: var(--primary-light);
}

.candidate-item code {
    color: var(--primary);
}

.candidate-item span {
    color: var(--text-muted);
    font-size: 12px;
}

.action-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 20px;
    margin-bottom: 14px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.action-bar span {
    color: var(--text-secondary);
}

.action-bar strong {
    width: 35%;
    margin-right: auto;
    color: var(--success-dark);
}

.action-bar .has-difference {
    color: var(--warning-dark);
}

.item-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

@media (max-width: 900px) {
    .form-row {
        align-items: stretch;
        flex-direction: column;
    }

    .remark-field,
    .search-field,
    .search-control {
        width: 100%;
    }

    .search-control input {
        width: calc(100% - 112px);
    }

    .action-bar {
        align-items: flex-start;
        flex-direction: column;
    }

    .action-bar strong {
        width: 100%;
    }
}
</style>
