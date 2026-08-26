<template>
    <div class="import-order-add">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">新建采购单</h2>
                <p class="page-label-desc">选择供应商和仓库，添加商品后保存草稿</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            @click="goBack"
                        >← 返回采购单</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="card form-card">
            <div class="form-grid">
                <div class="reference-column">
                    <div class="supplier-field">
                        <label>供应商 <span class="required">*</span></label>
                        <OptionValuePicker
                            v-model="supplierPickerValue"
                            :disabled="supplierLoading"
                            :options="supplierOptions"
                            error-message="请从供应商列表中选择有效供应商"
                            placeholder="输入供应商名称或编码筛选"
                            select-placeholder="选择供应商"
                            @update:model-value="syncSupplierId"
                        />
                    </div>
                    <div class="warehouse-field">
                        <label>仓库 <span class="required">*</span></label>
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
                </div>
                <div class="direction-column">
                    <div class="direction-field">
                        <label>业务方向 <span class="required">*</span></label>
                        <div class="direction-options">
                            <button
                                :class="form.direction === 'IN' ? 'direction-in-selected' : 'direction-in'"
                                type="button"
                                @click="form.direction = 'IN'"
                            >
                                <strong>采购入库</strong>
                                <span>商品进入所选仓库</span>
                            </button>
                            <button
                                :class="form.direction === 'OUT' ? 'direction-out-selected' : 'direction-out'"
                                type="button"
                                @click="form.direction = 'OUT'"
                            >
                                <strong>采购退货</strong>
                                <span>商品从所选仓库出库</span>
                            </button>
                        </div>
                    </div>
                </div>
                <div class="remark-column">
                    <div class="field">
                        <label>备注</label>
                        <textarea
                            v-model="form.remark"
                            maxlength="100"
                            placeholder="不超过 100 字"
                            rows="5"
                        ></textarea>
                    </div>
                </div>
            </div>
        </div>

        <div class="card search-card">
            <div class="field-product">
                <label>商品编码</label>
                <div class="search-control">
                    <input
                        v-model="searchCode"
                        placeholder="输入商品编码后查询"
                        type="text"
                        @keyup.enter="searchProduct"
                    />
                    <button
                        :disabled="searching"
                        class="search-button"
                        type="button"
                        @click="searchProduct"
                    >{{ searching ? '查询中...' : '查询并添加' }}</button>
                </div>
            </div>
        </div>

        <div v-if="items.length === 0" class="card empty-card">
            <div class="empty-state">
                <div class="empty-icon"><IconGraphic name="product"/></div>
                <div class="empty-text">查询商品后添加 SKU 到这里</div>
            </div>
        </div>

        <div v-if="items.length > 0" class="items-actions-bar">
            <span class="total-hint">共 {{ items.length }} 个 SKU</span>
            <span class="amount-hint">合计：¥{{ totalAmount }}</span>
            <button :disabled="saving || items.length === 0 || !canSave" class="btn-primary" @click="handleSave">
                {{ saving ? '保存中...' : '保存草稿' }}
            </button>
            <button class="btn-outline btn-sm" @click="clearItems">清空全部</button>
        </div>

        <ImportOrderItemList
            v-if="items.length > 0"
            :groups="groupedItems"
            @remove="removeSku"
        />
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import supplierInterface from '../../../axios/interface/SupplierInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import importOrderInterface from '../../../axios/interface/ImportOrderInterface.js'
import ImportOrderItemList from '../../../component/importOrder/ImportOrderItemList.vue'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'
import usePageDraft from '../../../composables/usePageDraft.js'

const router = useRouter()
const toast = useToastStore()

