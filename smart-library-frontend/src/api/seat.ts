import request from '@/utils/request'

/**
 * 获取座位列表
 */
export const getSeatList = (params?: any) => {
  return request.get('/seats', { params })
}

/**
 * 获取某楼层的座位（带用户信息）
 */
export const getSeatsByFloor = (floor: number) => {
  return request.get(`/seats/floor/${floor}`)
}

/**
 * 获取座位状态版本号
 */
export const getSeatVersion = () => {
  return request.get('/seats/version')
}

/**
 * 占用座位（现场选座）
 */
export const occupySeat = (id: number) => {
  return request.post(`/seats/${id}/occupy`)
}

/**
 * 释放座位
 */
export const releaseSeat = (id: number) => {
  return request.post(`/seats/${id}/release`)
}

/**
 * 暂离
 */
export const tempLeave = (id: number) => {
  return request.post(`/seats/${id}/temp-leave`)
}

/**
 * 暂离归来
 */
export const returnFromTempLeave = (id: number) => {
  return request.post(`/seats/${id}/return`)
}
