<template>
  <div class="page page-center">
    <div class="container container-tight py-4">
      <!-- 品牌区 -->
      <div class="text-center mb-4">
        <div class="login-brand">
          <span class="avatar avatar-md rounded bg-primary text-white flex-shrink-0">
            <el-icon :size="22"><Reading /></el-icon>
          </span>
          <div class="text-start lh-sm">
            <div class="h2 mb-0">智慧图书馆</div>
            <div class="text-secondary">Smart Library System</div>
          </div>
        </div>
      </div>

      <!-- 登录卡片 -->
      <div class="card card-md">
        <div class="card-body">
          <h2 class="h2 text-center mb-1">欢迎回来</h2>
          <p class="text-secondary text-center mb-4">读者用 11 位学号登录，管理员用 admin 登录</p>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <div class="mb-3">
              <label class="form-label">学号 / 管理员账号</label>
              <el-form-item prop="studentNo">
                <el-input
                  v-model="form.studentNo"
                  placeholder="11 位学号或 admin"
                  size="large"
                  maxlength="20"
                />
              </el-form-item>
            </div>

            <div class="mb-2">
              <label class="form-label">密码</label>
              <el-form-item prop="password">
                <el-input
                  v-model="form.password"
                  type="password"
                  placeholder="密码"
                  size="large"
                  maxlength="32"
                  show-password
                />
              </el-form-item>
            </div>

            <div class="form-footer">
              <button
                type="button"
                class="btn btn-primary w-100"
                :disabled="loading"
                @click="handleLogin"
              >
                <span v-if="loading" class="spinner-border spinner-border-sm me-2" role="status"></span>
                {{ loading ? '登录中…' : '登录' }}
              </button>
            </div>
          </el-form>

          <div class="hr-text">或</div>

          <button type="button" class="btn btn-outline-secondary w-100" @click="openRegister">
            创建账号
          </button>

          <ul class="list-unstyled text-secondary mt-3 mb-0 login-tips">
            <li>读者学号为 11 位阿拉伯数字，密码为 4-32 位字母或数字</li>
            <li>管理员账号为 admin</li>
            <li>未注册的学号会自动进入注册流程</li>
          </ul>
        </div>
      </div>

      <div class="text-center text-secondary mt-3">
        登录即表示同意《图书馆使用规范》与《读者服务条款》
      </div>
      <div class="text-center text-secondary mt-1">
        © 2026 Smart Library
      </div>
    </div>

    <!-- 注册对话框 -->
    <el-dialog
      v-model="showRegister"
      title="创建账号"
      width="500px"
      class="register-dialog"
      align-center
    >
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="88px"
        class="register-form"
      >
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="registerForm.studentNo" placeholder="请输入11位学号" maxlength="11" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" show-password placeholder="4-32 位字母或数字" maxlength="32" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" maxlength="32" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="registerForm.department" placeholder="请输入院系" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="registerForm.className" placeholder="请输入班级" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="registerForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="registerForm.grade" placeholder="如：2023级" />
        </el-form-item>
        <el-form-item label="手机">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn" @click="showRegister = false">取消</button>
          <button type="button" class="btn btn-primary" @click="handleRegister">注册</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/store/user'
import { isAdminUser } from '@/constants/auth'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const showRegister = ref(false)
const registerFormRef = ref()

const form = reactive({
  studentNo: '',
  password: ''
})

const registerForm = reactive({
  studentNo: '',
  password: '',
  confirmPassword: '',
  name: '',
  department: '',
  className: '',
  major: '',
  grade: '',
  phone: '',
  email: ''
})

const studentNoPattern = /^\d{11}$/
const passwordPattern = /^[A-Za-z0-9]{4,32}$/

// 管理员固定账号 admin，不走 11 位学号校验
const isAdminAccount = (value: string) => (value || '').trim().toLowerCase() === 'admin'

// 登录用：允许 admin 或 11 位学号
const validateLoginAccount = (_rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请输入学号或 admin'))
    return
  }
  if (!isAdminAccount(value) && !studentNoPattern.test(value)) {
    callback(new Error('请输入 11 位学号，或管理员账号 admin'))
    return
  }
  callback()
}

