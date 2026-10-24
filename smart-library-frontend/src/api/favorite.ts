import request from '@/utils/request'

/**
 * 收藏
 */
export const favorite = (targetType: string, targetId: number) => {
  return request.post('/favorites', null, { params: { targetType, targetId } })
}

/**
 * 取消收藏
 */
export const unfavorite = (targetType: string, targetId: number) => {
  return request.delete('/favorites', { params: { targetType, targetId } })
}

/**
 * 获取当前用户的收藏列表
 */
export const getMyFavorites = () => {
  return request.get('/favorites/my')
}

/**
 * 检查是否已收藏
 */
export const checkFavorite = (targetType: string, targetId: number) => {
  return request.get('/favorites/check', { params: { targetType, targetId } })
}