const suppliers = ref([])
const supplierPickerValue = ref('')
const supplierLoading = ref(false)
const warehouses = ref([])
const warehousePickerValue = ref('')
const warehouseLoading = ref(false)
const form = ref({
    supplierId: '',
    wareHouseId: '',
    direction: '',
    remark: ''
})
const items = ref([])
const searchCode = ref('')
const searching = ref(false)
const saving = ref(false)
const importDraft = usePageDraft(
    'clothing_manage_import_order_add',
    () => ({
        form: form.value,
        supplierPickerValue: supplierPickerValue.value,
        warehousePickerValue: warehousePickerValue.value,
        items: items.value,
        searchCode: searchCode.value
    }),
    saved => {
        form.value = {...form.value, ...(saved.form || {})}
        supplierPickerValue.value = saved.supplierPickerValue || ''
        warehousePickerValue.value = saved.warehousePickerValue || ''
        items.value = Array.isArray(saved.items) ? saved.items : []
        searchCode.value = saved.searchCode || ''
    },
    {
        saved: () => saving.value
    }
)

const canSave = computed(() => form.value.supplierId && form.value.wareHouseId && form.value.direction)
const supplierOptions = computed(() => suppliers.value.map(supplier => ({
    id: supplier.id,
    optionValue: formatSupplierOption(supplier)
})))
const warehouseOptions = computed(() => warehouses.value.map(warehouse => ({
    id: warehouse.id,
    optionValue: formatWarehouseOption(warehouse)
})))

const totalAmount = computed(() => {
    let total = 0
    items.value.forEach(item => {
        total += Number(item.importPrice || 0) * Number(item.quantity || 1)
    })
    return total.toFixed(2)
})

const groupedItems = computed(() => {
    const map = {}
    items.value.forEach(item => {
        const key = item.productId
        if (!map[key]) {
            map[key] = {
                productId: item.productId,
                productName: item.productName,
                productCode: item.productCode,
                skus: []
            }
        }
        map[key].skus.push(item)
    })
    return Object.values(map)
})

onMounted(async () => {
    supplierLoading.value = true
    try {
        suppliers.value = await supplierInterface.searchList()
    } catch {
        suppliers.value = []
    } finally {
        supplierLoading.value = false
    }
    warehouseLoading.value = true
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    } finally {
        warehouseLoading.value = false
    }
})

function syncSupplierId(value) {
    const selected = suppliers.value.find(supplier => formatSupplierOption(supplier) === value)
    form.value.supplierId = selected?.id || ''
}

function formatSupplierOption(supplier) {
    if (supplier.supplierCode) return `${supplier.supplierName}（${supplier.supplierCode}）`
    if (supplier.code) return `${supplier.supplierName}（${supplier.code}）`
    return supplier.supplierName
}

