<template>
    <div class="supplier-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">供应商管理</h2>
                <p class="page-label-desc">维护采购供应商的基础资料和联系方式</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>供应商检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <input
                    v-model="searchQuery"
                    class="search-code-input"
                    placeholder="搜索供应商编码、名称或电话"
                    type="text"
                />
                <router-link
                    class="btn-primary search-button"
                    to="/manage/supplier/add"
                >+ 添加供应商</router-link>
            </div>
        </div>
            </div>
        </div>

        <div class="card">
            <div
                v-if="loading"
                class="loading-overlay"
            >
                <div class="loading-spinner"></div>
            </div>
            <div
                v-else-if="supplierList.length === 0"
                class="empty-state"
            >
                <div class="empty-icon"><IconGraphic name="supplier"/></div>
                <div class="empty-text">暂无供应商数据</div>
            </div>
            <div
                v-else-if="filteredList.length === 0"
                class="empty-state"
            >
                <div class="empty-icon"><IconGraphic name="search"/></div>
                <div class="empty-text">没有匹配的供应商</div>
            </div>
            <table
                v-else
                class="data-table"
            >
                <thead>
                <tr>
                    <th>供应商编码</th>
                    <th>供应商名称</th>
                    <th>联系电话</th>
                    <th>状态</th>
                    <th>备注</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="item in filteredList"
                    :key="item.id"
                    class="supplier-row"
                    @dblclick="goDetail(item)"
                >
                    <td><code>{{ item.supplierCode }}</code></td>
                    <td><strong>{{ item.supplierName }}</strong></td>
                    <td>{{ item.contactPhone || '-' }}</td>
                    <td>
                        <span
                            :class="item.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                            class="status-badge"
                        >{{ statusLabels[item.status] || item.status || '-' }}</span>
                    </td>
                    <td class="remark-cell">{{ item.remark || '-' }}</td>
                    <td>
                        <div class="actions">
                            <router-link
                                :to="`/manage/supplier/${encodeURIComponent(item.supplierCode)}`"
                                class="btn-outline btn-sm"
                            >详情/编辑</router-link>
                            <button
                                :disabled="disabling"
                                class="btn-danger btn-sm"
                                @click="openDisableConfirm(item)"
                            >禁用</button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div
            v-if="showDisableConfirm"
            class="modal-overlay"
            @click.self="closeDisableConfirm"
        >
            <div class="modal-content confirm-modal">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon"><IconGraphic name="warning"/></div>
                        <div class="confirm-msg">确定要禁用“{{ disableTarget?.supplierName }}”吗？</div>
                        <div class="confirm-hint">禁用后将不能用于新的采购订单</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button
                        class="btn-outline"
                        @click="closeDisableConfirm"
                    >取消</button>
                    <button
                        :disabled="disabling"
                        class="btn-danger"
                        @click="handleDisable"
                    >{{ disabling ? '处理中...' : '确认禁用' }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import supplierInterface from '../../../axios/interface/SupplierInterface.js'
import {STATUS, STATUS_LABELS} from '../../../constants/status.js'

const router = useRouter()
const statusLabels = STATUS_LABELS
const supplierList = ref([])
const searchQuery = ref('')
const loading = ref(true)
const showDisableConfirm = ref(false)
const disableTarget = ref(null)
const disabling = ref(false)

const filteredList = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    if (!query) return supplierList.value
    return supplierList.value.filter(item => {
        return [item.supplierCode, item.supplierName, item.contactPhone]
            .some(value => String(value || '').toLowerCase().includes(query))
    })
})

onMounted(fetchList)

async function fetchList() {
    loading.value = true
    try {
        supplierList.value = await supplierInterface.searchList()
    } catch {
        supplierList.value = []
    } finally {
        loading.value = false
    }
}

function goDetail(item) {
    router.push(`/manage/supplier/${encodeURIComponent(item.supplierCode)}`)
}

function openDisableConfirm(item) {
    disableTarget.value = item
    showDisableConfirm.value = true
}

function closeDisableConfirm() {
    if (disabling.value) return
    showDisableConfirm.value = false
    disableTarget.value = null
}

async function handleDisable() {
    if (disabling.value || !disableTarget.value) return
    disabling.value = true
    try {
        await supplierInterface.softDelete(disableTarget.value.id)
        showDisableConfirm.value = false
        disableTarget.value = null
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        disabling.value = false
    }
}
</script>

<style scoped>
.supplier-manage {
    width: 100%;
    min-width: 0;
}

.data-table th,
.data-table td {
    text-align: center;
}

.data-table .actions {
    justify-content: center;
}

.supplier-row {
    cursor: pointer;
}

.remark-cell {
    max-width: 300px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.confirm-modal {
    width: 380px;
}

@media (max-width: 760px) {

    .supplier-manage > .card {
        overflow-x: auto;
    }

    .data-table {
        min-width: 860px;
    }
}
</style>
