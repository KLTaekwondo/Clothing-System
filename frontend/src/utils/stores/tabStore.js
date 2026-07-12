// stores/tabStore.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useTabStore = defineStore('tab', () => {
  // 状态
  const tabs = ref([])
  const activeTabPath = ref('')
  
  // Getter
  const activeTab = computed(() => {
    return tabs.value.find(tab => tab.fullPath === activeTabPath.value)
  })
  
  const hasTab = (fullPath) => {
    return tabs.value.some(tab => tab.fullPath === fullPath)
  }
  
  const findTabIndex = (fullPath) => {
    return tabs.value.findIndex(tab => tab.fullPath === fullPath)
  }

  // Actions
  const addTab = (tabInfo) => {
    // 避免重复添加
    if (hasTab(tabInfo.fullPath)) {
      setActiveTab(tabInfo.fullPath)
      return
    }
    
    const newTab = {
      id: `${tabInfo.name}-${Date.now()}`,
      name: tabInfo.name,
      path: tabInfo.path,
      fullPath: tabInfo.fullPath,
      title: tabInfo.title || tabInfo.name || '未命名',
      query: tabInfo.query || {},
      params: tabInfo.params || {},
      meta: tabInfo.meta || {}
    }
    
    tabs.value.push(newTab)
    setActiveTab(newTab.fullPath)
  }

  const removeTab = (fullPath) => {
    const index = findTabIndex(fullPath)
    if (index > -1) {
      tabs.value.splice(index, 1)
    }
  }

  const setActiveTab = (fullPath) => {
    if (hasTab(fullPath)) {
      activeTabPath.value = fullPath
    }
  }

  const closeOtherTabs = (keepFullPath) => {
    tabs.value = tabs.value.filter(tab => tab.fullPath === keepFullPath)
    setActiveTab(keepFullPath)
  }

  const closeAllTabs = () => {
    tabs.value = []
    activeTabPath.value = ''
  }

  const getTabByPath = (fullPath) => {
    return tabs.value.find(tab => tab.fullPath === fullPath)
  }

  return {
    // 状态
    tabs,
    activeTabPath,
    
    // Getter
    activeTab,
    hasTab,
    
    // Actions
    addTab,
    removeTab,
    setActiveTab,
    closeOtherTabs,
    closeAllTabs,
    getTabByPath
  }
})