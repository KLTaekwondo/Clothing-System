<template>
    <div class="status-filter-toolbar">
        <div class="status-filter-tabs">
            <button
                v-for="tab in tabs"
                :key="tab.value"
                :class="tab.value === modelValue ? 'status-filter-tab-active' : 'status-filter-tab'"
                type="button"
                @click="emit('update:modelValue', tab.value)"
            >{{ tab.label }}</button>
        </div>
        <input
            :placeholder="searchPlaceholder"
            :value="search"
            class="status-filter-search"
            type="text"
            @input="emit('update:search', $event.target.value)"
        />
    </div>
</template>

<script setup>
defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    tabs: {
        type: Array,
        default: () => []
    },
    search: {
        type: String,
        default: ''
    },
    searchPlaceholder: {
        type: String,
        default: '搜索'
    }
})

const emit = defineEmits(['update:modelValue', 'update:search'])
</script>

<style scoped>
.status-filter-toolbar {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
}

.status-filter-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.status-filter-tab,
.status-filter-tab-active {
    padding: 7px 15px;
    border-radius: 999px;
    color: var(--text-secondary);
    background: transparent;
    font-size: var(--font-sm);
}

.status-filter-tab:hover,
.status-filter-tab-active {
    color: var(--primary);
    background: var(--primary-light);
}

.status-filter-tab-active {
    font-weight: 700;
}

.status-filter-search {
    width: 240px;
    height: 40px;
    padding: 0 18px;
    border: 1px solid var(--border-strong);
    border-radius: 999px;
    background: var(--bg-card);
    font-size: 13px;
    outline: none;
    transition: var(--transition);
}

.status-filter-search:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.status-filter-search::placeholder {
    color: var(--text-muted);
}

@media (max-width: 900px) {
    .status-filter-toolbar {
        align-items: stretch;
        flex-direction: column;
    }

    .status-filter-search {
        width: 100%;
    }
}
</style>
