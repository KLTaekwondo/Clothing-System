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
.modal-overlay {
    position: fixed;
    inset: 0;
    z-index: 1000;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.35);
}

.draft-modal {
    width: 520px;
    max-height: 560px;
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
    overflow-y: auto;
    padding: 8px;
}

.draft-order-item {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 13px 14px;
    border-bottom: 1px solid #edf4f2;
    border-radius: 0;
    background: #fff;
    color: #0f172a;
}

.draft-order-item:hover {
    background: #f0fdfb;
}

.draft-order-main {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 3px;
}

.draft-order-main span {
    color: #94a3b8;
    font-size: 12px;
}

.draft-order-price {
    color: #0d9488;
    font-size: 16px;
    font-weight: 800;
}

.modal-empty {
    display: flex;
    justify-content: center;
    padding: 40px 0;
}
</style>
