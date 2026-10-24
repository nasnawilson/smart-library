<template>
  <div class="row row-cards">
    <!-- 顶部信息条 -->
    <div class="col-12">
      <div class="card">
        <div class="card-body">
          <div class="row align-items-center g-3">
            <div class="col">
              <div class="subheader">ADMIN CONSOLE</div>
              <div class="h1 mb-1">管理后台</div>
              <div class="text-secondary">用户、借阅与座位运行概览 · 每 5 秒自动刷新</div>
            </div>
            <div class="col-auto text-end">
              <div class="text-secondary fs-5">{{ currentDate }}</div>
              <div class="h1 mb-0 tabular-nums">{{ currentTime }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 六张实时指标卡 -->
    <div v-for="item in statCards" :key="item.label" class="col-6 col-lg-4 col-xl-2">
      <div class="card card-link stat-card" @click="go(item.path)">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <span class="avatar avatar-sm rounded" :class="`bg-${item.tone}-lt text-${item.tone}`">
              <el-icon :size="16"><component :is="item.icon" /></el-icon>
            </span>
            <el-icon class="text-secondary"><ArrowRight /></el-icon>
          </div>
          <div class="h1 mb-1">{{ item.value }}</div>
          <div class="text-secondary fs-5">{{ item.label }}</div>
        </div>
      </div>
    </div>

    <!-- 管理入口 -->
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">管理入口</h3>
          <div class="card-actions">
            <span class="text-secondary fs-5">Management</span>
          </div>
        </div>
        <div class="card-body">
          <div class="row row-cards">
            <div v-for="item in modules" :key="item.title" class="col-md-4">
              <div class="card card-link h-100" @click="go(item.path)">
                <div class="card-body d-flex align-items-center gap-3">
                  <span class="avatar rounded bg-primary-lt text-primary">
                    <el-icon :size="20"><component :is="item.icon" /></el-icon>
                  </span>
                  <div class="min-w-0">
                    <div class="fw-bold">{{ item.title }}</div>
                    <div class="text-secondary fs-5">{{ item.subtitle }}</div>
                  </div>
                  <el-icon class="ms-auto text-secondary"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { getUserList } from '@/api/user'
import { getBorrowList } from '@/api/borrow'
import { getSeatsByFloor } from '@/api/seat'
import { useRealtimeRefresh } from '@/utils/realtime'

const router = useRouter()
const now = ref(new Date())
const userTotal = ref<number | string>('-')
const borrowTotal = ref<number | string>('-')
const activeBorrowTotal = ref<number | string>('-')
const overdueBorrowTotal = ref<number | string>('-')
const occupiedSeats = ref<number | string>('-')
const freeSeats = ref<number | string>('-')
let timer: number | undefined

const modules = [
  { title: '用户管理', subtitle: '用户信息与状态', icon: 'User', path: '/users' },
  { title: '借阅管理', subtitle: '借阅记录与归还', icon: 'DocumentChecked', path: '/borrow' },
  { title: '座位管理', subtitle: '座位状态维护', icon: 'OfficeBuilding', path: '/seats' }
]

const statCards = computed(() => [
  { label: '用户总数', value: userTotal.value, icon: 'User', path: '/users', tone: 'primary' },
  { label: '借阅记录', value: borrowTotal.value, icon: 'DocumentChecked', path: '/borrow', tone: 'warning' },
  { label: '借阅中', value: activeBorrowTotal.value, icon: 'Reading', path: '/borrow', tone: 'info' },
  { label: '已逾期', value: overdueBorrowTotal.value, icon: 'Warning', path: '/borrow', tone: 'danger' },
  { label: '空闲座位', value: freeSeats.value, icon: 'OfficeBuilding', path: '/seats', tone: 'success' },
  { label: '占用座位', value: occupiedSeats.value, icon: 'OfficeBuilding', path: '/seats', tone: 'secondary' }
])

const currentDate = computed(() => {
  return now.value.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'short' })
})

const currentTime = computed(() => {
  return now.value.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
})

const loadStats = async () => {
  try {
    const userRes: any = await getUserList({ page: 1, size: 1 })
    userTotal.value = userRes.data?.total ?? userRes.data?.records?.length ?? '-'
  } catch (e) {
    userTotal.value = '-'
  }

  try {
    const borrowRes: any = await getBorrowList({ page: 1, size: 1 })
    borrowTotal.value = borrowRes.data?.total ?? borrowRes.data?.records?.length ?? '-'
  } catch (e) {
    borrowTotal.value = '-'
  }

  try {
    const borrowStatsRes: any = await getBorrowList({ page: 1, size: 1000 })
    const records = borrowStatsRes.data?.records || []
    activeBorrowTotal.value = records.filter((item: any) => item.status === 'BORROWED').length
    overdueBorrowTotal.value = records.filter((item: any) => item.status === 'OVERDUE').length
  } catch (e) {
    activeBorrowTotal.value = '-'
    overdueBorrowTotal.value = '-'
  }

  try {
    const [floor1, floor2]: any[] = await Promise.all([getSeatsByFloor(1), getSeatsByFloor(2)])
    const seats = [...(floor1.data || []), ...(floor2.data || [])]
    occupiedSeats.value = seats.filter((seat: any) => seat.status === 'OCCUPIED' || seat.status === 'TEMP_LEAVE').length
    freeSeats.value = seats.filter((seat: any) => seat.status === 'FREE').length
  } catch (e) {
    occupiedSeats.value = '-'
    freeSeats.value = '-'
  }
}

const go = (path: string) => {
  router.push(path)
}

onMounted(() => {
  loadStats()
  timer = window.setInterval(() => {
    now.value = new Date()
  }, 1000)
})

useRealtimeRefresh(loadStats, 5000)

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>

<style scoped>
.stat-card {
  cursor: pointer;
}

.min-w-0 {
  min-width: 0;
}

.tabular-nums {
  font-variant-numeric: tabular-nums;
}
</style>
