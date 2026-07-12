<template>
    <div class="modal-overlay" @click.self="handleClose">
        <div class="add-panel">
            <div class="panel-header">
                <h2 class="panel-title">{{ title }}</h2>
                <button class="close-btn" @click="handleClose">×</button>
            </div>

            <div class="panel-content">
                <form @submit.prevent="handleSubmit" class="add-form">
                    <div 
                        class="form-group" 
                        v-for="column in columns" 
                        :key="column.Id"
                    >
                        <label class="form-label">{{ column.Label }} {{ column.required ? '*' : '' }}</label>
                        
                        <!-- 文本输入框 -->
                        <input
                            v-if="getFieldType(column) === 'text'"
                            v-model="formData[column.Id]"
                            type="text"
                            class="form-input"
                            :placeholder="`请输入${column.Label}`"
                            :disabled="isDisabledField(column)"
                            :required="column.required"
                        />
                        
                        <!-- 下拉选择框 -->
                        <select
                            v-else-if="getFieldType(column) === 'select'"
                            v-model="formData[column.Id]"
                            class="form-input"
                            :placeholder="`请选择${column.Label}`"
                            :required="column.required"
                        >
                            <option v-for="option in getSelectOptions(column)" 
                                    :key="option.value" 
                                    :value="option.value">
                                {{ option.label }}
                            </option>
                        </select>
                        
                        <!-- 密码输入框 -->
                        <input
                            v-else-if="getFieldType(column) === 'password'"
                            v-model="formData[column.Id]"
                            type="password"
                            class="form-input"
                            :placeholder="`请输入${column.Label}`"
                            :required="column.required"
                        />
                    </div>

                    <div class="form-actions">
                        <button type="button" class="btn btn-cancel" @click="handleClose">取消</button>
                        <button type="submit" class="btn btn-submit" :disabled="loading">
                            {{ loading ? submitLoadingText : submitText }}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'

const props = defineProps({
    // 表单模式：'create' 或 'edit'
    mode: {
        type: String,
        default: 'create',
        validator: (value) => ['create', 'edit'].includes(value)
    },
    // 表单列配置
    columns: {
        type: Array,
        default: () => []
    },
    // 编辑模式下的实体ID
    entityId: {
        type: String,
        default: ''
    },
    // 标题前缀（如：销售员、仓库）
    entityName: {
        type: String,
        default: '数据'
    },
    // 数据获取函数（编辑模式使用）
    fetchDataFn: {
        type: Function,
        default: null
    },
    // 字段类型映射
    fieldTypes: {
        type: Object,
        default: () => ({})
    }
})

const emit = defineEmits(['close', 'success'])

const loading = ref(false)
const formData = reactive({})

// 计算属性
const title = computed(() => {
    return props.mode === 'create' 
        ? `添加新${props.entityName}` 
        : `编辑${props.entityName}信息`
})

const submitText = computed(() => {
    return props.mode === 'create' ? '确认添加' : '确认更新'
})

const submitLoadingText = computed(() => {
    return props.mode === 'create' ? '添加中...' : '更新中...'
})

// 方法
const getFieldType = (column) => {
    // 优先使用自定义字段类型映射
    if (props.fieldTypes[column.Id]) {
        return props.fieldTypes[column.Id]
    }
    
    // 默认逻辑
    if (column.Id.includes('Status') || column.Id.includes('status')) {
        return 'select'
    }
    if (column.Id.includes('Password') || column.Id.includes('password')) {
        return 'password'
    }
    return 'text'
}

const getSelectOptions = (column) => {
    if (column.Id.includes('Status') || column.Id.includes('status')) {
        return [
            { value: true, label: '启用' },
            { value: false, label: '禁用' }
        ]
    }
    return column.options || []
}

const isDisabledField = (column) => {
    // 编辑模式下，ID字段通常不可编辑
    return props.mode === 'edit' && (column.Id.includes('Id') || column.Id.includes('id'))
}

const handleClose = () => emit('close')

const handleSubmit = async () => {
    loading.value = true
    try {
        await emit('success', formData)
        handleClose()
    } catch (error) {
        console.error(`${props.mode === 'create' ? '添加' : '更新'}${props.entityName}失败:`, error)
        alert(`${props.mode === 'create' ? '添加' : '更新'}${props.entityName}失败，请重试`)
    } finally {
        loading.value = false
    }
}

// 初始化表单数据
const initFormData = () => {
    props.columns.forEach(column => {
        const defaultValue = column.defaultValue !== undefined 
            ? column.defaultValue 
            : getFieldType(column) === 'select' ? true : ''
        formData[column.Id] = defaultValue
    })
}

// 编辑模式下加载数据
const loadEditData = async () => {
    if (props.mode === 'edit' && props.entityId && props.fetchDataFn) {
        try {
            const response = await props.fetchDataFn(props.entityId)
            Object.keys(response).forEach(key => {
                if (formData.hasOwnProperty(key)) {
                    formData[key] = response[key]
                }
            })
        } catch (error) {
            console.error(`加载${props.entityName}数据失败:`, error)
            throw error
        }
    }
}

onMounted(async () => {
    initFormData()
    if (props.mode === 'edit') {
        await loadEditData()
    }
})
</script>

<style scoped>
.modal-overlay {
    position: absolute;
    top: 0; right: 0; bottom: 0;
    margin: 5px; width: 400px;
    background: rgba(0, 0, 0, 0.1);
    border-radius: 12px; z-index: 10;
}

.add-panel {
    height: 100%;
    background: white;
    border-radius: 12px;
    display: flex; flex-direction: column;
    box-shadow: -2px 0 12px rgba(0, 0, 0, 0.1);
}

.panel-header {
    background: linear-gradient(135deg, #1890ff, #52c41a);
    border-radius: 12px 12px 0 0;
    padding: 20px;
    display: flex; justify-content: space-between; align-items: center;
}

.panel-title {
    font-size: 20px; font-weight: 700;
    color: white; margin: 0;
}

.close-btn {
    background: none; border: none;
    color: white; font-size: 24px;
    cursor: pointer; padding: 4px;
    border-radius: 4px;
}

.close-btn:hover { background: rgba(255, 255, 255, 0.2); }

.panel-content {
    flex: 1; padding: 24px;
    overflow-y: auto;
}

.add-form {
    display: flex; flex-direction: column;
    gap: 20px;
}

.form-group {
    display: flex; flex-direction: column;
    gap: 8px;
}

.form-label {
    font-size: 14px; font-weight: 600;
    color: #2d3748;
}

.form-input {
    padding: 10px 12px;
    border: 2px solid #e2e8f0;
    border-radius: 6px; font-size: 14px;
    transition: border-color 0.3s ease;
}

.form-input:focus {
    outline: none;
    border-color: #1890ff;
}

.form-input:disabled {
    background-color: #f7fafc;
    cursor: not-allowed;
}

.form-actions {
    display: flex; gap: 12px;
    margin-top: 20px;
}

.btn {
    flex: 1; padding: 10px;
    border: none; border-radius: 6px;
    font-size: 14px; font-weight: 600;
    cursor: pointer;
}

.btn-cancel {
    background: #f7fafc;
    color: #4a5568;
    border: 1px solid #e2e8f0;
}

.btn-submit {
    background: linear-gradient(135deg, #1890ff, #52c41a);
    color: white;
}

.btn-submit:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

@media (max-width: 768px) {
    .modal-overlay {
        position: fixed;
        top: auto; bottom: 0;
        left: 0; right: 0;
        width: 100%; height: 70vh;
    }
}
</style>