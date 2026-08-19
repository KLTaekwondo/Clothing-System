<template>
    <div
        v-if="visible"
        class="modal-overlay"
        @click.self="$emit('cancel')"
    >
        <div class="confirm-modal">
            <div class="modal-header">
                <div class="modal-heading">
                    <span
                        v-if="icon"
                        class="modal-title-icon"
                    >
                        <IconGraphic :name="icon"/>
                    </span>
                    <span class="modal-title">{{ title }}</span>
                </div>
                <button
                    class="modal-close"
                    @click="$emit('cancel')"
                >&times;</button>
            </div>
            <div
                v-if="message || details.length > 0"
                class="confirm-body"
            >
                <div
                    v-if="message"
                    class="confirm-message"
                >{{ message }}</div>
                <div
                    v-if="details.length > 0"
                    class="confirm-details"
                >
                    <div
                        v-for="detail in details"
                        :key="detail.label"
                        class="confirm-detail"
                    >
                        <span class="detail-label">{{ detail.label }}</span>
                        <strong :class="detail.emphasis ? 'detail-value-emphasis' : 'detail-value'">
                            {{ detail.value }}
                        </strong>
                    </div>
                </div>
            </div>
            <div class="confirm-actions">
                <button
                    class="confirm-cancel"
                    @click="$emit('cancel')"
                >取消</button>
                <button
                    class="confirm-submit"
                    @click="$emit('confirm')"
                >{{ confirmText }}</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import IconGraphic from '../IconGraphic.vue'

defineProps({
    visible: Boolean,
    title: {
        type: String,
        default: ''
    },
    message: {
        type: String,
        default: ''
    },
    icon: {
        type: String,
        default: ''
    },
    details: {
        type: Array,
        default: () => []
    },
    confirmText: {
        type: String,
        default: '确定'
    }
})

defineEmits(['cancel', 'confirm'])
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

.confirm-modal {
    width: 400px;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 12px 36px rgba(0, 0, 0, 0.18);
    overflow: hidden;
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e3efed;
}

.modal-heading {
    display: flex;
    align-items: center;
    gap: 10px;
}

.modal-title-icon {
    width: 34px;
    height: 34px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 8px;
    background: #e8f5f2;
}

.modal-title-icon :deep(img) {
    width: 20px;
    height: 20px;
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

.confirm-body {
    padding: 22px 20px 10px;
    color: #475569;
    font-size: 14px;
    line-height: 1.7;
}

.confirm-message {
    width: 100%;
}

.confirm-details {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 2px;
    padding: 6px 0;
    border: 1px solid #dceae7;
    border-radius: 10px;
    background: #f8fbfa;
}

.confirm-detail {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 20px;
    min-height: 42px;
    padding: 0 16px;
}

.detail-label {
    color: #64748b;
}

.detail-value,
.detail-value-emphasis {
    color: #0f172a;
    font-size: 15px;
}

.detail-value-emphasis {
    color: #d97706;
    font-size: 20px;
}

.confirm-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 14px 20px 18px;
}

.confirm-cancel,
.confirm-submit {
    min-width: 96px;
    height: 38px;
    border-radius: 8px;
    font-weight: 700;
    cursor: pointer;
}

.confirm-cancel {
    border: 1px solid #dceae7;
    background: #fff;
    color: #475569;
}

.confirm-submit {
    border: none;
    background: #0f766e;
    color: #fff;
}
</style>
