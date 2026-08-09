export const AUDIT_STATUS = Object.freeze({
    DRAFT: 'DRAFT',
    CHECKING: 'CHECKING',
    APPROVED: 'APPROVED',
    REJECTED: 'REJECTED'
})

export const AUDIT_STATUS_LABELS = Object.freeze({
    [AUDIT_STATUS.DRAFT]: '草稿',
    [AUDIT_STATUS.CHECKING]: '审核中',
    [AUDIT_STATUS.APPROVED]: '已审批',
    [AUDIT_STATUS.REJECTED]: '已拒绝'
})

export const AUDIT_STATUS_TABS = Object.freeze([
    {value: '', label: '全部'},
    {value: AUDIT_STATUS.DRAFT, label: AUDIT_STATUS_LABELS[AUDIT_STATUS.DRAFT]},
    {value: AUDIT_STATUS.CHECKING, label: AUDIT_STATUS_LABELS[AUDIT_STATUS.CHECKING]},
    {value: AUDIT_STATUS.APPROVED, label: AUDIT_STATUS_LABELS[AUDIT_STATUS.APPROVED]},
    {value: AUDIT_STATUS.REJECTED, label: AUDIT_STATUS_LABELS[AUDIT_STATUS.REJECTED]}
])

export const DIRECTION_LABELS = Object.freeze({
    IN: '采购入库',
    OUT: '采购退货'
})
