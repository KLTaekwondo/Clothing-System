<template>
    <div class="manage-container">
        <header class="mobile-header">
            <button
                aria-label="打开导航菜单"
                class="mobile-menu-button"
                type="button"
                @click="mobileMenuOpen = true"
            >
                <span></span>
                <span></span>
                <span></span>
            </button>
            <div class="mobile-brand">
                <img
                    :src="brandMark"
                    alt=""
                />
                <span>KL-KOOLE</span>
            </div>
            <strong class="mobile-page-title">{{ currentPageTitle }}</strong>
        </header>
        <button
            v-if="mobileMenuOpen"
            aria-label="关闭导航菜单"
            class="mobile-sidebar-mask"
            type="button"
            @click="mobileMenuOpen = false"
        ></button>
        <aside
            :class="{
                collapsed,
                'mobile-open': mobileMenuOpen
            }"
            class="sidebar"
        >
            <div class="sidebar-logo">
                <button
                    :title="collapsed ? '展开导航栏' : '收起导航栏'"
                    class="logo-toggle"
                    @click="collapsed = !collapsed"
                >
                    <img
                        :src="brandMark"
                        alt=""
                    />
                </button>
                <span class="logo-text">KL-KOOLE</span>
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
import {computed, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useUserStore} from '../../stores/userStore.js'
import adminInterface from '../../axios/interface/AdminInterface.js'
import dashboardIcon from '../../assets/icons/navigation/dashboard.svg'
import productIcon from '../../assets/icons/navigation/product.svg'
import employeeIcon from '../../assets/icons/navigation/employee.svg'
import memberIcon from '../../assets/icons/navigation/member.svg'
import supplierIcon from '../../assets/icons/supplier.svg'
import warehouseIcon from '../../assets/icons/navigation/warehouse.svg'
import stockIcon from '../../assets/icons/navigation/stock.svg'
import stockViewIcon from '../../assets/icons/navigation/stock-view.svg'
import stockRecordIcon from '../../assets/icons/navigation/stock-record.svg'
import stockCheckIcon from '../../assets/icons/navigation/stock-check.svg'
import orderIcon from '../../assets/icons/navigation/order.svg'
import importOrderIcon from '../../assets/icons/navigation/import-order.svg'
import transferOrderIcon from '../../assets/icons/navigation/transfer-order.svg'
import optionIcon from '../../assets/icons/navigation/option.svg'
import settingsIcon from '../../assets/icons/navigation/settings.svg'
import aiIcon from '../../assets/icons/navigation/ai.svg'
import statisticsIcon from '../../assets/icons/trend.svg'
import logoutIcon from '../../assets/icons/navigation/logout.svg'
import brandMark from '../../assets/brand-mark.svg'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const collapsed = ref(false)
const mobileMenuOpen = ref(false)
const currentPath = ref('')
const tabs = ref(loadSavedTabs())
const currentPageTitle = computed(() => route.meta?.title || '管理后台')

const navItems = [
    {path: '/manage/dashboard', label: '仪表盘', icon: dashboardIcon},
    {path: '/manage/product', label: '商品管理', icon: productIcon},
    {path: '/manage/warehouse', label: '仓库管理', icon: warehouseIcon},
    {path: '/manage/employee', label: '员工管理', icon: employeeIcon},
    {path: '/manage/member', label: '会员管理', icon: memberIcon},
    {path: '/manage/option', label: '选项管理', icon: optionIcon},
    {path: '/manage/supplier', label: '供应商管理', icon: supplierIcon},
    {path: '/manage/stock-check', label: '库存盘点', icon: stockCheckIcon},
    {path: '/manage/import-order', label: '采购订单', icon: importOrderIcon},
    {path: '/manage/transfer-order', label: '调拨订单', icon: transferOrderIcon},
    {path: '/manage/stock/view', label: '查看库存', icon: stockViewIcon},
    {path: '/manage/stock', label: '人工库存调整', icon: stockIcon},
    {path: '/manage/order', label: '订单管理', icon: orderIcon},
    {path: '/manage/stock/record', label: '库存记录', icon: stockRecordIcon},
    {path: '/manage/statistics', label: '数据统计', icon: statisticsIcon},
    {path: '/manage/ai', label: 'AI 助手', icon: aiIcon},
    {path: '/manage/setting', label: '系统设置', icon: settingsIcon}
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
    mobileMenuOpen.value = false
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
    mobileMenuOpen.value = false
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
    position: relative;
    display: flex;
    height: 100vh;
    height: 100dvh;
    background: var(--bg-body);
}

.mobile-header,
.mobile-sidebar-mask {
    display: none;
}

/* ── 侧栏 ── */
.sidebar {
    position: relative;
    width: 236px;
    background: color-mix(in srgb, var(--bg-card) 88%, transparent);
    backdrop-filter: blur(18px);
    display: flex;
    flex-direction: column;
    flex-grow: 0;
    flex-shrink: 0;
    border-right: 1px solid var(--border-light);
    box-shadow: 8px 0 28px rgba(22, 83, 78, 0.04);
    transition: width 0.3s;
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
    border-bottom: 1px solid var(--border-light);
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
    background: var(--bg-hover);
    transform: scale(1.06);
}

