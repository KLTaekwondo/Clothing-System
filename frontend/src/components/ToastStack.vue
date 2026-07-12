<template>
  <Teleport to="body">
    <div class="toast-stack">
      <transition-group name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="getToastClass(toast)"
        >
          <span class="toast-icon">{{ toast.icon }}</span>
          <div class="toast-copy">
            <strong class="toast-title">{{ toast.title }}</strong>
            <span v-if="toast.message" class="toast-message">{{ toast.message }}</span>
          </div>
          <button class="toast-close" @click="removeToast(toast.id)">×</button>
        </div>
      </transition-group>
    </div>
  </Teleport>
</template>

<script setup>
import { toasts, removeToast, pushToast } from '../utils/toastCenter'

const getToastClass = (toast) => {
  const map = {
    success: 'toast-success',
    error: 'toast-error',
    warning: 'toast-warning',
    info: 'toast-info'
  }
  return map[toast.status] || 'toast-info'
}

const show = (message, type = 'success', duration = 3000) => {
  pushToast({ status: type, title: type === 'error' ? '操作失败' : '操作成功', message, duration })
}

defineExpose({ show })
</script>

<style scoped>
.toast-stack {
  position: fixed;
  top: 18px;
  right: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  z-index: 9999;
  pointer-events: none;
}

.toast-success,
.toast-error,
.toast-warning,
.toast-info {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  min-width: 270px;
  max-width: 420px;
  padding: 13px 14px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--border);
  box-shadow: var(--shadow-lg);
  pointer-events: auto;
  backdrop-filter: blur(16px);
}

.toast-success {
  border-left: 4px solid var(--success);
}

.toast-error {
  border-left: 4px solid var(--danger);
}

.toast-warning {
  border-left: 4px solid var(--warning);
}

.toast-info {
  border-left: 4px solid var(--info);
}

.toast-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  margin-top: 1px;
  border-radius: 999px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 860;
  flex-shrink: 0;
}

.toast-success .toast-icon {
  background: var(--success);
}

.toast-error .toast-icon {
  background: var(--danger);
}

.toast-warning .toast-icon {
  background: var(--warning);
}

.toast-info .toast-icon {
  background: var(--info);
}

.toast-copy {
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}

.toast-title {
  color: var(--text);
  font-size: 14px;
  font-weight: 780;
}

.toast-message {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.55;
}

.toast-close {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
  color: var(--text-muted);
  background: transparent;
  border-radius: 7px;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  transition: color var(--transition-fast), background var(--transition-fast);
  flex-shrink: 0;
}

.toast-close:hover {
  color: var(--text);
  background: var(--surface-soft);
}

.toast-enter-active {
  transition: all 0.25s ease;
}

.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(42px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(42px);
}

@media (max-width: 640px) {
  .toast-stack {
    left: 12px;
    right: 12px;
  }

  .toast-success,
  .toast-error,
  .toast-warning,
  .toast-info {
    min-width: 0;
    max-width: none;
  }
}
</style>
