<template>
    <div class="product-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">商品管理</h2>
                <p class="page-desc">管理商品基础信息、价格和 SKU</p>
            </div>
            <router-link class="btn-primary" to="/manage/product/add">+ 添加商品</router-link>
        </div>

        <div class="card">
            <div class="card-header">
                <span class="card-title">商品列表</span>
                <div class="header-actions">
                    <div class="search-bar">
                        <input
                            v-model="searchQuery"
                            placeholder="搜索商品名称或编码"
                            type="text"
                        />
                    </div>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>
            <div v-else-if="filteredList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="product"/></div>
                <div class="empty-text">暂无商品数据</div>
            </div>
            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>编码</th>
                    <th>商品名称</th>
                    <th>类型</th>
                    <th>种类</th>
                    <th>季节</th>
                    <th>年份</th>
                    <th>销售价</th>
                    <th>是否特价</th>
                    <th>状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in filteredList" :key="item.id" style="cursor:pointer" @dblclick="goDetail(item)">
                    <td><code>{{ item.code }}</code></td>
                    <td><strong>{{ item.name }}</strong></td>
                    <td>{{ item.type || '-' }}</td>
                    <td>{{ item.category || '-' }}</td>
                    <td>{{ seasonLabels[item.season] || item.season || '-' }}</td>
                    <td>{{ item.year || '-' }}</td>
                    <td>¥{{ item.salePrice ?? '-' }}</td>
                    <td>
                        <span :class="item.special ? 'special-status' : 'regular-status'">
                            {{ item.special ? '是' : '否' }}
                        </span>
                    </td>
                    <td>
                            <span :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                                  class="status-badge">
                                {{ statusLabels[item.status] || item.status || '-' }}
                            </span>
                    </td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/product/${item.id}`" class="btn-outline btn-sm">详情/编辑
                            </router-link>
                            <router-link :to="`/manage/product/${item.id}/sku`" class="btn-outline btn-sm">SKU
                            </router-link>
                            <button class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>

            <TablePagination
                :loading="loading"
                :page="pageInfo.page"
                :total-elements="pageInfo.totalElements"
                :total-pages="totalPages"
                @change="changePage"
            />
        </div>

        <DeleteConfirmDialog
            :loading="deleting"
            :title="`确定要删除“${deleteTarget?.name || ''}”吗？`"
            :visible="showDelete"
            hint="商品会被禁用，已有数据不会被物理删除"
            loading-text="处理中..."
            @cancel="showDelete = false"
            @confirm="handleDelete"
        />
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import DeleteConfirmDialog from '../../../component/DeleteConfirmDialog.vue'
import TablePagination from '../../../component/common/TablePagination.vue'
import {SEASON_LABELS} from '../../../constants/season.js'
import {STATUS, STATUS_LABELS} from '../../../constants/status.js'

const router = useRouter()
const toast = useToastStore()
const seasonLabels = SEASON_LABELS
const statusLabels = STATUS_LABELS

const productList = ref([])
const searchQuery = ref('')
const loading = ref(true)
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

const pageInfo = ref({
    totalElements: 0,
    totalPages: 0,
    page: 0,
    size: 10
})

const totalPages = computed(() => Math.max(pageInfo.value.totalPages || 1, 1))

const filteredList = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    if (!query) return productList.value
    return productList.value.filter(item =>
        item.name?.toLowerCase().includes(query) ||
        item.code?.toLowerCase().includes(query) ||
        String(item.id).includes(query)
    )
})

onMounted(fetchList)

async function fetchList() {
    loading.value = true
    try {
        const data = await productInterface.searchPage(pageInfo.value.page, pageInfo.value.size)
        productList.value = data.content || []
        pageInfo.value = {
            totalElements: data.totalElements || 0,
            totalPages: data.totalPages || 0,
            page: data.page || 0,
            size: data.size || pageInfo.value.size
        }
    } catch {
        productList.value = []
    } finally {
        loading.value = false
    }
}

function changePage(page) {
    pageInfo.value.page = page
    fetchList()
}

function goDetail(item) {
    router.push(`/manage/product/${item.id}`)
}

function confirmDelete(item) {
    deleteTarget.value = item
    showDelete.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await productInterface.softDelete(deleteTarget.value.id)
        // 后端已返回提示
        showDelete.value = false
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        deleting.value = false
    }
}
</script>

<style scoped>
.product-manage {
    width: 100%;
    min-width: 0;
}

.page-heading {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 28px;
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

.page-heading .btn-primary {
    min-width: 120px;
    border-radius: 10px;
}

.card {
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(15, 118, 110, 0.06);
}

.header-actions {
    display: flex;
    align-items: center;
}

.search-bar input {
    width: 300px;
    height: 42px;
    border-radius: 10px;
    background: #f8fafc;
}

.data-table th,
.data-table td {
    text-align: center;
}

.data-table th {
    height: 46px;
    background: #fbfdfd;
}

.data-table td {
    height: 58px;
}

.data-table tbody tr {
    transition: background 0.2s;
}

.data-table tbody tr:hover td {
    background: #f2fbfa;
}

.special-status,
.regular-status {
    display: inline-block;
    min-width: 42px;
    padding: 4px 9px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 700;
    text-align: center;
}

.special-status {
    background: #fef3c7;
    color: #b45309;
}

.regular-status {
    background: #f1f5f9;
    color: #64748b;
}

.actions {
    opacity: 0.8;
    transition: opacity 0.2s;
}

tr:hover .actions {
    opacity: 1;
}

@media (max-width: 900px) {
    .page-heading,
    .card-header {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }

    .search-bar,
    .search-bar input {
        width: 100%;
    }

    .data-table {
        min-width: 980px;
    }

    .card {
        overflow-x: auto;
    }
}
</style>
