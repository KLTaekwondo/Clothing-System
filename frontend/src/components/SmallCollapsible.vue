<template>
    <div class="collapsible-container">
        <!-- 折叠面板头部 -->
        <div 
            class="collapsible-header nav-item"
            :class="{ 'active': isExpanded }"
            @click="toggleCollapse"
        >
            <img :src="iconPath" alt="icon" class="icon">
            <span class="text">{{ title }}</span>
            <img 
                :src="arrowPath" 
                alt="arrow" 
                class="arrow-icon"
                :class="{ 'expanded': isExpanded }"
            >
        </div>
        
        <!-- 折叠内容区域 -->
        <div 
            class="collapsible-content"
            :class="{ 'expanded': isExpanded }"
        >
            <div class="content-inner">
                <slot></slot>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
    icon: {
        type: String,
        default: ''
    },
    title: {
        type: String,
        default: ''
    },
    defaultExpanded: {
        type: Boolean,
        default: false
    }
})

// 折叠状态
const isExpanded = ref(props.defaultExpanded)

// 图标类名计算
const iconPath = computed(() => {
    return new URL("../assets/"+props.icon+".svg", import.meta.url).href
})
// 箭头图标路径计算
const arrowPath = computed(() => {
    return new URL("../assets/zhankai.svg", import.meta.url).href
})

// 切换折叠状态
const toggleCollapse = () => {
    isExpanded.value = !isExpanded.value
}
</script>

<style scoped>
.collapsible-container {
    margin: 4px 8px;
    border-radius: 8px;
    overflow: hidden;
    /* 增强立体阴影 */
    box-shadow: 
        0 4px 12px rgba(24, 144, 255, 0.25),
        0 2px 4px rgba(0, 0, 0, 0.1);
}

.collapsible-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: linear-gradient(135deg, #1890ff 0%, #52c41a 100%);
    padding: 14px 16px;
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: 8px;
    gap: 12px;
    position: relative;
    z-index: 2;
    /* 增强凸起效果 */
    box-shadow: 
        0 4px 8px rgba(24, 144, 255, 0.3),
        0 2px 4px rgba(0, 0, 0, 0.15),
        inset 0 1px 0 rgba(255, 255, 255, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.2);
}

.collapsible-header:hover {
    background: linear-gradient(135deg, #40a9ff 0%, #73d13d 100%);
    transform: translateY(-2px);
    box-shadow: 
        0 6px 16px rgba(24, 144, 255, 0.4),
        0 3px 6px rgba(0, 0, 0, 0.2),
        inset 0 1px 0 rgba(255, 255, 255, 0.5);
}

.collapsible-header.active {
    background: linear-gradient(135deg, #096dd9 0%, #389e0d 100%);
    box-shadow: 
        0 6px 16px rgba(24, 144, 255, 0.5),
        0 3px 6px rgba(0, 0, 0, 0.2),
        inset 0 1px 0 rgba(255, 255, 255, 0.3);
    border-radius: 8px 8px 0 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 修复白边问题 */
.collapsible-header.active::after {
    content: '';
    position: absolute;
    bottom: -1px;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, 
        transparent 0%, 
        rgba(255, 255, 255, 0.3) 50%, 
        transparent 100%);
    z-index: 3;
}

.icon {
    width: 20px;
    height: 20px;
    fill: currentColor;
    flex-shrink: 0;
    filter: brightness(0) invert(1);
    /* 给图标添加微光 */
    filter: brightness(0) invert(1) drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.text {
    font-size: 14px;
    font-weight: 700; /* 加粗字体 */
    white-space: nowrap;
    color: white;
    text-shadow: 
        0 1px 3px rgba(0, 0, 0, 0.4),
        0 1px 0 rgba(255, 255, 255, 0.3); /* 文字浮雕效果 */
    flex: 1;
    letter-spacing: 0.5px; /* 字间距增加高级感 */
}

.arrow-icon {
    width: 16px;
    height: 16px;
    fill: currentColor;
    transition: transform 0.3s ease;
    filter: brightness(0) invert(1) drop-shadow(0 1px 2px rgba(0, 0, 0, 0.3));
}

.arrow-icon.expanded {
    transform: rotate(180deg);
}

.collapsible-content {
    max-height: 0;
    overflow: hidden;
    transition: max-height 0.3s ease, padding 0.3s ease;
    background: rgba(255, 255, 255, 0.98);
    border-radius: 0 0 8px 8px;
    /* 修复白边：确保内容区域与header完美衔接 */
    margin-top: -1px;
    position: relative;
    z-index: 1;
}

.collapsible-content.expanded {
    max-height: 500px;
    padding: 16px;
    /* 内容区域也添加轻微阴影 */
    box-shadow: 
        inset 0 2px 4px rgba(0, 0, 0, 0.05),
        0 2px 8px rgba(0, 0, 0, 0.1);
}

.content-inner {
    color: #333;
    font-size: 14px;
    line-height: 1.5;
}

/* 内容区域内的链接样式 */
.content-inner a {
    color: #1890ff;
    text-decoration: none;
    transition: color 0.3s ease;
}

.content-inner a:hover {
    color: #40a9ff;
}

/* 内容区域内的列表样式 */
.content-inner ul {
    list-style: none;
    padding: 0;
    margin: 8px 0;
}

.content-inner li {
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
}

.content-inner li:last-child {
    border-bottom: none;
}
</style>