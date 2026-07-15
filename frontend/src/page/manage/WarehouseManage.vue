<template>
    <div class="warehouse-manage">
        <div class="card">
            <div class="card-header">
                <span class="card-title">仓库列表</span>
                <div class="header-actions">
                    <button class="btn-primary" @click="openAdd">+ 添加仓库</button>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="warehouseList.length === 0" class="empty-state">
                <div class="empty-icon">🏭</div>
                <div class="empty-text">暂无仓库数据</div>
            </div>

            <table v-else class="data-table">
                <thead>
                    <tr>
                        <th>编码</th>
                        <th>仓库名称</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in warehouseList" :key="item.id">
                        <td><code>{{ item.code }}</code></td>
                        <td><strong>{{ item.name }}</strong></td>
                        <td>
                            <span class="status-badge" :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'">
                                {{ statusLabels[item.status] || item.status || '-' }}
                            </span>
                        </td>
                        <td>
                            <div class="actions">
                                <button class="btn-outline btn-sm" @click="openEdit(item)">编辑</button>
                                <button class="btn-outline btn-sm" @click="openStock(item)">库存</button>
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
                    <span class="modal-title">{{ isEditing ? '编辑仓库' : '添加仓库' }}</span>
                    <button class="modal-close" @click="closeForm">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleSubmit">
                    <div class="form-row">
                        <div class="form-group">
                            <label>仓库编码</label>
                            <input v-model="form.code" type="text" minlength="2" maxlength="10" placeholder="请输入仓库编码" required />
                        </div>
                        <div class="form-group">
                            <label>仓库名称</label>
                            <input v-model="form.name" type="text" minlength="2" maxlength="10" placeholder="请输入仓库名称" required />
                        </div>
                    </div>
                    <div class="form-group">
                        <label>仓库密码</label>
                        <input v-model="form.password" type="password" minlength="6" maxlength="12" placeholder="6-12位字母、数字或下划线" required />
                    </div>
                    <div v-if="!isEditing" class="form-group">
                        <label>管理员 ID</label>
                        <input v-model.number="form.adminId" type="number" min="1" placeholder="请输入管理员 ID" required />
                    </div>
                    <div v-if="isEditing" class="form-group">
                        <label>状态</label>
                        <select v-model="form.status" required>
                            <option v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                        </select>
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

        <!-- 库存管理弹窗 -->
        <div v-if="showStock" class="modal-overlay" @click.self="showStock = false">
            <div class="modal-content modal-stock">
                <div class="modal-header">
                    <span class="modal-title">库存管理 - {{ stockWarehouse?.name }}</span>
                    <button class="modal-close" @click="showStock = false">&times;</button>
                </div>
                <div class="modal-body">
                    <div class="stock-toolbar">
                        <div class="stock-search">
                            <input
                                v-model="stockProductId"
                                type="number"
                                placeholder="输入商品ID查询库存"
                                min="1"
                            />
                            <button class="btn-primary btn-sm" @click="fetchStock">查询</button>
                        </div>
                        <div class="stock-transfer">
                            <button class="btn-outline btn-sm" @click="showTransfer = true">库存转移</button>
                        </div>
                    </div>

                    <div v-if="stockList.length === 0" class="empty-state">
                        <div class="empty-icon">📊</div>
                        <div class="empty-text">请输入商品ID查询库存</div>
                    </div>

                    <table v-else class="data-table">
                        <thead>
                            <tr>
                                <th>规格</th>
                                <th>库存数量</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(stock, index) in stockList" :key="stock.id || index">
                                <td>{{ formatSpec(stock.spec) }}</td>
                                <td>
                                    <span class="stock-num" :class="{ 'stock-low': stock.stock <= 10 }">
                                        {{ stock.stock ?? 0 }}
                                    </span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <!-- 库存转移弹窗 -->
        <div v-if="showTransfer" class="modal-overlay" @click.self="showTransfer = false">
            <div class="modal-content" style="min-width: 400px;">
                <div class="modal-header">
                    <span class="modal-title">库存转移</span>
                    <button class="modal-close" @click="showTransfer = false">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleTransfer">
                    <div class="form-group">
                        <label>源仓库</label>
                        <select v-model="transferForm.sourceWarehouseId" required>
                            <option value="" disabled>请选择源仓库</option>
                            <option v-for="wh in warehouseList" :key="wh.id" :value="wh.id">
                                {{ wh.name }}
                            </option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>目标仓库</label>
                        <select v-model="transferForm.targetWarehouseId" required>
                            <option value="" disabled>请选择目标仓库</option>
                            <option v-for="wh in warehouseList" :key="wh.id" :value="wh.id">
                                {{ wh.name }}
                            </option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>SKU ID</label>
                        <input v-model.number="transferForm.skuId" type="number" placeholder="请输入 SKU ID" required min="1" />
                    </div>
                    <div class="form-group">
                        <label>转移数量</label>
                        <input v-model.number="transferForm.stock" type="number" placeholder="请输入数量" required min="1" />
                    </div>
                </form>
                <div class="modal-footer">
                    <button class="btn-outline" @click="showTransfer = false">取消</button>
                    <button class="btn-primary" @click="handleTransfer" :disabled="transferring">
                        {{ transferring ? '转移中...' : '确认转移' }}
                    </button>
                </div>
            </div>
        </div>

        <!-- 删除确认 -->
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
import { ref, onMounted } from 'vue'
import { useToastStore } from '../../stores/toastStore.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import wareHouseStockInterface from '../../axios/interface/WareHouseStockInterface.js'
import { STATUS, STATUS_LABELS, STATUS_OPTIONS } from '../../constants/status.js'