// 注册用：必须是 11 位学号，且不能占用 admin
const validateStudentNo = (_rule: any, value: string, callback: Function) => {
  if (!value) {
    callback(new Error('请输入11位学号'))
    return
  }
  if (isAdminAccount(value)) {
    callback(new Error('admin 为系统保留账号，请更换学号'))
    return
  }
  if (!studentNoPattern.test(value)) {
    callback(new Error('学号必须是11位阿拉伯数字，请重新输入'))
    return
  }
  callback()
}

const validatePassword = (_rule: any, value: string, callback: Function) => {
  if (!value || !passwordPattern.test(value)) {
    callback(new Error('密码为 4-32 位字母或数字，请重新输入'))
    return
  }
  callback()
}

const rules = {
  studentNo: [{ validator: validateLoginAccount, trigger: ['blur', 'change'] }],
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }]
}

const registerRules = {
  studentNo: [{ validator: validateStudentNo, trigger: ['blur', 'change'] }],
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  confirmPassword: [
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (!value || !passwordPattern.test(value)) {
          callback(new Error('密码为 4-32 位字母或数字，请重新输入'))
        } else if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: ['blur', 'change']
    }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const openRegister = () => {
  showRegister.value = true
}

const handleLogin = async () => {
  try {
    if (!isAdminAccount(form.studentNo) && !studentNoPattern.test(form.studentNo)) {
      showRegister.value = false
      form.password = ''
      ElMessage.error('请输入 11 位学号，或管理员账号 admin')
      await formRef.value?.validateField('studentNo').catch(() => {})
      return
    }
    await formRef.value.validate()
    loading.value = true
    const res: any = await login({
      studentNo: form.studentNo,
      password: form.password
    })
    userStore.setToken(res.data.token)
    userStore.setUserInfo(res.data.userInfo)
    ElMessage.success('登录成功')
    router.push(isAdminUser(res.data.userInfo) ? '/admin' : '/dashboard')
  } catch (error: any) {
    const message = error.response?.data?.message || error.message || ''
    if (message.includes('未注册')) {
      ElMessage.warning('该学号未注册，已为你打开注册界面')
      registerForm.studentNo = form.studentNo
      registerForm.password = ''
      registerForm.confirmPassword = ''
      showRegister.value = true
      return
    }
    if (message) {
      ElMessage.error(message)
    }
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    await register({
      studentNo: registerForm.studentNo,
      password: registerForm.password,
      name: registerForm.name,
      department: registerForm.department,
      className: registerForm.className,
      major: registerForm.major,
      grade: registerForm.grade,
      phone: registerForm.phone,
      email: registerForm.email
    })
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
    form.studentNo = registerForm.studentNo
  } catch (error: any) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    }
  }
}
</script>

<style scoped>
.login-brand {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
}

/* 表单校验信息交给 el-form-item，去掉其默认底部间距，由外层 margin 控制节奏 */
.login-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.login-form :deep(.el-form-item__error) {
  position: static;
  padding-top: 0.25rem;
  font-size: 0.75rem;
}

.login-tips {
  font-size: 0.8125rem;
  line-height: 1.7;
}
</style>

<style>
/* 对话框被 teleport 到 body，需使用全局样式覆盖 */
.register-dialog {
  border-radius: var(--tblr-border-radius-lg);
  border: 1px solid var(--tblr-border-color);
  box-shadow: 0 20px 48px rgba(24, 36, 50, 0.18);
  padding: 0;
  overflow: hidden;
}

.register-dialog .el-dialog__header {
  margin: 0;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--tblr-border-color);
}

.register-dialog .el-dialog__title {
  font-size: 1rem;
  font-weight: 600;
  color: var(--tblr-body-color);
}

.register-dialog .el-dialog__headerbtn {
  top: 0.875rem;
  right: 1rem;
}

.register-dialog .el-dialog__body {
  max-height: 62vh;
  overflow-y: auto;
  padding: 1.25rem;
}

.register-dialog .el-dialog__footer {
  padding: 0.875rem 1.25rem;
  border-top: 1px solid var(--tblr-border-color);
  background: var(--tblr-bg-surface-secondary);
}

.register-dialog .register-form .el-form-item {
  margin-bottom: 1rem;
}
</style>
