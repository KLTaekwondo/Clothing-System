import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import adminInterface from '../axios/interface/AdminInterface.js'
import wareHouseInterface from '../axios/interface/wareHouseInterface.js'


export const useUserStore = defineStore('user', () => {
    // 用户信息
    const savedUserInfo = localStorage.getItem('userInfo')
    const userInfo = ref(savedUserInfo ? JSON.parse(savedUserInfo) : null)
    // 是否登录
    const isLoggedIn = computed(() => userInfo.value !== null)

    // 登录用户
    async function login(data,role){
        let loginUserInfo = null

        // 管理员登录
        if(role === 'admin'){
            loginUserInfo = await adminInterface.login(data)
        }

        // 仓库登录
        if(role === 'warehouse'){
            loginUserInfo = await wareHouseInterface.login(data)
        }

        if (loginUserInfo) {
            userInfo.value = loginUserInfo
            localStorage.setItem('userInfo',JSON.stringify(loginUserInfo))
        }
    }

    // 登出用户
    function logout() {
        userInfo.value = null
        localStorage.removeItem('userInfo')
    }
    return { userInfo, isLoggedIn,login, logout }
})