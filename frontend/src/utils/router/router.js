import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: '登录页面',
    component: () => import('../../Pages/Home.vue')
  },{
    path: '/dashboard',
    name: '管理页面',
    component: () => import('../../Pages/DashBoard.vue'),
    children:[
      {
        path: '/test',
        name: '测试页面',
        component: () => import('../../Pages/Test.vue')
      },
      {
        path: '/WareHouseManage',
        name: '仓库管理',
        component: () => import('../../Pages/WareHouseManage.vue')
      },
      {
        path: '/SalesManManage',
        name: '销售员管理',
        component: () => import('../../Pages/SalesManManage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router