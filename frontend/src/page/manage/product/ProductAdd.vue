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
                    <form
                        class="add-form"
                        @submit.prevent="handleSubmit"
                    >
                        <div class="basic-info-layout">
                            <div class="basic-info-column">
                                <div class="info-section-title">关键信息</div>
                                <div class="form-group">
                                    <label>商品编码</label>
                                    <input
                                        v-model="form.code"
                                        maxlength="20"
                                        required
                                        type="text"
                                    />
                                </div>
                                <div class="form-group">
                                    <label>商品名称</label>
                                    <input
                                        v-model="form.name"
                                        maxlength="20"
                                        required
                                        type="text"
                                    />
                                </div>
                                <div class="season-special-group">
                                    <div class="season-section">
                                        <label>季节</label>
                                        <div class="season-options">
                                            <button
                                                v-for="season in seasons"
                                                :key="season.value"
                                                :aria-pressed="form.season === season.value"
                                                :class="seasonClass(season)"
                                                type="button"
                                                @click="form.season = season.value"
                                            >
                                                {{ season.label }}
                                            </button>
                                        </div>
                                    </div>
                                    <div class="special-section">
                                        <label>是否特价</label>
                                        <button
                                            :class="form.special ? 'special-active' : 'special-inactive'"
                                            class="toggle-btn"
                                            type="button"
                                            @click="form.special = !form.special"
                                        >
                                            {{ form.special ? '是' : '否' }}
                                        </button>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label>进货价格</label>
                                    <input
                                        v-model.number="form.importPrice"
                                        min="0"
                                        required
                                        step="0.01"
                                        type="number"
                                    />
                                </div>
                                <div class="form-group">
                                    <label>销售价格</label>
                                    <input
                                        v-model.number="form.salePrice"
                                        min="0"
                                        required
                                        step="0.01"
                                        type="number"
                                    />
                                </div>
                            </div>
                            <div class="basic-info-column">
                                <div class="info-section-title">选项信息</div>
                                <div class="form-group">
                                    <label>类型</label>
                                    <OptionValuePicker
                                        ref="typePicker"
                                        v-model="form.type"
                                        :disabled="optionLoading"
                                        :options="typeOptions"
                                        error-message="该类型不在选项管理中，请选择有效类型"
                                        placeholder="输入类型关键词"
                                        select-placeholder="选择类型"
                                    />
                                </div>
                                <div class="form-group">
                                    <label>种类</label>
                                    <OptionValuePicker
                                        ref="categoryPicker"
                                        v-model="form.category"
                                        :disabled="optionLoading"
                                        :options="categoryOptions"
                                        error-message="该种类不在选项管理中，请选择有效种类"
                                        placeholder="输入种类关键词"
                                        select-placeholder="选择种类"
                                    />
                                </div>
                                <div class="form-group">
                                    <label>单位</label>
                                    <OptionValuePicker
                                        ref="unitPicker"
                                        v-model="form.unit"
                                        :disabled="optionLoading"
                                        :options="unitOptions"
                                        error-message="该单位不在选项管理中，请选择有效单位"
                                        placeholder="输入单位关键词"
                                        select-placeholder="选择单位"
                                    />
                                </div>
                                <div class="form-group">
                                    <label>年份</label>
                                    <OptionValuePicker
                                        ref="yearPicker"
                                        v-model="form.year"
                                        :disabled="optionLoading"
                                        :options="yearOptions"
                                        error-message="该年份不在选项管理中，请选择有效年份"
                                        placeholder="输入年份关键词"
                                        select-placeholder="选择年份"
                                    />
                                </div>
                                <div class="form-group">
                                    <label>面料组合</label>
                                    <OptionValuePicker
                                        ref="compositionPicker"
                                        v-model="form.composition"
                                        :disabled="optionLoading"
                                        :options="compositionOptions"
                                        error-message="该面料组合不在选项管理中，请选择有效面料组合"
                                        placeholder="输入面料组合关键词"
                                        select-placeholder="选择面料组合"
                                    />
                                </div>
                            </div>
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
import usePageDraft from '../../../composables/usePageDraft.js'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'

const router = useRouter()
const toast = useToastStore()

const submitting = ref(false)
const typePicker = ref(null)
const categoryPicker = ref(null)
const unitPicker = ref(null)
const compositionPicker = ref(null)
const yearPicker = ref(null)
const seasons = SEASON_OPTIONS
const seasonClassMap = {
    SPRING: 'season-spring',
    SUMMER: 'season-summer',
    AUTUMN: 'season-autumn',
    WINTER: 'season-winter',
    ALL_SEASONS: 'season-all'
}
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

function seasonClass(season) {
    return {
        'season-btn': true,
        [seasonClassMap[season.value]]: true,
        'season-active': form.value.season === season.value
    }
}

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

