<template>
    <Transition name="confirm">
        <div
            v-if="confirmStore.visible"
            class="confirm-overlay"
            @click.self="confirmStore.cancel"
        >
            <div
                aria-modal="true"
                class="confirm-dialog"
                role="dialog"
                @keydown.esc="confirmStore.cancel"
            >
                <div class="confirm-header">
                    <div class="confirm-heading">
                        <span :class="confirmStore.current?.danger ? 'confirm-icon-danger' : 'confirm-icon'">
                            <IconGraphic :name="confirmStore.current?.icon || 'warning'"/>
                        </span>
                        <strong>{{ confirmStore.current?.title }}</strong>
                    </div>
                    <button
                        aria-label="关闭确认窗口"
                        class="confirm-close"
                        type="button"
                        @click="confirmStore.cancel"
                    >×</button>
                </div>
                <div
                    v-if="confirmStore.current?.message"
                    class="confirm-message"
                >{{ confirmStore.current.message }}</div>
                <div class="confirm-actions">
                    <button
                        class="confirm-cancel"
                        type="button"
                        @click="confirmStore.cancel"
                    >{{ confirmStore.current?.cancelText || '取消' }}</button>
                    <button
                        :class="confirmStore.current?.danger ? 'confirm-submit-danger' : 'confirm-submit'"
                        type="button"
                        @click="confirmStore.accept"
                    >{{ confirmStore.current?.confirmText || '确定' }}</button>
                </div>
            </div>
        </div>
    </Transition>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, watch} from 'vue'
import IconGraphic from '../component/IconGraphic.vue'
import {useConfirmStore} from '../stores/confirmStore.js'

const confirmStore = useConfirmStore()

watch(() => confirmStore.visible, visible => {
    if (visible) nextTick(() => document.querySelector('.confirm-submit, .confirm-submit-danger')?.focus())
})

onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event) {
    if (event.key === 'Escape' && confirmStore.visible) confirmStore.cancel()
}
</script>

<style scoped>
.confirm-overlay {
    position: fixed;
    inset: 0;
    z-index: 1200;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    background: rgba(0, 0, 0, 0.42);
    backdrop-filter: blur(2px);
}

[data-theme="dark"] .confirm-overlay {
    background: rgba(0, 0, 0, 0.62);
}

.confirm-enter-active,
.confirm-leave-active {
    transition: opacity 0.2s ease;
}

.confirm-enter-active .confirm-dialog,
.confirm-leave-active .confirm-dialog {
    transition: transform 0.2s ease, opacity 0.2s ease;
}

.confirm-enter-from,
.confirm-leave-to {
    opacity: 0;
}

.confirm-enter-from .confirm-dialog,
.confirm-leave-to .confirm-dialog {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
}

.confirm-dialog {
    width: min(420px, 100%);
    overflow: hidden;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
}

.confirm-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 18px 20px;
    border-bottom: 1px solid var(--border-light);
}

.confirm-heading {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--text);
    font-size: 16px;
}

.confirm-icon,
.confirm-icon-danger {
    width: 34px;
    height: 34px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 9px;
    background: var(--primary-light);
}

.confirm-icon-danger {
    background: var(--error-light);
}

.confirm-icon :deep(img),
.confirm-icon-danger :deep(img) {
    width: 19px;
    height: 19px;
}

.confirm-close {
    width: 28px;
    height: 28px;
    border: none;
    color: var(--text-muted);
    background: transparent;
    font-size: 24px;
    line-height: 1;
    cursor: pointer;
}

.confirm-close:hover {
    color: var(--error);
}

.confirm-message {
    padding: 22px 20px 12px;
    color: var(--text-secondary);
    font-size: 14px;
    line-height: 1.7;
}

.confirm-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 14px 20px 18px;
}

.confirm-cancel,
.confirm-submit,
.confirm-submit-danger {
    min-width: 92px;
    height: 38px;
    padding: 0 16px;
    border-radius: 999px;
    font-weight: 700;
    cursor: pointer;
}

.confirm-cancel {
    border: 1px solid var(--border);
    color: var(--text-secondary);
    background: var(--bg-card);
}

.confirm-cancel:hover {
    border-color: var(--border-hover);
    background: var(--bg-hover);
}

.confirm-submit,
.confirm-submit-danger {
    border: none;
    color: var(--text-invert);
    background: var(--primary-dark);
}

.confirm-submit-danger {
    background: var(--error-dark);
}

.confirm-submit:hover {
    background: var(--primary);
}

.confirm-submit-danger:hover {
    background: var(--error);
}
@media (prefers-reduced-motion: reduce) {
    .confirm-enter-active,
    .confirm-leave-active,
    .confirm-enter-active .confirm-dialog,
    .confirm-leave-active .confirm-dialog {
        transition: none;
    }
}
</style>
