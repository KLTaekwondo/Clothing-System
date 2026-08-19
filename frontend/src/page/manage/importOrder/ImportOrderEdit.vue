<template>
    <div class="import-order-edit">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    type="button"
                    @click="goBack"
                >← 返回采购单</button>
                <div>
                    <h2 class="page-title">编辑采购订单草稿</h2>
                    <p class="page-desc">{{ order?.importOrderNo || '加载中...' }}</p>
                </div>
            </div>
        </div>

        <div
            v-if="loading"
            class="loading-overlay"
        >
            <div class="loading-spinner"></div>
        </div>
        <div
            v-else-if="!order"
            class="empty-card"
        >
            <div class="empty-state">
                <div class="empty-icon"><IconGraphic name="order"/></div>
                <div class="empty-text">采购订单不存在或已无法编辑</div>
            </div>
        </div>
        <template v-else>
            <div class="form-card">
                <div class="form-grid">
                    <div class="reference-column">
                        <div class="supplier-field">
                            <label>供应商</label>
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
                    </div>
                    <div class="direction-column">
                        <div class="direction-field">
                            <label>业务方向</label>
                            <div class="direction-options">
                                <button
                                    :class="form.direction === 'IN' ? 'direction-in-selected' : 'direction-in-disabled'"
                                    disabled
                                    type="button"
                                >
                                    <strong>采购入库</strong>
                                    <span>商品进入所选仓库</span>
                                </button>
                                <button
                                    :class="form.direction === 'OUT' ? 'direction-out-selected' : 'direction-out-disabled'"
                                    disabled
                                    type="button"
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
                                @input="dirty = true"
                            ></textarea>
                        </div>
                    </div>
                </div>
                <span
                    v-if="!matchedReferences"
                    class="field-warning"
                >未能匹配供应商或仓库，当前订单无法保存</span>
            </div>

            <div class="search-card">
                <div class="search-product-field">
                    <label>商品编码</label>
                    <div class="search-control">
                        <input
                            v-model="searchCode"
                            :disabled="searching"
                            placeholder="输入商品编码后查询"
                            type="text"
                            @keyup.enter="searchProduct"
                        />
                        <button
                            :disabled="searching || !searchCode.trim()"
                            class="search-button"
                            type="button"
                            @click="searchProduct"
                        >{{ searching ? '查询中...' : '查询并添加' }}</button>
                    </div>
                </div>
            </div>

            <div
                v-if="items.length > 0"
                class="items-actions-bar"
            >
                <span class="total-hint">共 {{ items.length }} 个 SKU</span>
                <span class="amount-hint">合计：¥{{ totalAmount }}</span>
                <button
                    :disabled="saving || !canSave"
                    class="save-button"
                    type="button"
                    @click="handleSave"
                >{{ saving ? '保存中...' : '保存修改' }}</button>
                <button
                    class="clear-button"
                    type="button"
                    @click="clearItems"
                >清空全部</button>
            </div>

            <div
                v-if="items.length === 0"
                class="empty-card"
            >
                <div class="empty-state">
                    <div class="empty-icon"><IconGraphic name="product"/></div>
                    <div class="empty-text">当前没有采购 SKU，请搜索并添加</div>
                </div>
            </div>
            <ImportOrderItemList
                v-else
                :groups="groupedItems"
                @remove="removeSku"
                @update-quantity="updateQuantity"
            />
        </template>
    </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave, useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import {useConfirmStore} from '../../../stores/confirmStore.js'
import importOrderInterface from '../../../axios/interface/ImportOrderInterface.js'
import supplierInterface from '../../../axios/interface/SupplierInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import ImportOrderItemList from '../../../component/importOrder/ImportOrderItemList.vue'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'
import {AUDIT_STATUS} from '../../../constants/auditStatus.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const confirmStore = useConfirmStore()
const order = ref(null)
const suppliers = ref([])
const supplierPickerValue = ref('')
const supplierLoading = ref(false)
const warehouses = ref([])
const warehousePickerValue = ref('')
const warehouseLoading = ref(false)
const items = ref([])
const searchCode = ref('')
const searching = ref(false)
const saving = ref(false)
const loading = ref(true)
const dirty = ref(false)
const form = ref({
    supplierId: '',
    wareHouseId: '',
    direction: '',
    remark: ''
})

