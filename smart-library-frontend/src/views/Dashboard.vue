<template>
  <div class="row row-cards">
    <!-- 实时统计 -->
    <div v-for="item in stats" :key="item.title" class="col-sm-6 col-lg-3">
      <div class="card">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <div class="subheader">{{ item.title }}</div>
            <span class="avatar avatar-sm rounded bg-primary-lt text-primary">
              <el-icon :size="16"><component :is="item.icon" /></el-icon>
            </span>
          </div>
          <div class="h1 mb-1">{{ item.value }}</div>
          <div class="text-secondary fs-5">{{ item.hint }}</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">快捷入口</h3>
          <div class="card-actions">
            <span class="text-secondary fs-5">常用服务直达</span>
          </div>
        </div>
        <div class="card-body">
          <div class="row g-2">
            <div v-for="item in quickActions" :key="item.title" class="col-6 col-lg-3">
              <button type="button" class="btn btn-outline-primary w-100" @click="goTo(item.path)">
                <el-icon class="me-2"><component :is="item.icon" /></el-icon>
                {{ item.title }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表 -->
    <div class="col-lg-6">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">借阅趋势（近 7 天）</h3>
          <div class="card-actions">
            <span class="badge bg-primary-lt">每 5 秒刷新</span>
          </div>
        </div>
        <div class="card-body">
          <div ref="trendChartRef" class="chart"></div>
        </div>
      </div>
    </div>

    <div class="col-lg-6">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">图书分类统计</h3>
          <div class="card-actions">
            <span class="badge bg-primary-lt">馆藏构成</span>
          </div>
        </div>
        <div class="card-body">
          <div ref="categoryChartRef" class="chart"></div>
        </div>
      </div>
    </div>

    <!-- 最近借阅 -->
    <div class="col-lg-8">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">最近借阅记录</h3>
        </div>
        <div class="table-responsive">
          <table class="table table-vcenter card-table">
            <thead>
              <tr>
                <th>图书名称</th>
                <th>读者</th>
                <th>借阅日期</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in recentBorrows" :key="index">
                <td>{{ row.bookTitle }}</td>
                <td class="text-secondary">{{ row.userName }}</td>
                <td class="text-secondary">{{ row.borrowDate }}</td>
                <td>
                  <span class="status" :class="statusClass(row.status)">
                    <span class="status-dot"></span>
                    {{ getStatusLabel(row.status) }}
                  </span>
                </td>
              </tr>
              <tr v-if="!recentBorrows.length">
                <td colspan="4" class="text-center text-secondary py-4">暂无借阅记录</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 系统公告 -->
    <div class="col-lg-4">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">系统公告</h3>
        </div>
        <div class="list-group list-group-flush">
          <div v-for="notice in notices" :key="notice.id" class="list-group-item">
            <div class="d-flex align-items-center justify-content-between">
              <span>{{ notice.content }}</span>
              <span class="badge" :class="`bg-${notice.type}-lt`">{{ notice.date }}</span>
            </div>
          </div>
        </div>
        <div class="card-footer text-secondary fs-5">
          数据每 5 秒自动同步 · 仅供馆内使用
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboardStats } from '@/api/dashboard'
import { useRealtimeRefresh } from '@/utils/realtime'

const router = useRouter()

// Tabler 调色板（由 --tblr-* 的 oklch 值换算，供 ECharts 使用）
const TABLER = {
  primary: '#066fd1',
  info: '#4299e1',
  success: '#2fb344',
  warning: '#f59f00',
  danger: '#d63939',
  text: '#6c7a91',
  grid: '#e6e7e9'
}

const stats = ref([
  { title: '图书总数', value: 0, hint: '馆藏资源', icon: 'Reading' },
  { title: '读者数量', value: 0, hint: '活跃用户', icon: 'User' },
  { title: '今日借阅', value: 0, hint: '今日流转', icon: 'DocumentChecked' },
  { title: '逾期未还', value: 0, hint: '需关注', icon: 'Warning' }
])

const quickActions = [
  { title: '图书检索', icon: 'Search', path: '/books' },
  { title: '座位预约', icon: 'OfficeBuilding', path: '/seats' },
  { title: '借阅管理', icon: 'DocumentChecked', path: '/borrow' },
  { title: '书单广场', icon: 'Collection', path: '/book-lists' }
]

