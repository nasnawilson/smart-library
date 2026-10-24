import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { mockAdapter } from '@/mock/mockAdapter'

/**
 * Axios 请求实例配置
 * 统一处理请求拦截、响应拦截、错误处理
 */
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  adapter: import.meta.env.VITE_USE_MOCK === 'true' ? mockAdapter : undefined,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：在请求发送前自动添加 JWT Token
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应数据和错误
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      if (!String(res.message || '').includes('未注册')) {
        ElMessage.error(res.message || '请求失败')
      }
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error: AxiosError) => {
    if (error.code === 'ERR_CANCELED' || String(error.message || '').toLowerCase().includes('abort')) {
      return Promise.reject(error)
    }
    let message = (error.response?.data as any)?.message || error.message || '网络错误'
    if (error.response?.status === 502) {
      message = '后端服务未启动或不可访问，请先启动 8080 端口的后端服务'
    } else if (error.response?.status === 404) {
      message = '接口不存在，请检查前后端接口路径是否一致'
    } else if (!error.response && error.code === 'ERR_NETWORK') {
      message = '网络连接失败，请检查后端服务或代理配置'
    }
    ElMessage.error(message)
    if (error.response?.status === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default request
