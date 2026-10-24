import request from '@/utils/request'

/**
 * 创建读书笔记
 */
export const createNote = (data: { bookId: number; title: string; content: string }) => {
  return request.post('/reading-notes', data)
}

/**
 * 更新读书笔记
 */
export const updateNote = (id: number, data: { bookId: number; title: string; content: string }) => {
  return request.put(`/reading-notes/${id}`, data)
}

/**
 * 删除读书笔记
 */
export const deleteNote = (id: number) => {
  return request.delete(`/reading-notes/${id}`)
}

/**
 * 获取当前用户的笔记列表
 */
export const getMyNotes = () => {
  return request.get('/reading-notes/my')
}

/**
 * 获取某图书的笔记列表
 */
export const getBookNotes = (bookId: number) => {
  return request.get(`/reading-notes/book/${bookId}`)
}

/**
 * 获取笔记详情
 */
export const getNoteDetail = (id: number) => {
  return request.get(`/reading-notes/${id}`)
}
