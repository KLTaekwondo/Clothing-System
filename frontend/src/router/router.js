import {createRouter, createWebHashHistory} from 'vue-router'
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
    },
    {
        path: '/checkout/orders',
        name: 'CheckoutOrderManage',
        component: () => import('../page/checkout/CheckoutOrderManage.vue'),
        meta: {title: '订单查询'}
    }
]

const router = createRouter({
    // hash 模式：刷新/直接访问不会因静态服务器缺少 rewrite 而 404
    history: createWebHashHistory(),
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
