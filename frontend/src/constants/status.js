export const STATUS = Object.freeze({
    ENABLE: 'ENABLE',
    DISABLE: 'DISABLE'
})

export const STATUS_OPTIONS = [
    { value: STATUS.ENABLE, label: '启用' },
    { value: STATUS.DISABLE, label: '禁用' }
]

export const STATUS_LABELS = Object.freeze(
    Object.fromEntries(STATUS_OPTIONS.map(item => [item.value, item.label]))
)
