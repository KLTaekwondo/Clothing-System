<template>
    <div
        ref="rootRef"
        class="option-picker"
    >
        <div :class="triggerClass">
            <input
                :aria-expanded="isOpen"
                :disabled="disabled"
                :placeholder="placeholder"
                :value="modelValue"
                aria-autocomplete="list"
                aria-haspopup="listbox"
                autocomplete="off"
                class="picker-input"
                role="combobox"
                type="text"
                @blur="handleBlur"
                @focus="handleFocus"
                @input="handleInput"
                @keydown.down.prevent="moveActive(1)"
                @keydown.enter.prevent="handleEnter"
                @keydown.esc.prevent="handleEscape"
                @keydown.up.prevent="moveActive(-1)"
            />
            <button
                v-if="hasValue"
                aria-label="清空"
                class="picker-clear"
                type="button"
                @mousedown.prevent="clearValue"
            >×</button>
            <button
                :aria-label="selectPlaceholder"
                :disabled="disabled || options.length === 0"
                class="picker-toggle"
                type="button"
                @mousedown.prevent="togglePanel"
            >
                <svg
                    :class="isOpen ? 'picker-arrow-open' : 'picker-arrow'"
                    fill="none"
                    viewBox="0 0 12 12"
                >
                    <path
                        d="M2.5 4.5L6 8l3.5-3.5"
                        stroke="currentColor"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="1.4"
                    />
                </svg>
            </button>
        </div>

        <ul
            v-if="isOpen"
            class="picker-panel"
            role="listbox"
        >
            <li
                v-if="filteredOptions.length === 0"
                class="picker-empty"
            >没有匹配的值</li>
            <li
                v-for="(option, index) in filteredOptions"
                :key="option.id"
                :class="index === activeIndex ? 'picker-option-active' : 'picker-option'"
                role="option"
                @mousedown.prevent="pick(option)"
                @mouseenter="activeIndex = index"
            >{{ option.optionValue }}</li>
        </ul>

        <span
            v-if="showError"
            class="picker-error"
        >{{ errorMessage }}</span>
        <span
            v-else-if="options.length === 0"
            class="picker-hint"
        >暂无可选值，请先前往选项管理添加</span>
    </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, ref, watch} from 'vue'

const props = defineProps({
    modelValue: {
        type: String,
        default: ''
    },
    options: {
        type: Array,
        default: () => []
    },
    placeholder: {
        type: String,
        default: '输入关键词筛选'
    },
    selectPlaceholder: {
        type: String,
        default: '展开选项'
    },
    errorMessage: {
        type: String,
        default: '该值不在选项管理中，请选择有效值'
    },
    disabled: Boolean
})

const emit = defineEmits(['update:modelValue'])

const rootRef = ref(null)
const isOpen = ref(false)
const activeIndex = ref(0)
const touched = ref(false)

const hasValue = computed(() => props.modelValue.trim() !== '')
const keyword = computed(() => props.modelValue.trim().toLocaleLowerCase())

const matchedOption = computed(() => {
    return props.options.find(option => {
        return option.optionValue?.trim().toLocaleLowerCase() === keyword.value
    })
})

// 已经精确命中时列出全部，方便直接换成别的值；否则按输入筛选
const filteredOptions = computed(() => {
    if (!keyword.value || matchedOption.value) {
        return props.options
    }
    return props.options.filter(option => {
        return option.optionValue?.toLocaleLowerCase().includes(keyword.value)
    })
})

const showError = computed(() => touched.value && !matchedOption.value && !props.disabled)

const triggerClass = computed(() => {
    if (props.disabled) {
        return 'picker-trigger-disabled'
    }
    if (showError.value) {
        return 'picker-trigger-error'
    }
    return 'picker-trigger'
})

watch(() => props.modelValue, () => {
    if (matchedOption.value) {
        touched.value = false
    }
})

watch(() => props.options.length, () => {
    if (props.options.length === 0) {
        isOpen.value = false
    }
})

// 边打字边筛时候选会变少，把高亮位置拉回第一个，避免按 Enter 选到不存在的项
watch(filteredOptions, () => {
    activeIndex.value = 0
})

onMounted(() => {
    document.addEventListener('mousedown', handleOutside)
})

onBeforeUnmount(() => {
    document.removeEventListener('mousedown', handleOutside)
})

function handleOutside(event) {
    if (!rootRef.value?.contains(event.target)) {
        closePanel()
        touched.value = true
    }
}

function handleFocus() {
    openPanel()
}

function openPanel() {
    if (props.disabled || props.options.length === 0) {
        return
    }
    isOpen.value = true
    activeIndex.value = 0
}

