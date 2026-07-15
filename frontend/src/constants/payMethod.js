export const PAY_METHOD = Object.freeze({
    CASH: 'CASH',
    CARD: 'CARD',
    ALIPAY: 'ALIPAY',
    WECHAT: 'WECHAT'
})

export const PAY_METHOD_OPTIONS = [
    { value: PAY_METHOD.CASH, label: '现金' },
    { value: PAY_METHOD.CARD, label: '信用卡' },
    { value: PAY_METHOD.ALIPAY, label: '支付宝' },
    { value: PAY_METHOD.WECHAT, label: '微信' }
]

export const PAY_METHOD_LABELS = Object.freeze(
    Object.fromEntries(PAY_METHOD_OPTIONS.map(item => [item.value, item.label]))
)
