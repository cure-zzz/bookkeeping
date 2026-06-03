import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录', guest: true }
  },
  // 注册功能已暂时关闭
  // {
  //   path: '/register',
  //   name: 'Register',
  //   component: () => import('../views/auth/Register.vue'),
  //   meta: { title: '注册', guest: true }
  // },
  {
    path: '/',
    component: () => import('../views/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'bills',
        name: 'Bills',
        component: () => import('../views/bill/BillList.vue'),
        meta: { title: '账单管理' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/statistics/Statistics.vue'),
        meta: { title: '统计报表' }
      },
      {
        path: 'budget',
        name: 'Budget',
        component: () => import('../views/budget/Budget.vue'),
        meta: { title: '预算管理' }
      },
      {
        path: 'liability',
        name: 'Liability',
        component: () => import('../views/liability/LiabilityList.vue'),
        meta: { title: '账户管理' }
      },
      {
        path: 'part-time',
        name: 'PartTime',
        component: () => import('../views/parttime/PartTimeJob.vue'),
        meta: { title: '兼职收入' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 记账本` : '记账本'
  
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn()

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if (to.meta.guest && isLoggedIn) {
    next('/home')
  } else {
    next()
  }
})

export default router
