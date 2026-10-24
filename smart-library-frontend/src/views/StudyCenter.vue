<template>
  <div class="row row-cards">
    <!-- 顶部统计卡片 -->
    <div class="col-12 col-md-6 col-xl-3">
      <div class="card">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between">
            <div class="subheader">累计阅读时长</div>
            <span class="avatar avatar-sm rounded bg-primary-lt text-primary">
              <el-icon :size="16"><Timer /></el-icon>
            </span>
          </div>
          <div class="h1 mb-0">
            {{ totalDuration }}
            <span class="fs-4 text-secondary">分钟</span>
          </div>
        </div>
      </div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
      <div class="card">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between">
            <div class="subheader">阅读图书数量</div>
            <span class="avatar avatar-sm rounded bg-success-lt text-success">
              <el-icon :size="16"><Reading /></el-icon>
            </span>
          </div>
          <div class="h1 mb-0">
            {{ totalBooks }}
            <span class="fs-4 text-secondary">本</span>
          </div>
        </div>
      </div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
      <div class="card">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between">
            <div class="subheader">读书笔记</div>
            <span class="avatar avatar-sm rounded bg-warning-lt text-warning">
              <el-icon :size="16"><Document /></el-icon>
            </span>
          </div>
          <div class="h1 mb-0">
            {{ notes.length }}
            <span class="fs-4 text-secondary">篇</span>
          </div>
        </div>
      </div>
    </div>
    <div class="col-12 col-md-6 col-xl-3">
      <div class="card">
        <div class="card-body">
          <div class="d-flex align-items-center justify-content-between">
            <div class="subheader">阅读打卡天数</div>
            <span class="avatar avatar-sm rounded bg-info-lt text-info">
              <el-icon :size="16"><Calendar /></el-icon>
            </span>
          </div>
          <div class="h1 mb-0">
            {{ readingDays }}
            <span class="fs-4 text-secondary">天</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 阅读打卡和图表 -->
    <div class="col-12 col-md-6">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">阅读打卡</h3>
          <div class="card-actions">
            <button type="button" class="btn btn-sm btn-primary" @click="showRecordDialog">
              打卡
            </button>
          </div>
        </div>
        <div class="card-body">
          <MonthlyReport :user-id="userStore.userInfo?.id" />
        </div>
      </div>
    </div>

    <div class="col-12 col-md-6">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">阅读趋势</h3>
          <div class="card-actions">
            <span class="badge bg-info-lt">近 30 天</span>
          </div>
        </div>
        <div class="card-body">
          <div ref="trendChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- 读书笔记 -->
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">我的读书笔记</h3>
          <div class="card-actions">
            <span class="badge bg-primary-lt">{{ notes.length }} 篇</span>
            <button type="button" class="btn btn-sm btn-primary" @click="showNoteDialog">
              写笔记
            </button>
          </div>
        </div>
        <div class="card-body">
          <el-empty v-if="notes.length === 0" description="暂无笔记" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="note in notes"
              :key="note.id"
              :timestamp="formatTime(note.createTime)"
              placement="top"
            >
              <div class="note-item">
                <h4 class="mb-1">{{ note.title }}</h4>
                <div class="text-secondary small mb-2">《{{ note.bookTitle }}》</div>
                <p class="mb-2">{{ note.content }}</p>
                <div class="d-flex flex-wrap gap-2">
                  <button type="button" class="btn btn-sm btn-link" @click="editNote(note)">
                    编辑
                  </button>
                  <button
                    type="button"
                    class="btn btn-sm btn-link text-danger"
                    @click="deleteNoteById(note.id)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </div>

    <!-- 打卡对话框 -->
    <el-dialog v-model="recordDialogVisible" title="阅读打卡" width="500px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="选择图书">
          <el-select v-model="recordForm.bookId" placeholder="请选择图书">
            <el-option
              v-for="book in allBooks"
              :key="book.id"
              :label="book.title"
              :value="book.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="阅读日期">
          <el-date-picker v-model="recordForm.readDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="阅读时长">
          <el-slider v-model="recordForm.durationMinutes" :max="120" show-stops :step="15" />
          <span>{{ recordForm.durationMinutes }} 分钟</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn" @click="recordDialogVisible = false">取消</button>
          <button type="button" class="btn btn-primary" @click="submitRecord">打卡</button>
        </div>
      </template>
    </el-dialog>

    <!-- 写笔记对话框 -->
    <el-dialog v-model="noteDialogVisible" :title="isEdit ? '编辑笔记' : '写笔记'" width="600px">
      <el-form :model="noteForm" label-width="100px">
        <el-form-item label="选择图书">
          <el-select v-model="noteForm.bookId" placeholder="请选择图书">
            <el-option
              v-for="book in allBooks"
              :key="book.id"
              :label="book.title"
              :value="book.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="noteForm.title" placeholder="请输入笔记标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="noteForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入笔记内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn" @click="noteDialogVisible = false">取消</button>
          <button type="button" class="btn btn-primary" @click="submitNote">保存</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Reading, Document, Calendar } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import MonthlyReport from '@/components/MonthlyReport.vue'
