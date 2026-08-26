<template>
    <div class="sku-detail">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">SKU 详情</h2>
                <p class="page-label-desc">查看 SKU 编码、规格和状态</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            @click="goBack"
                        >← 返回 SKU</button>
                        <router-link
                            v-if="sku"
                            class="btn-primary search-button"
                            :to="`/manage/product/${productId}/sku/${sku.id}/edit`"
                        >编辑 SKU</router-link>
                    </div>
                </div>
            </div>
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

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
    letter-spacing: -0.5px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.info-card, .info-wide-card {
    min-height: 112px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 8px;
    padding: 20px;
    background: linear-gradient(145deg, var(--bg-card), var(--bg-subtle));
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow);
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

@media (max-width: 720px) {
    .info-card,
    .info-wide-card {
        width: 100%;
        min-width: 0;
    }
}</style>