.logo-text {
    font-size: 18px;
    font-weight: 800;
    color: var(--text);
    white-space: nowrap;
    transition: opacity 0.2s;
}

.sidebar.collapsed .sidebar-logo {
    justify-content: center;
    gap: 0;
    padding: 20px 8px 18px;
}

.sidebar.collapsed .logo-text {
    display: none;
}

.collapse-btn {
    position: absolute;
    left: 100%;
    top: 50%;
    width: 24px;
    height: 42px;
    border-radius: 0 10px 10px 0;
    background: var(--bg-card);
    border: 1px solid var(--border);
    border-left: none;
    color: var(--text-secondary);
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
    background: var(--bg-hover);
    border-color: var(--border-hover);
    color: var(--primary);
    box-shadow: 4px 2px 12px rgba(13, 148, 136, 0.16);
}

.sidebar-nav {
    flex-grow: 1;
    min-height: 0;
    padding: 16px 8px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    box-shadow: 0 2px 7px rgba(22, 83, 78, 0.06) inset;
    border-top: 2px solid var(--border-light);
    border-bottom: 2px solid var(--border-light);
    /* 菜单多时在导航区内滚动，退出登录始终贴底可见 */
    overflow-y: auto;
    /* 隐藏滚动条：导航区可滚动是常识，不需要滚动条占位 */
    scrollbar-width: none;
}

.sidebar-nav::-webkit-scrollbar {
    display: none;
}

.nav-item {
    display: flex;
    align-items: center;
    gap: 11px;
    padding: 11px 14px;
    border-radius: 10px;
    color: var(--text-secondary);
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;
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
    background: var(--bg-card);
    border-color: var(--border-light);
    color: var(--text);
    box-shadow: 0 5px 12px rgba(22, 83, 78, 0.1);
    transform: translateY(-1px);
}

.nav-item-active {
    position: relative;
    background: linear-gradient(90deg, var(--primary-light), var(--bg-hover)) !important;
    border-color: var(--border-hover) !important;
    color: var(--primary-dark) !important;
    font-weight: 700;
    box-shadow: inset 3px 0 0 var(--primary), 0 4px 12px rgba(13, 148, 136, 0.12);
}

.sidebar.collapsed .nav-item-active {
    background: var(--bg-hover) !important;
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
    background: var(--primary);
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
    filter: invert(49%) sepia(61%) saturate(769%) hue-rotate(123deg) brightness(91%) contrast(93%);
}

.sidebar-nav .nav-item:nth-child(9) .nav-icon {
    filter: invert(45%) sepia(91%) saturate(1774%) hue-rotate(202deg) brightness(98%) contrast(94%);
}

