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
.modal-overlay {
    position: fixed;
    inset: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
}

.sku-modal {
    width: 400px;
    max-height: 500px;
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
    border-radius: 10px;
    background: #fff;
    cursor: pointer;
    transition: background 0.15s;
    text-align: left;
}

.sku-modal-item:hover,
.sku-modal-item:focus {
    background: #f0fdfb;
}

.sku-modal-name {
    font-weight: 600;
    font-size: 14px;
    color: #0f172a;
}

.sku-modal-code {
    font-size: 13px;
    color: #94a3b8;
    font-family: monospace;
}

.modal-empty {
    display: flex;
    justify-content: center;
    padding: 40px 0;
}
</style>
