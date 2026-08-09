import {onBeforeUnmount, onMounted, watch} from 'vue'

function usePageDraft(key, sources, restore, options = {}) {
    let restoring = false

    function clear() {
        sessionStorage.removeItem(key)
    }

    function save() {
        if (restoring || options.saved?.()) return
        const value = typeof sources === 'function' ? sources() : sources.value
        sessionStorage.setItem(key, JSON.stringify(value))
    }

    function restoreDraft() {
        const raw = sessionStorage.getItem(key)
        if (!raw) return false
        try {
            restoring = true
            restore(JSON.parse(raw))
            return true
        } catch {
            clear()
            return false
        } finally {
            restoring = false
        }
    }

    const stop = watch(sources, save, {deep: true})
    onMounted(() => {
        if (restoreDraft()) options.onRestored?.()
    })
    onBeforeUnmount(stop)

    return {
        clear,
        save
    }
}

export default usePageDraft
