import {createApp} from 'vue'
import './style.css'
import App from './App.vue'
import router from './router/router.js'
import {createPinia} from 'pinia'
import {restoreCurrentTab, setupTabGuard} from './stores/tabGuard.js'

const app = createApp(App)
const pinia = createPinia()

// 使用插件
app.use(pinia)
app.use(router)

router.isReady().then(() => {
    setupTabGuard(router)
    restoreCurrentTab(router)
})

app.mount('#app')