<template>
    <div class="option-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">选项管理</h2>
                <p class="page-desc">按类型管理颜色、尺码和其他商品选项值</p>
            </div>
            <router-link
                :to="addOptionPath"
                class="btn-primary"
            >
                {{ selectedType ? `+ 添加${selectedTypeLabel}` : '+ 添加选项值' }}
            </router-link>
        </div>

        <div class="capsule-toolbox">
            <div class="toolbox-header">
                <svg class="toolbox-icon" viewBox="0 0 20 20" fill="none" width="16" height="16">
                    <rect x="2" y="7" width="16" height="11" rx="2" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M6 7V5a4 4 0 0 1 8 0v2" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span class="toolbox-title">分类筛选</span>
            </div>
            <div class="toolbox-body">
                <button
                    :class="['capsule', selectedType === '' ? 'capsule-active' : '']"
                    type="button"
                    @click="selectType('')"
                >
                    <span class="capsule-icon">📋</span>
                    <span class="capsule-label">全部选项</span>
                </button>
                <button
                    v-for="item in typeOptions"
                    :key="item.value"
                    :class="['capsule', selectedType === item.value ? 'capsule-active' : '']"
                    type="button"
                    @click="selectType(item.value)"
                >
                    <span class="capsule-icon">{{ typeIcons[item.value] || '🏷️' }}</span>
                    <span class="capsule-label">{{ item.label }}</span>
                </button>
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
                    v-for="item in optionList"
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

        </div>

        <div
            v-if="showDelete"
            class="modal-overlay"
            @click.self="showDelete = false"
        >
            <div class="delete-modal">
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
import {OPTION_TYPE_LABELS, OPTION_TYPE_OPTIONS} from '../../../constants/optionType.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const typeOptions = OPTION_TYPE_OPTIONS
const typeLabels = OPTION_TYPE_LABELS
const typeIcons = {
    COLOR: '🎨',
    SIZE: '📏',
    TYPE: '🔖',
    CATEGORY: '📂',
    UNIT: '⚖️',
    COMPOSITION: '🧵',
    YEAR: '📅'
}
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

/* ============ 胶囊工具箱 ============ */
.capsule-toolbox {
    margin-bottom: 24px;
    border: 1px solid #dceae7;
    border-radius: 14px;
    background: #fff;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.toolbox-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 18px 0;
    font-size: 12px;
    font-weight: 700;
    color: #94a3b8;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.toolbox-icon {
    flex-shrink: 0;
    color: #94a3b8;
}

.toolbox-title {
    color: #64748b;
}

.toolbox-body {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    padding: 10px 18px 14px;
}

.capsule {
    height: 38px;
    padding: 0 16px 0 12px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    border: 1px solid #e2e8f0;
    border-radius: 999px;
    background: #f8fafc;
    color: #475569;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    white-space: nowrap;
}

.capsule:hover {
    border-color: #14b8a6;
    color: #0d9488;
    background: #f0fdfa;
}

.capsule-active {
    border-color: #0d9488;
    background: #0d9488;
    color: #fff;
    box-shadow: 0 3px 10px rgba(13, 148, 136, 0.25);
}

.capsule-active:hover {
    background: #0f766e;
    border-color: #0f766e;
    color: #fff;
}

.capsule-icon {
    font-size: 15px;
    line-height: 1;
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
    border-radius: 10px;
    background: #e8f5f2;
    color: #0f766e;
    font-size: 12px;
    font-weight: 700;
}

.inline-input {
    width: 220px;
    height: 34px;
    padding: 0 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    font-size: 13px;
    text-align: center;
    outline: none;
}

.inline-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.12);
}

.actions {
    display: flex;
    justify-content: center;
    gap: 8px;
}

.delete-modal {
    min-width: 360px;
    border-radius: 14px;
    background: #fff;
    overflow: hidden;
}

@media (max-width: 760px) {
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }

    .capsule-toolbox {
        border-radius: 12px;
    }

    .toolbox-body {
        gap: 6px;
        padding: 8px 14px 12px;
    }

    .capsule {
        height: 34px;
        padding: 0 12px 0 10px;
        font-size: 12px;
    }
}
</style>
