<template>
    <div class="manage-container">
        <aside class="sidebar">
            <div class="sidebar-logo">
                <span class="logo-icon">👕</span>
                <span class="logo-text">服装管理</span>
            </div>
            <nav class="sidebar-nav">
                <router-link
                    v-for="item in navItems"
                    :key="item.path"
                    :to="item.path"
                    class="nav-item"
                    active-class="nav-item-active"
                >
                    <span class="nav-icon">{{ item.icon }}</span>
                    <span class="nav-label">{{ item.label }}</span>
                </router-link>
            </nav>
            <div class="sidebar-footer">
                <router-link to="/checkout" class="nav-item nav-checkout">
                    <span class="nav-icon">🛒</span>
                    <span class="nav-label">收银结账</span>
                </router-link>
                <button class="nav-item nav-logout" @click="handleLogout">
                    <span class="nav-icon">🚪</span>
                    <span class="nav-label">退出登录</span>
                </button>
            </div>
        </aside>
        <main class="main-area">
            <div class="content-body">
                <router-view />
            </div>
        </main>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import adminInterface from '../axios/interface/AdminInterface.js'

const router = useRouter()

const navItems = [
    { path: '/manage/dashboard', label: '仪表盘', icon: '📊' },
    { path: '/manage/product', label: '商品管理', icon: '📦' },
    { path: '/manage/employee', label: '员工管理', icon: '👤' },
    { path: '/manage/warehouse', label: '仓库管理', icon: '🏭' },
    { path: '/manage/option', label: '选项管理', icon: '🏷️' }
]

async function handleLogout() {
    try {
        await adminInterface.logout()
    } catch {
        // 忽略登出错误
    }
    router.push('/')
}
</script>

<style scoped>
.manage-container {
    display: flex;
    height: 100vh;
    background: #f5f5f0;
}

/* ── 侧栏 ── */
.sidebar {
    width: 220px;
    background: #fff;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
    border-right: 1px solid #e8e8e8;
}

.sidebar-logo {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 20px 20px 18px;
    border-bottom: 1px solid #f0f0f0;
}

.logo-icon {
    font-size: 26px;
}

.logo-text {
    font-size: 18px;
    font-weight: 800;
    color: #2c3e50;
    letter-spacing: 1px;
}

/* ── 导航 ── */
.sidebar-nav {
    flex: 1;
    padding: 12px 10px;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: 8px;
    color: #636e72;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    text-decoration: none;
    border: none;
    background: none;
    width: 100%;
    text-align: left;
}

.nav-item:hover {
    background: #f5f5f0;
    color: #2c3e50;
}

.nav-item-active {
    background: rgb(204 251 241 / 0.8) !important;
    color: #0d9488 !important;
    font-weight: 700;
}

.nav-icon {
    font-size: 17px;
    width: 22px;
    text-align: center;
    flex-shrink: 0;
}

.nav-label {
    white-space: nowrap;
}

/* ── 侧栏底部 ── */
.sidebar-footer {
    padding: 8px 10px 16px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.nav-checkout {
    color: #d97706;
}

.nav-checkout:hover {
    background: rgb(254 243 199 / 0.8);
    color: #d97706;
}

.nav-logout:hover {
    background: rgb(254 226 226 / 0.8);
    color: #dc2626;
}

/* ── 主内容区 ── */
.main-area {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.content-body {
    flex: 1;
    padding: 24px;
    overflow-y: auto;
}
</style>
