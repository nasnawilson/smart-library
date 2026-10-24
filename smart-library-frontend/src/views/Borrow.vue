<template>
  <div>
    <!-- 借阅概览 -->
    <div class="row g-3 mb-3">
      <div
        v-for="(item, index) in borrowStatsCards"
        :key="item.label"
        class="col-12 col-md-6 col-xl-4"
      >
        <div class="card">
          <div class="card-body">
            <div class="subheader mb-1">{{ item.label }}</div>
            <div class="h1 mb-0 tabular-nums" :class="`stat-value-${index}`">{{ item.value }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="card mb-3">
      <div class="card-body">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item v-if="isAdmin" label="读者姓名">
            <el-input v-model="searchForm.userName" placeholder="请输入读者姓名" clearable />
          </el-form-item>
          <el-form-item label="图书名称">
            <el-input v-model="searchForm.bookTitle" placeholder="请输入图书名称" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="借阅中" value="BORROWED" />
              <el-option label="已逾期" value="OVERDUE" />
              <el-option label="归还申请中" value="RETURN_REQUESTED" />
              <el-option label="已归还" value="RETURNED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <button type="button" class="btn btn-primary me-2" @click="handleSearch">
              <el-icon class="me-1"><Search /></el-icon>查询
            </button>
            <button type="button" class="btn" @click="handleReset">重置</button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 借阅记录 -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">{{ isAdmin ? '借阅记录' : '我的借阅记录' }}</h3>
        <div class="card-actions">
          <span class="badge bg-primary-lt me-2">共 {{ total }} 条</span>
          <button v-if="!isAdmin" type="button" class="btn btn-primary btn-sm" @click="handleBorrow">
            <el-icon class="me-1"><Reading /></el-icon>借阅图书
          </button>
        </div>
      </div>

      <div class="card-body">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="bookTitle" label="图书名称" />
          <el-table-column v-if="isAdmin" prop="userName" label="读者姓名" />
          <el-table-column label="借阅时间" width="150">
            <template #default="{ row }">
              {{ formatDateTime(row.borrowTime || row.borrowDate) }}
            </template>
          </el-table-column>
          <el-table-column label="应还时间" width="150">
            <template #default="{ row }">
              {{ formatDateTime(row.dueTime || row.dueDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="returnDate" label="归还日期" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <span class="badge" :class="`bg-${getStatusType(row.status)}-lt`">
                {{ getStatusText(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="{ row }">
              <div class="d-flex flex-wrap align-items-center gap-1">
                <button
                  v-if="canReturn(row)"
                  type="button"
                  class="btn btn-sm btn-primary"
                  @click="handleReturn(row)"
                >
                  {{ getReturnButtonText(row) }}
                </button>
                <button
                  v-if="!isAdmin && row.status === 'BORROWED' && canRenew(row)"
                  type="button"
                  class="btn btn-sm"
                  @click="handleRenew(row)"
                >
                  续借
                </button>
                <button
                  v-if="isAdmin && row.status === 'OVERDUE'"
                  type="button"
                  class="btn btn-sm btn-danger"
                  @click="handleRemind(row)"
                >
                  提醒归还
                </button>
                <span
                  v-if="['BORROWED', 'OVERDUE', 'RETURN_REQUESTED'].includes(row.status) && !canReturn(row) && !canRenew(row)"
                  class="badge bg-secondary-lt"
                >
                  无操作权限
                </span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="total > 0" class="card-footer d-flex justify-content-end">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="借阅图书" width="560px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="图书" prop="bookId">
          <el-select v-model="form.bookId" placeholder="请选择图书" filterable>
            <el-option
              v-for="book in bookOptions"
              :key="book.id"
              :label="book.title"
              :value="book.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅时长">
          <div class="duration-picker">
            <div class="wheel-col">
              <div class="wheel-label">天</div>
              <NumberWheel v-model="form.borrowDays" :min="0" :max="30" unit="天" />
            </div>
            <div class="wheel-col">
              <div class="wheel-label">小时</div>
              <NumberWheel v-model="form.borrowExtraHours" :min="hourMin" :max="hourMax" unit="时" :pad="true" />
            </div>
            <div class="duration-summary">
              <div class="summary-tag">已选</div>
              <div class="summary-value">{{ durationText }}</div>
              <div class="summary-hint">应还：{{ estimatedDueTime }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="isAdmin" label="读者" prop="userId">
          <el-select v-model="form.userId" placeholder="请选择读者" filterable>
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <button type="button" class="btn me-2" @click="dialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary" @click="handleSubmit">确认</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBorrowList, borrowBook, returnBook, renewBook, remindReturn } from '@/api/borrow'
import { getBookList } from '@/api/book'
import { getUserList } from '@/api/user'
import { useUserStore } from '@/store/user'
import { isAdminUser } from '@/constants/auth'
import NumberWheel from '@/components/NumberWheel.vue'
import { useRealtimeRefresh } from '@/utils/realtime'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref()
const bookOptions = ref<any[]>([])
const userOptions = ref<any[]>([])
const borrowStats = reactive({
  total: 0,
  borrowed: 0,
  overdue: 0,
  requested: 0,
  returned: 0
})

const searchForm = reactive({
  userName: '',
  bookTitle: '',
  status: ''
})

const form = reactive({
  bookId: null as number | null,
  userId: null as number | null,
  borrowDays: 15,
  borrowExtraHours: 0
})

const formRules = computed(() => ({
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }],
  userId: isAdmin.value ? [{ required: true, message: '请选择读者', trigger: 'change' }] : []
}))

const isAdmin = computed(() => isAdminUser(userStore.userInfo))

const borrowStatsCards = computed(() => [
  { label: isAdmin.value ? '全部记录' : '我的记录', value: borrowStats.total, color: '#0f172a' },
  { label: '借阅中', value: borrowStats.borrowed, color: '#2563eb' },
  { label: '已逾期', value: borrowStats.overdue, color: '#dc2626' },
  { label: '待审核', value: borrowStats.requested, color: '#d97706' },
  { label: '已归还', value: borrowStats.returned, color: '#16a34a' }
])

const borrowHours = computed(() => form.borrowDays * 24 + form.borrowExtraHours)

const hourMin = computed(() => form.borrowDays === 0 ? 12 : 0)

const hourMax = computed(() => form.borrowDays === 30 ? 0 : 23)

const durationText = computed(() => {
  const parts: string[] = []
  if (form.borrowDays > 0) parts.push(`${form.borrowDays}天`)
  if (form.borrowExtraHours > 0) parts.push(`${form.borrowExtraHours}小时`)
  return parts.length ? parts.join('') : '12小时'
})

const estimatedDueTime = computed(() => {
  const date = new Date(Date.now() + borrowHours.value * 60 * 60 * 1000)
  return formatDateTime(date)
})

watch(() => form.borrowDays, () => {
  if (form.borrowExtraHours < hourMin.value) form.borrowExtraHours = hourMin.value
  if (form.borrowExtraHours > hourMax.value) form.borrowExtraHours = hourMax.value
})

const isMine = (row: any) => row.userId === userStore.userInfo?.id

const canReturn = (row: any) => {
  if (isAdmin.value) return ['BORROWED', 'OVERDUE', 'RETURN_REQUESTED'].includes(row.status)
  return isMine(row) && ['BORROWED', 'OVERDUE'].includes(row.status)
}

const canRenew = (row: any) => {
  return row.status === 'BORROWED' && isMine(row)
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    BORROWED: 'primary',
    RETURNED: 'success',
    OVERDUE: 'danger',
    RETURN_REQUESTED: 'warning'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    BORROWED: '借阅中',
    RETURNED: '已归还',
    OVERDUE: '已逾期',
    RETURN_REQUESTED: '归还申请中'
  }
  return map[status] || status
}

const getReturnButtonText = (row: any) => {
  if (isAdmin.value) return row.status === 'RETURN_REQUESTED' ? '同意归还' : '代还'
  return '申请归还'
}

const formatDateTime = (value: string | Date | null | undefined) => {
  if (!value) return '-'
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:00`
}

const fetchData = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const res: any = await getBorrowList({
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
    await fetchBorrowStats()
  } catch (error) {
    console.error(error)
  } finally {
    if (!silent) loading.value = false
  }
}

const fetchBorrowStats = async () => {
  try {
    const res: any = await getBorrowList({ page: 1, size: 1000 })
    const records = res.data?.records || []
    borrowStats.total = res.data?.total ?? records.length
    borrowStats.borrowed = records.filter((item: any) => item.status === 'BORROWED').length
    borrowStats.overdue = records.filter((item: any) => item.status === 'OVERDUE').length
    borrowStats.requested = records.filter((item: any) => item.status === 'RETURN_REQUESTED').length
    borrowStats.returned = records.filter((item: any) => item.status === 'RETURNED').length
  } catch (error) {
    // 静默等待下一轮实时刷新
  }
}

const fetchOptions = async () => {
  try {
    const bookRes = await getBookList({ size: 1000 })
    bookOptions.value = bookRes.data.records || []
    if (isAdmin.value) {
      const userRes = await getUserList({ size: 1000 })
      userOptions.value = userRes.data.records || []
    } else {
      userOptions.value = []
    }
  } catch (error) {
    console.error(error)
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.userName = ''
  searchForm.bookTitle = ''
  searchForm.status = ''
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchData()
}

const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchData()
}

const handleBorrow = () => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生借书功能')
    return
  }
  form.bookId = null
  form.userId = null
  form.borrowDays = 15
  form.borrowExtraHours = 0
  dialogVisible.value = true
  fetchOptions()
}

const handleReturn = (row: any) => {
  const book = row.bookTitle ? `《${row.bookTitle}》` : '该图书'
  const message = isAdmin.value
    ? `确认同意「${row.userName || row.studentNo || '该读者'}」归还${book}吗？同意后库存会恢复。`
    : `确认申请归还${book}吗？提交后需等待管理员审核。`
  ElMessageBox.confirm(message, isAdmin.value ? '审核归还确认' : '申请归还确认', {
    confirmButtonText: isAdmin.value ? '同意归还' : '提交申请',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await returnBook(row.id)
      ElMessage.success(isAdmin.value ? '归还审核通过' : '已提交归还申请，请等待管理员审核')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || (isAdmin.value ? '审核失败' : '申请失败'))
    }
  })
}

const handleRenew = (row: any) => {
  ElMessageBox.confirm(`确认续借《${row.bookTitle || '该图书'}》吗？`, '续借确认', {
    confirmButtonText: '确认续借',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await renewBook(row.id)
      ElMessage.success('续借成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '续借失败')
    }
  })
}

const handleSubmit = async () => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生借书功能')
    return
  }
  if (borrowHours.value < 12 || borrowHours.value > 720) {
    ElMessage.warning('借阅时间最小12小时，最大不超过1个月')
    return
  }
  try {
    await formRef.value.validate()
    await borrowBook({
      bookId: form.bookId!,
      userId: isAdmin.value ? form.userId || undefined : undefined,
      borrowHours: borrowHours.value
    })
    ElMessage.success('借阅成功')
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '借阅失败')
  }
}

const handleRemind = async (row: any) => {
  ElMessageBox.confirm(`确认提醒「${row.userName || '该读者'}」归还《${row.bookTitle || '逾期图书'}》吗？`, '逾期提醒确认', {
    confirmButtonText: '发送提醒',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res: any = await remindReturn(row.id)
      ElMessage.success(res.data || '已提醒用户归还')
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '提醒失败')
    }
  })
}

onMounted(() => {
  fetchData()
})

useRealtimeRefresh(() => {
  fetchBorrowStats()
  if (!dialogVisible.value) fetchData(true)
}, 5000)
</script>

<style scoped>
.search-form :deep(.el-form-item) {
  margin-bottom: 0.5rem;
  margin-right: 0.75rem;
}

.stat-value-0 {
  color: var(--tblr-body-color);
}

.stat-value-1 {
  color: var(--tblr-primary);
}

.stat-value-2 {
  color: var(--tblr-danger);
}

.stat-value-3 {
  color: var(--tblr-warning);
}

.stat-value-4 {
  color: var(--tblr-success);
}

.duration-picker {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
  padding-top: 0.25rem;
}

.wheel-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.wheel-label {
  color: var(--tblr-secondary);
  font-size: 0.75rem;
}

.duration-summary {
  min-width: 170px;
  padding: 0.75rem 1rem;
  border: 1px solid var(--tblr-border-color);
  border-radius: 6px;
  background: var(--tblr-bg-surface-secondary);
}

.summary-tag {
  margin-bottom: 0.25rem;
  color: var(--tblr-secondary);
  font-size: 0.75rem;
}

.summary-value {
  color: var(--tblr-primary);
  font-size: 1.25rem;
  font-weight: 700;
}

.summary-hint {
  margin-top: 0.375rem;
  color: var(--tblr-success);
  font-size: 0.75rem;
  line-height: 1.5;
}
</style>
