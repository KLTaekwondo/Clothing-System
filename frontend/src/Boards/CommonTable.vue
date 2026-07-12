<template>
    <div class="table-container">
        <!-- 简洁的排序栏 -->
        <div class="sort-bar">
            <div class="sort-control">
                <span class="sort-label">排序方式：</span>
                <select v-model="sortBy" class="sort-select">
                    <option v-for="column in columns" :key="column.Id" :value="column.Id">
                        {{ column.Label }}
                    </option>
                </select>
                
                <button 
                    @click="toggleSortOrder" 
                    class="sort-order-btn"
                    :class="{ 'active': sortOrder === 'asc' }"
                >
                    {{ sortOrder === 'asc' ? '↑ 升序' : '↓ 降序' }}
                </button>
            </div>
            
            <!-- 数据统计 -->
            <div class="data-info">
                共 {{ tableData.length }} 条数据
                <span v-if="selectedRow" class="selected-info"> | 已选中: {{ getSelectedDisplayText() }}</span>
            </div>
        </div>

        <!-- 表格主体 -->
        <div class="table-wrapper">
            <table class="data-table">
                <thead>
                    <tr>
                        <th v-for="column in columns" :key="column.Id">{{ column.Label }}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="sortedData.length === 0">
                        <td :colspan="columns.length" class="no-data">
                            暂无数据
                        </td>
                    </tr>
                    <tr v-else 
                        v-for="item in sortedData" 
                        :key="getRowKey(item)"
                        @click="selectRow(item)"
                        :class="{ 'selected': isRowSelected(item) }"
                        class="table-row"
                    >
                        <td v-for="column in columns" :key="column.Id">
                            <template v-if="isStatusColumn(column.Id)">
                                <span class="status-badge" :class="getStatusClass(item[column.Id])">
                                    {{ formatStatus(item[column.Id]) }}
                                </span>
                            </template>
                            <template v-else>
                                {{ item[column.Id] }}
                            </template>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
    tableData: {
        type: Array,
        default: () => []
    },
    columns: {
        type: Array,
        default: () => []
    },
    primaryKey: {
        type: String,
        default: '' // 可选：明确指定主键字段
    },
    displayField: {
        type: String,
        default: '' // 可选：明确指定显示字段
    }
})

// 排序状态
const sortBy = ref('')
const sortOrder = ref('asc') // 'asc' or 'desc'
const emit = defineEmits(['rowClick'])

// 选中状态
const selectedRow = ref(null)

// 初始化排序字段
watch(() => props.columns, (newColumns) => {
    if (newColumns.length > 0 && !sortBy.value) {
        sortBy.value = newColumns[0]?.Id || ''
    }
}, { immediate: true })

// 排序逻辑
const sortedData = computed(() => {
    if (!props.tableData || props.tableData.length === 0 || !sortBy.value) {
        return props.tableData || []
    }
    
    return [...props.tableData].sort((a, b) => {
        const aVal = a[sortBy.value]
        const bVal = b[sortBy.value]
        
        // 处理undefined或null值
        if (aVal == null && bVal == null) return 0
        if (aVal == null) return 1
        if (bVal == null) return -1
        
        if (sortOrder.value === 'asc') {
            return aVal < bVal ? -1 : aVal > bVal ? 1 : 0
        } else {
            return aVal > bVal ? -1 : aVal < bVal ? 1 : 0
        }
    })
})

// 切换排序顺序
const toggleSortOrder = () => {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
}

// 判断行是否被选中
const isRowSelected = (item) => {
    if (!selectedRow.value) return false
    
    const currentRowKey = getRowKey(item)
    const selectedRowKey = getRowKey(selectedRow.value)
    
    return currentRowKey === selectedRowKey
}

// 选中行
const selectRow = (item) => {
    // 如果点击的是已选中的行，则取消选中
    if (selectedRow.value && getRowKey(item) === getRowKey(selectedRow.value)) {
        selectedRow.value = null
        emit('rowClick', null)
    } else {
        selectedRow.value = item
        emit('rowClick', selectedRow.value)
    }
}

