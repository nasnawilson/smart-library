<template>
  <div class="page">
    <!-- 侧边栏：Tabler navbar-vertical -->
    <aside
      class="navbar navbar-vertical navbar-expand-lg app-sidebar"
      data-bs-theme="dark"
      :class="{ 'is-open': sidebarOpen }"
    >
      <div class="container-fluid">
        <div class="navbar-brand navbar-brand-autodark d-flex align-items-center gap-2">
          <span class="avatar avatar-sm rounded bg-primary-lt text-primary flex-shrink-0">
            <el-icon :size="18"><Reading /></el-icon>
          </span>
          <span class="d-flex flex-column lh-sm">
            <span class="fw-bold">{{ isAdmin ? '图书馆管理后台' : '智慧图书馆' }}</span>
            <span class="text-secondary fs-5">{{ isAdmin ? 'Admin Console' : 'Smart Library' }}</span>
          </span>
          <button
            type="button"
            class="btn btn-sm btn-ghost-secondary ms-auto d-lg-none"
            aria-label="收起菜单"
            @click="sidebarOpen = false"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M18 6l-12 12" /><path d="M6 6l12 12" /></svg>
          </button>
        </div>

        <div class="navbar-collapse collapse show">
          <ul class="navbar-nav pt-2">
            <li
              v-for="item in visibleMenuItems"
              :key="item.path"
              class="nav-item"
              :class="{ active: isActive(item.path) }"
            >
              <router-link class="nav-link" :to="item.path" @click="sidebarOpen = false">
                <span class="nav-link-icon d-inline-block">
                  <el-icon :size="18"><component :is="item.icon" /></el-icon>
                </span>
                <span class="nav-link-title">{{ item.title }}</span>
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </aside>

    <div v-if="sidebarOpen" class="sidebar-backdrop d-lg-none" @click="sidebarOpen = false"></div>

    <!-- 主区域 -->
    <div class="page-wrapper">
      <div class="page-header">
        <div class="container-xl">
          <div class="row g-2 align-items-center">
            <div class="col">
              <button
                type="button"
                class="btn btn-sm btn-ghost-secondary d-lg-none me-2"
                aria-label="展开菜单"
                @click="sidebarOpen = true"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M4 6h16" /><path d="M4 12h16" /><path d="M4 18h16" /></svg>
              </button>
              <div class="page-pretitle">{{ greeting }}，{{ displayName }}</div>
              <h2 class="page-title">{{ currentMenuTitle }}</h2>
            </div>
            <div class="col-auto ms-auto d-print-none">
              <div class="d-flex align-items-center gap-2">
                <span v-if="isMockMode" class="badge bg-warning-lt">演示模式</span>
                <span class="status status-success d-none d-md-inline-flex">
                  <span class="status-dot status-dot-animated"></span>
                  后台已连接
                </span>
                <el-dropdown @command="handleCommand">
                  <span class="user-chip">
                    <span class="avatar avatar-sm rounded-circle bg-primary text-white">
                      {{ displayName.slice(0, 1) }}
                    </span>
                    <span class="d-none d-xl-flex flex-column lh-sm">
                      <strong class="fs-5">{{ displayName }}</strong>
                      <small class="text-secondary fs-6">{{ isAdmin ? '管理员' : '读者' }}</small>
                    </span>
                    <el-icon class="text-secondary"><arrow-down /></el-icon>
                  </span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                      <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="page-body">
        <div class="container-xl">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { isAdminUser } from '@/constants/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isMockMode = import.meta.env.VITE_USE_MOCK === 'true'
const sidebarOpen = ref(false)

const allMenuItems = [
  { path: '/admin', title: '管理后台', icon: 'DataLine', adminOnly: true },
  { path: '/dashboard', title: '数据概览', icon: 'DataLine', studentOnly: true },
  { path: '/books', title: '图书检索', icon: 'Reading', studentOnly: true },
  { path: '/borrow', title: '借阅管理', icon: 'DocumentChecked' },
  { path: '/seats', title: '座位管理', icon: 'OfficeBuilding' },
  { path: '/navigation', title: '室内导航', icon: 'MapLocation', studentOnly: true },
  { path: '/book-lists', title: '书单广场', icon: 'Collection', studentOnly: true },
  { path: '/study-center', title: '学习中心', icon: 'Timer', studentOnly: true },
  { path: '/users', title: '用户管理', icon: 'User', adminOnly: true },
  { path: '/profile', title: '个人中心', icon: 'Setting' }
]

const visibleMenuItems = computed(() => {
  const admin = isAdminUser(userStore.userInfo)
  return allMenuItems.filter(item => {
    if (item.adminOnly) return admin
    if (item.studentOnly) return !admin
    return true
  })
})

const isAdmin = computed(() => isAdminUser(userStore.userInfo))

const displayName = computed(() => userStore.userInfo?.name || userStore.userInfo?.studentNo || '读者')

const currentMenuTitle = computed(() => {
  return allMenuItems.find(item => route.path.startsWith(item.path))?.title || '智慧图书馆'
})

const isActive = (path: string) => route.path === path || route.path.startsWith(`${path}/`)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 11) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

watch(() => route.path, () => {
  sidebarOpen.value = false
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确认退出登录吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  /* 侧栏宽度必须声明在共同父级：.page-wrapper 的左边距也读取这个变量 */
  --tblr-sidebar-width: 15rem;
}

.app-sidebar .navbar-brand {
  padding-inline: 0.25rem;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.25rem 0.5rem;
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);
  background: var(--tblr-bg-surface);
  cursor: pointer;
}

.user-chip:hover {
  background: var(--tblr-bg-surface-secondary);
}

@media (max-width: 991.98px) {
  .app-sidebar {
    position: fixed;
    inset-inline-start: 0;
    top: 0;
    bottom: 0;
    z-index: 1045;
    transform: translateX(-100%);
    transition: transform 0.25s ease;
    box-shadow: 0 0 32px rgba(0, 0, 0, 0.24);
  }

  .app-sidebar.is-open {
    transform: none;
  }

  .page-wrapper {
    margin-inline-start: 0;
  }

  .sidebar-backdrop {
    position: fixed;
    inset: 0;
    z-index: 1040;
    background: rgba(24, 36, 50, 0.5);
  }
}
</style>
