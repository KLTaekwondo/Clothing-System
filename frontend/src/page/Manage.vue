<template>
    <div class="manage-container">
        <aside :class="{ collapsed }" class="sidebar">
            <div class="sidebar-logo">
                <span class="logo-icon">👕</span>
                <span class="logo-text">服装管理</span>
                <button class="collapse-btn" @click="collapsed = !collapsed">{{ collapsed ? '▶' : '◀' }}</button>
            </div>
            <nav class="sidebar-nav">
                <router-link v-for="item in navItems" :key="item.path" :title="collapsed ? item.label : ''" :to="item.path" active-class="nav-item-active"
                             class="nav-item"
                             @click.prevent="openTab(item.path, item.label, item.icon)">
                    <span class="nav-icon">{{ item.icon }}</span>
                    <span class="nav-label">{{ item.label }}</span>
                </router-link>
            </nav>
            <div class="sidebar-footer">
                <button :title="collapsed ? '退出登录' : ''" class="nav-item nav-logout" @click="handleLogout">
                    <span class="nav-icon">🚪</span>
                    <span class="nav-label">退出登录</span>
                </button>
            </div>
        </aside>
        <div class="right-area">
            <div v-if="tabs.length > 0" class="tab-bar">
                <div class="tab-scroll">
                    <div v-for="tab in tabs" :key="tab.path" :class="{ 'tab-active': tab.path === currentPath }"
                         class="tab-item"
                         @click="switchTab(tab.path)">
                        <span>{{ tab.label }}</span>
                        <button :title="'关闭 ' + tab.label" class="tab-close" @click.stop="closeTab(tab.path)">
                            &times;
                        </button>
                    </div>
                </div>
            </div>
            <main :class="{ 'has-tabs': tabs.length > 0 }" class="main-area">
                <div class="content-body">
                    <router-view/>
                </div>
            </main>
        </div>
    </div>
</template>

