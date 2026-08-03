<template>
    <div class="sku-manage">
        <div class="page-heading">
            <div class="heading-left">
                <button class="btn-outline" @click="goBack">← 返回商品</button>
                <div>
                    <h2 class="page-title">SKU 管理</h2>
                    <p v-if="product" class="page-desc">{{ product.name }} · {{ product.code }}</p>
                </div>
            </div>
            <router-link v-if="product" :to="`/manage/product/${product.id}/sku/add`" class="btn-primary">+ 添加 SKU
            </router-link>
        </div>

        <div v-if="product" class="sku-product-strip">
            <span>销售价：<strong>¥{{ product.salePrice }}</strong></span>
            <span>季节：{{ seasonLabels[product.season] || product.season }}</span>
            <span>SKU 数量：{{ skuList.length }}</span>
        </div>

        <div class="sku-table-card">
            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="skuList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="tag"/></div>
                <div class="empty-text">暂无 SKU，先添加一个吧</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>SKU 编码</th>
                    <th>SKU 名称</th>
                    <th>规格</th>
                    <th>状态</th>
                    <th>更新时间</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="sku in skuList" :key="sku.id">
                    <td><code>{{ sku.code }}</code></td>
                    <td><strong>{{ sku.name }}</strong></td>
                    <td>{{ sku.spec }}</td>
                    <td>
                            <span :class="sku.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                                  class="status-badge">
                                {{ statusLabels[sku.status] || sku.status }}
                            </span>
                    </td>
                    <td>{{ sku.updateTime || '-' }}</td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/product/${productId}/sku/${sku.id}`" class="btn-outline btn-sm">
                                详情
                            </router-link>
                            <router-link :to="`/manage/product/${productId}/sku/${sku.id}/edit`"
                                         class="btn-outline btn-sm">编辑
                            </router-link>
                            <button class="btn-danger btn-sm" @click="handleDelete(sku)">删除</button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../../axios/interface/ProductSkuInterface.js'
import {SEASON_LABELS} from '../../../constants/season.js'
import {STATUS, STATUS_LABELS} from '../../../constants/status.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const productId = route.params.id
const seasonLabels = SEASON_LABELS
const statusLabels = STATUS_LABELS
const product = ref(null)
const skuList = ref([])
const loading = ref(true)

onMounted(async () => {
    try {
        const [productData, skuData] = await Promise.all([
            productInterface.search(productId),
            productSkuInterface.searchListByProductId(productId)
        ])
        product.value = productData
        skuList.value = skuData
    } catch {
        skuList.value = []
    } finally {
        loading.value = false
    }
})

async function handleDelete(sku) {
    if (!window.confirm(`确定要删除 SKU“${sku.name}”吗？`)) return
    try {
        await productSkuInterface.softDelete(sku.id)
        // 后端已返回提示
        skuList.value = await productSkuInterface.searchListByProductId(productId)
    } catch {
        // 拦截器已处理
    }
}

function goBack() {
    router.push(`/manage/product/${productId}`)
}
</script>

<style scoped>
.sku-manage {
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

.sku-product-strip {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
    margin-bottom: 16px;
    padding: 16px 20px;
    background: linear-gradient(135deg, #0d9488, #14b8a6);
    border-radius: 14px;
    color: rgba(255, 255, 255, 0.82);
    box-shadow: 0 10px 24px rgba(13, 148, 136, 0.18);
}

.sku-product-strip span {
    padding-right: 20px;
    border-right: 1px solid rgba(255, 255, 255, 0.22);
}

.sku-product-strip span:last-child {
    border-right: none;
}

.sku-product-strip strong {
    color: #fff;
    font-size: 18px;
}

.sku-table-card {
    padding: 8px 20px 20px;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.sku-table-card .data-table td {
    height: 62px;
}

.sku-table-card tbody tr:hover td {
    background: #f2fbfa;
}

@media (max-width: 900px) {
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }

    .sku-table-card {
        overflow-x: auto;
    }

    .sku-table-card .data-table {
        min-width: 900px;
    }
}
</style>
