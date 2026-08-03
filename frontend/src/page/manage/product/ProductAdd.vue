<template>
    <div class="product-add">
        <div class="page-top">
            <button class="btn-outline" @click="goBack">← 返回</button>
            <h2 class="page-title">添加商品</h2>
        </div>

        <div class="add-layout">
            <div class="add-main">
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">基本信息</span>
                    </div>
                    <form class="add-form" @submit.prevent="handleSubmit">
                        <div class="form-row">
                            <div class="form-group"><label>商品编码</label><input v-model="form.code" maxlength="20"
                                                                                  required type="text"/></div>
                            <div class="form-group"><label>商品名称</label><input v-model="form.name" maxlength="20"
                                                                                  required type="text"/></div>
                        </div>
                        <div class="form-row">
                            <div class="form-group"><label>季节</label><select v-model="form.season" required>
                                <option disabled value="">请选择</option>
                                <option v-for="s in seasons" :key="s.value" :value="s.value">{{ s.label }}</option>
                            </select></div>
                            <div class="form-group"><label>类型</label><select v-model="form.type" required>
                                <option disabled value="">请选择</option>
                                <option v-for="opt in typeOptions" :key="opt.id" :value="opt.optionValue">
                                    {{ opt.optionValue }}
                                </option>
                            </select></div>
                        </div>
                        <div class="form-row">
                            <div class="form-group"><label>种类</label><select v-model="form.category" required>
                                <option disabled value="">请选择</option>
                                <option v-for="opt in categoryOptions" :key="opt.id" :value="opt.optionValue">
                                    {{ opt.optionValue }}
                                </option>
                            </select></div>
                            <div class="form-group"><label>单位</label><select v-model="form.unit" required>
                                <option disabled value="">请选择</option>
                                <option v-for="opt in unitOptions" :key="opt.id" :value="opt.optionValue">
                                    {{ opt.optionValue }}
                                </option>
                            </select></div>
                        </div>
                        <div class="form-row">
                            <div class="form-group"><label>年份</label><select v-model="form.year" required>
                                <option disabled value="">请选择</option>
                                <option v-for="opt in yearOptions" :key="opt.id" :value="opt.optionValue">
                                    {{ opt.optionValue }}
                                </option>
                            </select></div>
                            <div class="form-group"><label>是否特价</label>
                                <div class="toggle-row">
                                    <button :class="{ active: form.special }" class="toggle-btn" type="button"
                                            @click="form.special = !form.special">{{ form.special ? '是' : '否' }}
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group"><label>面料组合</label><select v-model="form.composition" required>
                                <option disabled value="">请选择</option>
                                <option v-for="opt in compositionOptions" :key="opt.id" :value="opt.optionValue">
                                    {{ opt.optionValue }}
                                </option>
                            </select></div>
                            <div class="form-group"><label>进货价格</label><input v-model.number="form.importPrice"
                                                                                  min="0" required step="0.01"
                                                                                  type="number"/></div>
                            <div class="form-group"><label>销售价格</label><input v-model.number="form.salePrice"
                                                                                  min="0" required step="0.01"
                                                                                  type="number"/></div>
                        </div>
                    </form>
                </div>

                <div class="card">
                    <div class="card-header">
                        <span class="card-title">规格选项（颜色 / 尺码）</span>
                        <span class="card-hint">选择后将自动生成 SKU</span>
                    </div>
                    <div v-if="optionLoading" class="loading-overlay" style="padding:32px">
                        <div class="loading-spinner"></div>
                    </div>
                    <div v-else class="options-area">
                        <div v-for="group in skuOptionGroups" :key="group.type" class="option-group">
                            <div class="option-label">{{ group.label }}</div>
                            <div class="option-tags">
                                <button v-for="opt in group.items" :key="opt.id"
                                        :class="{ 'tag-active': isSelected(group.type, opt.optionValue) }"
                                        class="tag-btn"
                                        type="button"
                                        @click="toggleOption(group.type, opt.optionValue)">{{ opt.optionValue }}
                                </button>
                                <div v-if="group.items.length === 0" class="no-options">暂无可用选项</div>
                            </div>
                        </div>
                    </div>
                    <div v-if="selectedCount > 0" class="option-summary">已选 <strong>{{ selectedCount }}</strong>
                        个选项，将生成 <strong>{{ skuPreview.length }}</strong> 个SKU
                    </div>
                </div>
            </div>

            <div class="add-side">
                <div class="card">
                    <div class="card-header"><span class="card-title">SKU 预览</span><span
                        class="card-hint">{{ skuPreview.length }} 项</span></div>
                    <div v-if="skuPreview.length === 0" class="empty-state" style="padding:24px">
                        <div class="empty-icon"><IconGraphic name="tag"/></div>
                        <div class="empty-text">选择颜色/尺码后预览</div>
                    </div>
                    <div v-else class="sku-list">
                        <div v-for="(sku, idx) in skuPreview" :key="idx" class="sku-item"><span class="sku-spec">{{
                                sku
                            }}</span></div>
                    </div>
                </div>
                <button :disabled="submitting" class="submit-btn" @click="handleSubmit">
                    {{ submitting ? '提交中…' : '提交商品' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import {SEASON_OPTIONS} from '../../../constants/season.js'
import {OPTION_TYPE} from '../../../constants/optionType.js'
import {useProductOptions} from './composables/useProductOptions.js'

const router = useRouter()
const toast = useToastStore()

const submitting = ref(false)
const seasons = SEASON_OPTIONS
const {
    loading: optionLoading,
    typeOptions,
    categoryOptions,
    unitOptions,
    compositionOptions,
    yearOptions,
    colorOptions,
    sizeOptions
} = useProductOptions(true)

const form = ref({
    code: '',
    name: '',
    season: '',
    type: '',
    category: '',
    unit: '',
    composition: '',
    year: '',
    importPrice: '',
    salePrice: '',
    special: false
})

// SKU 多选 tags（仅颜色+尺码）
const skuOptionGroups = computed(() => [
    {
        type: OPTION_TYPE.COLOR,
        label: '颜色',
        items: colorOptions.value
    },
    {
        type: OPTION_TYPE.SIZE,
        label: '尺码',
        items: sizeOptions.value
    }
])
const selectedOptions = ref({
    [OPTION_TYPE.COLOR]: [],
    [OPTION_TYPE.SIZE]: []
})

watch(skuOptionGroups, groups => {
    groups.forEach(group => {
        if (!selectedOptions.value[group.type]) {
            selectedOptions.value[group.type] = []
        }
    })
}, {immediate: true})

function isSelected(type, value) {
    return selectedOptions.value[type]?.includes(value)
}

function toggleOption(type, value) {
    if (!selectedOptions.value[type]) selectedOptions.value[type] = []
    const arr = selectedOptions.value[type]
    const idx = arr.indexOf(value)
    if (idx === -1) arr.push(value)
    else arr.splice(idx, 1)
    selectedOptions.value = {...selectedOptions.value}
}

const selectedCount = computed(() => Object.values(selectedOptions.value).reduce((sum, arr) => sum + arr.length, 0))

const selectedOptionsPayload = computed(() => Object.fromEntries(Object.entries(selectedOptions.value).filter(([, values]) => values.length > 0)))

const skuPreview = computed(() => {
    const groups = Object.entries(selectedOptions.value).filter(([, values]) => values.length > 0)
    if (groups.length === 0) return []

    function cartesian(arrays) {
        if (arrays.length === 0) return [[]];
        const [first, ...rest] = arrays;
        const restResult = cartesian(rest);
        return first.flatMap(v => restResult.map(r => [v, ...r]))
    }

    const values = groups.map(([, v]) => v)
    const keys = groups.map(([k]) => k)
    return cartesian(values).map(combo => keys.map((k, i) => `${k}:${combo[i]}`).join(' / '))
})

async function handleSubmit() {
    if (!form.value.code || !form.value.name || !form.value.season || !form.value.type || !form.value.category || !form.value.unit || !form.value.composition || !form.value.year || form.value.importPrice === '' || form.value.salePrice === '') {
        toast.warning('请填写完整的商品信息');
        return
    }
    if (selectedCount.value === 0) {
        toast.warning('请至少选择一个颜色或尺码');
        return
    }
    submitting.value = true
    try {
        await productInterface.create({
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
            selectedOptions: selectedOptionsPayload.value
        })
        // 后端已返回提示
        router.push('/manage/product')
    } catch {
    } finally {
        submitting.value = false
    }
}

function goBack() {
    router.push('/manage/product')
}
</script>

<style scoped>
.product-add {
    width: 100%;
    min-width: 0;
}

.page-top {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 28px;
}

.page-top .page-title {
    margin-bottom: 0;
    font-size: 26px;
    letter-spacing: -0.5px;
}

.add-layout {
    display: flex;
    gap: 22px;
    align-items: flex-start;
}

.add-main {
    width: calc(100% - 322px);
    display: flex;
    flex-direction: column;
    gap: 18px;
}

.add-side {
    width: 300px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    position: sticky;
    top: 24px;
}

.add-main .card, .add-side .card {
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.add-main .card {
    padding: 24px;
}

.add-form {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.card-hint {
    font-size: 12px;
    color: var(--text-muted);
    font-weight: 400;
}

.toggle-btn {
    width: 60px;
    height: 32px;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    border: 1px solid var(--border);
    background: #fff;
    color: var(--text-secondary);
    cursor: pointer;
}

.toggle-btn.active {
    background: var(--primary-gradient);
    color: #fff;
    border-color: transparent;
}

.options-area {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.option-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.option-label {
    font-size: 13px;
    font-weight: 600;
    color: var(--text-secondary);
}

.option-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.tag-btn {
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 13px;
    cursor: pointer;
}

.tag-btn:hover {
    border-color: var(--primary);
    color: var(--primary);
}

.tag-active {
    background: var(--primary-light);
    border-color: var(--primary);
    color: var(--primary);
    font-weight: 600;
}

.option-summary {
    margin-top: 14px;
    padding-top: 14px;
    border-top: 1px solid var(--border-light);
    font-size: 13px;
    color: var(--text-secondary);
}

.option-summary strong {
    color: var(--primary);
}

.sku-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    max-height: 360px;
    overflow-y: auto;
}

.sku-item {
    padding: 8px 12px;
    border: 1px solid var(--border-light);
    border-radius: 6px;
    font-size: 13px;
    color: var(--text-secondary);
    background: #fafafa;
}

.sku-spec {
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
    font-size: 12px;
}

.submit-btn {
    width: 100%;
    height: 44px;
    border-radius: 10px;
    background: var(--primary-gradient);
    color: #fff;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
}

.submit-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(13, 148, 136, 0.4);
}

.submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

@media (max-width: 900px) {
    .add-layout {
        flex-direction: column;
    }

    .add-main, .add-side {
        width: 100%;
    }

    .add-side {
        position: static;
    }
}
</style>
