<template>
  <div>
    <!-- 返回与页面标题 -->
    <div class="card mb-3">
      <div class="card-body">
        <div class="d-flex flex-wrap align-items-center justify-content-between gap-2">
          <div class="d-flex align-items-center gap-2">
            <button type="button" class="btn btn-sm btn-back" @click="goBack">
              <el-icon class="me-1"><ArrowDown /></el-icon>返回
            </button>
            <h3 class="card-title mb-0">图书详情</h3>
          </div>
          <span class="badge bg-primary-lt">馆藏信息</span>
        </div>
      </div>
    </div>

    <!-- 封面与信息 -->
    <div class="row g-3" v-loading="loading">
      <div class="col-12 col-md-6 col-xl-4">
        <div class="card h-100">
          <div class="card-body">
            <div class="book-cover">
              <img v-if="book.coverImage" :src="book.coverImage" :alt="book.title" />
              <div v-else class="text-center text-secondary">
                <el-icon :size="64"><Reading /></el-icon>
                <p class="mt-2 mb-0">{{ book.title }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-md-6 col-xl-8">
        <div class="card h-100">
          <div class="card-header">
            <h3 class="card-title text-truncate">{{ book.title }}</h3>
            <div class="card-actions">
              <span v-if="book.isHot" class="badge bg-danger-lt">热门</span>
              <span v-if="book.isNew" class="badge bg-success-lt">新书</span>
              <span class="badge bg-secondary-lt">{{ book.category }}</span>
              <span class="badge bg-info-lt">{{ book.subject }}</span>
            </div>
          </div>

          <div class="card-body">
            <div class="row g-3">
              <div class="col-12 col-md-6">
                <div class="text-secondary small">作者</div>
                <div>{{ book.author }}</div>
              </div>
              <div class="col-12 col-md-6">
                <div class="text-secondary small">出版社</div>
                <div>{{ book.publisher }}</div>
              </div>
              <div class="col-12 col-md-6">
                <div class="text-secondary small">出版日期</div>
                <div>{{ book.publishDate }}</div>
              </div>
              <div class="col-12 col-md-6">
                <div class="text-secondary small">ISBN</div>
                <div>{{ book.isbn }}</div>
              </div>
              <div class="col-12 col-md-6">
                <div class="text-secondary small">书架位置</div>
                <div>{{ book.location }}（{{ book.floor }}楼）</div>
              </div>
              <div class="col-12 col-md-6">
                <div class="text-secondary small">库存</div>
                <span
                  class="badge"
                  :class="book.availableCount > 0 ? 'bg-success-lt' : 'bg-danger-lt'"
                >
                  可借 {{ book.availableCount }} / 共 {{ book.totalCount }} 本
                </span>
              </div>
            </div>

            <div v-if="book.tags" class="d-flex flex-wrap gap-1 mt-3">
              <span v-for="tag in book.tags.split(',')" :key="tag" class="badge bg-secondary-lt">
                {{ tag.trim() }}
              </span>
            </div>

            <div class="d-flex flex-wrap gap-2 mt-3">
              <button
                type="button"
                class="btn btn-primary"
                :disabled="book.availableCount <= 0"
                @click="handleBorrow"
              >
                立即借阅
              </button>
              <button type="button" class="btn" @click="handleAddNote">写笔记</button>
              <button type="button" class="btn" @click="handleAddRecord">记录阅读</button>
            </div>

            <div class="book-description">
              <div class="subheader mb-2">图书简介</div>
              <p class="text-secondary mb-0">{{ book.description || '暂无简介' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 读书笔记区域 -->
    <div class="card mt-3">
      <div class="card-header">
        <h3 class="card-title">读书笔记</h3>
        <div class="card-actions">
          <span class="badge bg-primary-lt">共 {{ notes.length }} 条</span>
        </div>
      </div>
      <div class="card-body">
        <el-empty v-if="notes.length === 0" description="暂无笔记" />
        <el-timeline v-else>
          <el-timeline-item
            v-for="note in notes"
            :key="note.id"
            :timestamp="note.createTime"
            placement="top"
          >
            <div class="card card-sm">
              <div class="card-body">
                <div class="fw-bold mb-1">{{ note.title }}</div>
                <p class="text-secondary mb-2">{{ note.content }}</p>
                <div class="text-end text-secondary small">—— {{ note.userName }}</div>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <!-- 写笔记对话框 -->
    <el-dialog v-model="noteDialogVisible" title="写读书笔记" width="600px">
      <el-form :model="noteForm" label-width="80px">
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
        <button type="button" class="btn me-2" @click="noteDialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary" @click="submitNote">提交</button>
      </template>
    </el-dialog>

    <!-- 记录阅读对话框 -->
    <el-dialog v-model="recordDialogVisible" title="记录阅读时长" width="500px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="阅读日期">
          <el-date-picker v-model="recordForm.readDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="阅读时长">
          <el-input-number v-model="recordForm.durationMinutes" :min="1" :max="600" />
          <span class="text-secondary small ms-2">分钟</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <button type="button" class="btn me-2" @click="recordDialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary" @click="submitRecord">提交</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading } from '@element-plus/icons-vue'
import { getBookById } from '@/api/book'
import { borrowBook } from '@/api/borrow'
import { getBookNotes, createNote } from '@/api/readingNote'
import { recordReading } from '@/api/readingRecord'
import { useRealtimeRefresh } from '@/utils/realtime'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const book = ref<any>({})
const notes = ref<any[]>([])

const noteDialogVisible = ref(false)
const noteForm = ref({ title: '', content: '' })

const recordDialogVisible = ref(false)
const recordForm = ref({ readDate: new Date(), durationMinutes: 30 })

const fetchBookDetail = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const res = await getBookById(Number(route.params.id))
    book.value = res.data
  } finally {
    if (!silent) loading.value = false
  }
}

const fetchNotes = async () => {
  try {
    const res = await getBookNotes(Number(route.params.id))
    notes.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

const goBack = () => {
  router.back()
}

const handleBorrow = async () => {
  try {
    await borrowBook({ bookId: book.value.id })
    ElMessage.success('借阅成功')
    fetchBookDetail()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '借阅失败')
  }
}

const handleAddNote = () => {
  noteForm.value = { title: '', content: '' }
  noteDialogVisible.value = true
}

const submitNote = async () => {
  if (!noteForm.value.title || !noteForm.value.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await createNote({
      bookId: book.value.id,
      title: noteForm.value.title,
      content: noteForm.value.content
    })
    ElMessage.success('笔记添加成功')
    noteDialogVisible.value = false
    fetchNotes()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

const handleAddRecord = () => {
  recordForm.value = { readDate: new Date(), durationMinutes: 30 }
  recordDialogVisible.value = true
}

const submitRecord = async () => {
  try {
    await recordReading({
      bookId: book.value.id,
      readDate: recordForm.value.readDate.toISOString().split('T')[0],
      durationMinutes: recordForm.value.durationMinutes
    })
    ElMessage.success('阅读记录添加成功')
    recordDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '添加失败')
  }
}

onMounted(() => {
  fetchBookDetail()
  fetchNotes()
})

useRealtimeRefresh(() => fetchBookDetail(true), 5000)
</script>

<style scoped>
.btn-back .el-icon {
  transform: rotate(90deg);
}

.book-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 1rem;
  border-radius: 6px;
  background: var(--tblr-bg-surface-secondary);
}

.book-cover img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
}

.book-description {
  margin-top: 1rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--tblr-border-color);
}

.card-title.text-truncate {
  min-width: 0;
}
</style>
