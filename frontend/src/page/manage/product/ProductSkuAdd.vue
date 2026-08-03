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
                    <textarea v-model="form.spec" placeholder="例如：颜色:黑色 / 尺码:M" required rows="4"></textarea>
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
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const product = ref(null)
const submitting = ref(false)
const form = ref({name: '', spec: ''})

onMounted(async () => {
    try {
        product.value = await productInterface.search(route.params.id)
    } catch {
        goBack()
    }
})

async function handleSubmit() {
    submitting.value = true
    try {
        await productSkuInterface.create({
            productId: Number(route.params.id),
            name: form.value.name,
            spec: form.value.spec
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
    padding: 28px;
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.edit-form {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.edit-form input, .edit-form textarea {
    border-radius: 10px;
    background: #fbfdfd;
}

.edit-form textarea {
    min-height: 120px;
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
    .form-actions {
        flex-direction: column-reverse;
    }
}
</style>