const toast = useToastStore()
const statusLabels = STATUS_LABELS
const statusOptions = STATUS_OPTIONS

const warehouseList = ref([])
const loading = ref(true)

onMounted(() => fetchList())

async function fetchList() {
    loading.value = true
    try {
        warehouseList.value = await wareHouseInterface.searchList()
    } catch {
        warehouseList.value = []
    } finally {
        loading.value = false
    }
}

// 表单
const showForm = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const form = ref({
    code: '',
    name: '',
    password: '',
    adminId: '',
    status: STATUS.ENABLE
})

function openAdd() {
    isEditing.value = false
    form.value = { code: '', name: '', password: '', adminId: '', status: STATUS.ENABLE }
    showForm.value = true
}

function openEdit(item) {
    isEditing.value = true
    form.value = {
        id: item.id,
        code: item.code || '',
        name: item.name || '',
        password: '',
        adminId: '',
        status: item.status || STATUS.ENABLE
    }
    showForm.value = true
}

function closeForm() {
    showForm.value = false
}

async function handleSubmit() {
    if (!form.value.code || !form.value.name || !form.value.password || (!isEditing.value && !form.value.adminId)) return
    submitting.value = true
    try {
        const payload = {
            code: form.value.code,
            name: form.value.name,
            password: form.value.password
        }
        if (!isEditing.value) {
            payload.adminId = Number(form.value.adminId)
        }
        if (isEditing.value) {
            payload.status = form.value.status
        }
        if (isEditing.value) {
            await wareHouseInterface.update(form.value.id, payload)
            toast.success('仓库更新成功')
        } else {
            await wareHouseInterface.create(payload)
            toast.success('仓库添加成功')
        }
        closeForm()
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

// 库存查询
const showStock = ref(false)
const stockWarehouse = ref(null)
const stockList = ref([])
const stockProductId = ref('')

function formatSpec(spec) {
    if (!spec) return '-'
    if (typeof spec === 'string') {
        try {
            return Object.entries(JSON.parse(spec)).map(([key, value]) => `${key}:${value}`).join(' / ')
        } catch {
            return spec
        }
    }
    return Object.entries(spec).map(([key, value]) => `${key}:${value}`).join(' / ')
}

function openStock(item) {
    stockWarehouse.value = item
    stockList.value = []
    stockProductId.value = ''
    showStock.value = true
}

async function fetchStock() {
    if (!stockProductId.value) {
        toast.warning('请输入商品ID')
        return
    }
    try {
        stockList.value = await wareHouseStockInterface.searchStock(
            stockWarehouse.value.id,
            Number(stockProductId.value)
        )
        if (stockList.value.length === 0) {
            toast.info('暂无库存数据')
        }
    } catch {
        stockList.value = []
    }
}

// 库存转移
const showTransfer = ref(false)
const transferring = ref(false)
const transferForm = ref({
    sourceWarehouseId: '',
    targetWarehouseId: '',
    skuId: '',
    stock: ''
})

async function handleTransfer() {
    const f = transferForm.value
    if (!f.sourceWarehouseId || !f.targetWarehouseId || !f.skuId || !f.stock) {
        toast.warning('请填写完整的转移信息')
        return
    }
    if (Number(f.sourceWarehouseId) === Number(f.targetWarehouseId)) {
        toast.warning('源仓库和目标仓库不能相同')
        return
    }
    transferring.value = true
    try {
        await wareHouseStockInterface.transferStock({
            sourceWarehouseId: Number(f.sourceWarehouseId),
            targetWarehouseId: Number(f.targetWarehouseId),
            skuId: Number(f.skuId),
            stock: Number(f.stock)
        })
        toast.success('库存转移成功')
        showTransfer.value = false
        transferForm.value = { sourceWarehouseId: '', targetWarehouseId: '', skuId: '', stock: '' }
    } catch {
        // 拦截器已处理
    } finally {
        transferring.value = false
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
        await wareHouseInterface.softDelete(deleteTarget.value.id)
        toast.success('仓库已删除')
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
.warehouse-manage {
    max-width: 1100px;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.modal-stock {
    min-width: 600px;
}

.stock-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.stock-search {
    display: flex;
    gap: 8px;
    align-items: center;
}

.stock-search input {
    width: 180px;
}

.stock-num {
    font-weight: 600;
    font-size: var(--font-md);
}

.stock-low {
    color: var(--error);
}
</style>
