<template>
    <div class="product-manage">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">商品管理</h2>
                <p class="page-label-desc">管理商品基础信息、价格和 SKU</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-label">
                    <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                        <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    <span>商品检索</span>
                </div>
                <div class="search-shell">
                    <div class="search-controls">
                        <input
                            v-model="searchQuery"
                            class="search-code-input"
                            placeholder="搜索商品名称或编码"
                            type="text"
                        />
                        <router-link
                            class="btn-primary search-button"
                            to="/manage/product/add"
                        >
                            + 添加商品
                        </router-link>
                        <button
                            :disabled="exporting"
                            class="btn-outline search-button"
                            type="button"
                            @click="handleExport"
                        >{{ exporting ? '导出中...' : '导出商品' }}</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="card">
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
                    <th>图片</th>
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
                <tr
                    v-for="item in filteredList"
                    :key="item.id"
                    style="cursor:pointer"
                    @dblclick="goDetail(item)"
                    @mouseenter="showPreview(item, $event)"
                    @mousemove="movePreview($event)"
                    @mouseleave="hidePreview"
                >
                    <td>
                        <img
                            v-if="item.imgUrl"
                            :alt="item.name"
                            :src="toImageUrl(item.imgUrl)"
                            class="row-thumb"
                        />
                        <span v-else class="row-thumb-empty">-</span>
                    </td>
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

        <div
            v-if="previewItem"
            :style="previewStyle"
            class="image-zoom"
        >
            <img
                :alt="previewItem.name"
                :src="toImageUrl(previewItem.imgUrl)"
            />
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import productInterface from '../../../axios/interface/ProductInterface.js'
import exportInterface from '../../../axios/interface/ExportInterface.js'
import DeleteConfirmDialog from '../../../component/DeleteConfirmDialog.vue'
import TablePagination from '../../../component/common/TablePagination.vue'
import {SEASON_LABELS} from '../../../constants/season.js'
import {STATUS, STATUS_LABELS} from '../../../constants/status.js'
import {toImageUrl} from '../../../constants/image.js'

const router = useRouter()
const toast = useToastStore()
const exporting = ref(false)
const seasonLabels = SEASON_LABELS
const statusLabels = STATUS_LABELS

const productList = ref([])
const searchQuery = ref('')
const loading = ref(true)
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
const previewItem = ref(null)
const previewPos = ref({x: 0, y: 0})

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

// 放大图跟随光标：默认贴右下方，靠近视口边缘时翻到另一侧，避免被裁掉
const previewStyle = computed(() => {
    const size = 220
    const gap = 18
    const flipX = previewPos.value.x + gap + size > window.innerWidth
    const flipY = previewPos.value.y + gap + size > window.innerHeight
    const left = flipX ? previewPos.value.x - gap - size : previewPos.value.x + gap
    const top = flipY ? previewPos.value.y - gap - size : previewPos.value.y + gap
    return {
        left: Math.max(gap, left) + 'px',
        top: Math.max(gap, top) + 'px'
    }
})

onMounted(fetchList)

// 悬停商品行时展示放大图（没有图片的商品不弹）
function showPreview(item, event) {
    if (!item.imgUrl) return
    previewItem.value = item
    movePreview(event)
}

function movePreview(event) {
    if (!previewItem.value) return
    previewPos.value = {x: event.clientX, y: event.clientY}
}

function hidePreview() {
    previewItem.value = null
}

// 导出商品列表 CSV
async function handleExport() {
    exporting.value = true
    try {
        const ok = await exportInterface.exportProducts()
        if (ok) {
            toast.success('商品列表已导出')
        } else {
            toast.error('导出失败，请稍后重试')
        }
    } finally {
        exporting.value = false
    }
}

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

.card {
    border-radius: 16px;
    box-shadow: var(--shadow);
}

.data-table th,
.data-table td {
    text-align: center;
}

.data-table th {
    height: 46px;
    background: var(--bg-subtle);
}

.data-table td {
    height: 58px;
}

.data-table tbody tr {
    transition: background 0.2s;
}

.data-table tbody tr:hover td {
    background: var(--bg-hover);
}

.row-thumb {
    width: 44px;
    height: 44px;
    display: block;
    margin: 0 auto;
    object-fit: cover;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: 8px;
}

.row-thumb-empty {
    color: var(--text-muted);
}

.image-zoom {
    position: fixed;
    z-index: 900;
    width: 220px;
    height: 220px;
    padding: 6px;
    box-sizing: border-box;
    pointer-events: none;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: 14px;
    box-shadow: var(--shadow-lg);
}

.image-zoom img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    border-radius: 10px;
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
    background: var(--warning-light);
    color: var(--warning-dark);
}

.regular-status {
    background: var(--bg-muted);
    color: var(--text-muted);
}

.actions {
    opacity: 0.8;
    transition: opacity 0.2s;
}

tr:hover .actions {
    opacity: 1;
}

@media (max-width: 900px) {
    .data-table {
        min-width: 1060px;
    }

    .card {
        overflow-x: auto;
    }
}
</style>
