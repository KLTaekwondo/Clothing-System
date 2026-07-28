<template>
    <div class="warehouse-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">仓库管理</h2>
                <p class="page-desc">管理仓库信息、查看和调拨库存</p>
            </div>
            <router-link class="btn-primary" to="/manage/warehouse/add">+ 添加仓库</router-link>
        </div>
        <div class="card">
            <div class="card-header">
                <span class="card-title">仓库列表</span>
                <div class="header-actions">
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="warehouseList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="warehouse"/></div>
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
                <tr v-for="item in warehouseList" :key="item.id" style="cursor:pointer" @dblclick="goDetail(item)">
                    <td><code>{{ item.code }}</code></td>
                    <td><strong>{{ item.name }}</strong></td>
                    <td>
                            <span :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                                  class="status-badge">
                                {{ statusLabels[item.status] || item.status || '-' }}
                            </span>
                    </td>
                    <td>
                        <div class="actions">
                            <router-link :to="`/manage/warehouse/${item.id}`" class="btn-outline btn-sm">详情/编辑
                            </router-link>
                            <router-link :to="`/manage/warehouse/${item.id}/stock`" class="btn-outline btn-sm">库存
                            </router-link>
                            <button class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <!-- 删除确认 -->
        <div v-if="showDelete" class="modal-overlay" @click.self="showDelete = false">
            <div class="modal-content" style="min-width: 360px;">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon"><IconGraphic name="warning"/></div>
                        <div class="confirm-msg">确定要删除"{{ deleteTarget?.name }}"吗？</div>
                        <div class="confirm-hint">此操作不可恢复</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn-outline" @click="showDelete = false">取消</button>
                    <button :disabled="deleting" class="btn-danger" @click="handleDelete">
                        {{ deleting ? '删除中...' : '确认删除' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../stores/toastStore.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import {STATUS, STATUS_LABELS} from '../../constants/status.js'

const router = useRouter()
const toast = useToastStore()
const statusLabels = STATUS_LABELS

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

// 删除
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)

function goDetail(item) {
    router.push(`/manage/warehouse/${item.id}`)
}

function confirmDelete(item) {
    deleteTarget.value = item
    showDelete.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await wareHouseInterface.softDelete(deleteTarget.value.id)
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
.warehouse-manage {
    width: 100%;
    min-width: 0;
}

.page-heading {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    margin-bottom: 24px;
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

.warehouse-manage > .card {
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

@media (max-width: 760px) {
    .header-actions {
        align-items: stretch;
        flex-direction: column;
        gap: 12px;
    }
}
</style>
