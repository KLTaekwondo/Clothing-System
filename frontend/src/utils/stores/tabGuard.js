// 标签页守卫 - 防止多标签页登录冲突
const STORAGE_KEY = 'clothing_tab_session'

export function setupTabGuard(router) {
    // 在页面加载时检查是否已有活跃标签页
    const sessionId = Date.now().toString(36) + Math.random().toString(36).slice(2, 6)
    const existing = sessionStorage.getItem(STORAGE_KEY)

    if (!existing) {
        sessionStorage.setItem(STORAGE_KEY, sessionId)
    }

    // 监听 beforeunload，清理当前标签页标记
    window.addEventListener('beforeunload', () => {
        sessionStorage.removeItem(STORAGE_KEY)
    })
}

export function restoreCurrentTab(router) {
    // 恢复当前标签页的路由状态
    const saved = sessionStorage.getItem('clothing_current_route')
    if (saved) {
        try {
            const route = JSON.parse(saved)
            if (route.name && router.hasRoute(route.name)) {
                router.push({ name: route.name })
            }
        } catch {
            // 忽略解析错误
        }
    }
}
