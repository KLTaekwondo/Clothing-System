import {defineStore} from 'pinia'
import {computed, ref} from 'vue'

// 中心弹窗：给"必须停下来读完"的长消息用，比如 Excel 校验失败的整批错误
// 和 toast 的区别：toast 3 秒就走，这种消息用户得逐条对着表格改
export const useErrorDialogStore = defineStore('errorDialog', () => {
    const current = ref(null)

    const visible = computed(() => Boolean(current.value))

    function open(options = {}) {
        current.value = {
            title: options.title || '操作未完成',
            message: options.message || ''
        }
    }

    function close() {
        current.value = null
    }

    return {
        current,
        visible,
        open,
        close
    }
})

export function openErrorDialog(options) {
    useErrorDialogStore().open(options)
}
