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
    justify-content: space-between;
    gap: 16px;
    padding: 10px 0 16px;
    border-bottom: 1px solid var(--border-light);
}

.status-filter-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
}

.status-filter-tab,
.status-filter-tab-active {
    padding: 7px 13px;
    border-radius: 8px;
    color: var(--text-secondary);
    background: transparent;
    font-size: 13px;
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
    width: 280px;
    height: 36px;
}

@media (max-width: 900px) {
    .status-filter-toolbar {
        align-items: flex-start;
        flex-direction: column;
    }

    .status-filter-search {
        width: 100%;
    }
}
</style>
