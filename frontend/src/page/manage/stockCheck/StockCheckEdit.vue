<template>
    <div class="stock-check-edit">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    type="button"
                    @click="goBack"
                >← 返回盘点单</button>
                <div>
                    <h2 class="page-title">编辑库存盘点草稿</h2>
                    <p class="page-desc">{{ checkOrder?.stockCheckNo || '加载中...' }}</p>
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
            v-else-if="!checkOrder"
            class="empty-card"
        >
            <div class="empty-state">
                <div class="empty-icon"><IconGraphic name="document"/></div>
                <div class="empty-text">盘点草稿不存在或已无法编辑</div>
            </div>
        </div>
        <template v-else>
            <div class="form-card">
                <div class="form-row">
                    <div class="field">
                        <label>盘点仓库</label>
                        <input
                            :value="warehouseLabel"
                            class="locked-input"
                            disabled
                            type="text"
                        />
                        <span class="field-hint">草稿编辑期间不能更换盘点仓库</span>
                    </div>
                    <div class="remark-field">
                        <label>备注</label>
                        <input
                            v-model="remark"
                            maxlength="100"
                            placeholder="不超过 100 字"
                            type="text"
                            @input="dirty = true"
                        />
                    </div>
                </div>
            </div>

            <div class="search-card">
                <div class="search-row">
                    <div class="search-field">
                        <label>商品或 SKU 编码</label>
                        <input
                            v-model="searchCode"
                            :disabled="searching"
                            placeholder="输入编码后按回车"
                            type="text"
                            @keyup.enter="searchSku"
                        />
                    </div>
                    <button
                        :disabled="searching || !searchCode.trim()"
                        class="btn-primary"
                        type="button"
                        @click="searchSku"
                    >{{ searching ? '查询中...' : '查询 SKU' }}</button>
                </div>
                <div
                    v-if="candidates.length > 0"
                    class="candidate-list"
                >
                    <button
                        v-for="candidate in candidates"
                        :key="candidate.code"
                        class="candidate-item"
                        type="button"
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
                    type="button"
                    @click="handleSave"
                >{{ saving ? '保存中...' : '保存修改' }}</button>
                <button
                    class="btn-outline"
                    type="button"
                    @click="clearItems"
                >清空全部</button>
            </div>

            <div
                v-if="items.length === 0"
                class="empty-card"
            >
                <div class="empty-state">
                    <div class="empty-icon"><IconGraphic name="stock"/></div>
                    <div class="empty-text">当前没有盘点 SKU，请搜索并添加</div>
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
        </template>
    </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref} from 'vue'
import {onBeforeRouteLeave, useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import stockCheckInterface from '../../../axios/interface/StockCheckInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../../axios/interface/WareHouseStockInterface.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import StockCheckItemList from './components/StockCheckItemList.vue'
import {AUDIT_STATUS} from '../../../constants/auditStatus.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const checkOrder = ref(null)
const warehouses = ref([])
const warehouseId = ref('')
const warehouseLabel = ref('')
const remark = ref('')
const searchCode = ref('')
const searching = ref(false)
const saving = ref(false)
const loading = ref(true)
const candidates = ref([])
const items = ref([])
const dirty = ref(false)

const differenceCount = computed(() => items.value.filter(item => {
    return Number(item.actualQuantity) !== Number(item.systemQuantity)
}).length)

const hasUnsavedContent = computed(() => {
    return dirty.value
})

onMounted(async () => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    try {
        const detail = await stockCheckInterface.search(route.params.id)
        if (detail.status !== AUDIT_STATUS.DRAFT) {
            toast.warning('只有草稿状态的盘点单可以编辑')
            await router.replace(`/manage/stock-check/${route.params.id}`)
            return
        }
        checkOrder.value = detail
        remark.value = detail.remark || ''
        items.value = (detail.stockCheckItems || []).map(item => ({
            skuCode: item.skuCode,
            name: item.skuName,
            spec: {},
            productName: item.productName,
            productCode: item.productCode,
            systemQuantity: Number(item.systemQuantity || 0),
            actualQuantity: Number(item.actualQuantity || 0)
        }))
        warehouses.value = await wareHouseInterface.searchList()
        const warehouse = warehouses.value.find(item => item.code === detail.wareHouseCode)
        if (!warehouse) {
            toast.warning('未能匹配盘点仓库，当前草稿无法保存')
            return
        }
        warehouseId.value = warehouse.id
        warehouseLabel.value = `${warehouse.name}（${warehouse.code}）`
    } catch {
        checkOrder.value = null
    } finally {
        loading.value = false
    }
})

