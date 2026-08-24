import {ref, watch} from 'vue'

const THEME_KEY = 'clothing_theme'
const ACCENT_KEY = 'clothing_accent'

export const ACCENTS = ['teal', 'blue', 'orange', 'purple', 'rose']

export const ACCENT_LABELS = {
    teal: '青绿',
    blue: '蓝色',
    orange: '橙色',
    purple: '紫色',
    rose: '玫红'
}

function getInitialTheme() {
    const saved = localStorage.getItem(THEME_KEY)
    if (saved === 'light' || saved === 'dark') return saved
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

function getInitialAccent() {
    const saved = localStorage.getItem(ACCENT_KEY)
    return ACCENTS.includes(saved) ? saved : 'teal'
}

export const theme = ref(getInitialTheme())
export const accent = ref(getInitialAccent())

export function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
}

export function setTheme(value) {
    if (value === 'light' || value === 'dark') theme.value = value
}

export function cycleAccent() {
    const index = ACCENTS.indexOf(accent.value)
    accent.value = ACCENTS[(index + 1) % ACCENTS.length]
}

export function setAccent(value) {
    if (ACCENTS.includes(value)) accent.value = value
}

watch(theme, value => {
    localStorage.setItem(THEME_KEY, value)
    document.documentElement.setAttribute('data-theme', value)
}, {immediate: true})

watch(accent, value => {
    localStorage.setItem(ACCENT_KEY, value)
    if (value === 'teal') document.documentElement.removeAttribute('data-accent')
    else document.documentElement.setAttribute('data-accent', value)
}, {immediate: true})