function closePanel() {
    isOpen.value = false
}

function togglePanel() {
    if (isOpen.value) {
        closePanel()
        return
    }
    openPanel()
}

function handleInput(event) {
    emit('update:modelValue', event.target.value)
    openPanel()
}

function moveActive(step) {
    if (!isOpen.value) {
        openPanel()
        return
    }
    const total = filteredOptions.value.length
    if (total === 0) {
        return
    }
    const next = activeIndex.value + step
    if (next < 0) {
        activeIndex.value = total - 1
        return
    }
    if (next >= total) {
        activeIndex.value = 0
        return
    }
    activeIndex.value = next
}

function handleEnter() {
    if (!isOpen.value) {
        openPanel()
        return
    }
    const option = filteredOptions.value[activeIndex.value] || matchedOption.value
    if (option) {
        pick(option)
    }
}

function handleEscape() {
    closePanel()
}

function pick(option) {
    emit('update:modelValue', option.optionValue)
    touched.value = false
    closePanel()
}

function clearValue() {
    emit('update:modelValue', '')
    touched.value = false
    openPanel()
}

function handleBlur() {
    // 面板里的点击用了 mousedown.prevent，焦点不会离开输入框，这里只处理真的离开
    touched.value = true
    if (matchedOption.value && props.modelValue !== matchedOption.value.optionValue) {
        emit('update:modelValue', matchedOption.value.optionValue)
    }
    closePanel()
}

function validate() {
    touched.value = true
    return Boolean(matchedOption.value)
}

defineExpose({validate})
</script>

<style scoped>
.option-picker {
    position: relative;
    width: 100%;
}

.picker-trigger,
.picker-trigger-error,
.picker-trigger-disabled {
    display: flex;
    align-items: center;
    gap: 2px;
    width: 100%;
    padding: 0 8px 0 16px;
    border: 1px solid var(--border-strong);
    border-radius: 999px;
    background: var(--bg-subtle);
    box-shadow: inset 0 1px 2px rgba(22, 83, 78, 0.05);
    transition: var(--transition);
}

.picker-trigger:hover {
    border-color: var(--border-hover);
}

.picker-trigger:focus-within {
    border-color: var(--primary);
    background: var(--bg-card);
    box-shadow: 0 0 0 4px var(--primary-focus), 0 5px 14px rgba(13, 148, 136, 0.08);
}

.picker-trigger-error {
    border-color: var(--error);
    background: var(--error-light);
    box-shadow: 0 0 0 3px var(--error-focus);
}

.picker-trigger-disabled {
    opacity: 0.6;
    background: var(--bg-subtle);
}

.picker-input {
    width: 100%;
    min-width: 0;
    height: 40px;
    border: none;
    background: transparent;
    color: var(--text);
    font-size: 13px;
    outline: none;
}

.picker-input::placeholder {
    color: var(--text-muted);
}

.picker-clear,
.picker-toggle {
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 26px;
    height: 26px;
    border: none;
    border-radius: 50%;
    color: var(--text-muted);
    background: transparent;
    cursor: pointer;
}

.picker-clear {
    font-size: 16px;
    line-height: 1;
}

.picker-clear:hover,
.picker-toggle:hover {
    color: var(--primary);
    background: var(--bg-hover);
}

.picker-toggle:disabled {
    opacity: 0.4;
    cursor: not-allowed;
}

.picker-arrow,
.picker-arrow-open {
    width: 12px;
    height: 12px;
    transition: transform 0.18s ease;
}

.picker-arrow-open {
    transform: rotate(180deg);
}

.picker-panel {
    position: absolute;
    top: calc(100% + 6px);
    right: 0;
    left: 0;
    z-index: 30;
    max-height: 220px;
    margin: 0;
    padding: 6px;
    overflow-y: auto;
    list-style: none;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-radius: 14px;
    box-shadow: var(--shadow-lg);
}

.picker-option,
.picker-option-active {
    padding: 9px 12px;
    border-radius: 9px;
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.4;
    cursor: pointer;
    word-break: break-all;
}

.picker-option:hover {
    background: var(--bg-hover);
}

.picker-option-active {
    background: var(--primary-light);
    color: var(--primary-dark);
    font-weight: 600;
}

.picker-empty {
    padding: 10px 12px;
    color: var(--text-muted);
    font-size: 13px;
}

.picker-error,
.picker-hint {
    display: block;
    margin-top: 6px;
    font-size: 12px;
}

.picker-error {
    color: var(--error-dark);
}

.picker-hint {
    color: var(--text-muted);
}
</style>
