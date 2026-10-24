<template>
  <div class="monthly-report">
    <div ref="calendarChartRef" class="calendar-chart"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import { getDailyStats } from '@/api/readingRecord'

const props = defineProps<{
  userId?: number
}>()

const calendarChartRef = ref<HTMLElement>()
let calendarChart: echarts.ECharts | null = null

const initCalendarChart = async () => {
  if (!calendarChartRef.value) return

  calendarChart = echarts.init(calendarChartRef.value)

  // 获取本月数据
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const startDate = new Date(year, month, 1)
  const endDate = new Date(year, month + 1, 0)

  try {
    const res = await getDailyStats(
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    )
    const data = res.data || []

    const chartData = data.map((item: any) => [item.date, item.totalMinutes])

    const option = {
      tooltip: {
        formatter: (params: any) => {
          return `${params.value[0]}<br/>阅读时长: ${params.value[1]} 分钟`
        }
      },
      visualMap: {
        min: 0,
        max: 120,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '0',
        inRange: {
          color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
        }
      },
      calendar: {
        top: 30,
        left: 30,
        right: 30,
        cellSize: ['auto', 30],
        range: `${year}-${String(month + 1).padStart(2, '0')}`,
        itemStyle: {
          borderWidth: 0.5
        },
        yearLabel: { show: false },
        dayLabel: {
          firstDay: 1,
          nameMap: 'cn'
        },
        monthLabel: {
          nameMap: 'cn'
        }
      },
      series: {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: chartData
      }
    }

    calendarChart.setOption(option)
  } catch (e) {
    // 使用空数据渲染
    const option = {
      tooltip: { formatter: (params: any) => `${params.value[0]}<br/>阅读时长: 0 分钟` },
      visualMap: {
        min: 0,
        max: 120,
        calculable: true,
        orient: 'horizontal',
        left: 'center',
        bottom: '0',
        inRange: { color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695'] }
      },
      calendar: {
        top: 30,
        left: 30,
        right: 30,
        cellSize: ['auto', 30],
        range: `${year}-${String(month + 1).padStart(2, '0')}`,
        itemStyle: { borderWidth: 0.5 },
        yearLabel: { show: false },
        dayLabel: { firstDay: 1, nameMap: 'cn' },
        monthLabel: { nameMap: 'cn' }
      },
      series: {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: []
      }
    }
    calendarChart.setOption(option)
  }
}

watch(() => props.userId, () => {
  initCalendarChart()
})

onMounted(() => {
  initCalendarChart()
})
</script>

<style scoped>
.monthly-report {
  width: 100%;
}

.calendar-chart {
  width: 100%;
  height: 280px;
}
</style>