onBeforeUnmount(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
})

onBeforeRouteLeave(() => {
    if (!hasUnsavedContent.value) return true
    return window.confirm('当前盘点修改尚未保存，确定要离开吗？')
})

async function searchSku() {
    const code = searchCode.value.trim()
    if (!code) return
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
        clearSearch()
        return
    }
    searching.value = true
    try {
        const product = await findProductByCode(sku.productCode)
        if (!product) {
            toast.warning('未找到该 SKU 对应的商品信息')
            return
        }
        const stocks = await wareHouseStockInterface.searchStock(warehouseId.value, product.id)
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
        dirty.value = true
        clearSearch()
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

function updateQuantity(skuCode, quantity) {
    const item = items.value.find(value => value.skuCode === skuCode)
    if (item) {
        item.actualQuantity = quantity
        dirty.value = true
    }
}

function removeItem(skuCode) {
    const index = items.value.findIndex(item => item.skuCode === skuCode)
    if (index !== -1) {
        items.value.splice(index, 1)
        dirty.value = true
    }
}

function clearItems() {
    if (!items.value.length) return
    if (!window.confirm('确定清空全部盘点项吗？')) return
    items.value = []
    dirty.value = true
}

function clearSearch() {
    candidates.value = []
    searchCode.value = ''
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

async function handleSave() {
    if (!warehouseId.value) {
        toast.warning('未能匹配盘点仓库，无法保存修改')
        return
    }
    if (!items.value.length) {
        toast.warning('请至少保留一个盘点项')
        return
    }
    saving.value = true
    try {
        await stockCheckInterface.update(checkOrder.value.id, {
            wareHouseId: Number(warehouseId.value),
            remark: remark.value.trim(),
            stockCheckItems: items.value.map(item => ({
                skuCode: item.skuCode,
                actualQuantity: Number(item.actualQuantity)
            }))
        })
        dirty.value = false
        toast.success('盘点单草稿已更新')
        await router.push(`/manage/stock-check/${checkOrder.value.id}`)
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
    router.push(`/manage/stock-check/${route.params.id}`)
}
</script>

<style scoped>
.stock-check-edit {
    width: 100%;
    min-width: 0;
}

.page-heading,
.heading-left {
    display: flex;
    align-items: center;
}

.page-heading {
    margin-bottom: 26px;
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

.form-row,
.search-row {
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

.field,
.remark-field {
    width: calc(50% - 8px);
}

.field input,
.remark-field input,
.search-field input {
    width: 100%;
    height: 40px;
    box-sizing: border-box;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
}

.locked-input {
    color: var(--text-secondary);
    cursor: not-allowed;
}

.field-hint {
    color: var(--text-muted);
    font-size: 12px;
}

.search-field {
    width: 420px;
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
    border: 1px solid #dceae7;
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
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.action-bar span {
    color: var(--text-secondary);
}

.action-bar strong {
    width: 35%;
    margin-right: auto;
    color: #16a34a;
}

.action-bar .has-difference {
    color: #d97706;
}

.item-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

@media (max-width: 900px) {
    .form-row,
    .search-row {
        align-items: stretch;
        flex-direction: column;
    }

    .field,
    .remark-field,
    .search-field {
        width: 100%;
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
