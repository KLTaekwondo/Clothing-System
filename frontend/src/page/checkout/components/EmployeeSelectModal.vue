<template>
    <div
        v-if="visible"
        class="modal-overlay"
        @click.self="$emit('close')"
    >
        <div class="employee-modal">
            <div class="modal-header">
                <span class="modal-title">选择收银员工</span>
                <button
                    class="modal-close"
                    @click="$emit('close')"
                >&times;</button>
            </div>
            <div class="employee-list">
                <input
                    ref="searchField"
                    v-model="query"
                    class="employee-search"
                    placeholder="输入员工姓名或编码"
                    type="text"
                    @keydown.enter="selectOnlyEmployee"
                />
                <button
                    v-for="employee in filteredEmployees"
                    :key="employee.id"
                    :class="{ active: employee.id === selectedEmployeeId }"
                    class="employee-option"
                    @click="selectEmployee(employee)"
                >
                    <span class="employee-option-name">{{ employee.name }}</span>
                    <span class="employee-option-code">{{ employee.code }}</span>
                </button>
                <div
                    v-if="filteredEmployees.length === 0"
                    class="modal-empty"
                >
                    <div class="empty-text">
                        {{ employees.length === 0 ? '暂无可选员工' : '没有匹配的员工' }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, nextTick, ref, watch} from 'vue'

const props = defineProps({
    visible: Boolean,
    employees: {
        type: Array,
        default: () => []
    },
    selectedEmployeeId: {
        type: [String, Number],
        default: ''
    }
})

const emit = defineEmits(['close', 'select'])
const query = ref('')
const searchField = ref(null)

const filteredEmployees = computed(() => {
    const keyword = query.value.trim().toLowerCase()
    if (!keyword) return props.employees
    return props.employees.filter(employee => {
        return employee.name?.toLowerCase().includes(keyword) ||
            employee.code?.toLowerCase().includes(keyword)
    })
})

watch(() => props.visible, async visible => {
    if (!visible) return
    query.value = ''
    await nextTick()
    searchField.value?.focus()
})

function selectEmployee(employee) {
    emit('select', employee)
}

function selectOnlyEmployee() {
    if (filteredEmployees.value.length === 1) {
        selectEmployee(filteredEmployees.value[0])
    }
}
</script>

<style scoped>
.modal-overlay {
    position: fixed;
    inset: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
}

.employee-modal {
    width: 440px;
    max-height: 520px;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e3efed;
}

.modal-title {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
}

.modal-close {
    padding: 0;
    border: none;
    background: none;
    font-size: 24px;
    color: #94a3b8;
    cursor: pointer;
    line-height: 1;
}

.modal-close:hover {
    color: #ef4444;
}

.employee-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
    overflow-y: auto;
    padding: 8px;
}

.employee-search {
    width: 100%;
    height: 40px;
    margin-bottom: 6px;
    background: #fbfefd;
}

.employee-option {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border: 1px solid #e3efed;
    border-radius: 10px;
    background: #fff;
    color: #0f172a;
}

.employee-option:hover {
    border-color: #8bd5ca;
    background: #f0fdfb;
}

.employee-option.active {
    border-color: #0d9488;
    background: #ccfbf1;
    color: #0f766e;
}

.employee-option-name {
    font-size: 14px;
    font-weight: 700;
}

.employee-option-code {
    padding: 5px 9px;
    border-radius: 6px;
    background: #e8f5f2;
    color: #0f766e;
    font-family: monospace;
    font-size: 15px;
    font-weight: 700;
}

.employee-option.active .employee-option-code {
    background: #99f6e4;
    color: #115e59;
}

.modal-empty {
    display: flex;
    justify-content: center;
    padding: 40px 0;
}
</style>
