<template>
    <div class="sku-form-page">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回 SKU</button>
                <div>
                    <h2 class="page-title">编辑 SKU</h2>
                    <p class="page-desc">修改 SKU 名称、规格和状态</p>
                </div>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else class="card form-card">
            <form class="edit-form" @submit.prevent="handleSubmit">
                <div class="form-group">
                    <label>SKU 编码</label>
                    <input :value="sku?.code" disabled type="text"/>
                </div>
                <div class="form-group">
                    <label>SKU 名称</label>
                    <input v-model="form.name" required type="text"/>
                </div>
                <div class="form-group">
                    <label>SKU 规格</label>
                    <textarea v-model="form.spec" required rows="4"></textarea>
                </div>
                <div class="form-group">
                    <label>状态</label>
                    <select v-model="form.status" required>
                        <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{
                                item.label
                            }}
                        </option>
                    </select>
                </div>
                <div class="form-actions">
                    <button class="btn-outline" type="button" @click="goBack">取消</button>
                    <button :disabled="submitting" class="btn-primary" type="submit">
                        {{ submitting ? '保存中...' : '保存修改' }}
                    </button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'
import {STATUS, STATUS_OPTIONS} from '../../constants/status.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const statusOptions = STATUS_OPTIONS
const sku = ref(null)
const loading = ref(true)
const submitting = ref(false)
const form = ref({name: '', spec: '', status: STATUS.ENABLE})

onMounted(async () => {
    try {
        sku.value = await productSkuInterface.search(route.params.skuId)
        form.value = {
            name: sku.value.name || '',
            spec: sku.value.spec || '',
            status: sku.value.status || STATUS.ENABLE
        }
    } catch {
        goBack()
    } finally {
        loading.value = false
    }
})

async function handleSubmit() {
    submitting.value = true
    try {
        await productSkuInterface.update(route.params.skuId, {
            name: form.value.name,
            spec: form.value.spec,
            status: form.value.status
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
    router.push(`/manage/product/${route.params.productId}/sku`)
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

.edit-form input, .edit-form select, .edit-form textarea {
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
