<template>
    <Transition name="error-dialog">
        <div
            v-if="errorDialogStore.visible"
            class="error-overlay"
            @click.self="errorDialogStore.close"
        >
            <div
                aria-modal="true"
                class="error-dialog"
                role="dialog"
            >
                <div class="error-header">
                    <div class="error-heading">
                        <span class="error-icon">
                            <IconGraphic name="warning"/>
                        </span>
                        <strong class="error-title">{{ errorDialogStore.current.title }}</strong>
                    </div>
                    <button
                        aria-label="关闭弹窗"
                        class="error-close"
                        type="button"
                        @click="errorDialogStore.close"
                    >×</button>
                </div>

                <div class="error-body">
                    <p class="error-message">{{ errorDialogStore.current.message }}</p>
                </div>

                <div class="error-actions">
                    <button
                        class="error-confirm"
                        type="button"
                        @click="errorDialogStore.close"
                    >知道了</button>
                </div>
            </div>
        </div>
    </Transition>
</template>

<script setup>
import {onBeforeUnmount, onMounted} from 'vue'
import {useErrorDialogStore} from '../stores/errorDialogStore.js'

const errorDialogStore = useErrorDialogStore()

onMounted(() => {
    window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
    window.removeEventListener('keydown', handleKeydown)
})

function handleKeydown(event) {
    if (event.key === 'Escape' && errorDialogStore.visible) {
        errorDialogStore.close()
    }
}
</script>

<style scoped>
.error-overlay {
    position: fixed;
    inset: 0;
    z-index: 1300;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    background: rgba(0, 0, 0, 0.42);
    backdrop-filter: blur(2px);
}

[data-theme="dark"] .error-overlay {
    background: rgba(0, 0, 0, 0.62);
}

.error-dialog {
    width: min(600px, 100%);
    max-height: 82vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: var(--radius-xl);
    box-shadow: var(--shadow-lg);
}

.error-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 18px 20px;
    border-bottom: 1px solid var(--border-light);
}

.error-heading {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
}

.error-icon {
    width: 34px;
    height: 34px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 9px;
    background: var(--error-light);
}

.error-icon :deep(img) {
    width: 19px;
    height: 19px;
}

.error-title {
    overflow: hidden;
    color: var(--text);
    font-size: 16px;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.error-close {
    width: 28px;
    height: 28px;
    flex-shrink: 0;
    border: none;
    border-radius: 8px;
    color: var(--text-muted);
    background: transparent;
    font-size: 24px;
    line-height: 1;
    cursor: pointer;
}

.error-close:hover {
    color: var(--error);
    background: var(--error-light);
}

.error-body {
    min-height: 0;
    padding: 18px 20px 6px;
    overflow-y: auto;
}

.error-message {
    margin: 0;
    padding: 14px 16px;
    border-radius: 10px;
    color: var(--error-dark);
    background: var(--error-light);
    font-size: 13px;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-word;
}

.error-actions {
    display: flex;
    justify-content: flex-end;
    padding: 14px 20px 18px;
}

.error-confirm {
    min-width: 92px;
    height: 38px;
    padding: 0 18px;
    border: none;
    border-radius: 999px;
    color: var(--text-invert);
    background: var(--primary-dark);
    font-weight: 700;
    cursor: pointer;
}

.error-confirm:hover {
    background: var(--primary);
}

.error-dialog-enter-active,
.error-dialog-leave-active {
    transition: opacity 0.2s ease;
}

.error-dialog-enter-active .error-dialog,
.error-dialog-leave-active .error-dialog {
    transition: transform 0.2s ease, opacity 0.2s ease;
}

.error-dialog-enter-from,
.error-dialog-leave-to {
    opacity: 0;
}

.error-dialog-enter-from .error-dialog,
.error-dialog-leave-to .error-dialog {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
}

@media (prefers-reduced-motion: reduce) {
    .error-dialog-enter-active,
    .error-dialog-leave-active,
    .error-dialog-enter-active .error-dialog,
    .error-dialog-leave-active .error-dialog {
        transition: none;
    }
}
</style>
