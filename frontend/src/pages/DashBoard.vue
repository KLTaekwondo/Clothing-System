<template>
  <div class="maincontainer">
    <!-- 添加收起按钮 -->
    <div class="header" :class="{ 'collapsed': isCollapsed }">
      <Navbar /> 
    </div>
    <div class="collapse-btn" 
         @click="toggleSidebar" 
         :class="{ 'collapsed': isCollapsed }">
      <span class="icon">{{ isCollapsed ? '▶' : '◀' }}</span>
      <span class="text">{{ isCollapsed ? '展开' : '收起' }}</span>
    </div>
    
    <div 
      class="leftcontainer" 
      :class="{ 'collapsed': isCollapsed }"
    >
      <LeftGuideBar />
    </div>
    <div 
      class="rightcontainer" 
      :class="{ 'expanded': isCollapsed }"
    >
      <!-- 将标签页和路由视图包装在一起 -->
      <div class="content-with-tabs">
        <ChromeTabs />
        <div class="floating-card-container">
          <div class="floating-card">
            <div class="router-content">
              <router-view />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import LeftGuideBar from '../components/LeftGuideBar.vue'
import Navbar from '../components/Navbar.vue'
import ChromeTabs from '../components/ChromeTabs.vue'

const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<style scoped>
.maincontainer {
  display: flex;
  height: 100vh;
  width: 100%;
  position: relative;
  overflow: hidden;
}

.header {
  position: absolute;
  background: linear-gradient(45deg, rgb(0, 221, 255) 0%,rgb(0, 145, 255) 50%, white 100%);
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  z-index: 999;
  box-shadow: 
    inset 3px 3px 6px rgba(0, 0, 0, 0.1),
    inset -3px -3px 6px rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  opacity: 0;
  visibility: hidden;
}

.header.collapsed {
  opacity: 1;
  visibility: visible;
}

.collapse-btn {
  position: absolute;
  top: 350px;
  left: 250px;
  width: 20px;
  height: 40px;
  border-radius: 0 0 10px 10px;
  background: linear-gradient(225deg, rgb(0, 221, 255) 0%, rgb(0, 145, 255) 50%, white 100%);
  color: white;
  display: flex;
  align-items: center;
  cursor: pointer;
  border-radius: 0 4px 4px 0;
  z-index: 1000;
  transition: all 0.3s ease;
  overflow: hidden;
}

.collapse-btn.collapsed {
  left: 0;
  top: 80px;
}

.collapse-btn:hover {
  background: linear-gradient(45deg, rgb(0, 221, 255) 0%, rgb(0, 145, 255) 50%, white 100%);
  width: 60px;
}

.icon {
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.text {
  font-size: 12px;
  white-space: nowrap;
  color: white;
  font-weight: bold;
  opacity: 0;
  transition: opacity 0.2s ease;
  margin-left: 4px;
}

.collapse-btn:hover .text {
  opacity: 1;
}

.leftcontainer {
  width: 250px;
  height: 100%;
  background: linear-gradient(45deg, rgb(0, 221, 255) 0%,rgb(0, 145, 255) 50%, white 100%);
  flex-shrink: 0;
  transition: all 0.3s ease;
  overflow: hidden;
  position: relative;
  z-index: 100;
}

.leftcontainer.collapsed {
  width: 0;
  min-width: 0;
  visibility: hidden;
  opacity: 0;
}

.rightcontainer {
  flex: 1;
  overflow: hidden;
  background: transparent;
  transition: all 0.3s ease;
  margin-top: 0;
  display: flex;
  flex-direction: column;
  position: relative;
}

.rightcontainer.expanded {
  margin-left: 0;
  margin-top: 50px;
}

/* 新增：悬浮卡片容器 */
.content-with-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  padding: 20px;
  position: relative;
}

.floating-card-container {
  flex: 1;
  display: flex;
  position: relative;
  overflow: hidden;
}

.floating-card {
  flex: 1;
  background: white;
  border-radius: 16px;
  box-shadow: 
    0 10px 40px rgba(0, 0, 0, 0.1),
    0 2px 15px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  position: relative;
  transition: all 0.3s ease;
  margin: 8px; /* 为阴影留出空间 */
}

/* 悬浮卡片的微光效果 */
.floating-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.8) 50%, 
    transparent 100%);
  z-index: 2;
}

.floating-card:hover {
  box-shadow: 
    0 15px 50px rgba(0, 0, 0, 0.15),
    0 5px 20px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
}

.router-content {
  height: 100%;
  overflow-y: auto;
  padding: 0;
  border-radius: 16px;
}

/* 滚动条样式美化 */
.router-content::-webkit-scrollbar {
  width: 6px;
}

.router-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 3px;
}

.router-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.router-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .content-with-tabs {
    padding: 10px;
  }
  
  .floating-card {
    border-radius: 12px;
    margin: 5px;
  }
  
  .floating-card:hover {
    transform: none; /* 移动端取消悬浮效果 */
  }
}
</style>