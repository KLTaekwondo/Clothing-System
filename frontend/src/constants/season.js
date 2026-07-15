export const SEASON = Object.freeze({
    SPRING: 'SPRING',
    SUMMER: 'SUMMER',
    AUTUMN: 'AUTUMN',
    WINTER: 'WINTER'
})

export const SEASON_OPTIONS = [
    { value: SEASON.SPRING, label: '春款' },
    { value: SEASON.SUMMER, label: '夏款' },
    { value: SEASON.AUTUMN, label: '秋款' },
    { value: SEASON.WINTER, label: '冬款' }
]

export const SEASON_LABELS = Object.freeze(
    Object.fromEntries(SEASON_OPTIONS.map(item => [item.value, item.label]))
)
