<template>
    <div
        v-if="visible"
        class="modal-overlay"
        @click.self="$emit('close')"
    >
        <div class="draft-modal">
            <div class="modal-header">
                <span class="modal-title">选择挂单</span>
                <button
                    class="modal-close"
                    @click="$emit('close')"
                >&times;</button>
            </div>
            <div class="modal-body">
                <button
                    v-for="order in orders"
                    :key="order.id"
                    class="draft-order-item"
                    @click="$emit('select', order)"
                >
                    <span class="draft-order-main">
                        <strong>{{ order.orderNo }}</strong>
                        <span>{{ order.employeeName || '未指定员工' }}</span>
                    </span>
                    <span class="draft-order-price">¥{{ order.actualPrice ?? '0.00' }}</span>
                </button>
                <div
                    v-if="orders.length === 0"
                    class="modal-empty"
                >
                    <div class="empty-text">暂无挂单</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    visible: Boolean,
    orders: {
        type: Array,
        default: () => []
    }
})

defineEmits(['close', 'select'])
</script>

<style scoped>
.draft-modal {
    width: 520px;
    max-height: 560px;
    display: flex;
    flex-direction: column;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    overflow: hidden;
}

.modal-body {
    overflow-y: auto;
    padding: 8px;
}

.draft-order-item {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 13px 14px;
    border-bottom: 1px solid var(--border-light);
    border-radius: 0;
    background: var(--bg-card);
    color: var(--text);
}

.draft-order-item:hover {
    background: var(--bg-hover);
}

.draft-order-main {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 3px;
}

.draft-order-main span {
    color: var(--text-muted);
    font-size: 12px;
}

.draft-order-price {
    color: var(--primary);
    font-size: 16px;
    font-weight: 800;
}
</style>
