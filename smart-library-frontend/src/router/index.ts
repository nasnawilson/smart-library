import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { isAdminUser } from '@/constants/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/admin',
        name: 'AdminDashboard',
        component: () => import('@/views/AdminDashboard.vue'),
        meta: { title: '管理后台', icon: 'DataLine', adminOnly: true }
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据概览', icon: 'DataLine', studentOnly: true }
      },
      {
        path: '/books',
        name: 'Books',
        component: () => import('@/views/Books.vue'),
        meta: { title: '图书检索', icon: 'Reading', studentOnly: true }
      },
      {
        path: '/books/:id',
        name: 'BookDetail',
        component: () => import('@/views/BookDetail.vue'),
        meta: { title: '图书详情', icon: 'Reading', hidden: true, studentOnly: true }
      },
      {
        path: '/borrow',
        name: 'Borrow',
        component: () => import('@/views/Borrow.vue'),
        meta: { title: '借阅管理', icon: 'DocumentChecked' }
      },
      {
        path: '/seats',
        name: 'Seats',
        component: () => import('@/views/Seats.vue'),
        meta: { title: '座位预约', icon: 'OfficeBuilding' }
      },
      {
        path: '/navigation',
        name: 'Navigation',
        component: () => import('@/views/Navigation.vue'),
        meta: { title: '室内导航', icon: 'MapLocation', studentOnly: true }
      },
      {
        path: '/book-lists',
        name: 'BookLists',
        component: () => import('@/views/BookLists.vue'),
        meta: { title: '书单广场', icon: 'Collection', studentOnly: true }
      },
      {
        path: '/study-center',
        name: 'StudyCenter',
        component: () => import('@/views/StudyCenter.vue'),
        meta: { title: '学习中心', icon: 'Timer', studentOnly: true }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '读者管理', icon: 'User', adminOnly: true }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'Setting' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  const isAdmin = isAdminUser(userStore.userInfo)
  if (!to.meta.public && !userStore.token) {
    next('/login')
  } else if (to.path === '/' && isAdmin) {
    next('/admin')
  } else if (to.meta.adminOnly && !isAdmin) {
    ElMessage.warning('该页面仅管理员可访问')
    next('/dashboard')
  } else if (to.meta.studentOnly && isAdmin) {
    ElMessage.warning('管理员不具备学生端功能，已进入管理后台')
    next('/admin')
  } else {
    next()
  }
})

export default router
