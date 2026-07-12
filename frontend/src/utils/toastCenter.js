import { ref } from 'vue'

export const toasts = ref([])

let toastId = 0

const DEFAULT_DURATION = 3200
const VALID_STATUSES = ['success', 'error', 'warning', 'info']

const normalizeStatus = (status) => VALID_STATUSES.includes(status) ? status : 'info'

const getIcon = (status) => {
  const map = { success: '✓', error: '!', warning: '!', info: 'i' }
  return map[normalizeStatus(status)] || 'i'
}

export const removeToast = (id) => {
  toasts.value = toasts.value.filter(item => item.id !== id)
}

export const pushToast = ({ status = 'success', title = '提示', message = '', duration = DEFAULT_DURATION } = {}) => {
  const nextStatus = normalizeStatus(status)
  const nextToast = {
    id: ++toastId,
    status: nextStatus,
    title,
    message,
    icon: getIcon(nextStatus),
  }

  toasts.value = [nextToast, ...toasts.value].slice(0, 10)

  const timeout = Number(duration)
  if (timeout > 0 && typeof globalThis.setTimeout === 'function') {
    globalThis.setTimeout(() => removeToast(nextToast.id), timeout)
  }

  return nextToast.id
}

export const showMessage = (status, title, message, duration) => {
  return pushToast({ status, title, message, duration })
}

export const successMessage = (title = '操作成功', message = '', duration) => {
  return pushToast({ status: 'success', title, message, duration })
}

export const errorMessage = (title = '操作失败', message = '', duration) => {
  return pushToast({ status: 'error', title, message, duration })
}

export const warningMessage = (title = '提示', message = '', duration) => {
  return pushToast({ status: 'warning', title, message, duration })
}

export const infoMessage = (title = '提示', message = '', duration) => {
  return pushToast({ status: 'info', title, message, duration })
}
