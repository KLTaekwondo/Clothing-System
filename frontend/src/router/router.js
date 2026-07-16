import {createRouter, createWebHistory} from 'vue-router'
import { useUserStore } from '../stores/userStore.js'
import ManageChildren from './ManageChildren.js'

const routes = [
    {
        path: '/',
        name: 'Login',
        component: () => import('../page/index.vue'),
        meta: {title: '登录'}
    },
    {
        path: '/manage',
        name: 'Manage',
        component: () => import('../page/Manage.vue'),
        meta: {title: '管理后台'},
        redirect: '/manage/dashboard',
        children: ManageChildren
    },
    {
        path: '/checkout',
        name: 'Checkout',
        component: () => import('../page/Checkout.vue'),
        meta: {title: '收银结账'}
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const isLoginPage = to.path === '/'
    const isManagePage = to.path === '/manage' || to.path.startsWith('/manage/')
    const isCheckoutPage = to.path === '/checkout' || to.path.startsWith('/checkout/')

    if (isLoginPage) {
        next()
        return
    }

    if ((isManagePage || isCheckoutPage) && !userStore.isLoggedIn) {
        next('/')
        return
    }

    next()
})
export default router
