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
                            <div class="form-group">
                                <label>商品编码</label>
                                <input v-model="form.code" type="text" placeholder="例如: TSH001" maxlength="20" required />
                            </div>
                            <div class="form-group">
                                <label>商品名称</label>
                                <input v-model="form.name" type="text" placeholder="例如: 纯棉圆领T恤" maxlength="20" required />
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>季节</label>
                                <select v-model="form.season" required>
                                    <option value="" disabled>请选择季节</option>
                                    <option v-for="s in seasons" :key="s.value" :value="s.value">{{ s.label }}</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>是否特价</label>
                                <div class="toggle-row">
                                    <button type="button" class="toggle-btn" :class="{ active: form.special }" @click="form.special = !form.special">
                                        {{ form.special ? '是' : '否' }}
                                    </button>
                                </div>
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>进货价格</label>
                                <input v-model.number="form.importPrice" type="number" min="0" step="0.01" placeholder="0.00" required />
                            </div>
                            <div class="form-group">
                                <label>销售价格</label>
                                <input v-model.number="form.salePrice" type="number" min="0" step="0.01" placeholder="0.00" required />
                            </div>
                        </div>
                    </form>
                </div>

                <div class="card">
                    <div class="card-header">
                        <span class="card-title">规格选项</span>
                        <span class="card-hint">选择商品的可选项，将自动生成SKU</span>
                    </div>

                    <div v-if="optionLoading" class="loading-overlay" style="padding:32px">
                        <div class="loading-spinner"></div>
                    </div>

                    <div v-else class="options-area">
                        <div v-for="group in optionGroups" :key="group.type" class="option-group">
                            <div class="option-label">{{ group.label }}</div>
                            <div class="option-tags">
                                <button
                                    v-for="opt in group.items"
                                    :key="opt.id"
                                    type="button"
                                    class="tag-btn"
                                    :class="{ 'tag-active': isSelected(group.type, opt.optionValue) }"
                                    @click="toggleOption(group.type, opt.optionValue)"
                                >
                                    {{ opt.optionValue }}
                                </button>
                                <div v-if="group.items.length === 0" class="no-options">暂无可用选项</div>
                            </div>
                        </div>
                    </div>

                    <div v-if="selectedCount > 0" class="option-summary">
                        已选 <strong>{{ selectedCount }}</strong> 个选项，将生成 <strong>{{ skuPreview.length }}</strong> 个SKU
                    </div>
                </div>
            </div>

            <div class="add-side">
                <div class="card">
                    <div class="card-header">
                        <span class="card-title">SKU 预览</span>
                        <span class="card-hint">{{ skuPreview.length }} 项</span>
                    </div>
                    <div v-if="skuPreview.length === 0" class="empty-state" style="padding:24px">
                        <div class="empty-icon">🏷️</div>
                        <div class="empty-text">选择规格后预览</div>
                    </div>
                    <div v-else class="sku-list">
                        <div v-for="(sku, idx) in skuPreview" :key="idx" class="sku-item">
                            <span class="sku-spec">{{ sku }}</span>
                        </div>
                    </div>
                </div>

                <button class="submit-btn" :disabled="submitting" @click="handleSubmit">
                    {{ submitting ? '提交中…' : '提交商品' }}
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToastStore } from '../../stores/toastStore.js'
import productInterface from '../../axios/interface/ProductInterface.js'
import optionValueInterface from '../../axios/interface/OptionValueInterface.js'
import { SEASON_OPTIONS } from '../../constants/season.js'
import { OPTION_TYPE, OPTION_TYPE_OPTIONS } from '../../constants/optionType.js'

const router = useRouter()
const toast = useToastStore()

const submitting = ref(false)
const optionLoading = ref(true)

const seasons = SEASON_OPTIONS

const form = ref({
    code: '',
    name: '',
    season: '',
    importPrice: '',
    salePrice: '',
    special: false
})

const selectedOptions = ref({})
const optionGroups = ref([])

onMounted(async () => {
    await loadOptions()
})