import { getMyNotes, createNote, updateNote, deleteNote } from '@/api/readingNote'
import { recordReading, getDailyStats, getTotalStats } from '@/api/readingRecord'
import { getBookList } from '@/api/book'
import { useRealtimeRefresh } from '@/utils/realtime'
import * as echarts from 'echarts'

const userStore = useUserStore()
const totalDuration = ref(0)
const totalBooks = ref(0)
const readingDays = ref(0)
const notes = ref<any[]>([])
const allBooks = ref<any[]>([])
const trendChartRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null

const recordDialogVisible = ref(false)
const recordForm = ref({
  bookId: null as number | null,
  readDate: new Date(),
  durationMinutes: 30
})

const noteDialogVisible = ref(false)
const isEdit = ref(false)
const noteForm = ref({
  id: null as number | null,
  bookId: null as number | null,
  title: '',
  content: ''
})

const fetchStats = async () => {
  try {
    const res = await getTotalStats()
    totalDuration.value = res.data.totalDuration
    totalBooks.value = res.data.totalBooks
  } catch (e) {
    // 忽略错误
  }
}

// 后端返回的是 ISO 时间串（2026-10-24T19:47:08.112171），时间线里只展示到分钟
const formatTime = (value?: string) => (value ? value.replace('T', ' ').slice(0, 16) : '')

const fetchNotes = async () => {
  try {
    const res = await getMyNotes()
    notes.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

const fetchBooks = async () => {
  try {
    const res = await getBookList({ size: 100 })
    allBooks.value = res.data.records || []
  } catch (e) {
    // 忽略错误
  }
}

// 用本地时间拼 YYYY-MM-DD。不用 toISOString()，否则东八区凌晨会把日期算成前一天
const toDateStr = (d: Date) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const fetchTrendData = async () => {
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - 29)

    const res = await getDailyStats(toDateStr(startDate), toDateStr(endDate))
    const data = res.data || []
    readingDays.value = data.length

    const dates: string[] = []
    const values: number[] = []

    for (let i = 29; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      const dateStr = toDateStr(d)
      dates.push(dateStr.substring(5))
      const record = data.find((item: any) => item.date === dateStr)
      values.push(record ? record.totalMinutes : 0)
    }

    await nextTick()
    if (!trendChartRef.value) return

    // 图表实例只创建一次：实时刷新时只更新数据。
    // 以前每次刷新都 dispose + init，折线动画会不断从头重放，看着像一直贴在地板上。
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }

    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
      yAxis: { type: 'value', name: '分钟' },
      series: [{
        data: values,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64,158,255,0.3)' },
              { offset: 1, color: 'rgba(64,158,255,0.05)' }
            ]
          }
        },
        itemStyle: { color: '#409eff' }
      }],
      grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true }
    }, true)
  } catch (e) {
    // 忽略错误
  }
}

const refreshStudyData = async () => {
  await Promise.all([fetchStats(), fetchNotes(), fetchBooks(), fetchTrendData()])
}

const showRecordDialog = () => {
  recordForm.value = { bookId: null, readDate: new Date(), durationMinutes: 30 }
  recordDialogVisible.value = true
}

const submitRecord = async () => {
  if (!recordForm.value.bookId) {
    ElMessage.warning('请选择图书')
    return
  }
  try {
    await recordReading({
      bookId: recordForm.value.bookId,
      readDate: toDateStr(recordForm.value.readDate),
      durationMinutes: recordForm.value.durationMinutes
    })
    ElMessage.success('打卡成功')
    recordDialogVisible.value = false
    fetchStats()
    fetchTrendData()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '打卡失败')
  }
}

const showNoteDialog = () => {
  isEdit.value = false
  noteForm.value = { id: null, bookId: null, title: '', content: '' }
  noteDialogVisible.value = true
}

const editNote = (note: any) => {
  isEdit.value = true
  noteForm.value = {
    id: note.id,
    bookId: note.bookId,
    title: note.title,
    content: note.content
  }
  noteDialogVisible.value = true
}

const submitNote = async () => {
  if (!noteForm.value.bookId || !noteForm.value.title || !noteForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    if (isEdit.value && noteForm.value.id) {
      await updateNote(noteForm.value.id, {
        bookId: noteForm.value.bookId,
        title: noteForm.value.title,
        content: noteForm.value.content
      })
      ElMessage.success('笔记更新成功')
    } else {
      await createNote({
        bookId: noteForm.value.bookId,
        title: noteForm.value.title,
        content: noteForm.value.content
      })
      ElMessage.success('笔记创建成功')
    }
    noteDialogVisible.value = false
    fetchNotes()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const deleteNoteById = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除该笔记吗？', '提示', { type: 'warning' })
    await deleteNote(id)
    ElMessage.success('删除成功')
    fetchNotes()
  } catch (e) {
    // 取消删除
  }
}

onMounted(() => {
  refreshStudyData()
})

useRealtimeRefresh(refreshStudyData, 5000)
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 300px;
}

.note-item {
  padding: 0.75rem 0.875rem;
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);
  background-color: var(--tblr-bg-surface-secondary);
}
</style>
