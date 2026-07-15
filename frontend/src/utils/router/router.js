import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Login',
        component: () => import('../../page/index.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/manage',
        name: 'Manage',
        component: () => import('../../page/Manage.vue'),
        meta: { title: '管理后台' },
        redirect: '/manage/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('../../page/manage/Dashboard.vue'),
                meta: { title: '仪表盘', icon: 'dashboard' }
            },
            {
                path: 'product',
                name: 'ProductManage',
                component: () => import('../../page/manage/ProductManage.vue'),
                meta: { title: '商品管理', icon: 'product' }
            },
            {
                path: 'employee',
                name: 'EmployeeManage',
                component: () => import('../../page/manage/EmployeeManage.vue'),
                meta: { title: '员工管理', icon: 'employee' }
            },
            {
                path: 'warehouse',
                name: 'WarehouseManage',
                component: () => import('../../page/manage/WarehouseManage.vue'),
                meta: { title: '仓库管理', icon: 'warehouse' }
            },
            {
                path: 'product/add',
                name: 'ProductAdd',
                component: () => import('../../page/manage/ProductAdd.vue'),
                meta: { title: '添加商品', icon: 'product' }
            },
            {
                path: 'option',
                name: 'OptionManage',
                component: () => import('../../page/manage/OptionManage.vue'),
                meta: { title: '选项管理', icon: 'option' }
            }
        ]
    },
    {
        path: '/checkout',
        name: 'Checkout',
        component: () => import('../../page/Checkout.vue'),
        meta: { title: '收银结账' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router
