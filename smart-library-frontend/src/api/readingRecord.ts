import request from '@/utils/request'

/**
 * 记录阅读时长
 */
export const recordReading = (data: {
  bookId: number
  readDate: string
  durationMinutes: number
}) => {
  return request.post('/reading-records', data)
}

/**
 * 获取当前用户的阅读记录
 */
export const getMyRecords = () => {
  return request.get('/reading-records/my')
}

/**
 * 获取每日阅读统计
 */
export const getDailyStats = (startDate: string, endDate: string) => {
  return request.get('/reading-records/daily-stats', { params: { startDate, endDate } })
}

/**
 * 获取图书阅读统计
 */
export const getBookStats = (startDate: string, endDate: string) => {
  return request.get('/reading-records/book-stats', { params: { startDate, endDate } })
}

/**
 * 获取总阅读时长和图书数量
 */
export const getTotalStats = () => {
  return request.get('/reading-records/total')
}