const matchedReferences = computed(() => Boolean(form.value.supplierId && form.value.wareHouseId))
const canSave = computed(() => matchedReferences.value && Boolean(form.value.direction))
const supplierOptions = computed(() => suppliers.value.map(supplier => ({
    id: supplier.id,
    optionValue: formatSupplierOption(supplier)
})))
const warehouseOptions = computed(() => warehouses.value.map(warehouse => ({
    id: warehouse.id,
    optionValue: formatWarehouseOption(warehouse)
})))
const totalAmount = computed(() => items.value.reduce((sum, item) => {
    return sum + Number(item.importPrice || 0) * Number(item.quantity || 0)
}, 0).toFixed(2))
const groupedItems = computed(() => {
    const groups = {}
    items.value.forEach(item => {
        const key = item.productId || item.productCode || item.skuCode
        if (!groups[key]) {
            groups[key] = {
                productId: item.productId || key,
                productName: item.productName,
                productCode: item.productCode,
                skus: []
            }
        }
        groups[key].skus.push(item)
    })
    return Object.values(groups)
})

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        const detail = await importOrderInterface.search(route.params.id)
        if (detail.status !== AUDIT_STATUS.DRAFT) {
            toast.warning('只有草稿状态的采购订单可以编辑')
            await router.replace(`/manage/import-order/${route.params.id}`)
            return
        }
        order.value = detail
        form.value.remark = detail.remark || ''
        form.value.direction = detail.direction || ''
        supplierLoading.value = true
        warehouseLoading.value = true
        const [supplierList, warehouseList] = await Promise.all([
            supplierInterface.searchList(),
            wareHouseInterface.searchList()
        ])
        suppliers.value = supplierList
        warehouses.value = warehouseList
        const matchedSupplier = suppliers.value.find(item => item.supplierName === detail.supplierName)
        form.value.supplierId = matchedSupplier?.id || ''
        supplierPickerValue.value = matchedSupplier ? formatSupplierOption(matchedSupplier) : ''
        const matchedWarehouse = warehouses.value.find(item => item.name === detail.wareHouseName)
        form.value.wareHouseId = matchedWarehouse?.id || ''
        warehousePickerValue.value = matchedWarehouse ? formatWarehouseOption(matchedWarehouse) : ''
        items.value = await hydrateItems(detail.items || [])
    } catch {
        order.value = null
    } finally {
        supplierLoading.value = false
        warehouseLoading.value = false
        loading.value = false
    }
})

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(async () => {
    if (!dirty.value) return true
    return await confirmStore.confirm({
        title: '放弃未保存的采购单？',
        message: '当前采购订单修改尚未保存，离开后修改将丢失。',
        confirmText: '确定离开',
        danger: true
    })
})

function syncSupplierId(value) {
    const selected = suppliers.value.find(supplier => formatSupplierOption(supplier) === value)
    form.value.supplierId = selected?.id || ''
    dirty.value = true
}

function formatSupplierOption(supplier) {
    if (supplier.supplierCode) return `${supplier.supplierName}（${supplier.supplierCode}）`
    if (supplier.code) return `${supplier.supplierName}（${supplier.code}）`
    return supplier.supplierName
}

