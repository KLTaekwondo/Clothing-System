<template>
    <div class="product-detail-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回</button>
                <div>
                    <h2 class="page-title">商品详情</h2>
                    <p v-if="product" class="page-desc">{{ product.name }} · {{ product.code }}</p>
                </div>
            </div>
            <button v-if="!editing" class="btn-primary" @click="startEdit">编辑商品</button>
        </div>

        <div class="detail-tabs">
            <button :class="{ active: activeTab === 'info' }" class="detail-tab" @click="activeTab = 'info'">基本信息
            </button>
            <button :class="{ active: activeTab === 'sku' }" class="detail-tab" @click="activeTab = 'sku'">SKU 管理
            </button>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!product" class="empty-state">
            <div class="empty-icon"><IconGraphic name="product"/></div>
            <div class="empty-text">商品不存在</div>
        </div>

        <!-- 基本信息 Tab -->
        <template v-if="activeTab === 'info' && product">
            <div class="info-grid">
                <div class="info-card">
                    <span class="info-label">商品编码</span>
                    <input v-if="editing" v-model="form.code" class="card-input" maxlength="20" type="text"/>
                    <strong v-else class="code-value">{{ product.code }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">商品名称</span>
                    <input v-if="editing" v-model="form.name" class="card-input" maxlength="20" type="text"/>
                    <strong v-else>{{ product.name }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">类型</span>
                    <select v-if="editing" v-model="form.type" class="card-select">
                        <option v-for="opt in typeOptions" :key="opt.id" :value="opt.optionValue">{{
                                opt.optionValue
                            }}
                        </option>
                    </select>
                    <strong v-else>{{ product.type || '-' }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">种类</span>
                    <select v-if="editing" v-model="form.category" class="card-select">
                        <option v-for="opt in categoryOptions" :key="opt.id" :value="opt.optionValue">{{
                                opt.optionValue
                            }}
                        </option>
                    </select>
                    <strong v-else>{{ product.category || '-' }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">季节</span>
                    <select v-if="editing" v-model="form.season" class="card-select">
                        <option v-for="s in seasonOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
                    </select>
                    <strong v-else>{{ seasonLabels[product.season] || product.season }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">单位</span>
                    <select v-if="editing" v-model="form.unit" class="card-select">
                        <option v-for="opt in unitOptions" :key="opt.id" :value="opt.optionValue">{{
                                opt.optionValue
                            }}
                        </option>
                    </select>
                    <strong v-else>{{ product.unit || '-' }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">面料组合</span>
                    <select v-if="editing" v-model="form.composition" class="card-select">
                        <option v-for="opt in compositionOptions" :key="opt.id" :value="opt.optionValue">
                            {{ opt.optionValue }}
                        </option>
                    </select>
                    <strong v-else>{{ product.composition || '-' }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">年份</span>
                    <select v-if="editing" v-model="form.year" class="card-select">
                        <option v-for="opt in yearOptions" :key="opt.id" :value="opt.optionValue">
                            {{ opt.optionValue }}
                        </option>
                    </select>
                    <strong v-else>{{ product.year || '-' }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">状态</span>
                    <select v-if="editing" v-model="form.status" class="card-select">
                        <option v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</option>
                    </select>
                    <span v-else :class="product.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                          class="status-badge">{{
                            statusLabels[product.status] || product.status
                        }}</span>
                </div>
                <div class="info-card">
                    <span class="info-label">进货价格</span>
                    <input v-if="editing" v-model.number="form.importPrice" class="card-input" min="0" step="0.01"
                           type="number"/>
                    <strong v-else>¥{{ product.importPrice }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">销售价格</span>
                    <input v-if="editing" v-model.number="form.salePrice" class="card-input price-input" min="0"
                           step="0.01"
                           type="number"/>
                    <strong v-else class="price-value">¥{{ product.salePrice }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">特价商品</span>
                    <button v-if="editing" :class="{ active: form.special }" class="card-toggle" type="button"
                            @click="form.special = !form.special">{{ form.special ? '是' : '否' }}
                    </button>
                    <strong v-else>{{ product.special ? '是' : '否' }}</strong>
                </div>
                <div class="info-card">
                    <span class="info-label">创建时间</span>
                    <strong class="time-value">{{ product.createTime || '-' }}</strong>
                </div>
            </div>
            <div v-if="editing" class="edit-actions">
                <button class="btn-outline" @click="cancelEdit">取消</button>
                <button :disabled="saving" class="btn-primary" @click="saveEdit">{{
                        saving ? '保存中...' : '保存修改'
                    }}
                </button>
            </div>
        </template>

        <!-- SKU 管理 Tab -->
        <template v-if="activeTab === 'sku' && product">
            <div class="tab-toolbar">
                <span class="sku-count">共 {{ skuList.length }} 个 SKU</span>
                <router-link :to="`/manage/product/${product.id}/sku/add`" class="btn-primary btn-sm">+ 添加 SKU
                </router-link>
            </div>
            <div class="card table-card">
                <div v-if="skuLoading" class="loading-overlay">
                    <div class="loading-spinner"></div>
                </div>
                <div v-else-if="skuList.length === 0" class="empty-state">
                    <div class="empty-icon"><IconGraphic name="tag"/></div>
                    <div class="empty-text">暂无 SKU</div>
                </div>
                <table v-else class="data-table">
                    <thead>
                    <tr>
                        <th>编码</th>
                        <th>名称</th>
                        <th>规格</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="sku in skuList" :key="sku.id">
                        <td><code>{{ sku.code }}</code></td>
                        <td><strong>{{ sku.name }}</strong></td>
                        <td>{{ sku.spec }}</td>
                        <td><span :class="sku.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                                  class="status-badge">{{
                                statusLabels[sku.status] || sku.status
                            }}</span></td>
                        <td>
                            <div class="actions">
                                <router-link :to="`/manage/product/${product.id}/sku/${sku.id}`"
                                             class="btn-outline btn-sm">详情
                                </router-link>
                                <button class="btn-danger btn-sm" @click="deleteSku(sku)">删除</button>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </template>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import productInterface from '../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'
import optionValueInterface from '../../axios/interface/OptionValueInterface.js'
import {SEASON_LABELS, SEASON_OPTIONS} from '../../constants/season.js'
import {STATUS, STATUS_LABELS, STATUS_OPTIONS} from '../../constants/status.js'
import {OPTION_TYPE} from '../../constants/optionType.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const seasonLabels = SEASON_LABELS
const seasonOptions = SEASON_OPTIONS
const statusLabels = STATUS_LABELS
const statusOptions = STATUS_OPTIONS

const product = ref(null)
const skuList = ref([])
const loading = ref(true)
const skuLoading = ref(false)
const editing = ref(false)
const saving = ref(false)
const form = ref({})
const formBackup = ref(null)
const activeTab = ref('info')
const typeOptions = ref([])
const categoryOptions = ref([])
const unitOptions = ref([])
const compositionOptions = ref([])
const yearOptions = ref([])

onMounted(fetchDetail)

async function fetchDetail() {
    try {
        const [productResult, skus, t, cat, u, comp, year] = await Promise.all([
            productInterface.search(route.params.id),
            productSkuInterface.searchListByProductId(route.params.id).catch(() => []),
            optionValueInterface.searchListByType(OPTION_TYPE.TYPE).catch(() => []),
            optionValueInterface.searchListByType(OPTION_TYPE.CATEGORY).catch(() => []),
            optionValueInterface.searchListByType(OPTION_TYPE.UNIT).catch(() => []),
            optionValueInterface.searchListByType(OPTION_TYPE.COMPOSITION).catch(() => []),
            optionValueInterface.searchListByType(OPTION_TYPE.YEAR).catch(() => [])
        ])
        product.value = productResult
        skuList.value = skus
        typeOptions.value = Array.isArray(t) ? t : []
        categoryOptions.value = Array.isArray(cat) ? cat : []
        unitOptions.value = Array.isArray(u) ? u : []
        compositionOptions.value = Array.isArray(comp) ? comp : []
        yearOptions.value = Array.isArray(year) ? year : []
    } catch {
        product.value = null
    } finally {
        loading.value = false
        skuLoading.value = false
    }
}

function startEdit() {
    const p = product.value
    form.value = {
        code: p.code || '',
        name: p.name || '',
        season: p.season || '',
        type: p.type || '',
        category: p.category || '',
        unit: p.unit || '',
        composition: p.composition || '',
        year: p.year || '',
        importPrice: p.importPrice ?? 0,
        salePrice: p.salePrice ?? 0,
        special: Boolean(p.special),
        status: p.status || STATUS.ENABLE
    }
    formBackup.value = {...form.value}
    editing.value = true
}

function cancelEdit() {
    form.value = {...formBackup.value}
    editing.value = false
    formBackup.value = null
}

async function saveEdit() {
    saving.value = true
    try {
        await productInterface.update(product.value.id, {
            code: form.value.code,
            name: form.value.name,
            season: form.value.season,
            type: form.value.type,
            category: form.value.category,
            unit: form.value.unit,
            composition: form.value.composition,
            year: form.value.year,
            importPrice: form.value.importPrice,
            salePrice: form.value.salePrice,
            special: form.value.special,
            status: form.value.status
        })
        product.value = await productInterface.search(route.params.id)
        editing.value = false
        formBackup.value = null
    } catch {
    } finally {
        saving.value = false
    }
}

async function deleteSku(sku) {
    if (!confirm(`确定要删除 SKU「${sku.name}」吗？`)) return
    try {
        await productSkuInterface.softDelete(sku.id)
        skuList.value = await productSkuInterface.searchListByProductId(route.params.id)
    } catch {
    }
}

function goBack() {
    router.push('/manage/product')
}
</script>

<style scoped>
.product-detail-page {
    width: 100%;
    min-width: 0;
}

.page-heading,
.heading-left {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 20px;
}

.heading-left {
    gap: 12px;
}

.page-title {
    margin-bottom: 4px;
    font-size: 26px;
    letter-spacing: -0.5px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.detail-tabs {
    display: flex;
    gap: 4px;
    margin-bottom: 22px;
    border-bottom: 1px solid #dfece9;
}

.detail-tab {
    padding: 10px 20px;
    border: none;
    border-bottom: 2px solid transparent;
    background: none;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    margin-bottom: -1px;
}

.detail-tab:hover {
    color: var(--primary);
}

.detail-tab.active {
    color: #0d9488;
    border-bottom-color: #0d9488;
}

.info-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
}

.info-card {
    width: calc(25% - 11px);
    min-width: 190px;
    min-height: 108px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 12px;
    padding: 18px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border: 1px solid #e6f0ef;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(15, 118, 110, 0.06);
}

.info-card:nth-child(4n+1) {
    border-top: 3px solid #0d9488;
}

.info-label {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.code-value {
    color: var(--primary);
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

.price-value {
    color: var(--primary);
    font-size: 22px;
    font-weight: 700;
}

.time-value {
    font-size: 13px;
}

.card-input {
    width: 100%;
    height: 38px;
    padding: 0 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fff;
    font-size: 14px;
    outline: none;
}

.card-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.price-input {
    font-size: 18px;
    font-weight: 700;
    color: #0d9488;
}

.card-select {
    width: 100%;
    height: 38px;
    padding: 0 24px 0 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fff;
    font-size: 14px;
    appearance: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364807e' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 8px center;
}

.card-select:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.card-toggle {
    width: 60px;
    height: 34px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 600;
    border: 1px solid #dceae7;
    background: #fff;
    color: var(--text-secondary);
    cursor: pointer;
}

.card-toggle.active {
    background: linear-gradient(135deg, #0f766e, #14b8a6);
    color: #fff;
    border-color: transparent;
}

.edit-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 18px;
    border-top: 1px solid var(--border-light);
}

.tab-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 14px;
}

.sku-count {
    font-size: 14px;
    color: var(--text-secondary);
}

.table-card {
    padding: 8px 20px 20px;
    border-radius: 16px;
}

.data-table td {
    height: 56px;
}

@media (max-width: 900px) {
    .info-card {
        width: calc(50% - 7px);
    }

    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 12px;
    }
}

@media (max-width: 560px) {
    .info-card {
        width: 100%;
    }
}
</style>
