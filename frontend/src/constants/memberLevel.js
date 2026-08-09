export const MEMBER_LEVEL = Object.freeze({
    COMMON: 'COMMON',
    VIP: 'VIP',
    MVP: 'MVP',
    CUSTOMER: 'CUSTOMER'
})

export const MEMBER_LEVEL_LABELS = Object.freeze({
    [MEMBER_LEVEL.COMMON]: '普通用户',
    [MEMBER_LEVEL.VIP]: '普通会员',
    [MEMBER_LEVEL.MVP]: '高级会员',
    [MEMBER_LEVEL.CUSTOMER]: '自定义会员'
})

export const MEMBER_LEVEL_OPTIONS = Object.freeze(
    Object.entries(MEMBER_LEVEL_LABELS).map(([value, label]) => ({
        value,
        label
    }))
)

export const MEMBER_LEVEL_DISCOUNTS = Object.freeze({
    [MEMBER_LEVEL.COMMON]: 0.85,
    [MEMBER_LEVEL.VIP]: 0.83,
    [MEMBER_LEVEL.MVP]: 0.8
})