// 动态获取行的key值
const getRowKey = (item) => {
    if (!item) return ''
    
    // 1. 如果明确指定了主键字段，优先使用
    if (props.primaryKey && item[props.primaryKey]) {
        return item[props.primaryKey]
    }
    
    // 2. 尝试从columns中查找包含"Id"的字段
    const idColumn = props.columns.find(col => 
        col.Id && (col.Id.toLowerCase().includes('id') || 
        col.Id.toLowerCase().endsWith('id'))
    )
    
    if (idColumn && item[idColumn.Id]) {
        return item[idColumn.Id]
    }
    
    // 3. 如果没有找到ID字段，尝试查找常见的ID字段名
    const commonIdFields = ['id', 'Id', 'ID', '_id', 'uuid', 'key']
    for (const field of commonIdFields) {
        if (item[field]) {
            return item[field]
        }
    }
    
    // 4. 如果都没有找到，使用JSON字符串作为fallback
    return JSON.stringify(item)
}

// 获取选中行的显示文本
const getSelectedDisplayText = () => {
    if (!selectedRow.value) return ''
    
    // 1. 如果明确指定了显示字段，优先使用
    if (props.displayField && selectedRow.value[props.displayField]) {
        return selectedRow.value[props.displayField]
    }
    
    // 2. 尝试查找名称字段
    const nameColumn = props.columns.find(col => 
        col.Id && (col.Id.toLowerCase().includes('name') || 
        col.Id.toLowerCase().endsWith('name'))
    )
    
    if (nameColumn && selectedRow.value[nameColumn.Id]) {
        return selectedRow.value[nameColumn.Id]
    }
    
    // 3. 使用主键作为显示文本
    return getRowKey(selectedRow.value)
}

// 判断是否为状态列
const isStatusColumn = (columnId) => {
    return columnId && columnId.toLowerCase().includes('status')
}

// 获取状态样式类
const getStatusClass = (status) => {
    if (status === true || status === 'true' || status === '启用' || status === 1) {
        return 'active'
    }
    return 'inactive'
}

// 格式化状态显示
const formatStatus = (status) => {
    if (status === true || status === 'true' || status === 1) {
        return '启用'
    }
    if (status === false || status === 'false' || status === 0) {
        return '禁用'
    }
    return status
}
</script>

<style scoped>
.table-container {
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    overflow: hidden;
}

/* 排序栏样式 */
.sort-bar {
    padding: 16px 20px;
    background: linear-gradient(135deg, #f8f9ff 0%, #f0f2ff 100%);
    border-bottom: 1px solid #eef0ff;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.sort-control {
    display: flex;
    align-items: center;
    gap: 12px;
}

.sort-label {
    font-size: 14px;
    color: #555;
    font-weight: 500;
}

.sort-select {
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 6px;
    background: white;
    font-size: 14px;
    outline: none;
    transition: all 0.2s ease;
}

.sort-select:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

.sort-order-btn {
    padding: 8px 16px;
    background: white;
    border: 1px solid #ddd;
    border-radius: 6px;
    cursor: pointer;
    font-size: 14px;
    transition: all 0.2s ease;
}

.sort-order-btn:hover {
    background: #f8f9ff;
    border-color: #667eea;
}

.sort-order-btn.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
}

.data-info {
    font-size: 14px;
    color: #666;
    font-weight: 500;
}

.selected-info {
    color: #1890ff;
    font-weight: 600;
}

/* 表格样式 */
.table-wrapper {
    overflow-x: auto;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
}

.data-table th {
    background: #f8f9fa;
    padding: 16px 12px;
    text-align: left;
    font-weight: 600;
    color: #333;
    border-bottom: 2px solid #e9ecef;
    font-size: 14px;
}

.data-table td {
    padding: 14px 12px;
    border-bottom: 1px solid #f0f0f0;
    transition: background 0.2s ease;
}

/* 表格行样式 */
.table-row {
    cursor: pointer;
    transition: all 0.2s ease;
}

.table-row:hover td {
    background: #f8f9ff;
}

/* 选中行样式 */
.table-row.selected td {
    background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
    border-left: 3px solid #1890ff;
    font-weight: 600;
}

.table-row.selected:hover td {
    background: linear-gradient(135deg, #d4f0ff 0%, #e6f7ff 100%);
}

/* 空数据样式 */
.no-data {
    text-align: center;
    color: #999;
    font-style: italic;
    padding: 40px;
}

/* 状态标签 */
.status-badge {
    padding: 4px 8px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 500;
}

.status-badge.active {
    background: #e7f7ef;
    color: #0ca678;
}

.status-badge.inactive {
    background: #fff5f5;
    color: #fa5252;
}
</style>