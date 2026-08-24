<template>
    <div
        v-if="visible"
        class="modal-overlay"
        @click.self="$emit('close')"
    >
        <div class="sku-modal">
            <div class="modal-header">
                <span class="modal-title">选择 SKU - {{ productName }}</span>
                <button
                    class="modal-close"
                    @click="$emit('close')"
                >&times;</button>
            </div>
            <div class="modal-body">
                <button
                    v-for="sku in results"
                    :key="sku.id"
                    ref="optionFields"
                    class="sku-modal-item"
                    type="button"
                    @click="$emit('select', sku)"
                >
                    <span class="sku-modal-name">{{ sku.name || sku.code }}</span>
                    <span class="sku-modal-code">{{ sku.code }}</span>
                </button>
                <div
                    v-if="results.length === 0"
                    class="modal-empty"
                >
                    <div class="empty-text">未找到匹配的商品</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {nextTick, ref, watch} from 'vue'

const props = defineProps({
    visible: Boolean,
    productName: {
        type: String,
        default: ''
    },
    results: {
        type: Array,
        default: () => []
    }
})

defineEmits(['close', 'select'])

const optionFields = ref([])

watch(() => props.visible, async visible => {
    if (!visible) return
    await nextTick()
    optionFields.value[0]?.focus()
})
</script>

<style scoped>
.sku-modal {
    width: 400px;
    max-height: 500px;
    display: flex;
    flex-direction: column;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    overflow: hidden;
}

.modal-body {
    flex-grow: 1;
    overflow-y: auto;
    padding: 8px;
}

.sku-modal-item {
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 14px;
    border: none;
    border-radius: var(--radius-md);
    background: var(--bg-card);
    cursor: pointer;
    transition: background 0.15s;
    text-align: left;
}

.sku-modal-item:hover,
.sku-modal-item:focus {
    background: var(--bg-hover);
}

.sku-modal-name {
    font-weight: 600;
    font-size: 14px;
    color: var(--text);
}

.sku-modal-code {
    font-size: 13px;
    color: var(--text-muted);
    font-family: var(--mono);
}
</style>