<script setup>
import {ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '../stores/userStore.js'
import adminInterface from '../axios/interface/AdminInterface.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)
const currentPath = ref('')
const tabs = ref(loadSavedTabs())

const navItems = [
    {path: '/manage/dashboard', label: '仪表盘', icon: '📊'},
    {path: '/manage/product', label: '商品管理', icon: '📦'},
    {path: '/manage/employee', label: '员工管理', icon: '👤'},
    {path: '/manage/warehouse', label: '仓库管理', icon: '🏭'},
    {path: '/manage/stock', label: '库存管理', icon: '📊'},
    {path: '/manage/stock/transfer', label: '库存转移', icon: '🔄'},
    {path: '/manage/stock/record', label: '库存记录', icon: '📄'},
    {path: '/manage/order', label: '订单管理', icon: '📋'},
    {path: '/manage/import-order', label: '采购订单', icon: '📥'},
    {path: '/manage/transfer-order', label: '调拨订单', icon: '🔄'},
    {path: '/manage/option', label: '选项管理', icon: '🏷️'}
]

function loadSavedTabs() {
    try {
        const saved = JSON.parse(sessionStorage.getItem('clothing_manage_tabs') || '[]')
        return Array.isArray(saved) ? saved : []
    } catch {
        sessionStorage.removeItem('clothing_manage_tabs')
        return []
    }
}

// 监听路由变化，自动加 tab
watch(() => route.path, (path) => {
    currentPath.value = path
    const meta = route.meta
    const label = meta?.title || path.split('/').pop() || '未知'
    sessionStorage.setItem('clothing_current_route', JSON.stringify({name: route.name}))
    addTab(path, label)
}, {immediate: true})

watch(tabs, (value) => {
    sessionStorage.setItem('clothing_manage_tabs', JSON.stringify(value))
}, {deep: true})

function addTab(path, label) {
    if (!tabs.value.some(t => t.path === path)) {
        tabs.value.push({path, label})
    }
}

function openTab(path, label) {
    addTab(path, label)
    router.push(path)
}

function switchTab(path) {
    currentPath.value = path
    router.push(path)
}

function closeTab(path) {
    const idx = tabs.value.findIndex(t => t.path === path)
    if (idx === -1) return
    tabs.value.splice(idx, 1)
    // 如果关闭的是当前 tab，切到相邻 tab
    if (path === currentPath.value && tabs.value.length > 0) {
        const target = tabs.value[Math.min(idx, tabs.value.length - 1)]
        router.push(target.path)
    }
    // 没有 tab 了，回到首页
    if (tabs.value.length === 0) {
        router.push('/manage/dashboard')
    }
}

async function handleLogout() {
    try {
        await adminInterface.logout()
    } catch {
        // 无论后端会话是否已失效，都要清理本地登录状态
    } finally {
        userStore.logout()
        tabs.value = []
        sessionStorage.removeItem('clothing_manage_tabs')
        sessionStorage.removeItem('clothing_current_route')
        await router.push('/')
    }
}
</script>

<style scoped>
.manage-container {
    display: flex;
    height: 100vh;
    background: var(--bg-body);
}

/* ── 侧栏 ── */
.sidebar {
    width: 236px;
    background: rgba(255, 255, 255, .88);
    backdrop-filter: blur(18px);
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    border-right: 1px solid #dfece9;
    box-shadow: 8px 0 28px rgba(22, 83, 78, .04);
    transition: width .3s;
    overflow: hidden;
}

.sidebar.collapsed {
    width: 60px;
}

.sidebar-logo {
    position: relative;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 20px 16px 18px;
    border-bottom: 1px solid #edf4f2;
    min-height: 64px;
}

.logo-icon {
    font-size: 26px;
    flex-shrink: 0;
}

.logo-text {
    font-size: 18px;
    font-weight: 800;
    color: #2c3e50;
    white-space: nowrap;
    transition: opacity .2s;
}

.sidebar.collapsed .logo-text {
    opacity: 0;
}

.collapse-btn {
    position: absolute;
    right: 6px;
    top: 22px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: #fff;
    border: 1px solid #dceae7;
    color: #64807e;
    font-size: 10px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2;
}

.collapse-btn:hover {
    background: #eef9f7;
    border-color: #0d9488;
    color: #0d9488;
}

.sidebar-nav {
    flex: 1;
    padding: 16px 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 11px;
    padding: 11px 14px;
    border-radius: 10px;
    color: #64807e;
    font-size: 14px;
    cursor: pointer;
    transition: all .2s;
    text-decoration: none;
    border: none;
    background: none;
    width: 100%;
    text-align: left;
    white-space: nowrap;
}

.sidebar.collapsed .nav-item {
    padding: 11px 10px;
    justify-content: center;
}

.nav-item:hover {
    background: #f5f5f0;
    color: #2c3e50;
}

.nav-item-active {
    background: linear-gradient(90deg, #d9f6f1, #effbf9) !important;
    color: #0f766e !important;
    font-weight: 700;
    box-shadow: inset 3px 0 0 #0d9488;
}

.nav-icon {
    font-size: 17px;
    width: 22px;
    text-align: center;
    flex-shrink: 0;
}

.nav-label {
    transition: opacity .2s;
}

.sidebar.collapsed .nav-label {
    opacity: 0;
    width: 0;
    display: inline-block;
    overflow: hidden;
}

.sidebar-footer {
    padding: 8px 8px 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.nav-logout:hover {
    background: rgb(254 226 226 / .8);
    color: #dc2626;
}

/* ── 右侧区域 ── */
.right-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
}

/* ── 标签栏 ── */
.tab-bar {
    display: flex;
    align-items: center;
    height: 40px;
    flex-shrink: 0;
    background: #f2f8f6;
    border-bottom: 1px solid #dfece9;
    padding: 0 8px;
}

.tab-scroll {
    display: flex;
    align-items: center;
    gap: 2px;
    overflow-x: auto;
    flex: 1;
    scrollbar-width: none;
}

.tab-scroll::-webkit-scrollbar {
    display: none;
}

.tab-item {
    display: flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 0 10px 0 14px;
    border-radius: 6px 6px 0 0;
    background: transparent;
    color: #64807e;
    font-size: 13px;
    cursor: pointer;
    white-space: nowrap;
    transition: all .15s;
    user-select: none;
    border: 1px solid transparent;
    border-bottom: none;
    margin-bottom: 0;
    position: relative;
}

.tab-item:hover {
    background: rgba(13, 148, 136, .06);
    color: #0d9488;
}

.tab-active {
    background: #fff !important;
    color: #0d9488 !important;
    font-weight: 700;
    border-color: #dfece9;
}

.tab-close {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    border: none;
    background: none;
    color: #9ab0ad;
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    line-height: 1;
}

.tab-close:hover {
    background: rgba(239, 68, 68, .12);
    color: #dc2626;
}

/* ── 主内容区 ── */
.main-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.main-area.has-tabs {
}

.content-body {
    flex: 1;
    padding: 30px 34px;
    overflow-y: auto;
}
</style>
