<template>
    <div class="pagination-bar">
        <span class="page-info">
            第 {{ page + 1 }} / {{ safeTotalPages }} 页，共 {{ totalElements }} 条
        </span>
        <div class="page-actions">
            <button
                :disabled="loading || page <= 0"
                class="btn-outline"
                @click="$emit('change', page - 1)"
            >上一页</button>
            <button
                :disabled="loading || page >= safeTotalPages - 1"
                class="btn-outline"
                @click="$emit('change', page + 1)"
            >下一页</button>
        </div>
    </div>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
    page: {
        type: Number,
        default: 0
    },
    totalPages: {
        type: Number,
        default: 0
    },
    totalElements: {
        type: Number,
        default: 0
    },
    loading: Boolean
})

defineEmits(['change'])

const safeTotalPages = computed(() => Math.max(props.totalPages || 1, 1))
</script>

<style scoped>
.pagination-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 14px 16px;
    border-top: 1px solid var(--border-light);
}

.page-info {
    color: var(--text-secondary);
    font-size: 13px;
}

.page-actions {
    display: flex;
    gap: 8px;
}

.btn-outline {
    height: 30px;
    padding: 4px 12px;
    background: #fff;
    color: var(--text-secondary);
    border: 1px solid var(--border);
    border-radius: 10px;
    font-size: var(--font-sm);
}

.btn-outline:hover:not(:disabled) {
    color: var(--primary);
    border-color: var(--primary);
    background: var(--primary-light);
}

.btn-outline:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}
</style>
