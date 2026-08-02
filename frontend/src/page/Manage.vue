<template>
    <div class="manage-container">
        <aside :class="{ collapsed }" class="sidebar">
            <div class="sidebar-logo">
                <button
                    :title="collapsed ? '展开导航栏' : '收起导航栏'"
                    class="logo-toggle"
                    @click="collapsed = !collapsed"
                >
                    <IconGraphic name="clothing"/>
                </button>
                <span class="logo-text">服装管理</span>
            </div>
            <nav class="sidebar-nav">
                <router-link v-for="item in navItems" :key="item.path" :title="collapsed ? item.label : ''" :to="item.path" active-class="nav-item-active"
                             class="nav-item"
                             @click.prevent="openTab(item.path, item.label, item.icon)">
                    <img
                        :src="item.icon"
                        alt=""
                        class="nav-icon"
                    />
                    <span class="nav-label">{{ item.label }}</span>
                </router-link>
            </nav>
            <div class="sidebar-footer">
                <button :title="collapsed ? '退出登录' : ''" class="nav-item nav-logout" @click="handleLogout">
                    <img
                        :src="logoutIcon"
                        alt=""
                        class="nav-icon"
                    />
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
import dashboardIcon from '../assets/icons/navigation/dashboard.svg'
import productIcon from '../assets/icons/navigation/product.svg'
import employeeIcon from '../assets/icons/navigation/employee.svg'
import supplierIcon from '../assets/icons/navigation/supplier.svg'
import warehouseIcon from '../assets/icons/navigation/warehouse.svg'
import stockIcon from '../assets/icons/navigation/stock.svg'
import stockRecordIcon from '../assets/icons/navigation/stock-record.svg'
import orderIcon from '../assets/icons/navigation/order.svg'
import importOrderIcon from '../assets/icons/navigation/import-order.svg'
import transferOrderIcon from '../assets/icons/navigation/transfer-order.svg'
import optionIcon from '../assets/icons/navigation/option.svg'
import logoutIcon from '../assets/icons/navigation/logout.svg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)
const currentPath = ref('')
const tabs = ref(loadSavedTabs())

