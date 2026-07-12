<template>
    <div class="nav-item">
        <img :src="iconSrc" :alt="title" class="icon">
        <span class="text">{{ title }}</span>
    </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
    icon: {
        type: String,
        default: ''
    },
    title: {
        type: String,
        default: ''
    }
})

const iconSrc = computed(() => {
    return new URL("../assets/"+props.icon+".svg", import.meta.url).href
})
</script>

<style scoped>
.nav-item {
    display: flex;
    align-items: center;
    background: linear-gradient(135deg, #1890ff 0%, #52c41a 100%);
    padding: 12px 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: 8px;
    margin: 4px 8px;
    gap: 12px;
    box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.nav-item:hover {
    background: linear-gradient(135deg, #40a9ff 0%, #73d13d 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.nav-item.router-link-active {
    background: linear-gradient(135deg, #096dd9 0%, #389e0d 100%);
    box-shadow: 0 4px 12px rgba(24, 144, 255, 0.5);
}

.icon {
    width: 20px;
    height: 20px;
    fill: currentColor;
    flex-shrink: 0;
    filter: brightness(0) invert(1);
}

.text {
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
    color:white;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}
</style>