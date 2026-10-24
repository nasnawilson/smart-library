<template>
  <div class="row g-3">
    <!-- 个人信息 -->
    <div class="col-12 col-md-6 col-xl-4">
      <div class="card h-100">
        <div class="card-header">
          <h3 class="card-title">个人信息</h3>
          <div class="card-actions">
            <span class="badge" :class="userInfo.role === 'ADMIN' ? 'bg-danger-lt' : 'bg-primary-lt'">
              {{ userInfo.role === 'ADMIN' ? '管理员' : '读者' }}
            </span>
          </div>
        </div>

        <div class="card-body">
          <div class="text-center mb-3">
            <el-avatar :size="80" :src="userInfo.avatar">
              {{ userInfo.name?.charAt(0) }}
            </el-avatar>
            <div class="h3 mt-3 mb-1">{{ userInfo.name }}</div>
            <div class="text-secondary small">{{ userInfo.studentNo }}</div>
          </div>

          <div class="profile-fields">
            <div class="profile-field">
              <span class="text-secondary small">院系</span>
              <span class="profile-value">{{ userInfo.department || '-' }}</span>
            </div>
            <div class="profile-field">
              <span class="text-secondary small">班级</span>
              <span class="profile-value">{{ userInfo.className || '-' }}</span>
            </div>
            <div class="profile-field">
              <span class="text-secondary small">专业</span>
              <span class="profile-value">{{ userInfo.major || '-' }}</span>
            </div>
            <div class="profile-field">
              <span class="text-secondary small">年级</span>
              <span class="profile-value">{{ userInfo.grade || '-' }}</span>
            </div>
            <div class="profile-field">
              <span class="text-secondary small">手机</span>
              <span class="profile-value">{{ userInfo.phone || '-' }}</span>
            </div>
            <div class="profile-field">
              <span class="text-secondary small">邮箱</span>
              <span class="profile-value">{{ userInfo.email || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="card-footer d-flex gap-2">
          <button type="button" class="btn btn-primary" @click="showEditDialog">编辑资料</button>
          <button type="button" class="btn" @click="showPasswordDialog">修改密码</button>
        </div>
      </div>
    </div>

    <div class="col-12 col-md-6 col-xl-8">
      <!-- 我的借阅 -->
      <div class="card mb-3">
        <div class="card-header">
          <h3 class="card-title">我的借阅</h3>
          <div class="card-actions">
            <span class="badge bg-secondary-lt">共 {{ myBorrows.length }} 条</span>
          </div>
        </div>

        <div class="card-body">
          <el-empty v-if="myBorrows.length === 0" description="暂无借阅记录" />
          <el-table v-else :data="myBorrows" style="width: 100%">
            <el-table-column prop="bookTitle" label="图书名称" />
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
            <el-table-column prop="status" label="状态" width="110">
              <template #default="{ row }">
                <span class="badge" :class="`bg-${getStatusType(row.status)}-lt`">
                  {{ getStatusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <button
                  v-if="row.status === 'BORROWED'"
                  type="button"
                  class="btn btn-sm btn-primary me-2"
                  @click="handleReturn(row.id)"
                >
                  归还
                </button>
                <button
                  v-if="row.status === 'BORROWED' && row.renewCount < 2"
                  type="button"
                  class="btn btn-sm"
                  @click="handleRenew(row.id)"
                >
                  续借
                </button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <!-- 我的收藏 -->
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">我的收藏</h3>
          <div class="card-actions">
            <span class="badge bg-secondary-lt">共 {{ myFavorites.length }} 项</span>
          </div>
        </div>

        <div class="card-body">
          <el-empty v-if="myFavorites.length === 0" description="暂无收藏" />
          <div v-else class="row g-3">
            <div v-for="item in myFavorites" :key="item.id" class="col-12 col-md-6">
              <div class="card card-sm h-100">
                <div class="card-body">
                  <div class="fw-bold text-truncate" :title="item.targetName">
                    {{ item.targetName }}
                  </div>
                  <span class="badge bg-primary-lt mt-2">
                    {{ item.targetType === 'BOOK_LIST' ? '书单' : '图书' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="580px">
      <el-form :model="editForm" class="dialog-form" label-position="top">
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <el-form-item label="姓名">
              <el-input v-model="editForm.name" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="院系">
              <el-input v-model="editForm.department" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="班级">
              <el-input v-model="editForm.className" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="专业">
              <el-input v-model="editForm.major" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="年级">
              <el-input v-model="editForm.grade" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="手机">
              <el-input v-model="editForm.phone" />
            </el-form-item>
          </div>
          <div class="col-12">
            <el-form-item label="邮箱">
              <el-input v-model="editForm.email" />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <button type="button" class="btn" @click="editDialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary ms-2" @click="submitEdit">保存</button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="440px">
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        class="dialog-form"
        label-position="top"
      >
        <div class="row g-3">
          <div class="col-12">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password maxlength="32" placeholder="请输入原密码" />
            </el-form-item>
          </div>
          <div class="col-12">
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password maxlength="32" placeholder="4-32 位字母或数字" />
            </el-form-item>
          </div>
          <div class="col-12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password maxlength="32" placeholder="请再次输入新密码" />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <button type="button" class="btn" @click="passwordDialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary ms-2" @click="submitPassword">确认修改</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserInfo } from '@/api/auth'
import { updateUser, changePassword } from '@/api/user'
import { getMyBorrows, returnBook, renewBook } from '@/api/borrow'
import { getMyFavorites } from '@/api/favorite'
import { useRealtimeRefresh } from '@/utils/realtime'

const userStore = useUserStore()
const userInfo = ref<any>({})
const myBorrows = ref<any[]>([])
const myFavorites = ref<any[]>([])

const editDialogVisible = ref(false)
const editForm = ref<any>({})

const passwordDialogVisible = ref(false)
const passwordFormRef = ref()
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordPattern = /^[A-Za-z0-9]{4,32}$/
const validatePassword = (_rule: any, value: string, callback: Function) => {
  if (!value || !passwordPattern.test(value)) {
    callback(new Error('密码为 4-32 位字母或数字，请重新输入'))
    return
  }
  callback()
}

const passwordRules = {
  oldPassword: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  newPassword: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  confirmPassword: [
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!value || !passwordPattern.test(value)) {
          callback(new Error('密码为 4-32 位字母或数字，请重新输入'))
        } else if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change']
    }
  ]
}

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    userStore.setUserInfo(res.data)
  } catch (e) {
    ElMessage.error('获取用户信息失败')
  }
}

const fetchMyBorrows = async () => {
  try {
    const res = await getMyBorrows()
    myBorrows.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

const fetchMyFavorites = async () => {
  try {
    const res = await getMyFavorites()
    myFavorites.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

const refreshProfileData = async () => {
  await Promise.all([fetchUserInfo(), fetchMyBorrows(), fetchMyFavorites()])
}

const showEditDialog = () => {
  editForm.value = { ...userInfo.value }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await updateUser(userInfo.value.id, editForm.value)
    ElMessage.success('资料更新成功')
    editDialogVisible.value = false
    fetchUserInfo()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '更新失败')
  }
}

const showPasswordDialog = () => {
  passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  passwordDialogVisible.value = true
}

const submitPassword = async () => {
  try {
    await passwordFormRef.value.validate()
    await changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  }
}

const handleReturn = async (id: number) => {
  try {
    await returnBook(id)
    ElMessage.success('归还成功')
    fetchMyBorrows()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '归还失败')
  }
}

const handleRenew = async (id: number) => {
  try {
    await renewBook(id)
    ElMessage.success('续借成功')
    fetchMyBorrows()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '续借失败')
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    BORROWED: 'primary',
    RETURNED: 'success',
    OVERDUE: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    BORROWED: '借阅中',
    RETURNED: '已归还',
    OVERDUE: '已逾期'
  }
  return map[status] || status
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

onMounted(() => {
  refreshProfileData()
})

useRealtimeRefresh(refreshProfileData, 5000)
</script>

<style scoped>
.profile-fields {
  display: flex;
  flex-direction: column;
}

.profile-field {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
  padding: 0.5rem 0;
  border-top: 1px solid var(--tblr-border-color);
  font-size: 0.875rem;
}

.profile-field:first-child {
  border-top: 0;
}

.profile-value {
  text-align: right;
  overflow-wrap: anywhere;
}

.card-actions {
  gap: 0.5rem;
}

.dialog-form :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>
