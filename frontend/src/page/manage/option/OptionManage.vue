<template>
    <div class="option-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">选项管理</h2>
                <p class="page-label-desc">按类型管理颜色、尺码和其他商品选项值</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <rect x="2" y="7" width="16" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
                <path d="M6 7V5a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>分类筛选</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <div class="filter-capsules">
                    <button
                        :class="selectedType === '' ? 'capsule-active' : 'capsule'"
                        type="button"
                        @click="selectType('')"
                    >
                        <i class="capsule-dot"></i>
                        <span class="capsule-label">全部选项</span>
                    </button>
                    <button
                        v-for="item in typeOptions"
                        :key="item.value"
                        :class="selectedType === item.value ? 'capsule-active' : 'capsule'"
                        type="button"
                        @click="selectType(item.value)"
                    >
                        <i class="capsule-dot"></i>
                        <span class="capsule-label">{{ item.label }}</span>
                    </button>
                </div>
                <router-link
                    :to="addOptionPath"
                    class="btn-primary search-button"
                >
                    {{ selectedType ? `+ 添加${selectedTypeLabel}` : '+ 添加选项值' }}
                </router-link>
            </div>
        </div>
            </div>
        </div>

            <div class="card">
            <div class="card-header">
                <span class="card-title">选项列表</span>
            </div>

            <div
                v-if="loading"
                class="loading-overlay"
            >
                <div class="loading-spinner"></div>
            </div>

            <div
                v-else-if="optionList.length === 0"
                class="empty-state"
            >
                <div class="empty-icon">
                    <IconGraphic name="tag"/>
                </div>
                <div class="empty-text">
                    {{ selectedType ? `暂无${selectedTypeLabel}选项` : '暂无选项数据' }}
                </div>
            </div>

            <table
                v-else
                class="data-table"
            >
                <thead>
                <tr>
                    <th>编号</th>
                    <th v-if="selectedType === ''">选项类型</th>
                    <th>{{ selectedType === '' ? '选项值' : `${selectedTypeLabel}值` }}</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="item in pagedList"
                    :key="item.id"
                >
                    <td>{{ item.id }}</td>
                    <td v-if="selectedType === ''">
                        <span class="option-type-badge">
                            {{ typeLabels[item.optionType] || item.optionType || '-' }}
                        </span>
                    </td>
                    <td v-if="editingId !== item.id">
                        <strong>{{ item.optionValue || '-' }}</strong>
                    </td>
                    <td v-else>
                        <input
                            v-model="editValue"
                            class="inline-input"
                            maxlength="8"
                        />
                    </td>
                    <td>
                        <div
                            v-if="editingId !== item.id"
                            class="actions"
                        >
                            <button
                                class="btn-outline"
                                type="button"
                                @click="startEdit(item)"
                            >
                                编辑
                            </button>
                            <button
                                class="btn-danger"
                                type="button"
                                @click="confirmDelete(item)"
                            >
                                删除
                            </button>
                        </div>
                        <div
                            v-else
                            class="actions"
                        >
                            <button
                                :disabled="saving"
                                class="btn-success"
                                type="button"
                                @click="saveEdit"
                            >
                                {{ saving ? '保存中...' : '保存' }}
                            </button>
                            <button
                                class="btn-outline"
                                type="button"
                                @click="cancelEdit"
                            >
                                取消
                            </button>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>

            <TablePagination
                :loading="loading"
                :page="safePage"
                :total-elements="optionList.length"
                :total-pages="totalPages"
                @change="changePage"
            />
        </div>

        <div
            v-if="showDelete"
            class="modal-overlay"
            @click.self="showDelete = false"
        >
            <div class="modal-content delete-modal">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon">
                            <IconGraphic name="warning"/>
                        </div>
                        <div class="confirm-msg">确定要删除“{{ deleteTarget?.optionValue }}”吗？</div>
                        <div class="confirm-hint">此操作不可恢复</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button
                        class="btn-outline"
                        type="button"
                        @click="showDelete = false"
                    >
                        取消
                    </button>
                    <button
                        :disabled="deleting"
                        class="btn-danger"
                        type="button"
                        @click="handleDelete"
                    >
                        {{ deleting ? '删除中...' : '确认删除' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import optionValueInterface from '../../../axios/interface/OptionValueInterface.js'
import TablePagination from '../../../component/common/TablePagination.vue'
import {OPTION_TYPE_LABELS, OPTION_TYPE_OPTIONS} from '../../../constants/optionType.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const typeOptions = OPTION_TYPE_OPTIONS
const typeLabels = OPTION_TYPE_LABELS
const initialType = typeof route.query.type === 'string' && typeOptions.some(item => item.value === route.query.type)
    ? route.query.type
    : ''

const selectedType = ref(initialType)
const optionList = ref([])
const loading = ref(true)
const editingId = ref(null)
const editValue = ref('')
const saving = ref(false)
const showDelete = ref(false)
const deleteTarget = ref(null)
const deleting = ref(false)
const page = ref(0)
const pageSize = 10

// 前端分页（接口返回全量列表，本地切片）
const totalPages = computed(() => Math.max(1, Math.ceil(optionList.value.length / pageSize)))
const safePage = computed(() => Math.min(page.value, totalPages.value - 1))
const pagedList = computed(() => {
    const start = safePage.value * pageSize
    return optionList.value.slice(start, start + pageSize)
})

function changePage(index) {
    page.value = index
}

const selectedTypeLabel = computed(() => {
    return selectedType.value
        ? typeLabels[selectedType.value] || '选项'
        : '全部'
})

const addOptionPath = computed(() => ({
    path: '/manage/option/add',
    query: selectedType.value
        ? {type: selectedType.value}
        : {}
}))

onMounted(fetchList)

async function selectType(type) {
    if (selectedType.value === type) return
    selectedType.value = type
    cancelEdit()
    page.value = 0
    await router.replace({
        path: '/manage/option',
        query: type
            ? {type}
            : {}
    })
    await fetchList()
}

async function fetchList() {
    loading.value = true
    try {
        optionList.value = selectedType.value
            ? await optionValueInterface.searchListByType(selectedType.value)
            : await optionValueInterface.searchList()
    } catch {
        optionList.value = []
    } finally {
        loading.value = false
    }
}

function startEdit(item) {
    editingId.value = item.id
    editValue.value = item.optionValue || ''
}

function cancelEdit() {
    editingId.value = null
    editValue.value = ''
}

async function saveEdit() {
    const value = editValue.value.trim()
    if (!value) {
        toast.warning(`请输入${selectedTypeLabel.value}值`)
        return
    }
    saving.value = true
    try {
        await optionValueInterface.update(editingId.value, {
            optionType: selectedType.value || optionList.value.find(item => item.id === editingId.value)?.optionType,
            optionValue: value
        })
        cancelEdit()
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        saving.value = false
    }
}

function confirmDelete(item) {
    deleteTarget.value = item
    showDelete.value = true
}

async function handleDelete() {
    deleting.value = true
    try {
        await optionValueInterface.softDelete(deleteTarget.value.id)
        showDelete.value = false
        deleteTarget.value = null
        await fetchList()
    } catch {
        // 拦截器已处理
    } finally {
        deleting.value = false
    }
}
</script>

<style scoped>
.option-manage {
    width: 100%;
    min-width: 0;
}

/* ============ 分类筛选（使用全局 search-shell 工具盒） ============ */
.filter-capsules {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
}

.capsule,
.capsule-active {
    height: 34px;
    padding: 0 15px;
    display: inline-flex;
    align-items: center;
    gap: 7px;
    border: 1.5px solid var(--border-strong);
    border-radius: 999px;
    background: var(--bg-card);
    color: var(--text-secondary);
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: var(--transition);
    white-space: nowrap;
}

.capsule:hover {
    border-color: var(--primary);
    color: var(--primary);
    background: var(--primary-light);
    transform: translateY(-1px);
}

.capsule-active {
    border-color: var(--primary);
    background: var(--primary);
    color: var(--text-invert);
    box-shadow: var(--shadow-primary);
}

.capsule-active:hover {
    border-color: var(--primary-dark);
    background: var(--primary-dark);
    color: var(--text-invert);
    transform: translateY(-1px);
}

.capsule-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: color-mix(in srgb, var(--primary) 55%, transparent);
    transition: var(--transition);
}

.capsule:hover .capsule-dot {
    background: var(--primary);
}

.capsule-active .capsule-dot {
    background: var(--text-invert);
}

.capsule-label {
    line-height: 1;
}

/* ============ 表格（全局样式） ============ */
.data-table th,
.data-table td {
    text-align: center;
}

.data-table .actions {
    justify-content: center;
}

.option-type-badge {
    display: inline-block;
    min-width: 70px;
    padding: 4px 10px;
    border-radius: 999px;
    background: var(--primary-light);
    color: var(--primary-dark);
    font-size: 12px;
    font-weight: 700;
}

.inline-input {
    width: 220px;
    height: 34px;
    padding: 0 10px;
    border: 1px solid var(--border);
    border-radius: 999px;
    font-size: 13px;
    text-align: center;
    outline: none;
}

.inline-input:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.actions {
    display: flex;
    justify-content: center;
    gap: 8px;
}

.delete-modal {
    min-width: 360px;
}

@media (max-width: 760px) {

    .filter-capsules {
        gap: 6px;
    }

    .capsule,
    .capsule-active {
        height: 32px;
        padding: 0 12px;
        font-size: 12px;
    }
}
</style>
