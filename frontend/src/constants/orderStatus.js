export const ORDER_STATUS = Object.freeze({
    DRAFT: 'DRAFT',
    COMPLETED: 'COMPLETED',
    REFUND: 'REFUND'
})

export const ORDER_STATUS_LABELS = Object.freeze({
    [ORDER_STATUS.DRAFT]: '挂单',
    [ORDER_STATUS.COMPLETED]: '已完成',
    [ORDER_STATUS.REFUND]: '已退款'
})
