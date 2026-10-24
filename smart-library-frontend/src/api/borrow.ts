import request from '@/utils/request'

/**
 * 获取借阅记录列表
 */
export const getBorrowList = (params?: any) => {
  return request.get('/borrows', { params })
}

/**
 * 借阅图书
 */
export const borrowBook = (data: { bookId: number; userId?: number; borrowHours?: number }) => {
  return request.post('/borrows', data)
}

/**
 * 归还图书
 */
export const returnBook = (id: number) => {
  return request.put(`/borrows/${id}/return`)
}

/**
 * 续借图书
 */
export const renewBook = (id: number) => {
  return request.put(`/borrows/${id}/renew`)
}

/**
 * 提醒逾期用户归还
 */
export const remindReturn = (id: number) => {
  return request.post(`/borrows/${id}/remind`)
}

/**
 * 获取当前用户的借阅记录
 */
export const getMyBorrows = () => {
  return request.get('/borrows/my')
}
