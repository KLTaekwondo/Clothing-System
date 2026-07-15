<template>
    <div class="option-manage">
        <div class="card">
            <div class="card-header">
                <span class="card-title">选项值管理</span>
                <div class="header-actions">
                    <div class="search-bar">
                        <select v-model="typeFilter" @change="fetchList">
                            <option value="">全部类型</option>
                            <option v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                        </select>
                    </div>
                    <button class="btn-primary" @click="openAdd">+ 添加选项值</button>
                </div>
            </div>

            <div v-if="loading" class="loading-overlay">
                <div class="loading-spinner"></div>
            </div>

            <div v-else-if="optionList.length === 0" class="empty-state">
                <div class="empty-icon">🏷️</div>
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
                        <td>
                            <span class="type-tag">{{ typeLabels[item.optionType] || item.optionType || '-' }}</span>
                        </td>
                        <td><strong>{{ item.optionValue || '-' }}</strong></td>
                        <td>
                            <div class="actions">
                                <button class="btn-outline btn-sm" @click="openEdit(item)">编辑</button>
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
                    <span class="modal-title">{{ isEditing ? '编辑选项值' : '添加选项值' }}</span>
                    <button class="modal-close" @click="closeForm">&times;</button>
                </div>
                <form class="modal-body" @submit.prevent="handleSubmit">
                    <div class="form-group">
                        <label>选项类型</label>
                        <select v-model="form.optionType" required>
                            <option value="" disabled>请选择选项类型</option>
                            <option v-for="item in typeOptions" :key="item.value" :value="item.value">{{ item.label }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>选项值</label>
                        <input
                            v-model="form.optionValue"
                            type="text"
                            maxlength="8"
                            placeholder="例如: 红色、XL、棉"
                            required
                        />
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

        <!-- 删除确认 -->
        <div v-if="showDelete" class="modal-overlay" @click.self="showDelete = false">
            <div class="modal-content" style="min-width: 360px;">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon">⚠️</div>
                        <div class="confirm-msg">确定要删除"{{ deleteTarget?.optionValue }}"吗？</div>
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
import optionValueInterface from '../../axios/interface/OptionValueInterface.js'
import { OPTION_TYPE_OPTIONS, OPTION_TYPE_LABELS } from '../../constants/optionType.js'

const toast = useToastStore()
const typeOptions = OPTION_TYPE_OPTIONS
const typeLabels = OPTION_TYPE_LABELS

const optionList = ref([])
const typeFilter = ref('')
const loading = ref(true)

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

// 表单
const showForm = ref(false)
const isEditing = ref(false)
const submitting = ref(false)
const form = ref({ optionType: '', optionValue: '' })

function openAdd() {
    isEditing.value = false
    form.value = { optionType: '', optionValue: '' }
    showForm.value = true
}

function openEdit(item) {
    isEditing.value = true
    form.value = {
        id: item.id,
        optionType: item.optionType || '',
        optionValue: item.optionValue || ''
    }
    showForm.value = true
}

function closeForm() {
    showForm.value = false
}

async function handleSubmit() {
    if (!form.value.optionType || !form.value.optionValue) {
        toast.warning('请填写完整信息')
        return
    }
    submitting.value = true
    try {
        if (isEditing.value) {
            await optionValueInterface.update(form.value.id, {
                optionType: form.value.optionType,
                optionValue: form.value.optionValue
            })
            toast.success('选项值更新成功')
        } else {
            await optionValueInterface.create({
                optionType: form.value.optionType,
                optionValue: form.value.optionValue
            })
            toast.success('选项值添加成功')
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
        await optionValueInterface.softDelete(deleteTarget.value.id)
        toast.success('选项值已删除')
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
    max-width: 900px;
}

.header-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.type-tag {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 12px;
    font-size: var(--font-sm);
    background: var(--primary-light);
    color: var(--primary);
}
</style>
