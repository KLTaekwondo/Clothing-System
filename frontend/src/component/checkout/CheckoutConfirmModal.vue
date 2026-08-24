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
.confirm-modal {
    width: 400px;
    display: flex;
    flex-direction: column;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
    overflow: hidden;
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
    background: var(--primary-light);
}

.modal-title-icon :deep(img) {
    width: 20px;
    height: 20px;
}

.confirm-body {
    padding: 22px 20px 10px;
    color: var(--text-secondary);
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
    border: 1px solid var(--border);
    border-radius: var(--radius-md);
    background: var(--bg-subtle);
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
    color: var(--text-muted);
}

.detail-value,
.detail-value-emphasis {
    color: var(--text);
    font-size: 15px;
}

.detail-value-emphasis {
    color: var(--warning-dark);
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
    border-radius: 999px;
    font-weight: 700;
    cursor: pointer;
}

.confirm-cancel {
    border: 1px solid var(--border);
    background: var(--bg-card);
    color: var(--text-secondary);
}

.confirm-submit {
    border: none;
    background: var(--primary-dark);
    color: var(--text-invert);
}
</style>
