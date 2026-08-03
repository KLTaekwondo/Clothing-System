import {createRouter, createWebHistory} from 'vue-router'
import { useUserStore } from '../stores/userStore.js'
import ManageChildren from './ManageChildren.js'

const routes = [
    {
        path: '/',
        name: 'Login',
        component: () => import('../page/auth/Login.vue'),
        meta: {title: '登录'}
    },
    {
        path: '/manage',
        name: 'Manage',
        component: () => import('../page/manage/Manage.vue'),
        meta: {title: '管理后台'},
        redirect: '/manage/dashboard',
        children: ManageChildren
    },
    {
        path: '/checkout',
        name: 'Checkout',
        component: () => import('../page/checkout/Checkout.vue'),
        meta: {title: '收银结账'}
    },
    {
        path: '/checkout/drafts',
        name: 'DraftOrderManage',
        component: () => import('../page/checkout/DraftOrderManage.vue'),
        meta: {title: '挂单查询'}
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
