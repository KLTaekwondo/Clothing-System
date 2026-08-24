<template>
    <div class="pagination-bar">
        <div class="pager">
            <button
                :disabled="loading || page <= 0"
                class="pager-btn pager-arrow"
                type="button"
                @click="$emit('change', page - 1)"
            >‹</button>
            <template v-for="(item, index) in pageItems" :key="index">
                <button
                    v-if="item === '...'"
                    class="pager-ellipsis"
                    disabled
                    type="button"
                >…</button>
                <button
                    v-else
                    :class="item === page ? 'pager-btn-active' : 'pager-btn'"
                    type="button"
                    @click="$emit('change', item)"
                >{{ item + 1 }}</button>
            </template>
            <button
                :disabled="loading || page >= safeTotalPages - 1"
                class="pager-btn pager-arrow"
                type="button"
                @click="$emit('change', page + 1)"
            >›</button>
        </div>
        <span class="page-info">第 {{ page + 1 }} / {{ safeTotalPages }} 页，共 {{ totalElements }} 条</span>
        <div class="page-jump">
            <input
                v-model="jumpPage"
                class="jump-input"
                max="safeTotalPages"
                min="1"
                placeholder="页码"
                type="number"
                @keyup.enter="goJump"
            />
            <button
                :disabled="loading"
                class="jump-button"
                type="button"
                @click="goJump"
            >跳转</button>
        </div>
    </div>
</template>

<script setup>
import {computed, ref} from 'vue'

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

const emit = defineEmits(['change'])

const safeTotalPages = computed(() => Math.max(props.totalPages || 1, 1))

// 页码序列：当前页前后各 1 页，首尾保留，中间用省略号
const pageItems = computed(() => {
    const total = safeTotalPages.value
    const current = props.page
    if (total <= 7) {
        return Array.from({length: total}, (_, i) => i)
    }
    const items = [0]
    if (current > 2) items.push('...')
    const start = Math.max(1, current - 1)
    const end = Math.min(total - 2, current + 1)
    for (let i = start; i <= end; i++) items.push(i)
    if (current < total - 3) items.push('...')
    items.push(total - 1)
    return items
})

const jumpPage = ref('')

function goJump() {
    const value = Number(jumpPage.value)
    if (!Number.isInteger(value) || value < 1) return
    const target = Math.min(value, safeTotalPages.value) - 1
    if (target !== props.page) emit('change', target)
    jumpPage.value = ''
}
</script>

<style scoped>
.pagination-bar {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    padding: 14px 0;
    border-top: 1px solid var(--border-light);
}

.pager {
    display: flex;
    align-items: center;
    gap: 6px;
}

.pager-btn,
.pager-btn-active {
    min-width: 32px;
    height: 32px;
    padding: 0 8px;
    border: 1px solid var(--border);
    border-radius: 999px;
    background: var(--bg-card);
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: var(--transition);
}

.pager-btn:hover:not(:disabled) {
    border-color: var(--primary);
    color: var(--primary);
}

.pager-btn-active {
    border-color: var(--primary);
    background: var(--primary);
    color: var(--text-invert);
    box-shadow: var(--shadow-primary);
    cursor: default;
}

.pager-btn:disabled {
    cursor: not-allowed;
    opacity: 0.45;
}

.pager-arrow {
    padding: 0 10px;
    font-size: 16px;
}

.pager-ellipsis {
    min-width: 24px;
    height: 32px;
    padding: 0;
    border: none;
    background: none;
    color: var(--text-muted);
    font-size: 13px;
    cursor: default;
}

.page-info {
    color: var(--text-muted);
    font-size: 13px;
    white-space: nowrap;
}

.page-jump {
    display: flex;
    align-items: center;
    gap: 6px;
}

.jump-input {
    width: 64px;
    height: 32px;
    padding: 0 8px;
    border: 1px solid var(--border);
    border-radius: 999px;
    background: var(--bg-card);
    font-size: 13px;
    text-align: center;
    outline: none;
    -moz-appearance: textfield;
}

.jump-input::-webkit-outer-spin-button,
.jump-input::-webkit-inner-spin-button {
    -webkit-appearance: none;
    margin: 0;
}

.jump-input:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.jump-button {
    height: 32px;
    padding: 0 12px;
    border: 1px solid var(--border);
    border-radius: 999px;
    background: var(--bg-card);
    color: var(--text-secondary);
    font-size: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: var(--transition);
}

.jump-button:hover:not(:disabled) {
    border-color: var(--primary);
    color: var(--primary);
}

.jump-button:disabled {
    cursor: not-allowed;
    opacity: 0.5;
}

@media (max-width: 900px) {
    .pagination-bar {
        flex-wrap: wrap;
        gap: 10px;
    }
}

@media (max-width: 560px) {
    .pagination-bar {
        align-items: stretch;
        flex-direction: column;
    }

    .pager {
        justify-content: center;
        flex-wrap: wrap;
    }

    .page-info {
        text-align: center;
    }

    .page-jump {
        justify-content: center;
    }
}
</style>