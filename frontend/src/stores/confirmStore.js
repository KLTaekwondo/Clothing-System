import {defineStore} from 'pinia'
import {computed, ref} from 'vue'

export const useConfirmStore = defineStore('confirm', () => {
    const current = ref(null)
    let resolver = null

    const visible = computed(() => Boolean(current.value))

    function confirm(options = {}) {
        if (resolver) resolver(false)
        current.value = {
            title: options.title || '请确认操作',
            message: options.message || '',
            icon: options.icon || 'warning',
            confirmText: options.confirmText || '确定',
            cancelText: options.cancelText || '取消',
            danger: Boolean(options.danger)
        }
        return new Promise(resolve => {
            resolver = resolve
        })
    }

    function resolveConfirm(result) {
        const pendingResolver = resolver
        resolver = null
        current.value = null
        pendingResolver?.(result)
    }

    function accept() {
        resolveConfirm(true)
    }

    function cancel() {
        resolveConfirm(false)
    }

    return {
        current,
        visible,
        confirm,
        accept,
        cancel
    }
})

export function confirm(options) {
    return useConfirmStore().confirm(options)
}
