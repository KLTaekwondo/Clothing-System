
const excludePaths = ['/Home', '/404','/dashboard','/']

export function setupTabGuard(router) {
  router.afterEach((to) => {
    if (!excludePaths.includes(to.path) && to.name) {
      import('./tabStore.js').then(({ useTabStore }) => {
        const tabStore = useTabStore()
        const tabInfo = {
          name: to.name,
          path: to.path,
          fullPath: to.fullPath,
          title: to.meta?.title || to.name || '未命名页面',
          query: { ...to.query },
          params: { ...to.params },
          meta: { ...to.meta }
        }
        tabStore.addTab(tabInfo)
      })
    }
  })
}

export function restoreCurrentTab(router) {
  const currentRoute = router.currentRoute.value
  if (currentRoute.name && !excludePaths.includes(currentRoute.path)) {
    import('./tabStore.js').then(({ useTabStore }) => {
      const tabStore = useTabStore()
      const tabInfo = {
        name: currentRoute.name,
        path: currentRoute.path,
        fullPath: currentRoute.fullPath,
        title: currentRoute.meta?.title || currentRoute.name || '未命名页面',
        query: { ...currentRoute.query },
        params: { ...currentRoute.params },
        meta: { ...currentRoute.meta }
      }
      tabStore.addTab(tabInfo)
    })
  }
}
