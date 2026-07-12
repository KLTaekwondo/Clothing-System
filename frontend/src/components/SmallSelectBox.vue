<template>
    <div class="select-container">
        <label v-if="label" class="select-label">{{ label }}</label>
        <div class="select-wrapper" :class="{ 'focused': isFocused, 'has-value': selectedOption }">
            <select 
                class="styled-select"
                :value="modelValue"
                @input="handleInput"
                @focus="isFocused = true"
                @blur="isFocused = false"
            >
                <option value="" disabled selected v-if="placeholder">{{ placeholder }}</option>
                <option 
                    v-for="option in options" 
                    :key="option.value" 
                    :value="option.value"
                    :disabled="option.disabled"
                >
                    {{ option.label }}
                </option>
            </select>
            <div class="select-arrow">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 10l5 5 5-5z"/>
                </svg>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
    modelValue: {
        type: [String, Number],
        default: ''
    },
    placeholder: {
        type: String,
        default: '请选择'
    },
    options: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['update:modelValue', 'change'])  // 添加 change 事件

const isFocused = ref(false)

const selectedOption = computed(() => {
    return props.options.find(option => option.value === props.modelValue)
})

const handleInput = (event) => {
    const value = event.target.value
    emit('update:modelValue', value)
    emit('change', value)
}
</script>

<style scoped>
/* SelectBox 的样式保持不变 */
.select-container {
    display: flex;
    flex-direction: column;
    margin: 8px 0;
}

.select-label {
    font-size: 14px;
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 8px;
    text-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}

.select-wrapper {
    position: relative;
    display: inline-block;
    width: 100%;
    border-radius: 8px;
    background: linear-gradient(135deg, #1890ff 0%, #52c41a 100%);
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
    transition: all 0.3s ease;
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.select-wrapper:hover {
    background: linear-gradient(135deg, #40a9ff 0%, #73d13d 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.select-wrapper.focused {
    background: linear-gradient(135deg, #096dd9 0%, #389e0d 100%);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.5);
    border-color: rgba(255, 255, 255, 0.3);
}

.styled-select {
    width: 100%;
    padding: 12px 16px;
    padding-right: 40px;
    background: transparent;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    color: white;
    cursor: pointer;
    appearance: none;
    outline: none;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.styled-select option {
    background: white;
    color: #2d3748;
    font-weight: normal;
    text-shadow: none;
}

.styled-select:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.select-arrow {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    pointer-events: none;
    color: white;
    filter: brightness(0) invert(1) drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
    transition: transform 0.3s ease;
}

.select-wrapper.focused .select-arrow {
    transform: translateY(-50%) rotate(180deg);
}

/* 自定义滚动条 */
.styled-select::-webkit-scrollbar {
    width: 8px;
}

.styled-select::-webkit-scrollbar-track {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
}

.styled-select::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.3);
    border-radius: 4px;
}

.styled-select::-webkit-scrollbar-thumb:hover {
    background: rgba(255, 255, 255, 0.5);
}
</style>