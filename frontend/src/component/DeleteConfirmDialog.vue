<template>
    <Transition name="delete-confirm">
        <div
            v-if="visible"
            class="delete-confirm-overlay"
            @click.self="emit('cancel')"
        >
            <div
                aria-modal="true"
                class="delete-confirm-dialog"
                role="dialog"
            >
                <div class="delete-confirm-body">
                    <div class="delete-confirm-icon">
                        <IconGraphic name="warning"/>
                    </div>
                    <strong>{{ title }}</strong>
                    <span v-if="hint">{{ hint }}</span>
                </div>
                <div class="delete-confirm-footer">
                    <button
                        :disabled="loading"
                        class="delete-confirm-cancel"
                        type="button"
                        @click="emit('cancel')"
                    >{{ cancelText }}</button>
                    <button
                        :disabled="loading"
                        class="delete-confirm-submit"
                        type="button"
                        @click="emit('confirm')"
                    >{{ loading ? loadingText : confirmText }}</button>
                </div>
            </div>
        </div>
    </Transition>
</template>

<script setup>
import IconGraphic from './IconGraphic.vue'

defineProps({
    visible: Boolean,
    title: {
        type: String,
        default: '确定删除吗？'
    },
    hint: {
        type: String,
        default: ''
    },
    loading: Boolean,
    cancelText: {
        type: String,
        default: '取消'
    },
    confirmText: {
        type: String,
        default: '确认删除'
    },
    loadingText: {
        type: String,
        default: '删除中...'
    }
})

const emit = defineEmits(['cancel', 'confirm'])
</script>

<style scoped>
.delete-confirm-overlay {
    position: fixed;
    inset: 0;
    z-index: 1200;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    background: rgba(15, 23, 42, 0.42);
    backdrop-filter: blur(2px);
}

.delete-confirm-dialog {
    width: min(400px, 100%);
    overflow: hidden;
    background: #fff;
    border: 1px solid #dceae7;
    border-radius: 16px;
    box-shadow: 0 22px 60px rgba(15, 23, 42, 0.2);
}

.delete-confirm-body {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 10px;
    padding: 28px 24px 22px;
    color: var(--text);
    text-align: center;
}

.delete-confirm-body span {
    color: var(--text-muted);
    font-size: 13px;
}

.delete-confirm-icon {
    width: 48px;
    height: 48px;
}

.delete-confirm-icon :deep(img) {
    width: 100%;
    height: 100%;
}

.delete-confirm-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding: 14px 20px;
    background: #f8fbfa;
    border-top: 1px solid var(--border-light);
}

.delete-confirm-cancel,
.delete-confirm-submit {
    min-width: 92px;
    height: 38px;
    padding: 0 16px;
    border-radius: 8px;
    font-weight: 700;
    cursor: pointer;
}

.delete-confirm-cancel {
    border: 1px solid #dceae7;
    color: #475569;
    background: #fff;
}

.delete-confirm-submit {
    border: 1px solid #dc2626;
    color: #fff;
    background: #dc2626;
}

.delete-confirm-cancel:hover:not(:disabled) {
    border-color: #91cec4;
    background: #f7fbfa;
}

.delete-confirm-submit:hover:not(:disabled) {
    border-color: #b91c1c;
    background: #b91c1c;
}

.delete-confirm-cancel:disabled,
.delete-confirm-submit:disabled {
    cursor: not-allowed;
    opacity: 0.6;
}

.delete-confirm-enter-active,
.delete-confirm-leave-active {
    transition: opacity 0.2s ease;
}

.delete-confirm-enter-active .delete-confirm-dialog,
.delete-confirm-leave-active .delete-confirm-dialog {
    transition: transform 0.2s ease, opacity 0.2s ease;
}

.delete-confirm-enter-from,
.delete-confirm-leave-to {
    opacity: 0;
}

.delete-confirm-enter-from .delete-confirm-dialog,
.delete-confirm-leave-to .delete-confirm-dialog {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
}

@media (prefers-reduced-motion: reduce) {
    .delete-confirm-enter-active,
    .delete-confirm-leave-active,
    .delete-confirm-enter-active .delete-confirm-dialog,
    .delete-confirm-leave-active .delete-confirm-dialog {
        transition: none;
    }
}
</style>
