<template>
    <div class="option-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">选项管理</h2>
                <p class="page-desc">管理颜色、尺码、类型等商品选项值</p>
            </div>
            <router-link class="btn-primary" to="/manage/option/add">+ 添加选项值</router-link>
        </div>
        <div class="card">
            <div class="card-header">
                <span class="card-title">选项值列表</span>
                <div class="header-actions">
                    <div class="search-bar">
                        <select v-model="typeFilter" @change="fetchList">
                            <option value="">全部类型</option>
                            <option v-for="item in typeOptions" :key="item.value" :value="item.value">{{
                                    item.label
                                }}
                            </option>
                        </select>
                    </div>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="optionList.length === 0" class="empty-state">
                <div class="empty-icon"><IconGraphic name="tag"/></div>
                <div class="empty-text">暂无选项值数据</div>
            </div>

            <table v-else class="data-table">
                <thead>
                <tr>
                    <th>编号</th>
                    <th>选项类型</th>
                    <th>选项值</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in optionList" :key="item.id">
                    <td>{{ item.id }}</td>
                    <td v-if="editingId !== item.id">
                        <span class="type-tag">{{ typeLabels[item.optionType] || item.optionType || '-' }}</span>
                    </td>
                    <td v-else>
                        <select v-model="editForm.optionType" class="inline-select">
                            <option v-for="opt in typeOptions" :key="opt.value" :value="opt.value">{{
                                    opt.label
                                }}
                            </option>
                        </select>
                    </td>
                    <td v-if="editingId !== item.id"><strong>{{ item.optionValue || '-' }}</strong></td>
                    <td v-else><input v-model="editForm.optionValue" class="inline-input" maxlength="8"/></td>
                    <td v-if="editingId !== item.id">
                        <div class="actions">
                            <button class="btn-outline btn-sm" @click="startEdit(item)">编辑</button>
                            <button class="btn-danger btn-sm" @click="confirmDelete(item)">删除</button>
                        </div>
                    </td>
                    <td v-else>
                        <div class="actions">
                            <button :disabled="saving" class="btn-success btn-sm" @click="saveEdit(item)">保存</button>
                            <button class="btn-outline btn-sm" @click="cancelEdit">取消</button>
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
                        <div class="confirm-msg">确定要删除"{{ deleteTarget?.optionValue }}"吗？</div>
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
import {useToastStore} from '../../../stores/toastStore.js'
import optionValueInterface from '../../../axios/interface/OptionValueInterface.js'
import {OPTION_TYPE_LABELS, OPTION_TYPE_OPTIONS} from '../../../constants/optionType.js'

const toast = useToastStore()
const typeOptions = OPTION_TYPE_OPTIONS
const typeLabels = OPTION_TYPE_LABELS

const optionList = ref([])
const typeFilter = ref('')
const loading = ref(true)

const editingId = ref(null)
const saving = ref(false)
const editForm = ref({optionType: '', optionValue: ''})
const editOrigin = ref(null)

onMounted(() => fetchList())

async function fetchList() {
    loading.value = true
    try {
        if (typeFilter.value) {
            optionList.value = await optionValueInterface.searchListByType(typeFilter.value)
        } else {
            optionList.value = await optionValueInterface.searchList()
        }
    } catch {
        optionList.value = []
    } finally {
        loading.value = false
    }
}

function startEdit(item) {
    editingId.value = item.id
    editOrigin.value = {...item}
    editForm.value = {optionType: item.optionType || '', optionValue: item.optionValue || ''}
}

function cancelEdit() {
    if (editOrigin.value) {
        const idx = optionList.value.findIndex(o => o.id === editingId.value)
        if (idx !== -1) optionList.value[idx] = {...editOrigin.value}
    }
    editingId.value = null
    editOrigin.value = null
}

async function saveEdit(item) {
    if (!editForm.value.optionType || !editForm.value.optionValue) {
        toast.warning('请填写完整信息');
        return
    }
    saving.value = true
    try {
        await optionValueInterface.update(editingId.value, {
            optionType: editForm.value.optionType,
            optionValue: editForm.value.optionValue
        })
        // 后端已返回提示
        await fetchList()
        editingId.value = null
        editOrigin.value = null
    } catch {
    } finally {
        saving.value = false
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
        await optionValueInterface.softDelete(deleteTarget.value.id)
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

.option-manage > .card {
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.type-tag {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 999px;
    font-size: var(--font-sm);
    background: var(--primary-light);
    color: var(--primary);
    font-weight: 600;
}

.inline-input {
    width: 120px;
    height: 32px;
    padding: 0 8px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    font-size: 13px;
    outline: none
}

.inline-input:focus {
    border-color: #14b8a6;
    box-shadow: 0 0 0 3px rgba(20, 184, 166, .12)
}

.inline-select {
    width: 120px;
    height: 32px;
    padding: 0 22px 0 8px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    font-size: 13px;
    appearance: none;
    cursor: pointer;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2364807e' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 8px center;
    background-color: #fff
}

.inline-select:focus {
    border-color: #14b8a6
}

tr:has(.inline-input) {
    background: #f4fbf9 !important
}

@media (max-width: 760px) {
    .header-actions {
        align-items: stretch;
        flex-direction: column;
    }

    .header-actions .search-bar,
    .header-actions select {
        width: 100%;
    }
}
</style>