function syncWarehouseId(value) {
    const selected = warehouses.value.find(warehouse => formatWarehouseOption(warehouse) === value)
    form.value.wareHouseId = selected?.id || ''
    dirty.value = true
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

function hydrateItems(orderItems) {
    return Promise.all(orderItems.map(async item => {
        let sku = null
        try {
            const result = await productSkuInterface.scan(item.skuCode)
            const results = Array.isArray(result) ? result : result ? [result] : []
            sku = results.find(value => value.code === item.skuCode) || results[0]
        } catch {
        }
        return {
            skuId: sku?.id || item.skuCode,
            skuCode: item.skuCode,
            skuName: item.skuName,
            spec: sku?.spec || {},
            productId: sku?.productId || item.productCode,
            productCode: item.productCode,
            productName: item.productName,
            importPrice: Number(item.importPrice || 0),
            quantity: Number(item.quantity || 1)
        }
    }))
}

async function searchProduct() {
    const code = searchCode.value.trim()
    if (!code) return
    searching.value = true
    try {
        const data = await productInterface.searchPage()
        const product = (data.content || []).find(item => item.code === code)
        if (!product) {
            toast.info('未找到该编码的商品')
            return
        }
        const skuList = await productSkuInterface.searchListByProductId(product.id)
        if (!skuList.length) {
            toast.info('该商品没有 SKU')
            return
        }
        if (items.value.some(item => item.productId === product.id)) {
            toast.info('该商品已添加')
            searchCode.value = ''
            return
        }
        skuList.forEach(sku => {
            items.value.push({
                skuId: sku.id,
                skuCode: sku.code,
                skuName: sku.name,
                spec: parseSpec(sku.spec),
                productId: product.id,
                productCode: product.code,
                productName: product.name,
                importPrice: Number(product.importPrice || 0),
                quantity: 1
            })
        })
        dirty.value = true
        toast.success(`已添加「${product.name}」的 ${skuList.length} 个 SKU`)
        searchCode.value = ''
    } catch {
        toast.info('查询失败')
    } finally {
        searching.value = false
    }
}

function updateQuantity(skuId, quantity) {
    const item = items.value.find(value => value.skuId === skuId)
    if (!item) return
    item.quantity = quantity
    dirty.value = true
}

function removeSku(skuId) {
    const index = items.value.findIndex(item => item.skuId === skuId)
    if (index !== -1) {
        items.value.splice(index, 1)
        dirty.value = true
    }
}

async function clearItems() {
    if (!items.value.length) return
    const confirmed = await confirmStore.confirm({
        title: '清空采购项？',
        message: '清空后当前采购单中的所有 SKU 将被移除。',
        confirmText: '确定清空',
        danger: true
    })
    if (!confirmed) return
    items.value = []
    dirty.value = true
}

async function handleSave() {
    if (!canSave.value) {
        toast.warning('请补全供应商、仓库和业务方向')
        return
    }
    if (!items.value.length) {
        toast.warning('请至少添加一个商品')
        return
    }
    saving.value = true
    try {
        await importOrderInterface.update(order.value.id, {
            supplierId: Number(form.value.supplierId),
            wareHouseId: Number(form.value.wareHouseId),
            direction: order.value.direction,
            totalAmount: Number(totalAmount.value),
            remark: form.value.remark.trim(),
            importItems: items.value.map(item => ({
                skuCode: item.skuCode,
                importQuantity: Number(item.quantity) || 1
            }))
        })
        dirty.value = false
        toast.success('采购订单草稿已更新')
        await router.push(`/manage/import-order/${order.value.id}`)
    } catch {
    } finally {
        saving.value = false
    }
}

function parseSpec(spec) {
    if (typeof spec !== 'string') return spec || {}
    try {
        return JSON.parse(spec)
    } catch {
        return {}
    }
}

function handleBeforeUnload(event) {
    if (!dirty.value) return
    event.preventDefault()
    event.returnValue = ''
}

function goBack() {
    router.push(`/manage/import-order/${route.params.id}`)
}
</script>

<style scoped>
.import-order-edit {
    width: 100%;
    min-width: 0;
}

.page-heading,
.heading-left {
    display: flex;
    align-items: center;
}

.page-heading {
    margin-bottom: 28px;
}

.heading-left {
    gap: 12px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.form-card,
.search-card,
.empty-card {
    padding: 20px;
    margin-bottom: 16px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.form-grid {
    display: flex;
    align-items: stretch;
    gap: 18px;
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
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
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

.direction-in-disabled,
.direction-in-selected,
.direction-out-disabled,
.direction-out-selected {
    width: 100%;
    min-height: 58px;
    display: flex;
    align-items: flex-start;
    flex-direction: column;
    justify-content: center;
    gap: 4px;
    padding: 8px 12px;
    border: 1px solid #dceae7;
    border-radius: 9px;
    background: #fbfefd;
    text-align: left;
    cursor: not-allowed;
    opacity: 0.55;
}

.direction-in-selected,
.direction-out-selected {
    opacity: 1;
}

.direction-in-disabled strong,
.direction-in-selected strong,
.direction-out-disabled strong,
.direction-out-selected strong {
    font-size: 13px;
}

.direction-in-disabled span,
.direction-in-selected span,
.direction-out-disabled span,
.direction-out-selected span {
    color: var(--text-muted);
    font-size: 11px;
}

.direction-in-selected {
    border-color: #22c55e;
    color: #15803d;
    background: #f0fdf4;
}

.direction-out-selected {
    border-color: #ef4444;
    color: #dc2626;
    background: #fef2f2;
}

.direction-in-selected {
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
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
    font: inherit;
}

.remark-column textarea:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
    outline: none;
}

.form-select,
.form-input {
    width: 260px;
    height: 40px;
    box-sizing: border-box;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
}

.form-select {
    padding-right: 28px;
}

.field-warning {
    display: block;
    margin-top: 10px;
    color: #dc2626;
    font-size: 12px;
}

.search-product-field {
    width: 432px;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.search-control {
    width: 432px;
    display: flex;
    align-items: stretch;
}

.search-product-field label {
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
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
    color: #fff;
    background: var(--primary);
    font-weight: 600;
    cursor: pointer;
}

.search-button:disabled {
    cursor: not-allowed;
    opacity: 0.55;
}

.items-actions-bar {
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
    color: var(--text-secondary);
    font-size: 14px;
}

.amount-hint {
    width: calc(100% - 480px);
    color: var(--primary);
    font-size: 15px;
    font-weight: 700;
}

.save-button {
    padding: 9px 16px;
    border: 1px solid var(--primary);
    border-radius: 9px;
    color: #fff;
    background: var(--primary);
    font-weight: 600;
    cursor: pointer;
}

.save-button:disabled {
    cursor: not-allowed;
    opacity: 0.55;
}

.clear-button {
    padding: 8px 14px;
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

@media (max-width: 900px) {
    .page-heading,
    .form-grid {
        align-items: flex-start;
        flex-direction: column;
    }

    .field,
    .supplier-field,
    .warehouse-field,
    .direction-field,
    .search-product-field,
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

    .direction-in-disabled,
    .direction-in-selected,
    .direction-out-disabled,
    .direction-out-selected {
        width: 100%;
    }

    .items-actions-bar {
        align-items: flex-start;
        flex-direction: column;
    }

    .items-actions-bar button {
        width: 100%;
    }
}
</style>
