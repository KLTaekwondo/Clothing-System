<!-- components/ChromeTabs.vue -->
<template>
    <div class="chrome-tabs">
        <div class="tabs-container">
            <!-- 改为从store读取标签页数据 -->
            <div v-for="tab in tabStore.tabs" :key="tab.fullPath" class="tab-item" :class="{ active: isActive(tab) }"
                @click="switchTab(tab)">
                <div class="tab-favicon">📄</div>
                <span class="tab-title">{{ tab.title }}</span>
                <div class="tab-close" @click.stop="closeTab(tab)">×</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useTabStore } from '../utils/stores/tabStore'

const router = useRouter()
const route = useRoute()
const tabStore = useTabStore()

// 检查是否激活
const isActive = (tab) => {
    return tab.fullPath === route.fullPath
}

// 切换标签页 - 改为路由跳转
const switchTab = (tab) => {
    router.push(tab.fullPath)
}

// 关闭标签页 - 改为store操作
const closeTab = async (tab) => {
    // 如果关闭的是当前激活的标签页
    if (isActive(tab)) {
        const currentIndex = tabStore.tabs.findIndex(t => t.fullPath === tab.fullPath)
        let targetTab = null

        // 优先选择右侧标签，没有则选择左侧
        if (currentIndex < tabStore.tabs.length - 1) {
            targetTab = tabStore.tabs[currentIndex + 1]
        } else if (currentIndex > 0) {
            targetTab = tabStore.tabs[currentIndex - 1]
        }

        // 跳转到其他标签页
        if (targetTab) {
            await router.push(targetTab.fullPath)
        } else {
            // 没有其他标签页，跳转到首页或默认页面
            await router.push('/dashboard')
        }
    }

    // 从store中移除标签页
    tabStore.removeTab(tab.fullPath)
}
</script>

<style scoped>
/* 保持原有样式不变 */
.chrome-tabs {
    background: linear-gradient(to right, #d0d0d0 0%, grey 100%);
    border-bottom: 1px solid #d0d0d0;
    padding: 8px 8px 0 8px;
    min-height: 42px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
}

.tabs-container {
    display: flex;
    align-items: flex-end;
    gap: 2px;
}

.tab-item {
    display: flex;
    align-items: center;
    background: #f1f3f4;
    border: 1px solid #d0d0d0;
    border-bottom: none;
    margin-bottom: 8px;
    border-radius: 8px;
    min-width: 160px;
    max-width: 240px;
    height: 36px;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    box-shadow: 0 -1px 2px rgba(0, 0, 0, 0.05);
}

.tab-item:hover {
    background: #f8f9fa;
}

.tab-item.active {
    background: white;
    border-color: #d0d0d0;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    z-index: 2;
    height: 38px;
}

.tab-favicon {
    margin-right: 8px;
    font-size: 14px;
    opacity: 0.8;
}

.tab-title {
    flex: 1;
    font-size: 13px;
    color: #3c4043;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-weight: 500;
}

.tab-close {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-left: 8px;
    opacity: 0.6;
    transition: all 0.2s ease;
    color: #5f6368;
}

.tab-close:hover {
    background: #e0e0e0;
    opacity: 1;
}
</style>