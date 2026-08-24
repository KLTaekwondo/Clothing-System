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
.employee-modal {
    width: 440px;
    max-height: 520px;
    display: flex;
    flex-direction: column;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    overflow: hidden;
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
    background: var(--bg-subtle);
}

.employee-option {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 14px;
    border: 1px solid var(--border-light);
    border-radius: var(--radius-md);
    background: var(--bg-card);
    color: var(--text);
}

.employee-option:hover {
    border-color: var(--border-hover);
    background: var(--bg-hover);
}

.employee-option.active {
    border-color: var(--primary);
    background: var(--bg-active);
    color: var(--primary-dark);
}

.employee-option-name {
    font-size: 14px;
    font-weight: 700;
}

.employee-option-code {
    padding: 5px 9px;
    border-radius: var(--radius-sm);
    background: var(--primary-light);
    color: var(--primary-dark);
    font-family: var(--mono);
    font-size: 15px;
    font-weight: 700;
}

.employee-option.active .employee-option-code {
    background: var(--bg-active);
    color: var(--primary-dark);
}
</style>
