<template>
    <div class="standalone-form-page">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    @click="goBack"
                >
                    ← 返回选项
                </button>
                <div>
                    <h2 class="page-title">添加选项值</h2>
                    <p class="page-desc">创建颜色、尺码、类型等商品选项</p>
                </div>
            </div>
        </div>
        <div class="standalone-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-group">
                    <label>选项类型</label>
                    <div class="type-options">
                        <button
                            v-for="item in typeOptions"
                            :key="item.value"
                            :class="form.optionType === item.value ? 'type-option-active' : 'type-option'"
                            type="button"
                            @click="form.optionType = item.value"
                        >
                            {{ item.label }}
                        </button>
                    </div>
                </div>
                <div class="form-group">
                    <label>选项值</label>
                    <input
                        v-model="form.optionValue"
                        maxlength="8"
                        placeholder="例如：红色、XL、棉"
                        required
                    />
                </div>
                <div class="form-actions">
                    <button
                        class="btn-outline"
                        type="button"
                        @click="goBack"
                    >
                        取消
                    </button>
                    <button
                        :disabled="submitting"
                        class="btn-primary"
                        type="submit"
                    >
                        {{ submitting ? '保存中...' : '保存选项值' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import optionValueInterface from '../../../axios/interface/OptionValueInterface.js'
import {OPTION_TYPE_OPTIONS} from '../../../constants/optionType.js'
import usePageDraft from '../../../composables/usePageDraft.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const typeOptions = OPTION_TYPE_OPTIONS
const initialType = typeOptions.some(item => item.value === route.query.type)
    ? route.query.type
    : ''
const submitting = ref(false)
const form = ref({optionType: initialType, optionValue: ''})
const optionDraft = usePageDraft(
    'clothing_manage_option_add',
    form,
    saved => {
        form.value = {...form.value, ...saved}
    },
    {
        saved: () => submitting.value
    }
)

async function handleSubmit() {
    if (!form.value.optionType) {
        toast.warning('请选择选项类型')
        return
    }
    submitting.value = true
    try {
        await optionValueInterface.create({
            optionType: form.value.optionType,
            optionValue: form.value.optionValue.trim()
        })
        optionDraft.clear()
        goBack()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

function goBack() {
    router.push({
        path: '/manage/option',
        query: form.value.optionType
            ? {type: form.value.optionType}
            : {}
    })
}
</script>

<style scoped>
.standalone-form-page {
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
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.standalone-form-card {
    width: 86%;
    max-width: 920px;
    margin: 0 auto;
    padding: 28px;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    background: #fff;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
}

.type-options {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    padding: 14px;
    border: 1px solid #e3efed;
    border-radius: 10px;
    background: #fbfdfd;
}

.type-option,
.type-option-active {
    min-width: 86px;
    padding: 9px 14px;
    border-radius: 9px;
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
    transition: border-color 0.2s, background 0.2s, color 0.2s;
}

.type-option {
    border: 1px solid #dceae7;
    background: #fff;
    color: #475569;
}

.type-option:hover {
    border-color: #5eead4;
    background: #f0fdfa;
    color: #0f766e;
}

.type-option-active {
    border: 1px solid #0d9488;
    background: #ccfbf1;
    color: #0f766e;
    box-shadow: 0 3px 8px rgba(13, 148, 136, 0.14);
}

.form-group input {
    border-radius: 10px;
    background: #fbfdfd;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 20px;
    border-top: 1px solid var(--border-light);
}

@media (max-width: 640px) {
    .type-option,
    .type-option-active {
        min-width: 74px;
        padding: 8px 10px;
    }

    .form-actions {
        flex-direction: column-reverse;
    }
}
</style>