const productDraft = usePageDraft(
    'clothing_manage_product_add',
    () => ({form: form.value, selectedOptions: selectedOptions.value}),
    saved => {
        form.value = {...form.value, ...(saved.form || {})}
        selectedOptions.value = saved.selectedOptions || selectedOptions.value
        toast.info('已恢复上次未完成的商品信息')
    }
)

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
    if (!form.value.code || !form.value.name || !form.value.season || form.value.importPrice === '' || form.value.salePrice === '') {
        toast.warning('请填写完整的商品信息')
        return
    }
    const optionValidation = [
        typePicker,
        categoryPicker,
        unitPicker,
        compositionPicker,
        yearPicker
    ].map(picker => picker.value?.validate())
    if (optionValidation.some(valid => !valid)) {
        toast.warning('商品选项必须使用选项管理中的已有值')
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
        productDraft.clear()
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
    width: 100%;
}

.basic-info-layout {
    width: 100%;
    display: flex;
    align-items: flex-start;
    gap: 28px;
}

.basic-info-column {
    width: calc(50% - 14px);
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.basic-info-column + .basic-info-column {
    padding-left: 28px;
    border-left: 1px solid #e3efed;
}

.basic-info-column .form-group {
    width: 100%;
    margin-bottom: 0;
}

.info-section-title {
    padding-bottom: 10px;
    border-bottom: 1px solid #eaf2f0;
    color: #315d59;
    font-size: 14px;
    font-weight: 700;
}

.season-special-group {
    display: flex;
    align-items: flex-end;
    gap: 18px;
}

.season-section {
    width: calc(100% - 78px);
}

.special-section {
    width: 60px;
}

.season-section label,
.special-section label {
    display: block;
    margin-bottom: 7px;
    color: #496865;
    font-size: 13px;
    font-weight: 700;
}

.season-options {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.season-btn {
    min-width: 64px;
    height: 36px;
    padding: 0 15px;
    border: 1px solid #d5e3e0;
    border-radius: 8px;
    background: #fff;
    color: #64807e;
    font-size: 13px;
    font-weight: 600;
}

.season-btn:hover {
    border-color: #9ebbb6;
    background: #f7fbfa;
    color: #315d59;
}

.season-spring.season-active {
    border-color: #69b77b;
    background: #e4f5e6;
    color: #28733b;
    box-shadow: 0 0 0 3px rgba(105, 183, 123, 0.18), 0 5px 12px rgba(40, 115, 59, 0.12);
}

.season-summer.season-active {
    border-color: #eea43e;
    background: #ffedc2;
    color: #9a4908;
    box-shadow: 0 0 0 3px rgba(238, 164, 62, 0.18), 0 5px 12px rgba(154, 73, 8, 0.12);
}

.season-autumn.season-active {
    border-color: #c99420;
    background: #ffefaa;
    color: #765006;
    box-shadow: 0 0 0 3px rgba(201, 148, 32, 0.18), 0 5px 12px rgba(118, 80, 6, 0.12);
}

.season-winter.season-active {
    border-color: #83b7db;
    background: #edf8ff;
    color: #31698f;
    box-shadow: 0 0 0 3px rgba(131, 183, 219, 0.2), 0 5px 12px rgba(49, 105, 143, 0.12);
}

.season-all.season-active {
    border-color: #7eb9af;
    background: #e8f5f2;
    color: #315d59;
    box-shadow: 0 0 0 3px rgba(126, 185, 175, 0.18), 0 5px 12px rgba(49, 93, 89, 0.12);
}

.season-active {
    transform: translateY(-1px);
}

.card-hint {
    font-size: 12px;
    color: var(--text-muted);
    font-weight: 400;
}

.toggle-btn {
    width: 60px;
    height: 36px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
}

.toggle-btn.special-inactive {
    border: 1px solid #ef9a9a;
    background: #fff1f1;
    color: #b42323;
    box-shadow: 0 3px 8px rgba(180, 35, 35, 0.08);
}

.toggle-btn.special-inactive:hover {
    border-color: #e56b6b;
    background: #ffe5e5;
}

.toggle-btn.special-active {
    border: 1px solid #69b77b;
    background: #e4f5e6;
    color: #28733b;
    box-shadow: 0 3px 8px rgba(40, 115, 59, 0.1);
}

.toggle-btn.special-active:hover {
    border-color: #48a460;
    background: #d8f0dc;
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

    .add-main,
    .add-side {
        width: 100%;
    }

    .add-side {
        position: static;
    }
}

@media (max-width: 760px) {
    .basic-info-layout {
        flex-direction: column;
        gap: 22px;
    }

    .basic-info-column {
        width: 100%;
    }

    .basic-info-column + .basic-info-column {
        padding-top: 22px;
        padding-left: 0;
        border-top: 1px solid #e3efed;
        border-left: none;
    }
}
@media (max-width: 560px) {
    .season-special-group {
        align-items: stretch;
        flex-direction: column;
        gap: 14px;
    }

    .season-section,
    .special-section {
        width: 100%;
    }
}
</style>