const navItems = [
    {path: '/manage/dashboard', label: '仪表盘', icon: dashboardIcon},
    {path: '/manage/product', label: '商品管理', icon: productIcon},
    {path: '/manage/employee', label: '员工管理', icon: employeeIcon},
    {path: '/manage/supplier', label: '供应商管理', icon: supplierIcon},
    {path: '/manage/warehouse', label: '仓库管理', icon: warehouseIcon},
    {path: '/manage/stock', label: '库存管理', icon: stockIcon},
    {path: '/manage/stock/record', label: '库存记录', icon: stockRecordIcon},
    {path: '/manage/order', label: '订单管理', icon: orderIcon},
    {path: '/manage/import-order', label: '采购订单', icon: importOrderIcon},
    {path: '/manage/transfer-order', label: '调拨订单', icon: transferOrderIcon},
    {path: '/manage/option', label: '选项管理', icon: optionIcon}
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
    position: relative;
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

.logo-toggle {
    width: 30px;
    height: 30px;
    padding: 3px;
    border-radius: 9px;
    background: transparent;
    flex-shrink: 0;
}

.logo-toggle img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.logo-toggle:hover {
    background: #effbf9;
    transform: scale(1.06);
}

.logo-text {
    font-size: 18px;
    font-weight: 800;
    color: #2c3e50;
    white-space: nowrap;
    transition: opacity .2s;
}

.sidebar.collapsed .sidebar-logo {
    justify-content: center;
    padding: 20px 8px 18px;
}

.sidebar.collapsed .logo-text {
    opacity: 0;
}

.collapse-btn {
    position: absolute;
    left: 100%;
    top: 50%;
    width: 24px;
    height: 42px;
    border-radius: 0 10px 10px 0;
    background: #ffffff;
    border: 1px solid #dceae7;
    border-left: none;
    color: #64807e;
    font-size: 22px;
    font-weight: 400;
    line-height: 1;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transform: translateY(-50%);
    box-shadow: 4px 2px 10px rgba(22, 83, 78, 0.08);
    z-index: 3;
}

.collapse-btn:hover {
    background: #effbf9;
    border-color: #9fd8cf;
    color: #0d9488;
    box-shadow: 4px 2px 12px rgba(13, 148, 136, 0.16);
}

.sidebar-nav {
    flex: 1;
    padding: 16px 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    box-shadow: 0 2px 7px rgba(22, 83, 78, 0.06) inset;
    border-top: 2px solid #dfece9;
    border-bottom: 2px solid #dfece9;
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
    width: 100%;
    text-align: left;
    white-space: nowrap;
}

.sidebar.collapsed .nav-item {
    width: 44px;
    align-self: center;
    gap: 0;
    padding: 11px 10px;
    justify-content: center;
}

.nav-item:hover {
    background: #fff;
    border-color: #cce4df;
    color: #2c3e50;
    box-shadow: 0 5px 12px rgba(22, 83, 78, 0.1);
    transform: translateY(-1px);
}

.nav-item-active {
    position: relative;
    background: linear-gradient(90deg, #d9f6f1, #effbf9) !important;
    border-color: #b9ded7 !important;
    color: #0f766e !important;
    font-weight: 700;
    box-shadow: inset 3px 0 0 #0d9488, 0 4px 12px rgba(13, 148, 136, 0.12);
}

.sidebar.collapsed .nav-item-active {
    background: #effbf9 !important;
    border-color: transparent !important;
    box-shadow: none;
}

.sidebar.collapsed .nav-item-active::after {
    content: '';
    position: absolute;
    left: 9px;
    right: 9px;
    bottom: 3px;
    height: 3px;
    border-radius: 3px;
    background: #0d9488;
    box-shadow: 0 1px 5px rgba(13, 148, 136, 0.35);
}

.nav-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    object-fit: contain;
    opacity: 0.95;
    transition: opacity 0.2s, transform 0.2s;
}

.nav-item:hover .nav-icon {
    opacity: 1;
    transform: scale(1.06);
}

.sidebar-nav .nav-item:nth-child(1) .nav-icon {
    filter: invert(45%) sepia(91%) saturate(1774%) hue-rotate(202deg) brightness(98%) contrast(94%);
}

.sidebar-nav .nav-item:nth-child(2) .nav-icon {
    filter: invert(57%) sepia(93%) saturate(1277%) hue-rotate(346deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(3) .nav-icon {
    filter: invert(49%) sepia(61%) saturate(769%) hue-rotate(123deg) brightness(91%) contrast(93%);
}

.sidebar-nav .nav-item:nth-child(4) .nav-icon {
    filter: invert(45%) sepia(68%) saturate(1230%) hue-rotate(234deg) brightness(90%) contrast(91%);
}

.sidebar-nav .nav-item:nth-child(5) .nav-icon {
    filter: invert(49%) sepia(61%) saturate(769%) hue-rotate(123deg) brightness(91%) contrast(93%);
}

.sidebar-nav .nav-item:nth-child(6) .nav-icon {
    filter: invert(71%) sepia(85%) saturate(1900%) hue-rotate(358deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(7) .nav-icon {
    filter: invert(54%) sepia(11%) saturate(578%) hue-rotate(124deg) brightness(91%) contrast(88%);
}

.sidebar-nav .nav-item:nth-child(8) .nav-icon {
    filter: invert(45%) sepia(91%) saturate(1774%) hue-rotate(202deg) brightness(98%) contrast(94%);
}

.sidebar-nav .nav-item:nth-child(9) .nav-icon {
    filter: invert(57%) sepia(93%) saturate(1277%) hue-rotate(346deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(10) .nav-icon {
    filter: invert(45%) sepia(68%) saturate(1230%) hue-rotate(234deg) brightness(90%) contrast(91%);
}

.sidebar-nav .nav-item:nth-child(11) .nav-icon {
    filter: invert(71%) sepia(85%) saturate(1900%) hue-rotate(358deg) brightness(101%) contrast(96%);
}

.nav-item-active .nav-icon {
    opacity: 1;
}

.nav-logout .nav-icon {
    filter: invert(31%) sepia(91%) saturate(2210%) hue-rotate(344deg) brightness(91%) contrast(91%);
}

.nav-logout:hover .nav-icon {
    opacity: 1;
}

.nav-label {
    transition: opacity .2s;
}

.sidebar.collapsed .nav-label {
    opacity: 0;
    width: 0;
    display: none;
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
