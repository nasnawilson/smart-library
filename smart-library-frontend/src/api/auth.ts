import request from '@/utils/request'

/**
 * 用户登录
 * @param data 登录参数 { studentNo, password }
 */
export const login = (data: { studentNo: string; password: string }) => {
  return request.post('/auth/login', data)
}

/**
 * 用户注册
 * @param data 注册参数
 */
export const register = (data: {
  studentNo: string
  password: string
  name: string
  department?: string
  className?: string
  major?: string
  grade?: string
  phone?: string
  email?: string
}) => {
  return request.post('/auth/register', data)
}

/**
 * 获取当前登录用户信息
 */
export const getUserInfo = () => {
  return request.get('/auth/info')
}
