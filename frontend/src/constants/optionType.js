export const OPTION_TYPE = Object.freeze({
    COLOR: 'COLOR',
    SIZE: 'SIZE',
    TYPE: 'TYPE',
    CATEGORY: 'CATEGORY',
    UNIT: 'UNIT',
    COMPOSITION: 'COMPOSITION',
    YEAR: 'YEAR'
})

export const OPTION_TYPE_OPTIONS = [
    {value: OPTION_TYPE.COLOR, label: '颜色'},
    {value: OPTION_TYPE.SIZE, label: '尺码'},
    {value: OPTION_TYPE.TYPE, label: '类型'},
    {value: OPTION_TYPE.CATEGORY, label: '种类'},
    {value: OPTION_TYPE.UNIT, label: '单位'},
    {value: OPTION_TYPE.COMPOSITION, label: '面料组合'},
    {value: OPTION_TYPE.YEAR, label: '年份'}
]

export const OPTION_TYPE_LABELS = Object.freeze(
    Object.fromEntries(OPTION_TYPE_OPTIONS.map(item => [item.value, item.label]))
)
