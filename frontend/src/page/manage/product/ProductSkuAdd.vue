<template>
    <div class="sku-form-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回 SKU</button>
                <div>
                    <h2 class="page-title">添加 SKU</h2>
                    <p v-if="product" class="page-desc">为「{{ product.name }}」添加 SKU</p>
                </div>
            </div>
        </div>

        <div class="card form-card">
            <form class="edit-form" @submit.prevent="handleSubmit">
                <div class="form-group">
                    <label>SKU 名称</label>
                    <input v-model="form.name" placeholder="例如：黑色圆领 T 恤" required type="text"/>
                </div>
                <div class="form-group">
                    <label>SKU 规格</label>
                    <div class="spec-options">
                        <div class="spec-option-group">
                            <span class="spec-option-label">颜色</span>
                            <div class="option-tags">
                                <button
                                    v-for="option in colorOptions"
                                    :key="option.id"
                                    :class="form.color === option.optionValue ? 'option-tag-active' : 'option-tag'"
                                    type="button"
                                    @click="toggleSpec('color', option.optionValue)"
                                >
                                    {{ option.optionValue }}
                                </button>
                                <span
                                    v-if="!optionLoading && colorOptions.length === 0"
                                    class="option-empty"
                                >
                                    暂无颜色选项
                                </span>
                            </div>
                        </div>
                        <div class="spec-option-group">
                            <span class="spec-option-label">尺码</span>
                            <div class="option-tags">
                                <button
                                    v-for="option in sizeOptions"
                                    :key="option.id"
                                    :class="form.size === option.optionValue ? 'option-tag-active' : 'option-tag'"
                                    type="button"
                                    @click="toggleSpec('size', option.optionValue)"
                                >
                                    {{ option.optionValue }}
                                </button>
                                <span
                                    v-if="!optionLoading && sizeOptions.length === 0"
                                    class="option-empty"
                                >
                                    暂无尺码选项
                                </span>
                            </div>
                        </div>
                    </div>
                    <div
                        v-if="hasSelectedSpec"
                        class="spec-preview"
                    >
                        规格预览：{{ specPreview }}
                    </div>
                    <div
                        v-else-if="!optionLoading"
                        class="spec-hint"
                    >
                        请至少选择一个颜色或尺码
                    </div>
                </div>
                <div class="form-actions">
                    <button class="btn-outline" type="button" @click="goBack">取消</button>
                    <button :disabled="submitting" class="btn-primary" type="submit">
                        {{ submitting ? '保存中...' : '保存 SKU' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import {useProductOptions} from './composables/useProductOptions.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const product = ref(null)
const submitting = ref(false)
const form = ref({name: '', color: '', size: ''})
const {
    loading: optionLoading,
    colorOptions,
    sizeOptions
} = useProductOptions(true)

const selectedSpec = computed(() => {
    const spec = {}
    if (form.value.color) spec.COLOR = form.value.color
    if (form.value.size) spec.SIZE = form.value.size
    return spec
})

const hasSelectedSpec = computed(() => {
    return Object.keys(selectedSpec.value).length > 0
})

const specPreview = computed(() => {
    return JSON.stringify(selectedSpec.value)
})

function toggleSpec(type, value) {
    form.value[type] = form.value[type] === value ? '' : value
}

onMounted(async () => {
    try {
        product.value = await productInterface.search(route.params.id)
    } catch {
        goBack()
    }
})

async function handleSubmit() {
    if (Object.keys(selectedSpec.value).length === 0) {
        toast.warning('请至少选择一个颜色或尺码')
        return
    }
    submitting.value = true
    try {
        await productSkuInterface.create({
            productId: Number(route.params.id),
            name: form.value.name,
            spec: specPreview.value
        })
        // 后端已返回提示
        goBack()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

function goBack() {
    router.push(`/manage/product/${route.params.id}/sku`)
}
</script>

<style scoped>
.sku-form-page {
    width: 100%;
    min-width: 0;
}

.page-heading {
    margin-bottom: 28px;
}

.heading-left {
    display: flex;
    align-items: center;
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

.form-card {
    max-width: 780px;
    margin: 0 auto;
    padding: 28px;
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.edit-form {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.edit-form input {
    border-radius: 10px;
    background: #fbfdfd;
}

.spec-options {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.spec-option-group {
    padding: 14px;
    border: 1px solid #e3efed;
    border-radius: 10px;
    background: #fbfdfd;
}

.spec-option-label {
    display: block;
    margin-bottom: 10px;
    color: #475569;
    font-size: 13px;
    font-weight: 700;
}

.option-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.option-tag,
.option-tag-active {
    min-width: 64px;
    padding: 8px 14px;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
    transition: border-color 0.2s, background 0.2s, color 0.2s;
}

.option-tag {
    border: 1px solid #dceae7;
    background: #fff;
    color: #475569;
}

.option-tag:hover {
    border-color: #5eead4;
    background: #f0fdfa;
    color: #0f766e;
}

.option-tag-active {
    border: 1px solid #0d9488;
    background: #ccfbf1;
    color: #0f766e;
    box-shadow: 0 3px 8px rgba(13, 148, 136, 0.14);
}

.option-empty {
    color: #94a3b8;
    font-size: 13px;
}

.spec-preview,
.spec-hint {
    margin-top: 10px;
    padding: 10px 12px;
    border-radius: 8px;
    font-size: 13px;
}

.spec-preview {
    background: #ecfdf5;
    color: #0f766e;
    font-weight: 700;
}

.spec-hint {
    background: #f8fafc;
    color: #64748b;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 16px;
    padding-top: 20px;
    border-top: 1px solid var(--border-light);
}

@media (max-width: 640px) {
    .option-tag,
    .option-tag-active {
        min-width: 56px;
        padding: 8px 10px;
    }

    .form-actions {
        flex-direction: column-reverse;
    }
}
</style>