const recentBorrows = ref<any[]>([])
const notices = ref([
  { id: 1, content: '请及时归还到期图书', date: '提醒', type: 'warning' },
  { id: 2, content: '借阅数据每 5 秒同步', date: '后台', type: 'primary' },
  { id: 3, content: '读者与管理员权限隔离', date: '安全', type: 'info' },
  { id: 4, content: '座位状态实时在线', date: '运行', type: 'success' }
])

const trendChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()
type EChartsModule = typeof import('echarts')
type EChartsInstance = ReturnType<EChartsModule['init']>
let echartsModule: EChartsModule | null = null
let trendChart: EChartsInstance | null = null
let categoryChart: EChartsInstance | null = null

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    BORROWED: '借阅中',
    RETURNED: '已归还',
    OVERDUE: '已逾期'
  }
  return map[status] || status
}

const statusClass = (status: string) => {
  const map: Record<string, string> = {
    BORROWED: 'status-info',
    RETURNED: 'status-success',
    OVERDUE: 'status-danger'
  }
  return map[status] || 'status-secondary'
}

const goTo = (path: string) => {
  router.push(path)
}

const fetchDashboardData = async () => {
  try {
    const res = await getDashboardStats()
    const data = res.data

    stats.value[0].value = data.totalBooks
    stats.value[1].value = data.totalUsers
    stats.value[2].value = data.todayBorrows
    stats.value[3].value = data.overdueCount

    recentBorrows.value = data.recentBorrows || []

    nextTick(async () => {
      await initCharts(data.borrowTrend, data.categoryStats)
    })
  } catch (e) {
    // 使用默认数据
  }
}

const loadECharts = async () => {
  if (!echartsModule) {
    echartsModule = await import('echarts')
  }
  return echartsModule
}

const initCharts = async (trendData: any[], categoryData: any[]) => {
  const echarts = await loadECharts()
  initTrendChart(echarts, trendData)
  initCategoryChart(echarts, categoryData)
}

const initTrendChart = (echarts: EChartsModule, trendData: any[]) => {
  if (!trendChartRef.value) return
  trendChart?.dispose()
  trendChart = echarts.init(trendChartRef.value)

  const dates = (trendData || []).map((item: any) => item.date?.substring(5) || '')
  const values = (trendData || []).map((item: any) => item.count || 0)

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#ffffff',
      borderColor: TABLER.grid,
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#182433', fontSize: 12, fontFamily: 'system-ui, sans-serif' },
      axisPointer: { type: 'line', lineStyle: { color: TABLER.grid, width: 1 } }
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false,
      axisLine: { lineStyle: { color: TABLER.grid } },
      axisTick: { show: false },
      axisLabel: { color: TABLER.text, fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: TABLER.grid } },
      axisLabel: { color: TABLER.text, fontSize: 12 }
    },
    series: [
      {
        data: values,
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2, color: TABLER.primary },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(6, 111, 209, 0.18)' },
              { offset: 1, color: 'rgba(6, 111, 209, 0)' }
            ]
          }
        },
        itemStyle: { color: TABLER.primary, borderColor: '#ffffff', borderWidth: 2 }
      }
    ],
    grid: { left: '3%', right: '4%', top: '10%', bottom: '3%', containLabel: true }
  })
}

const initCategoryChart = (echarts: EChartsModule, categoryData: any[]) => {
  if (!categoryChartRef.value) return
  categoryChart?.dispose()
  categoryChart = echarts.init(categoryChartRef.value)

  const data = (categoryData || []).map((item: any) => ({
    name: item.category,
    value: item.count
  }))

  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#ffffff',
      borderColor: TABLER.grid,
      borderWidth: 1,
      padding: [8, 12],
      textStyle: { color: '#182433', fontSize: 12, fontFamily: 'system-ui, sans-serif' }
    },
    color: [TABLER.primary, TABLER.info, TABLER.success, TABLER.warning, TABLER.danger, '#ae3ec9'],
    series: [
      {
        type: 'pie',
        radius: ['46%', '70%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 4,
          borderColor: '#ffffff',
          borderWidth: 2
        },
        label: { show: true, color: TABLER.text, fontSize: 12, formatter: '{b} {c}' },
        labelLine: { lineStyle: { color: TABLER.grid } },
        data: data
      }
    ]
  })
}

onMounted(() => {
  fetchDashboardData()
})

useRealtimeRefresh(fetchDashboardData, 5000)

onUnmounted(() => {
  trendChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.chart {
  width: 100%;
  height: 280px;
}

.card-table {
  margin-bottom: 0;
}
</style>
