import request from '@/utils/request'

/**
 * 获取书单列表
 */
export const getBookListAll = () => {
  return request.get('/book-lists')
}

/**
 * 获取书单详情
 */
export const getBookListDetail = (id: number) => {
  return request.get(`/book-lists/${id}`)
}

/**
 * 获取书单图书列表
 */
export const getBookListItems = (id: number) => {
  return request.get(`/book-lists/${id}/items`)
}

/**
 * 创建书单
 */
export const createBookList = (data: any) => {
  return request.post('/book-lists', data)
}

/**
 * 更新书单
 */
export const updateBookList = (id: number, data: any) => {
  return request.put(`/book-lists/${id}`, data)
}

/**
 * 删除书单
 */
export const deleteBookList = (id: number) => {
  return request.delete(`/book-lists/${id}`)
}

/**
 * 点赞书单
 */
export const likeBookList = (id: number) => {
  return request.post(`/book-lists/${id}/like`)
}
