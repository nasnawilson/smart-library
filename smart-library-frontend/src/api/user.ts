import request from '@/utils/request'

/**
 * 获取用户列表
 */
export const getUserList = (params?: any) => {
  return request.get('/users', { params })
}

/**
 * 根据ID获取用户详情
 */
export const getUserById = (id: number) => {
  return request.get(`/users/${id}`)
}

/**
 * 新增用户
 */
export const addUser = (data: any) => {
  return request.post('/users', data)
}

/**
 * 更新用户
 */
export const updateUser = (id: number, data: any) => {
  return request.put(`/users/${id}`, data)
}

/**
 * 删除用户
 */
export const deleteUser = (id: number) => {
  return request.delete(`/users/${id}`)
}

/**
 * 修改密码
 */
export const changePassword = (data: { oldPassword: string; newPassword: string }) => {
  return request.put('/users/password', data)
}