function syncWarehouseId(value) {
    const selected = warehouses.value.find(warehouse => formatWarehouseOption(warehouse) === value)
    form.value.wareHouseId = selected?.id || ''
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

async function searchProduct() {
    const code = searchCode.value.trim()
    if (!code) {
        toast.warning('请输入商品编码')
        return
    }
    searching.value = true
    try {
        const data = await productInterface.searchPage()
        const all = data.content || []
        const matched = all.find(p => p.code === code)
        if (!matched) {
            toast.info('未找到该编码的商品')
            return
        }
        const skuList = await productSkuInterface.searchListByProductId(matched.id)
        if (!skuList.length) {
            toast.info('该商品没有 SKU')
            return
        }

        const alreadyExists = items.value.some(item => item.productId === matched.id)
        if (alreadyExists) {
            toast.info('该商品已添加')
            searchCode.value = ''
            return
        }

        skuList.forEach(sku => {
            let spec = sku.spec || '{}'
            try {
                spec = JSON.parse(spec)
            } catch {
                spec = {}
            }
            items.value.push({
                skuId: sku.id,
                skuCode: sku.code,
                skuName: sku.name,
                spec,
                productId: matched.id,
                productCode: matched.code,
                productName: matched.name,
                importPrice: matched.importPrice || 0,
                quantity: 1
            })
        })
        toast.success(`已添加「${matched.name}」的 ${skuList.length} 个 SKU`)
        searchCode.value = ''
    } catch {
        toast.info('查询失败')
    } finally {
        searching.value = false
    }
}

function removeSku(skuId) {
    const index = items.value.findIndex(item => item.skuId === skuId)
    if (index !== -1) {
        items.value.splice(index, 1)
    }
}

function clearItems() {
    items.value = []
}

async function handleSave() {
    if (!canSave.value) {
        toast.warning('请补全供应商、仓库和方向')
        return
    }
    if (!items.value.length) {
        toast.warning('请至少添加一个商品')
        return
    }
    const importItems = items.value.map(item => ({
        skuCode: item.skuCode,
        importQuantity: Number(item.quantity) || 1
    }))
    saving.value = true
    try {
        await importOrderInterface.create({
            supplierId: Number(form.value.supplierId),
            wareHouseId: Number(form.value.wareHouseId),
            direction: form.value.direction,
            totalAmount: Number(totalAmount.value),
            remark: form.value.remark || '',
            importItems
        })
        importDraft.clear()
        toast.success('采购单草稿已保存')
        router.push('/manage/import-order')
    } catch {} finally {
        saving.value = false
    }
}

function goBack() {
    router.push('/manage/import-order')
}
</script>

<style scoped>
.import-order-add {
    width: 100%;
    min-width: 0;
}

.card {
    padding: 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.form-card {
    margin-bottom: 16px;
}

.reference-column,
.direction-column,
.remark-column {
    min-width: 0;
    display: flex;
    flex-direction: column;
}

.reference-column {
    width: calc(40% - 12px);
    gap: 14px;
}

.direction-column {
    width: calc(28% - 12px);
}

.remark-column {
    width: calc(32% - 12px);
}

.field,
.supplier-field,
.warehouse-field,
.direction-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.field label,
.supplier-field label,
.warehouse-field label,
.direction-field label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
}

.supplier-field,
.warehouse-field,
.direction-field {
    width: 100%;
}

.direction-options {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.direction-in,
.direction-in-selected,
.direction-out,
.direction-out-selected {
    width: 100%;
    min-height: 58px;
    display: flex;
    align-items: flex-start;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    padding: 8px 12px;
    border: 1px solid var(--border);
    border-radius: 9px;
    background: var(--bg-subtle);
    text-align: left;
    cursor: pointer;
}

.direction-in strong,
.direction-in-selected strong,
.direction-out strong,
.direction-out-selected strong {
    font-size: 13px;
}

.direction-in span,
.direction-in-selected span,
.direction-out span,
.direction-out-selected span {
    color: var(--text-muted);
    font-size: 11px;
}

.direction-in:hover,
.direction-in-selected {
    border-color: #22c55e;
    color: var(--success-dark);
    background: var(--success-light);
}

.direction-out:hover,
.direction-out-selected {
    border-color: var(--error);
    color: var(--error-dark);
    background: var(--error-light);
}

.direction-in-selected,
.direction-out-selected {
    box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.1);
}

.direction-out-selected {
    box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.remark-column textarea {
    width: 100%;
    min-height: 137px;
    box-sizing: border-box;
    padding: 12px;
    resize: vertical;
    border: 1px solid var(--border);
    border-radius: 10px;
    background: var(--bg-subtle);
    font: inherit;
}

.remark-column textarea:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
    outline: none;
}

.required {
    color: var(--error-dark);
}

.field-product {
    width: 432px;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.field-product label {
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
}

.search-control {
    width: 432px;
    display: flex;
    align-items: stretch;
}

.search-control input {
    width: 320px;
    height: 40px;
    padding: 0 12px;
    border: 1px solid var(--border);
    border-right: 0;
    border-radius: 10px 0 0 10px;
    background: var(--bg-subtle);
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

.form-select {
    padding-right: 28px;
    appearance: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364807e' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 10px center;
}

.items-actions-bar {
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

.total-hint {
    width: 100px;
    color: var(--text-secondary);
    font-size: 14px;
}

.amount-hint {
    width: calc(100% - 260px);
    color: var(--primary);
    font-weight: 700;
    font-size: 15px;
}

@media (max-width: 900px) {
    .field-product,
    .supplier-field,
    .warehouse-field,
    .direction-field,
    .form-select,
    .form-input,
    .search-control {
        width: 100%;
    }

    .search-control input {
        width: calc(100% - 112px);
    }

    .direction-options {
        flex-direction: column;
    }

    .direction-in,
    .direction-in-selected,
    .direction-out,
    .direction-out-selected {
        width: 100%;
    }

    .field-product {
        width: 100%;
    }
}
</style>
