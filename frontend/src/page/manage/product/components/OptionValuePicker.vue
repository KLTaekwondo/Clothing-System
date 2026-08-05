<template>
    <div class="option-value-picker">
        <div :class="showError ? 'picker-controls-error' : 'picker-controls'">
            <input
                :class="showError ? 'picker-input-error' : 'picker-input'"
                :disabled="disabled"
                :placeholder="placeholder"
                :value="modelValue"
                type="text"
                @blur="handleBlur"
                @input="handleInput"
                @keydown.enter.prevent="handleEnter"
            />
            <select
                :disabled="disabled || options.length === 0"
                :value="matchedValue"
                class="picker-select"
                @change="handleSelect"
            >
                <option
                    class="picker-placeholder"
                    disabled
                    value=""
                >{{ selectPlaceholder }}</option>
                <option
                    v-for="option in filteredOptions"
                    :key="option.id"
                    :value="option.optionValue"
                >
                    {{ option.optionValue }}
                </option>
            </select>
        </div>
        <span
            v-if="showError"
            class="picker-error"
        >
            {{ errorMessage }}
        </span>
        <span
            v-else-if="options.length === 0"
            class="picker-hint"
        >
            暂无可选值，请先前往选项管理添加
        </span>
    </div>
</template>

<script setup>
import {computed, ref, watch} from 'vue'

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
        default: '选择已有值'
    },
    errorMessage: {
        type: String,
        default: '该值不在选项管理中，请选择有效值'
    },
    disabled: Boolean
})

const emit = defineEmits(['update:modelValue'])
const touched = ref(false)

const normalizedValue = computed(() => props.modelValue.trim())
const normalizedKeyword = computed(() => normalizedValue.value.toLocaleLowerCase())

const matchedOption = computed(() => {
    return props.options.find(option => {
        return option.optionValue?.trim().toLocaleLowerCase() === normalizedKeyword.value
    })
})

const matchedValue = computed(() => matchedOption.value?.optionValue || '')

const filteredOptions = computed(() => {
    const keyword = normalizedKeyword.value
    if (!keyword || matchedOption.value) return props.options
    return props.options.filter(option => {
        return option.optionValue?.toLocaleLowerCase().includes(keyword)
    })
})

const valid = computed(() => Boolean(matchedOption.value))
const showError = computed(() => touched.value && !valid.value)

watch(() => props.modelValue, () => {
    if (valid.value) touched.value = false
})

function handleInput(event) {
    emit('update:modelValue', event.target.value)
}

function handleSelect(event) {
    touched.value = true
    emit('update:modelValue', event.target.value)
}

function handleEnter() {
    touched.value = true
    const option = matchedOption.value || filteredOptions.value[0]
    if (option) emit('update:modelValue', option.optionValue)
}

function handleBlur() {
    touched.value = true
    if (matchedOption.value && props.modelValue !== matchedOption.value.optionValue) {
        emit('update:modelValue', matchedOption.value.optionValue)
    }
}

function validate() {
    touched.value = true
    return valid.value
}

defineExpose({validate})
</script>

<style scoped>
.option-value-picker {
    width: 100%;
}

.picker-controls,
.picker-controls-error {
    width: 100%;
    display: flex;
    gap: 0;
    overflow: hidden;
    border: 1px solid #c8ded9;
    border-radius: 10px;
    background: #f2f8f7;
    box-shadow: inset 0 1px 2px rgba(22, 83, 78, 0.05);
    transition: var(--transition);
}

.picker-controls:hover,
.picker-controls-error:hover {
    border-color: #91cec4;
    background: #f7fbfa;
}

.picker-controls:focus-within {
    border-color: #14b8a6;
    background: #fff;
    box-shadow: 0 0 0 4px rgba(20, 184, 166, 0.12), 0 5px 14px rgba(13, 148, 136, 0.08);
}

.picker-controls-error,
.picker-controls-error:hover,
.picker-controls-error:focus-within {
    border-color: #ef4444;
    background: #fff7f7;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.picker-controls .picker-input,
.picker-controls .picker-input-error,
.picker-controls-error .picker-input,
.picker-controls-error .picker-input-error {
    width: 55%;
    border: none;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
}

.picker-controls .picker-input:focus,
.picker-controls .picker-input-error:focus,
.picker-controls-error .picker-input:focus,
.picker-controls-error .picker-input-error:focus {
    border: none;
    background: transparent;
    box-shadow: none;
}

.picker-controls .picker-select,
.picker-controls-error .picker-select {
    width: 45%;
    padding: 0 32px 0 14px;
    border: none;
    border-left: 1px solid #d7e5e2;
    border-radius: 0;
    color: #47615e;
    background-color: rgba(255, 255, 255, 0.58);
    background-image: linear-gradient(45deg, transparent 50%, #64807e 50%), linear-gradient(135deg, #64807e 50%, transparent 50%);
    background-position: calc(100% - 18px) 17px, calc(100% - 13px) 17px;
    background-repeat: no-repeat;
    background-size: 5px 5px, 5px 5px;
    box-shadow: none;
    cursor: pointer;
    appearance: none;
    transition: background-color 0.2s, color 0.2s;
}

.picker-controls .picker-select:hover,
.picker-controls-error .picker-select:hover {
    color: var(--primary);
    background-color: rgba(255, 255, 255, 0.86);
}

.picker-controls .picker-select:focus,
.picker-controls-error .picker-select:focus {
    border-color: #d7e5e2;
    color: var(--primary);
    background-color: rgba(255, 255, 255, 0.92);
    box-shadow: none;
    outline: none;
}

.picker-controls .picker-select option,
.picker-controls-error .picker-select option {
    padding: 10px 12px;
    color: #47615e;
    background: #ffffff;
    font-size: 13px;
}

.picker-controls .picker-select option:checked,
.picker-controls-error .picker-select option:checked {
    color: #0f766e;
    background: #dff5f1;
}

.picker-controls .picker-select .picker-placeholder,
.picker-controls-error .picker-select .picker-placeholder {
    color: #829895;
    background: #f2f8f7;
}

.picker-error,
.picker-hint {
    display: block;
    margin-top: 6px;
    font-size: 12px;
}

.picker-error {
    color: #dc2626;
}

.picker-hint {
    color: #829895;
}

@media (max-width: 560px) {
    .picker-controls,
    .picker-controls-error {
        flex-direction: column;
    }

    .picker-controls .picker-input,
    .picker-controls .picker-input-error,
    .picker-controls-error .picker-input,
    .picker-controls-error .picker-input-error,
    .picker-controls .picker-select,
    .picker-controls-error .picker-select {
        width: 100%;
    }

    .picker-controls .picker-select,
    .picker-controls-error .picker-select {
        padding: 0 14px;
        border-left: none;
        border-top: 1px solid #d7e5e2;
        background-position: calc(100% - 16px) 17px, calc(100% - 11px) 17px;
    }
}
</style>
