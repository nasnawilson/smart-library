import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 用户状态管理 Store
 * 使用 Pinia 管理用户登录状态和用户信息
 */
export const useUserStore = defineStore('user', () => {
  // Token 存储在 localStorage 中，实现持久化登录
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<any>(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性：判断是否已登录
  const isLoggedIn = computed(() => !!token.value)

  /**
   * 设置 JWT Token
   */
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /**
   * 设置用户信息
   */
  const setUserInfo = (info: any) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  /**
   * 退出登录，清除 Token 和用户信息
   */
  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout
  }
})
