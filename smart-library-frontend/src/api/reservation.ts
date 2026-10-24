import request from '@/utils/request'

/**
 * 预约座位
 */
export const reserveSeat = (data: {
  seatId: number
  reservationDate: string
  startHour: number
  endHour: number
}) => {
  return request.post('/reservations', data)
}

/**
 * 签到
 */
export const checkIn = (id: number) => {
  return request.post(`/reservations/${id}/check-in`)
}

/**
 * 取消预约
 */
export const cancelReservation = (id: number) => {
  return request.post(`/reservations/${id}/cancel`)
}

/**
 * 完成预约（释放座位）
 */
export const completeReservation = (id: number) => {
  return request.post(`/reservations/${id}/complete`)
}

/**
 * 获取当前用户的预约记录
 */
export const getMyReservations = () => {
  return request.get('/reservations/my')
}
