<template>
    <div class="product-manage">
        <div class="card">
            <div class="card-header">
                <span class="card-title">商品列表</span>
                <div class="header-actions">
                    <div class="search-bar">
                        <input
                            v-model="searchQuery"
                            type="text"
                            placeholder="搜索商品名称..."
                            @input="filterList"
                        />
                    </div>
                    <router-link to="/manage/product/add" class="btn-primary">+ 添加商品</router-link>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="filteredList.length === 0" class="empty-state">
                <div class="empty-icon">📦</div>
                <div class="empty-text">暂无商品数据</div>
            </div>

            <table v-else class="data-table">
                <thead>
                    <tr>
                        <th>编码</th>
                        <th>商品名称</th>
                        <th>季节</th>
                        <th>销售价</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in filteredList" :key="item.id">
                        <td><code>{{ item.code }}</code></td>
                        <td><strong>{{ item.name }}</strong></td>
                        <td>{{ seasonLabels[item.season] || item.season || '-' }}</td>
                        <td>¥{{ item.salePrice ?? '-' }}</td>
                        <td>
                            <span class="status-badge" :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'">
                                {{ statusLabels[item.status] || item.status || '-' }}
                            </span>
                        </td>
                        <td>
                            <div class="actions">
                                <button class="btn-outline btn-sm" @click="openEdit(item)">编辑</button>
                                <button class="btn-outline btn-sm" @click="openSku(item)">SKU</button>
                                <button class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <!-- 新增/编辑弹窗 -->
        <div v-if="showForm" class="modal-overlay" @click.self="closeForm">
            <div class="modal-content">
                <div class="modal-header">
                    <span class="modal-title">{{ isEditing ? '编辑商品' : '添加商品' }}</span>
                    <button class="modal-close" @click="closeForm">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleSubmit">
                    <div class="form-group">
                        <label>商品编码</label>
                        <input v-model="form.code" type="text" maxlength="20" placeholder="请输入商品编码" required />
                    </div>
                    <div class="form-group">
                        <label>商品名称</label>
                        <input v-model="form.name" type="text" maxlength="20" placeholder="请输入商品名称" required />
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>季节</label>
                            <select v-model="form.season" required>
                                <option value="" disabled>请选择季节</option>
                                <option v-for="item in seasonOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>状态</label>
                            <select v-model="form.status" required>
                                <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>进货价格</label>
                            <input v-model.number="form.importPrice" type="number" min="0" step="0.01" required />
                        </div>
                        <div class="form-group">
                            <label>销售价格</label>
                            <input v-model.number="form.salePrice" type="number" min="0" step="0.01" required />
                        </div>
                    </div>
                    <div class="form-group special-field">
                        <label>是否特价</label>
                        <button type="button" class="toggle-btn" :class="{ active: form.special }" @click="form.special = !form.special">
                            {{ form.special ? '是' : '否' }}
                        </button>
                    </div>
                </form>
                <div class="modal-footer">
                    <button type="button" class="btn-outline" @click="closeForm">取消</button>
                    <button type="button" class="btn-primary" @click="handleSubmit" :disabled="submitting">
                        {{ submitting ? '提交中...' : '确认' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- SKU 管理弹窗 -->
        <div v-if="showSku" class="modal-overlay" @click.self="showSku = false">
            <div class="modal-content modal-sku">
                <div class="modal-header">
                    <span class="modal-title">SKU 管理 - {{ skuProduct?.name }}</span>
                    <button class="modal-close" @click="showSku = false">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="sku-toolbar">
                        <span class="sku-count">共 {{ skuList.length }} 个SKU</span>
                        <button class="btn-primary btn-sm" @click="openSkuAdd">+ 添加SKU</button>
                    </div>
                    <div v-if="skuLoading" class="loading-overlay">
                        <div class="loading-spinner"></div>
                    </div>
                    <table v-else-if="skuList.length > 0" class="data-table">
                        <thead>
                            <tr>
                                <th>SKU 编码</th>
                                <th>SKU 名称</th>
                                <th>规格</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="sku in skuList" :key="sku.id">
                                <td><code>{{ sku.code }}</code></td>
                                <td>{{ sku.name || '-' }}</td>
                                <td>{{ sku.spec || '-' }}</td>
                                <td>
                                    <span class="status-badge" :class="sku.status === STATUS.ENABLE ? 'status-ok' : 'status-error'">
                                        {{ statusLabels[sku.status] || sku.status || '-' }}
                                    </span>
                                </td>
                                <td>
                                    <div class="actions">
                                        <button class="btn-outline btn-sm" @click="openSkuEdit(sku)">编辑</button>
                                        <button class="btn-danger btn-sm" @click="confirmDeleteSku(sku)">删除</button>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div v-else class="empty-state">
                        <div class="empty-icon">🏷️</div>
                        <div class="empty-text">暂无SKU</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- SKU 表单弹窗 -->
        <div v-if="showSkuForm" class="modal-overlay" @click.self="showSkuForm = false">
            <div class="modal-content">
                <div class="modal-header">
                    <span class="modal-title">{{ isSkuEditing ? '编辑SKU' : '添加SKU' }}</span>
                    <button class="modal-close" @click="showSkuForm = false">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleSkuSubmit">
                    <div class="form-group">
                        <label>SKU 名称</label>
                        <input v-model="skuForm.name" type="text" placeholder="例如: 黑色圆领T恤" required />
                    </div>
                    <div class="form-group">
                        <label>SKU 规格</label>
                        <input v-model="skuForm.spec" type="text" placeholder="例如: 颜色:黑色 / 尺码:M" required />
                    </div>
                    <div v-if="isSkuEditing" class="form-group">
                        <label>状态</label>
                        <select v-model="skuForm.status" required>
                            <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                        </select>
                    </div>
                </form>
                <div class="modal-footer">
                    <button type="button" class="btn-outline" @click="showSkuForm = false">取消</button>
                    <button type="button" class="btn-primary" @click="handleSkuSubmit" :disabled="skuSubmitting">
                        {{ skuSubmitting ? '提交中...' : '确认' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 删除确认弹窗 -->
        <div v-if="showDelete" class="modal-overlay" @click.self="showDelete = false">
            <div class="modal-content" style="min-width: 360px;">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon">⚠️</div>
                        <div class="confirm-msg">确定要删除"{{ deleteTarget?.name }}"吗？</div>
                        <div class="confirm-hint">此操作不可恢复</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn-outline" @click="showDelete = false">取消</button>
                    <button class="btn-danger" @click="handleDelete" :disabled="deleting">
                        {{ deleting ? '删除中...' : '确认删除' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useToastStore } from '../../stores/toastStore.js'
import productInterface from '../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'
import { SEASON_LABELS, SEASON_OPTIONS } from '../../constants/season.js'
import { STATUS, STATUS_LABELS, STATUS_OPTIONS } from '../../constants/status.js'

const toast = useToastStore()
const seasonLabels = SEASON_LABELS
const statusLabels = STATUS_LABELS
const seasonOptions = SEASON_OPTIONS
const statusOptions = STATUS_OPTIONS

// 商品列表
const productList = ref([])
const searchQuery = ref('')
const loading = ref(true)

const filteredList = computed(() => {
    if (!searchQuery.value) return productList.value
    const q = searchQuery.value.toLowerCase()
    return productList.value.filter(p =>
        p.name?.toLowerCase().includes(q) ||
        p.code?.toLowerCase().includes(q) ||
        String(p.id).includes(q)
    )
})

onMounted(() => fetchList())

async function fetchList() {
    loading.value = true
    try {
        productList.value = await productInterface.searchList()
    } catch {
        productList.value = []
    } finally {
        loading.value = false
    }
}

function filterList() {
    // computed handles filtering
}

// 新增/编辑
const showForm = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const form = ref({
    code: '',
    name: '',
    season: '',
    importPrice: 0,
    salePrice: 0,
    special: false,
    status: STATUS.ENABLE
})

function openAdd() {
    isEditing.value = false
    form.value = {
        code: '',
        name: '',
        season: '',
        importPrice: 0,
        salePrice: 0,
        special: false,
        status: STATUS.ENABLE
    }
    showForm.value = true
}

function openEdit(item) {
    isEditing.value = true
    form.value = {
        id: item.id,
        code: item.code || '',
        name: item.name || '',
        season: item.season || '',
        importPrice: item.importPrice ?? 0,
        salePrice: item.salePrice ?? 0,
        special: Boolean(item.special),
        status: item.status || STATUS.ENABLE
    }
    showForm.value = true
}

function closeForm() {
    showForm.value = false
}

async function handleSubmit() {
    if (!form.value.code || !form.value.name || !form.value.season) return
    submitting.value = true
    try {
        if (isEditing.value) {
            await productInterface.update(form.value.id, {
                code: form.value.code,
                name: form.value.name,
                season: form.value.season,
                importPrice: form.value.importPrice,
                salePrice: form.value.salePrice,
                special: form.value.special,
                status: form.value.status
            })
            toast.success('商品更新成功')
        } else {
            toast.info('请使用独立的添加商品页面')
            return
        }
        closeForm()
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 删除
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

function confirmDelete(item) {
    deleteTarget.value = item
    showDelete.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await productInterface.softDelete(deleteTarget.value.id)
        toast.success('商品已删除')
        showDelete.value = false
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        deleting.value = false
    }
}

// SKU 管理
const showSku = ref(false)
const skuProduct = ref(null)
const skuList = ref([])
const skuLoading = ref(false)

async function openSku(item) {
    skuProduct.value = item
    showSku.value = true
    skuLoading.value = true
    try {
        skuList.value = await productSkuInterface.searchListByProductId(item.id)
    } catch {
        skuList.value = []
    } finally {
        skuLoading.value = false
    }
}

// SKU 表单
const showSkuForm = ref(false)
const isSkuEditing = ref(false)
const skuSubmitting = ref(false)
const skuForm = ref({ name: '', spec: '', status: STATUS.ENABLE })
const editingSkuId = ref(null)

function openSkuAdd() {
    isSkuEditing.value = false
    skuForm.value = { name: '', spec: '', status: STATUS.ENABLE }
    showSkuForm.value = true
}

function openSkuEdit(sku) {
    isSkuEditing.value = true
    editingSkuId.value = sku.id
    skuForm.value = {
        name: sku.name || '',
        spec: sku.spec || '',
        status: sku.status || STATUS.ENABLE
    }
    showSkuForm.value = true
}

async function handleSkuSubmit() {
    if (!skuForm.value.name || !skuForm.value.spec) return
    skuSubmitting.value = true
    try {
        if (isSkuEditing.value) {
            await productSkuInterface.update(editingSkuId.value, {
                name: skuForm.value.name,
                spec: skuForm.value.spec,
                status: skuForm.value.status
            })
            toast.success('SKU 更新成功')
        } else {
            await productSkuInterface.create({
                productId: skuProduct.value.id,
                name: skuForm.value.name,
                spec: skuForm.value.spec
            })
            toast.success('SKU 添加成功')
        }
        showSkuForm.value = false
        await openSku(skuProduct.value)
    } catch {
        // 拦截器已处理
    } finally {
        skuSubmitting.value = false
    }
}

// 删除SKU
async function confirmDeleteSku(sku) {
    if (!confirm('确定要删除该SKU吗？')) return
    try {
        await productSkuInterface.softDelete(sku.id)
        toast.success('SKU 已删除')
        await openSku(skuProduct.value)
    } catch {
        // 拦截器已处理
    }
}
</script>

<style scoped>
.product-manage {
    max-width: 1100px;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.desc-cell {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--text-secondary);
}

/* SKU */
.modal-sku {
    min-width: 600px;
}

.sku-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.sku-count {
    font-size: var(--font-sm);
    color: var(--text-secondary);
}

code {
    background: #f1f5f9;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: var(--font-sm);
    color: var(--primary);
}
</style>
