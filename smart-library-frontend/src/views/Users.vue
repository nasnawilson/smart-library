<template>
  <div>
    <!-- 搜索条件 -->
    <div class="card mb-3">
      <div class="card-body">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="姓名">
            <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model="searchForm.studentNo" placeholder="请输入学号" clearable />
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

    <!-- 读者列表 -->
    <div class="card">
      <div class="card-header">
        <div>
          <h3 class="card-title">读者管理</h3>
          <div class="text-secondary small">用户资料、角色与状态维护</div>
        </div>
        <div class="card-actions">
          <span class="badge bg-secondary-lt">共 {{ total }} 位读者</span>
          <button type="button" class="btn btn-primary" @click="handleAdd">新增读者</button>
        </div>
      </div>

      <div class="card-body">
        <el-table :data="tableData" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="studentNo" label="学号/工号" />
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="department" label="院系" />
          <el-table-column prop="className" label="班级" />
          <el-table-column prop="major" label="专业" />
          <el-table-column prop="grade" label="年级" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="role" label="角色">
            <template #default="{ row }">
              <span class="badge" :class="row.role === 'ADMIN' ? 'bg-danger-lt' : 'bg-primary-lt'">
                {{ row.role === 'ADMIN' ? '管理员' : '读者' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <span class="badge" :class="row.status === 'ACTIVE' ? 'bg-success-lt' : 'bg-secondary-lt'">
                {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <button type="button" class="btn btn-sm btn-primary me-2" @click="handleEdit(row)">
                编辑
              </button>
              <button type="button" class="btn btn-sm btn-danger" @click="handleDelete(row)">
                删除
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="card-footer d-flex justify-content-end">
        <el-pagination
          class="pagination"
          layout="prev, pager, next, sizes, total"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" :rules="formRules" ref="formRef" class="dialog-form" label-position="top">
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" :disabled="isEdit" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </div>
          <div v-if="!isEdit" class="col-12 col-md-6">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="4-32 位字母或数字" maxlength="32" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="角色" prop="role">
              <el-select v-model="form.role" placeholder="请选择角色">
                <el-option label="读者" value="USER" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="院系">
              <el-input v-model="form.department" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="班级">
              <el-input v-model="form.className" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="专业">
              <el-input v-model="form.major" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="年级">
              <el-input v-model="form.grade" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="手机号">
              <el-input v-model="form.phone" />
            </el-form-item>
          </div>
          <div class="col-12 col-md-6">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </div>
        </div>
      </el-form>
      <template #footer>
        <button type="button" class="btn" @click="dialogVisible = false">取消</button>
        <button type="button" class="btn btn-primary ms-2" @click="handleSubmit">确认</button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser } from '@/api/user'
import { useRealtimeRefresh } from '@/utils/realtime'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const isEdit = ref(false)
const currentId = ref<number | null>(null)

const searchForm = reactive({
  name: '',
  studentNo: ''
})

const form = reactive({
  studentNo: '',
  name: '',
  password: '',
  department: '',
  className: '',
  major: '',
  grade: '',
  phone: '',
  email: '',
  role: 'USER'
})

const passwordPattern = /^[A-Za-z0-9]{4,32}$/
const validatePassword = (_rule: any, value: string, callback: Function) => {
  if (!isEdit.value && (!value || !passwordPattern.test(value))) {
    callback(new Error('密码为 4-32 位字母或数字，请重新输入'))
    return
  }
  callback()
}

const formRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const fetchData = async (silent = false) => {
  if (!silent) loading.value = true
  try {
    const res: any = await getUserList({
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    if (!silent) loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.studentNo = ''
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

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增读者'
  Object.assign(form, {
    studentNo: '',
    name: '',
    password: '',
    department: '',
    className: '',
    major: '',
    grade: '',
    phone: '',
    email: '',
    role: 'USER'
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑读者'
  currentId.value = row.id
  Object.assign(form, {
    studentNo: row.studentNo,
    name: row.name,
    password: '',
    department: row.department,
    className: row.className,
    major: row.major,
    grade: row.grade,
    phone: row.phone,
    email: row.email,
    role: row.role
  })
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确认删除读者「${row.name || row.studentNo}」吗？删除后不可恢复。`, '危险操作确认', {
    confirmButtonText: '确认删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      fetchData()
    } catch (error: any) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  })
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    if (isEdit.value && currentId.value) {
      const payload = { ...form }
      delete (payload as any).password
      await updateUser(currentId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await addUser(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

onMounted(() => {
  fetchData()
})

useRealtimeRefresh(() => {
  if (dialogVisible.value) return
  fetchData(true)
}, 5000)
</script>

<style scoped>
.search-form :deep(.el-form-item) {
  margin-bottom: 0.5rem;
  margin-right: 0.75rem;
}

.search-form :deep(.el-form-item:last-child) {
  margin-right: 0;
}

.card-actions {
  gap: 0.5rem;
}

.dialog-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination {
  margin: 0;
}
</style>
