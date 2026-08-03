<template>
    <div class="sku-detail">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回 SKU</button>
                <div>
                    <h2 class="page-title">SKU 详情</h2>
                    <p class="page-desc">查看 SKU 编码、规格和状态</p>
                </div>
            </div>
            <router-link v-if="sku" :to="`/manage/product/${productId}/sku/${sku.id}/edit`" class="btn-primary">编辑
                SKU
            </router-link>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
        </div>
        <div v-else-if="!sku" class="empty-state">
            <div class="empty-icon"><IconGraphic name="tag"/></div>
            <div class="empty-text">SKU 不存在</div>
        </div>
        <div v-else class="info-grid">
            <div class="info-card"><span class="info-label">SKU 编码</span><strong class="code-value">{{
                    sku.code
                }}</strong></div>
            <div class="info-card"><span class="info-label">SKU 名称</span><strong>{{ sku.name }}</strong></div>
            <div class="info-wide-card"><span class="info-label">规格</span><strong>{{ sku.spec }}</strong></div>
            <div class="info-card"><span class="info-label">状态</span><span
                :class="sku.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                class="status-badge">{{
                    statusLabels[sku.status] || sku.status
                }}</span></div>
            <div class="info-card"><span class="info-label">创建时间</span><strong>{{ sku.createTime || '-' }}</strong>
            </div>
            <div class="info-card"><span class="info-label">更新时间</span><strong>{{ sku.updateTime || '-' }}</strong>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import {STATUS, STATUS_LABELS} from '../../../constants/status.js'

const route = useRoute()
const router = useRouter()
const productId = route.params.productId
const statusLabels = STATUS_LABELS
const sku = ref(null)
const loading = ref(true)

onMounted(async () => {
    try {
        sku.value = await productSkuInterface.search(route.params.skuId)
    } catch {
        sku.value = null
    } finally {
        loading.value = false
    }
})

function goBack() {
    router.push(`/manage/product/${productId}/sku`)
}
</script>

<style scoped>
.sku-detail {
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

.info-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
}

.info-card, .info-wide-card {
    min-height: 112px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 8px;
    padding: 20px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border: 1px solid #e6f0ef;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(15, 118, 110, 0.06);
}

.info-card {
    width: calc(33.33% - 10px);
    min-width: 210px;
}

.info-wide-card {
    width: calc(66.66% - 4px);
    min-width: 420px;
}

.info-card:first-child, .info-wide-card {
    border-top: 3px solid var(--primary);
}

.info-label {
    color: var(--text-muted);
    font-size: var(--font-sm);
}

.code-value {
    color: var(--primary);
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

@media (max-width: 720px) {
    .info-card,
    .info-wide-card {
        width: 100%;
        min-width: 0;
    }

    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }
}
</style>
