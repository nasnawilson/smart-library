import request from '@/utils/request'

/**
 * 获取图书列表（支持多条件查询）
 */
export const getBookList = (params?: any) => {
  return request.get('/books', { params })
}

/**
 * 根据ID获取图书详情
 */
export const getBookById = (id: number) => {
  return request.get(`/books/${id}`)
}

/**
 * 新增图书
 */
export const addBook = (data: any) => {
  return request.post('/books', data)
}

/**
 * 更新图书
 */
export const updateBook = (id: number, data: any) => {
  return request.put(`/books/${id}`, data)
}

/**
 * 删除图书
 */
export const deleteBook = (id: number) => {
  return request.delete(`/books/${id}`)
}

/**
 * 获取热门图书
 */
export const getHotBooks = (limit: number = 10) => {
  return request.get('/books/hot', { params: { limit } })
}

/**
 * 获取新书
 */
export const getNewBooks = (limit: number = 10) => {
  return request.get('/books/new', { params: { limit } })
}

/**
 * 获取所有图书分类
 */
export const getCategories = () => {
  return request.get('/books/categories')
}