async function loadOptions() {
    optionLoading.value = true
    try {
        const types = OPTION_TYPE_OPTIONS.filter(item => item.value !== OPTION_TYPE.UNIT)
        const results = await Promise.all(
            types.map(t => optionValueInterface.searchListByType(t.value).catch(() => []))
        )
        optionGroups.value = types.map((t, i) => ({
            type: t.value,
            label: t.label,
            items: Array.isArray(results[i]) ? results[i] : []
        }))
        // 初始化选中状态
        optionGroups.value.forEach(g => {
            if (!selectedOptions.value[g.type]) {
                selectedOptions.value[g.type] = []
            }
        })
    } catch {
        optionGroups.value = []
    } finally {
        optionLoading.value = false
    }
}

function isSelected(type, value) {
    return selectedOptions.value[type]?.includes(value)
}

function toggleOption(type, value) {
    if (!selectedOptions.value[type]) {
        selectedOptions.value[type] = []
    }
    const arr = selectedOptions.value[type]
    const idx = arr.indexOf(value)
    if (idx === -1) {
        arr.push(value)
    } else {
        arr.splice(idx, 1)
    }
    // 触发响应式
    selectedOptions.value = { ...selectedOptions.value }
}

const selectedCount = computed(() => {
    return Object.values(selectedOptions.value).reduce((sum, arr) => sum + arr.length, 0)
})

const selectedOptionsPayload = computed(() => {
    return Object.fromEntries(
        Object.entries(selectedOptions.value).filter(([, values]) => values.length > 0)
    )
})

const skuPreview = computed(() => {
    const groups = Object.entries(selectedOptions.value).filter(([, values]) => values.length > 0)
    if (groups.length === 0) return []

    function cartesian(arrays) {
        if (arrays.length === 0) return [[]]
        const [first, ...rest] = arrays
        const restResult = cartesian(rest)
        return first.flatMap(v => restResult.map(r => [v, ...r]))
    }

    const values = groups.map(([, v]) => v)
    const combinations = cartesian(values)
    const keys = groups.map(([k]) => k)

    return combinations.map(combo =>
        keys.map((k, i) => `${k}:${combo[i]}`).join(' / ')
    )
})

async function handleSubmit() {
    if (
        !form.value.code ||
        !form.value.name ||
        !form.value.season ||
        form.value.importPrice === '' ||
        form.value.salePrice === ''
    ) {
        toast.warning('请填写完整的商品信息')
        return
    }
    if (selectedCount.value === 0) {
        toast.warning('请至少选择一个规格选项')
        return
    }

    submitting.value = true
    try {
        await productInterface.create({
            code: form.value.code,
            name: form.value.name,
            season: form.value.season,
            importPrice: form.value.importPrice,
            salePrice: form.value.salePrice,
            special: form.value.special,
            selectedOptions: selectedOptionsPayload.value
        })
        toast.success('商品添加成功')
        router.push('/manage/product')
    } catch {
        // 拦截器处理
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
    max-width: 1000px;
}

.page-top {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 24px;
}

.page-top .page-title {
    margin-bottom: 0;
}

.add-layout {
    display: flex;
    gap: 20px;
    align-items: flex-start;
}

.add-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.add-side {
    width: 300px;
    display: flex;
    flex-direction: column;
    gap: 16px;
    position: sticky;
    top: 24px;
}

/* ── 表单 ── */
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

/* ── 开关按钮 ── */
.toggle-row {
    padding-top: 4px;
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
    transition: var(--transition);
}

.toggle-btn.active {
    background: var(--primary-gradient);
    color: #fff;
    border-color: transparent;
}

/* ── 规格选项 ── */
.options-area {
    display: flex;
    flex-direction: column;
    gap: 20px;
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
    border: 1px solid var(--border);
    border-radius: 20px;
    background: #fff;
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    transition: var(--transition);
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

.no-options {
    font-size: 13px;
    color: var(--text-muted);
    padding: 4px 0;
}

.option-summary {
    margin-top: 16px;
    padding-top: 14px;
    border-top: 1px solid var(--border-light);
    font-size: 13px;
    color: var(--text-secondary);
}

.option-summary strong {
    color: var(--primary);
}

/* ── SKU 预览 ── */
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

/* ── 提交 ── */
.submit-btn {
    width: 100%;
    height: 44px;
    border: none;
    border-radius: 10px;
    background: var(--primary-gradient);
    color: #fff;
    font-size: 15px;
    font-weight: 700;
    cursor: pointer;
    box-shadow: 0 4px 12px rgba(13, 148, 136, 0.3);
    transition: var(--transition);
}

.submit-btn:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(13, 148, 136, 0.4);
}

.submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}
</style>
