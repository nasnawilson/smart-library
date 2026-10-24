<template>
  <div class="book-lists-page">
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">书单广场</h3>
        <div class="card-actions">
          <span class="badge bg-primary-lt me-2">共 {{ bookLists.length }} 个书单</span>
          <button type="button" class="btn btn-primary btn-sm" @click="handleCreate">
            <el-icon class="me-1"><Collection /></el-icon>
            创建书单
          </button>
        </div>
      </div>

      <div class="card-body">
        <el-empty v-if="bookLists.length === 0" description="暂无书单" />

        <div v-else class="row row-cards">
          <div
            v-for="list in bookLists"
            :key="list.id"
            class="col-12 col-md-6 col-xl-4"
          >
            <div class="card card-link h-100 list-card" @click="viewDetail(list.id)">
              <div class="card-body">
                <div class="list-cover mb-3">
                  <img v-if="list.coverImage" :src="list.coverImage" :alt="list.title" />
                  <el-icon v-else :size="40" class="text-secondary"><Collection /></el-icon>
                </div>

                <div class="fw-bold text-truncate mb-1" :title="list.title">{{ list.title }}</div>
                <div class="text-secondary small list-desc mb-2">{{ list.description }}</div>

                <div class="d-flex align-items-center flex-wrap gap-2 mb-2">
                  <span class="badge bg-primary-lt">{{ list.category }}</span>
                  <span class="text-secondary small d-inline-flex align-items-center gap-1">
                    <el-icon><Star /></el-icon> {{ list.likeCount }}
                  </span>
                  <span class="text-secondary small d-inline-flex align-items-center gap-1">
                    <el-icon><Collection /></el-icon> {{ list.collectCount }}
                  </span>
                </div>

                <div class="text-secondary small">by {{ list.creatorName }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 书单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" :title="currentList?.title" width="700px">
      <div v-if="currentList">
        <p class="text-secondary mb-3">{{ currentList.description }}</p>

        <div class="d-flex flex-wrap gap-2 mb-3">
          <button type="button" class="btn btn-primary" @click="handleLike">
            <el-icon class="me-1"><Star /></el-icon> 点赞 ({{ currentList.likeCount }})
          </button>
          <button type="button" class="btn" @click="handleFavorite">
            <el-icon class="me-1"><Collection /></el-icon>
            {{ isFavorited ? '已收藏' : '收藏' }}
          </button>
        </div>

        <h4 class="subheader mb-2">包含图书</h4>
        <el-table :data="listItems" class="w-100">
          <el-table-column type="index" width="50" />
          <el-table-column prop="bookTitle" label="书名" />
          <el-table-column prop="bookAuthor" label="作者" width="150" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <button
                type="button"
                class="btn btn-sm btn-ghost-primary"
                @click="viewBook(row.bookId)"
              >
                查看
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 创建书单对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建书单" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="请输入书单标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入书单描述"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="createForm.category" placeholder="请输入分类" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="createForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <button type="button" class="btn me-2" @click="createDialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary" @click="submitCreate">创建</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Star, Collection } from '@element-plus/icons-vue'
import { getBookListAll, getBookListDetail, getBookListItems, createBookList, likeBookList } from '@/api/bookList'
import { favorite, unfavorite, checkFavorite } from '@/api/favorite'
import { useRealtimeRefresh } from '@/utils/realtime'

const router = useRouter()
const bookLists = ref<any[]>([])
const detailDialogVisible = ref(false)
const createDialogVisible = ref(false)
const currentList = ref<any>(null)
const listItems = ref<any[]>([])
const isFavorited = ref(false)

const createForm = ref({
  title: '',
  description: '',
  category: '',
  tags: ''
})

const fetchBookLists = async () => {
  try {
    const res = await getBookListAll()
    bookLists.value = res.data || []
  } catch (e) {
    ElMessage.error('获取书单失败')
  }
}

const viewDetail = async (id: number) => {
  try {
    const res = await getBookListDetail(id)
    currentList.value = res.data
    const itemsRes = await getBookListItems(id)
    listItems.value = itemsRes.data || []

    // 检查是否已收藏
    const favRes = await checkFavorite('BOOK_LIST', id)
    isFavorited.value = favRes.data

    detailDialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取书单详情失败')
  }
}

const handleLike = async () => {
  try {
    await likeBookList(currentList.value.id)
    ElMessage.success('点赞成功')
    currentList.value.likeCount++
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const handleFavorite = async () => {
  try {
    if (isFavorited.value) {
      await unfavorite('BOOK_LIST', currentList.value.id)
      ElMessage.success('已取消收藏')
      currentList.value.collectCount--
    } else {
      await favorite('BOOK_LIST', currentList.value.id)
      ElMessage.success('收藏成功')
      currentList.value.collectCount++
    }
    isFavorited.value = !isFavorited.value
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const viewBook = (bookId: number) => {
  router.push(`/books/${bookId}`)
}

const handleCreate = () => {
  createForm.value = { title: '', description: '', category: '', tags: '' }
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createForm.value.title) {
    ElMessage.warning('请输入书单标题')
    return
  }
  try {
    await createBookList(createForm.value)
    ElMessage.success('书单创建成功')
    createDialogVisible.value = false
    fetchBookLists()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '创建失败')
  }
}

onMounted(() => {
  fetchBookLists()
})

useRealtimeRefresh(fetchBookLists, 5000)
</script>

<style scoped>
.list-card {
  cursor: pointer;
}

.list-cover {
  height: 132px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: var(--tblr-bg-surface-secondary);
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);
}

.list-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.list-desc {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.5rem;
}
</style>
