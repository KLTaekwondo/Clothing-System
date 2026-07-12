<template>
  <div class="container">
    <div class="header">
      <Navbar></Navbar>
      <button @click="test">管理界面测试按钮</button>
    </div>
    <div class="main-container">
      <div class="left-container">
        <Welcome></Welcome>
      </div>
      <div class="right-container">
        <LoginCard></LoginCard>
      </div>
    </div>
  </div>
</template>

<script setup>
import Welcome from '../components/Welcome.vue'
import Navbar from '../components/Navbar.vue'
import LoginCard from '../components/LoginCard.vue'
import { useRouter } from 'vue-router'
const router = useRouter()
const test = () => {
  router.push('/dashboard')
}
</script>

<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.container {
  min-height: 100vh;
  background: linear-gradient(45deg, rgb(0, 119, 255) 0%, white 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 防止滚动 */
}

.header {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.main-container {
    flex: 1;
    display: grid;
    grid-template-columns: 1fr 500px; /* 左侧自适应，右侧固定500px */
    height: calc(100vh - 70px);
    padding: 0;
    gap: 0;
    max-width: none;
    margin: 0;
    width: 100%;
    overflow: hidden;
}

.left-container, .right-container {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 60px;
}

.left-container {
  flex: 1;
  backdrop-filter: blur(10px);
}

.right-container {
  flex: 0 0 500px;
}

/* 桌面端优化 - 移除响应式设计，专注于桌面体验 */
.left-container,
.right-container {
  height: 100%;
}

/* 滚动条样式优化 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 添加一些桌面端特有的优化 */
@media (min-width: 1200px) {
  .left-container {
    padding: 80px;
  }
  
  .right-container {
    flex: 0 0 550px;
    padding: 80px;
  }
}

/* 超大屏幕优化 */
@media (min-width: 1600px) {
  .left-container {
    padding: 100px;
  }
  
  .right-container {
    flex: 0 0 600px;
    padding: 100px;
  }
}

/* 确保在超宽屏幕上内容不会过度拉伸 */
@media (min-width: 2000px) {
  .main-container {
    max-width: 2000px;
    margin: 0 auto;
  }
}
</style>