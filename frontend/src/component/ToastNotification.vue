<template>
    <Teleport to="body">
        <div class="toast-container">
            <TransitionGroup name="toast">
                <div
                    v-for="toast in toasts"
                    :key="toast.id"
                    :class="'toast-' + toast.type"
                    class="toast-item"
                    @click="store.remove(toast.id)"
                >
                    <span class="toast-icon">{{ icons[toast.type] }}</span>
                    <span class="toast-msg">{{ toast.message }}</span>
                    <span class="toast-close">&times;</span>
                </div>
            </TransitionGroup>
        </div>
    </Teleport>
</template>

<script setup>
import {computed} from 'vue'
import {useToastStore} from '../stores/toastStore.js'

const store = useToastStore()
const toasts = computed(() => store.toasts)

const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
}
</script>

<style scoped>
.toast-container {
    position: fixed;
    top: 16px;
    right: 16px;
    z-index: 9999;
    display: flex;
    flex-direction: column;
    gap: 8px;
    pointer-events: none;
}

.toast-item {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 280px;
    max-width: 420px;
    padding: 13px 16px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.4;
    color: var(--text-invert);
    cursor: pointer;
    pointer-events: auto;
    box-shadow: 0 12px 28px rgba(22, 83, 78, 0.18);
    transition: box-shadow 0.2s;
}

.toast-item:hover {
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.toast-item.toast-success {
    background: #22c55e;
}

.toast-item.toast-error {
    background: #ef4444;
}

.toast-item.toast-warning {
    background: #f59e0b;
}

.toast-item.toast-info {
    background: #3b82f6;
}

.toast-icon {
    flex-shrink: 0;
    width: 22px;
    height: 22px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    font-size: 13px;
    font-weight: bold;
}

.toast-msg {
    flex: 1;
    word-break: break-word;
}

.toast-close {
    flex-shrink: 0;
    font-size: 18px;
    line-height: 1;
    opacity: 0.6;
    transition: opacity 0.15s;
}

.toast-item:hover .toast-close {
    opacity: 1;
}

/* 进出场动画 */
.toast-enter-active {
    transition: all 0.3s ease;
}

.toast-leave-active {
    transition: all 0.25s ease;
}

.toast-enter-from {
    opacity: 0;
    transform: translateX(80px);
}

.toast-leave-to {
    opacity: 0;
    transform: translateX(80px);
}
@media (prefers-reduced-motion: reduce) {
    .toast-enter-active,
    .toast-leave-active,
    .toast-move {
        transition: none;
    }
}
</style>
