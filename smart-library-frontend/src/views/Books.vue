<template>
  <div>
    <!-- 搜索栏 -->
    <div class="card mb-3">
      <div class="card-body">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="图书名称">
            <el-input v-model="searchForm.title" placeholder="请输入图书名称" clearable />
          </el-form-item>
          <el-form-item label="作者">
            <el-input v-model="searchForm.author" placeholder="请输入作者" clearable />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.category" placeholder="请选择分类" clearable>
              <el-option
                v-for="cat in categories"
                :key="cat"
                :label="cat"
                :value="cat"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="楼层">
            <el-select v-model="searchForm.floor" placeholder="请选择楼层" clearable>
              <el-option label="一楼" :value="1" />
              <el-option label="二楼" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <button type="button" class="btn btn-primary me-2" @click="handleSearch">
              <el-icon class="me-1"><Search /></el-icon>搜索
            </button>
            <button type="button" class="btn" @click="handleReset">重置</button>
          </el-form-item>
        </el-form>

        <!-- 快捷筛选 -->
        <div class="quick-filter">
          <span class="text-secondary me-2">快捷筛选</span>
          <el-radio-group v-model="searchForm.filter" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="hot">热门图书</el-radio-button>
            <el-radio-button label="new">新书推荐</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <!-- 图书列表 -->
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">图书列表</h3>
        <div class="card-actions">
          <span class="badge bg-primary-lt">共 {{ total }} 本</span>
        </div>
      </div>

      <div class="card-body">
        <el-empty v-if="books.length === 0" description="暂无图书" />

        <div v-else class="row row-cards">
          <div
            v-for="book in books"
            :key="book.id"
            class="col-12 col-sm-6 col-md-4 col-xl-3"
          >
            <div class="card card-sm book-card h-100" @click="viewDetail(book.id)">
              <div class="book-cover">
                <img v-if="book.coverImage" :src="book.coverImage" :alt="book.title" />
                <el-icon v-else :size="40" class="text-secondary"><Reading /></el-icon>
              </div>
              <div class="card-body">
                <div class="fw-bold text-truncate" :title="book.title">{{ book.title }}</div>
                <div class="text-secondary small text-truncate mb-2">{{ book.author }}</div>
                <div class="d-flex flex-wrap gap-1 mb-2">
                  <span class="badge bg-secondary-lt">{{ book.category }}</span>
                  <span v-if="book.isHot" class="badge bg-danger-lt">热门</span>
                  <span v-if="book.isNew" class="badge bg-success-lt">新书</span>
                </div>
                <div class="text-secondary small d-flex align-items-center gap-1 mb-2">
                  <el-icon><Location /></el-icon>
                  <span class="text-truncate">{{ book.location }}（{{ book.floor }}楼）</span>
                </div>
                <span
                  class="badge"
                  :class="book.availableCount > 0 ? 'bg-success-lt' : 'bg-danger-lt'"
                >
                  可借 {{ book.availableCount }} / {{ book.totalCount }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > 0" class="card-footer d-flex justify-content-end">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Reading, Location } from '@element-plus/icons-vue'
import { getBookList, getCategories, getHotBooks, getNewBooks } from '@/api/book'
import { useRealtimeRefresh } from '@/utils/realtime'

const router = useRouter()
const books = ref<any[]>([])
const categories = ref<string[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(12)

const searchForm = ref({
  title: '',
  author: '',
  category: '',
  floor: null as number | null,
  filter: ''
})

const fetchBooks = async () => {
  try {
    const params: any = {
      page: page.value,
      size: size.value
    }
    if (searchForm.value.title) params.title = searchForm.value.title
    if (searchForm.value.author) params.author = searchForm.value.author
    if (searchForm.value.category) params.category = searchForm.value.category
    if (searchForm.value.floor) params.floor = searchForm.value.floor

    const res = await getBookList(params)
    books.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    ElMessage.error('获取图书列表失败')
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

const handleSearch = () => {
  page.value = 1
  fetchBooks()
}

const handleReset = () => {
  searchForm.value = { title: '', author: '', category: '', floor: null, filter: '' }
  page.value = 1
  fetchBooks()
}

const handleFilterChange = async (val: string) => {
  page.value = 1
  if (val === 'hot') {
    try {
      const res = await getHotBooks(24)
      books.value = res.data || []
      total.value = res.data?.length || 0
    } catch (e) {
      ElMessage.error('获取热门图书失败')
    }
  } else if (val === 'new') {
    try {
      const res = await getNewBooks(24)
      books.value = res.data || []
      total.value = res.data?.length || 0
    } catch (e) {
      ElMessage.error('获取新书失败')
    }
  } else {
    fetchBooks()
  }
}

const refreshBooksData = async () => {
  if (searchForm.value.filter === 'hot') {
    const res = await getHotBooks(24)
    books.value = res.data || []
    total.value = res.data?.length || 0
  } else if (searchForm.value.filter === 'new') {
    const res = await getNewBooks(24)
    books.value = res.data || []
    total.value = res.data?.length || 0
  } else {
    await fetchBooks()
  }
}

const handleSizeChange = (val: number) => {
  size.value = val
  fetchBooks()
}

const handlePageChange = (val: number) => {
  page.value = val
  fetchBooks()
}

const viewDetail = (id: number) => {
  router.push(`/books/${id}`)
}

onMounted(() => {
  fetchBooks()
  fetchCategories()
})

useRealtimeRefresh(refreshBooksData, 5000)
</script>

<style scoped>
.search-form :deep(.el-form-item) {
  margin-bottom: 0.5rem;
  margin-right: 0.75rem;
}

.quick-filter {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--tblr-border-color);
}

.book-card {
  cursor: pointer;
  transition: box-shadow 0.15s ease, transform 0.15s ease;
}

.book-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 14px rgba(24, 36, 50, 0.12);
}

.book-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 150px;
  overflow: hidden;
  background: var(--tblr-bg-surface-secondary);
  border-bottom: 1px solid var(--tblr-border-color);
}

.book-cover img {
  max-width: 100%;
  max-height: 150px;
  object-fit: contain;
}

.book-card .card-body {
  display: flex;
  flex-direction: column;
}
</style>