.sidebar-nav .nav-item:nth-child(10) .nav-icon {
    filter: invert(57%) sepia(93%) saturate(1277%) hue-rotate(346deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(11) .nav-icon {
    filter: invert(45%) sepia(68%) saturate(1230%) hue-rotate(234deg) brightness(90%) contrast(91%);
}

.sidebar-nav .nav-item:nth-child(12) .nav-icon {
    filter: invert(71%) sepia(85%) saturate(1900%) hue-rotate(358deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(13) .nav-icon {
    filter: invert(71%) sepia(85%) saturate(1900%) hue-rotate(358deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(14) .nav-icon {
    filter: invert(71%) sepia(85%) saturate(1900%) hue-rotate(358deg) brightness(101%) contrast(96%);
}

.sidebar-nav .nav-item:nth-child(15) .nav-icon {
    filter: invert(54%) sepia(11%) saturate(578%) hue-rotate(124deg) brightness(91%) contrast(88%);
}

.sidebar-nav .nav-item:nth-child(16) .nav-icon {
    filter: invert(45%) sepia(68%) saturate(1230%) hue-rotate(234deg) brightness(90%) contrast(91%);
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
    border-top: 1px solid var(--border-light);
    display: flex;
    flex-direction: column;
    gap: 2px;
}

.nav-logout:hover {
    background: var(--error-light);
    color: var(--error-dark);
}

/* 深色模式适配 */
[data-theme="dark"] .sidebar {
    background: color-mix(in srgb, var(--bg-card) 80%, transparent);
    border-right-color: var(--border);
    box-shadow: 8px 0 28px rgba(0, 0, 0, 0.25);
}

[data-theme="dark"] .tab-bar {
    background: var(--bg-body);
    border-bottom-color: var(--border);
}

[data-theme="dark"] .mobile-header {
    background: color-mix(in srgb, var(--bg-card) 96%, transparent);
    border-bottom-color: var(--border);
}

[data-theme="dark"] .mobile-sidebar-mask {
    background: rgba(0, 0, 0, 0.55);
}

[data-theme="dark"] .collapse-btn {
    background: var(--bg-card);
    border-color: var(--border);
}

/* ── 右侧区域 ── */
.right-area {
    width: calc(100% - var(--sidebar-width));
    height: 100dvh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
}

.manage-container:has(.sidebar.collapsed) .right-area {
    width: calc(100% - 60px);
}

/* ── 标签栏 ── */
.tab-bar {
    display: flex;
    align-items: center;
    height: var(--tab-height);
    flex-shrink: 0;
    background: var(--bg-subtle);
    border-bottom: 1px solid var(--border);
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
    color: var(--text-secondary);
    font-size: 13px;
    cursor: pointer;
    white-space: nowrap;
    transition: all 0.15s;
    user-select: none;
    border: 1px solid transparent;
    border-bottom: none;
    margin-bottom: 0;
    position: relative;
}

.tab-item:hover {
    background: var(--bg-hover);
    color: var(--primary);
}

.tab-active {
    background: var(--bg-card) !important;
    color: var(--primary) !important;
    font-weight: 700;
    border-color: var(--border);
}

.tab-close {
    width: 18px;
    height: 18px;
    border-radius: 4px;
    border: none;
    background: none;
    color: var(--text-muted);
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0;
    line-height: 1;
}

.tab-close:hover {
    background: var(--error-light);
    color: var(--error-dark);
}

/* ── 主内容区 ── */
.main-area {
    height: 100dvh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.main-area.has-tabs {
    height: calc(100dvh - var(--tab-height));
}

.content-body {
    height: 100%;
    padding: 30px 34px;
    overflow-y: auto;
}

@media (max-width: 900px) {
    .manage-container {
        display: block;
        padding-top: 56px;
        overflow: hidden;
    }

    .mobile-header {
        position: fixed;
        top: 0;
        right: 0;
        left: 0;
        z-index: 40;
        height: 56px;
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 0 16px;
        border-bottom: 1px solid var(--border);
        background: color-mix(in srgb, var(--bg-card) 96%, transparent);
        backdrop-filter: blur(18px);
        box-shadow: 0 4px 16px rgba(22, 83, 78, 0.08);
    }

    .mobile-menu-button {
        width: 38px;
        height: 38px;
        flex-shrink: 0;
        display: flex;
        flex-direction: column;
        gap: 4px;
        padding: 9px;
        border: 1px solid var(--border);
        background: var(--bg-subtle);
    }

    .mobile-menu-button span {
        width: 18px;
        height: 2px;
        border-radius: 2px;
        background: var(--primary-dark);
    }

    .mobile-brand {
        display: flex;
        align-items: center;
        gap: 7px;
        color: var(--text);
        font-size: 14px;
        font-weight: 800;
        white-space: nowrap;
    }

    .mobile-brand :deep(img) {
        width: 25px;
        height: 25px;
    }

    .mobile-page-title {
        min-width: 0;
        margin-left: auto;
        overflow: hidden;
        color: var(--text-secondary);
        font-size: 13px;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .mobile-sidebar-mask {
        position: fixed;
        inset: 56px 0 0;
        z-index: 45;
        width: 100%;
        height: calc(100dvh - 56px);
        display: block;
        border-radius: 0;
        background: rgba(11, 48, 45, 0.38);
        backdrop-filter: blur(2px);
    }

    .sidebar,
    .sidebar.collapsed {
        position: fixed;
        top: 56px;
        bottom: 0;
        left: 0;
        z-index: 50;
        width: min(82vw, 300px);
        transform: translateX(-105%);
        transition: transform 0.25s var(--ease-out);
        box-shadow: 14px 0 34px rgba(11, 48, 45, 0.18);
    }

    .sidebar.mobile-open {
        transform: translateX(0);
    }

    .sidebar-logo {
        display: none;
    }

    .sidebar.collapsed .nav-item {
        width: 100%;
        align-self: stretch;
        justify-content: flex-start;
        gap: 11px;
        padding: 11px 14px;
    }

    .sidebar.collapsed .nav-label {
        width: auto;
        display: inline;
        overflow: visible;
        opacity: 1;
    }

    .sidebar.collapsed .nav-item-active {
        background: linear-gradient(90deg, #d9f6f1, #effbf9) !important;
        border-color: #b9ded7 !important;
        box-shadow: inset 3px 0 0 #0d9488, 0 4px 12px rgba(13, 148, 136, 0.12);
    }

    .sidebar.collapsed .nav-item-active::after {
        display: none;
    }

    .sidebar-nav {
        min-height: 0;
        padding-top: 14px;
        overflow-y: auto;
    }

    .right-area {
        width: 100%;
        height: calc(100dvh - 56px);
    }

    .main-area {
        height: calc(100dvh - 56px);
    }

    .main-area.has-tabs {
        height: calc(100dvh - 94px);
    }

    .tab-bar {
        height: 38px;
        padding: 0 6px;
    }

    .tab-item {
        height: 30px;
        padding: 0 8px 0 11px;
        font-size: 12px;
    }

    .content-body {
        padding: 20px 18px;
    }
}

@media (max-width: 560px) {
    .mobile-header {
        gap: 9px;
        padding: 0 10px;
    }

    .mobile-brand span {
        display: none;
    }

    .content-body {
        padding: 14px 10px 22px;
    }
}
</style>
