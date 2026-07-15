import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
    const toasts = ref([])
    let nextId = 0

    function showToast(type, message, duration = 3000) {
        const id = ++nextId
        toasts.value.push({ id, type, message })
        setTimeout(() => remove(id), duration)
    }
    function remove(id) {
        const idx = toasts.value.findIndex(t => t.id === id)
        if (idx !== -1) toasts.value.splice(idx, 1)
    }

    function success(message, duration=3000) { showToast('success', message, duration) }
    function error(message, duration=3000)   { showToast('error', message, duration) }
    function warning(message, duration=3000) { showToast('warning', message, duration) }
    function info(message, duration=3000)    { showToast('info', message, duration) }

    return { toasts, showToast, remove, success, error, warning, info }
})