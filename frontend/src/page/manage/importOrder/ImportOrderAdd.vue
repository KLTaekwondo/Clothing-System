<template>
    <div class="import-order-add">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回采购单</button>
                <div>
                    <h2 class="page-title">新建采购单</h2>
                    <p class="page-desc">选择供应商和仓库，添加商品后保存草稿</p>
                </div>
            </div>
        </div>

        <div class="card form-card">
            <div class="form-grid">
                <div class="field">
                    <label>供应商 <span class="required">*</span></label>
                    <select v-model="form.supplierId" class="form-select">
                        <option disabled value="">请选择供应商</option>
                        <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.supplierName }}</option>
                    </select>
                </div>
                <div class="field">
                    <label>仓库 <span class="required">*</span></label>
                    <select v-model="form.wareHouseId" class="form-select">
                        <option disabled value="">请选择仓库</option>
                        <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
                    </select>
                </div>
                <div class="field">
                    <label>业务方向 <span class="required">*</span></label>
                    <select v-model="form.direction" class="form-select">
                        <option disabled value="">请选择方向</option>
                        <option value="IN">采购入库</option>
                        <option value="OUT">采购退货</option>
                    </select>
                </div>
                <div class="field">
                    <label>备注</label>
                    <input v-model="form.remark" class="form-input" maxlength="100" placeholder="不超过 100 字" type="text"/>
                </div>
            </div>
        </div>

        <div class="card search-card">
            <div class="search-row">
                <div class="field field-product">
                    <label>商品编码</label>
                    <input v-model="searchCode" placeholder="输入商品编码后查询" type="text" @keyup.enter="searchProduct"/>
                </div>
                <button :disabled="searching" class="btn-primary search-button" @click="searchProduct">
                    {{ searching ? '查询中...' : '查询并添加' }}
                </button>
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
import ImportOrderItemList from './components/ImportOrderItemList.vue'

const router = useRouter()
const toast = useToastStore()

const suppliers = ref([])
const warehouses = ref([])
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

const canSave = computed(() => form.value.supplierId && form.value.wareHouseId && form.value.direction)

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
    try {
        suppliers.value = await supplierInterface.searchList()
    } catch {
        suppliers.value = []
    }
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    }
})

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
            totalAmount,
            remark: form.value.remark || '',
            importItems
        })
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

.page-heading, .heading-left {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 28px;
}

.heading-left {
    gap: 12px;
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

.card {
    padding: 20px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.form-card {
    margin-bottom: 16px;
}

.form-grid {
    display: flex;
    gap: 16px;
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

.required {
    color: #dc2626;
}

.field-product {
    width: 400px;
}

.form-select, .form-input {
    width: 260px;
    height: 40px;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
    font-size: 14px;
}

.form-select {
    padding-right: 28px;
    appearance: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364807e' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 10px center;
}

.search-card {
    margin-bottom: 16px;
}

.search-row {
    display: flex;
    gap: 14px;
    align-items: flex-end;
    flex-wrap: wrap;
}

.search-button {
    height: 40px;
}

.search-row input {
    width: 320px;
    height: 40px;
    padding: 0 12px;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #fbfefd;
}

.empty-card {
    border-radius: 16px;
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
    flex: 1;
    color: var(--primary);
    font-weight: 700;
    font-size: 15px;
}

@media (max-width: 900px) {
    .search-row {
        flex-direction: column;
        align-items: stretch;
    }

    .search-row input, .form-select, .form-input {
        width: 100%;
    }

    .field-product {
        width: 100%;
    }
}
</style>
