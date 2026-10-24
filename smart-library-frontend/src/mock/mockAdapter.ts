import type { AxiosAdapter, AxiosRequestConfig, AxiosResponse } from 'axios'

type MockUser = {
  id: number
  studentNo: string
  password: string
  name: string
  department: string
  className: string
  violationCount: number
  role: string
  status: string
}

type MockBook = {
  id: number
  title: string
  author: string
  isbn: string
  publisher: string
  category: string
  coverImage: string
  description: string
  location: string
  totalCount: number
  borrowNum: number
  availableCount: number
  inLibrary: number
  floor: number
  subject: string
  tags: string
  isHot: number
  isNew: number
}

type MockSeat = {
  id: number
  floor: number
  seatNo: string
  status: 'FREE' | 'OCCUPIED' | 'TEMP_LEAVE'
  area: string
  currentUserId?: number
  currentUserName?: string
  occupyTime?: string
  tempLeaveExpireTime?: string
}

const users: MockUser[] = [
  { id: 1, studentNo: 'admin', password: 'admin', name: '系统管理', department: '系统管理', className: '管理员', violationCount: 0, role: 'ADMIN', status: 'ACTIVE' },
  { id: 2, studentNo: '20255030345', password: '123456', name: '20255030345', department: '文学院', className: '中文2302', violationCount: 0, role: 'USER', status: 'ACTIVE' },
  { id: 3, studentNo: '20255030346', password: '123456', name: '20255030346', department: '经管学院', className: '金融2301', violationCount: 0, role: 'USER', status: 'ACTIVE' }
]

const baseBooks = [
  ['Python编程：从入门到实践', '埃里克·马瑟斯', '热门', '1楼A区01架'],
  ['数据结构与算法分析（Java版）', '马克·艾伦·维斯', '学科', '1楼B区05架'],
  ['考研英语真题大全解', '张剑', '考研', '2楼C区03架'],
  ['百年孤独', '加西亚·马尔克斯', '文学', '2楼D区08架'],
  ['经济学原理（微观分册）', '曼昆', '学科', '1楼E区02架'],
  ['JavaScript高级程序设计', '马特·弗里斯比', '热门', '1楼A区08架'],
  ['红楼梦', '曹雪芹', '文学', '2楼D区01架'],
  ['金融市场学', '张亦春', '学科', '2楼E区06架'],
  ['算法图解', '巴尔加瓦', '热门', '1楼B区09架'],
  ['平凡的世界', '路遥', '文学', '2楼D区05架'],
  ['Java编程思想', 'Bruce Eckel', '学科', '1楼A区02架'],
  ['深入理解计算机系统', 'Randal E. Bryant', '学科', '1楼A区03架'],
  ['机器学习', '周志华', '热门', '1楼A区07架'],
  ['Vue.js设计与实现', '霍春阳', '新书', '1楼A区12架'],
  ['人工智能：一种现代方法', 'Stuart Russell', '新书', '1楼A区15架']
]

const books: MockBook[] = baseBooks.map((item, index) => {
  const total = 5 + (index % 6)
  const borrowed = index % 4
  const category = item[2]
  return {
    id: index + 1,
    title: item[0],
    author: item[1],
    isbn: `97870000000${String(index + 1).padStart(2, '0')}`,
    publisher: index % 2 === 0 ? '人民邮电出版社' : '机械工业出版社',
    category,
    coverImage: '',
    description: `${item[0]} 是智慧图书馆演示数据中的推荐图书。`,
    location: item[3],
    totalCount: total,
    borrowNum: borrowed,
    availableCount: total - borrowed,
    inLibrary: total > borrowed ? 1 : 0,
    floor: item[3].startsWith('2楼') ? 2 : 1,
    subject: category,
    tags: category,
    isHot: category === '热门' ? 1 : 0,
    isNew: category === '新书' ? 1 : 0
  }
})

const seats: MockSeat[] = Array.from({ length: 100 }, (_, index) => {
  const floor = index < 50 ? 1 : 2
  const num = (index % 50) + 1
  return {
    id: index + 1,
    floor,
    seatNo: `${floor}-${String(num).padStart(2, '0')}`,
    status: num === 3 ? 'OCCUPIED' : num === 8 ? 'TEMP_LEAVE' : 'FREE',
    area: floor === 1 ? '一楼阅览区' : '二楼自习区'
  }
})

const bookLists = [
  { id: 1, title: 'Python入门必备', description: '零基础入门 Python 的经典书单', bookIds: '1,6,9', creatorName: '系统推荐', bookCount: 3, likeCount: 12, collectCount: 8 },
  { id: 2, title: '计算机专业核心教材', description: '计科专业必读书目', bookIds: '2,11,12,13', creatorName: '系统推荐', bookCount: 4, likeCount: 20, collectCount: 15 },
  { id: 3, title: '文学经典必读', description: '中外文学经典作品', bookIds: '4,7,10', creatorName: '系统推荐', bookCount: 3, likeCount: 18, collectCount: 11 }
]

const borrowRecords: any[] = [
  { id: 1, userId: 2, bookId: 1, bookTitle: 'Python编程：从入门到实践', userName: '20255030345', studentNo: '20255030345', borrowDate: '2026-06-01', dueDate: '2026-07-01', status: 'BORROWED', renewCount: 0 }
]

const reservations: any[] = []

const hasActiveSeat = (userId = 1) => reservations.some((item) => item.userId === userId && ['PENDING', 'IN_USE', 'TEMP_LEAVE'].includes(item.status))

const enrichSeats = (list: MockSeat[]) => list.map((seat) => {
  const active = reservations.find((item) => item.seatId === seat.id && ['IN_USE', 'TEMP_LEAVE'].includes(item.status))
  return {
    ...seat,
    currentUserId: active?.userId,
    currentUserName: active ? users.find((user) => user.id === active.userId)?.name : undefined,
    occupyTime: active?.checkInTime,
    tempLeaveExpireTime: active?.tempLeaveExpireTime
  }
})

const ok = (config: AxiosRequestConfig, data: unknown): AxiosResponse => ({
  data: { code: 200, message: 'success', data },
  status: 200,
  statusText: 'OK',
  headers: {},
  config: config as any
})

const fail = (config: AxiosRequestConfig, message: string, status = 200): AxiosResponse => ({
  data: { code: 500, message, data: null },
  status,
  statusText: 'ERROR',
  headers: {},
  config: config as any
})

const getBody = (config: AxiosRequestConfig) => {
  if (!config.data) return {}
  return typeof config.data === 'string' ? JSON.parse(config.data) : config.data
}

const getCurrentUser = () => users[0]

const page = <T>(records: T[], current = 1, size = 10) => ({
  records,
  total: records.length,
  size,
  current,
  pages: Math.max(1, Math.ceil(records.length / size))
})

export const mockAdapter: AxiosAdapter = async (config) => {
  await new Promise((resolve) => setTimeout(resolve, 180))

  const method = (config.method || 'get').toLowerCase()
  const url = (config.url || '').replace(/^\/api/, '')
  const params: any = config.params || {}

  if (method === 'post' && url === '/auth/login') {
    const body = getBody(config)
    const account = String(body.studentNo || '').trim()
    const isAdmin = account.toLowerCase() === 'admin'
    if (!isAdmin && !/^\d{11}$/.test(account)) return fail(config, '请输入 11 位学号，或管理员账号 admin')
    const user = users.find((u) => u.studentNo === (isAdmin ? 'admin' : account))
    if (!user) return fail(config, '账号未注册，请先注册')
    if (user.password !== body.password) return fail(config, '密码错误，请重新输入')
    const { password, ...userInfo } = user
    return ok(config, { token: `mock-token-${user.id}`, userInfo })
  }

  if (method === 'post' && url === '/auth/register') {
    const body = getBody(config)
    if (String(body.studentNo || '').trim().toLowerCase() === 'admin') return fail(config, 'admin 为系统保留账号，请更换学号')
    users.push({
      id: users.length + 1,
      studentNo: body.studentNo,
      password: body.password,
      name: body.studentNo,
      department: body.department || '',
      className: body.className || '',
      violationCount: 0,
      role: 'USER',
      status: 'ACTIVE'
    })
    return ok(config, null)
  }

  if (method === 'get' && url === '/auth/info') {
    const { password, ...userInfo } = getCurrentUser()
    return ok(config, userInfo)
  }

  if (method === 'get' && url === '/books/categories') {
    return ok(config, [...new Set(books.map((book) => book.category))])
  }

  if (method === 'get' && url === '/books/hot') {
    return ok(config, books.filter((book) => book.isHot).slice(0, Number(params.limit || 10)))
  }

  if (method === 'get' && url === '/books/new') {
    return ok(config, books.filter((book) => book.isNew).slice(0, Number(params.limit || 10)))
  }

  if (method === 'get' && url === '/books') {
    let list = [...books]
    if (params.title) list = list.filter((book) => book.title.includes(params.title))
    if (params.author) list = list.filter((book) => book.author.includes(params.author))
    if (params.category) list = list.filter((book) => book.category === params.category)
    if (params.floor) list = list.filter((book) => book.floor === Number(params.floor))
    if (params.filter === 'hot') list = list.filter((book) => book.isHot)
    if (params.filter === 'new') list = list.filter((book) => book.isNew)
    return ok(config, page(list, Number(params.page || 1), Number(params.size || 10)))
  }

  const bookMatch = url.match(/^\/books\/(\d+)$/)
  if (method === 'get' && bookMatch) {
    return ok(config, books.find((book) => book.id === Number(bookMatch[1])) || null)
  }

  const seatFloorMatch = url.match(/^\/seats\/floor\/(\d+)$/)
  if (method === 'get' && seatFloorMatch) {
    return ok(config, enrichSeats(seats.filter((seat) => seat.floor === Number(seatFloorMatch[1]))))
  }

  if (method === 'get' && url === '/seats') {
    const list = params.floor ? seats.filter((seat) => seat.floor === Number(params.floor)) : seats
    return ok(config, enrichSeats(list))
  }

  const seatActionMatch = url.match(/^\/seats\/(\d+)\/(occupy|release|temp-leave|return)$/)
  if (method === 'post' && seatActionMatch) {
    const seat = seats.find((item) => item.id === Number(seatActionMatch[1]))
    if (seat) {
      const action = seatActionMatch[2]
      if (action === 'occupy') {
        if (hasActiveSeat()) return fail(config, '每位用户同一时间只能预约或入座一个座位，请先结束当前座位使用')
        if (seat.status !== 'FREE') return fail(config, '座位不可用')
        seat.status = 'OCCUPIED'
        reservations.unshift({ id: reservations.length + 1, userId: 1, seatId: seat.id, reservationDate: new Date().toISOString().slice(0, 10), timeSlot: '当前时段', status: 'IN_USE', checkInTime: new Date().toISOString(), seatNo: seat.seatNo, floor: seat.floor, area: seat.area })
      } else if (action === 'release') {
        const active = reservations.find((item) => item.userId === 1 && item.seatId === seat.id && ['IN_USE', 'TEMP_LEAVE'].includes(item.status))
        if (!active) return fail(config, '当前用户未使用该座位')
        active.status = 'COMPLETED'
        seat.status = 'FREE'
      } else if (action === 'temp-leave') {
        seat.status = 'TEMP_LEAVE'
        const active = reservations.find((item) => item.userId === 1 && item.seatId === seat.id && item.status === 'IN_USE')
        if (active) {
          active.status = 'TEMP_LEAVE'
          active.tempLeaveExpireTime = new Date(Date.now() + 15 * 60 * 1000).toISOString()
        }
      } else {
        seat.status = 'OCCUPIED'
        const active = reservations.find((item) => item.userId === 1 && item.seatId === seat.id && item.status === 'TEMP_LEAVE')
        if (active) {
          active.status = 'IN_USE'
          active.tempLeaveExpireTime = undefined
        }
      }
    }
    return ok(config, null)
  }

  if (method === 'post' && url === '/reservations') {
    const body = getBody(config)
    if (hasActiveSeat()) return fail(config, '每位用户同一时间只能预约或入座一个座位，请先结束当前座位使用')
    const seat = seats.find((item) => item.id === Number(body.seatId))
    const record = {
      id: reservations.length + 1,
      userId: 1,
      seatId: Number(body.seatId),
      reservationDate: body.reservationDate,
      timeSlot: body.timeSlot,
      status: 'PENDING',
      seatNo: seat?.seatNo,
      floor: seat?.floor,
      area: seat?.area
    }
    reservations.push(record)
    return ok(config, null)
  }

  const reservationActionMatch = url.match(/^\/reservations\/(\d+)\/(check-in|cancel|complete)$/)
  if (method === 'post' && reservationActionMatch) {
    const record = reservations.find((item) => item.id === Number(reservationActionMatch[1]))
    const action = reservationActionMatch[2]
    if (!record) return fail(config, '预约记录不存在')
    const seat = seats.find((item) => item.id === record.seatId)
    if (action === 'check-in') {
      const otherActive = reservations.some((item) => item.userId === 1 && item.id !== record.id && ['PENDING', 'IN_USE', 'TEMP_LEAVE'].includes(item.status))
      if (otherActive) return fail(config, '每位用户同一时间只能预约或入座一个座位，请先结束当前座位使用')
      record.status = 'IN_USE'
      record.checkInTime = new Date().toISOString()
      if (seat) seat.status = 'OCCUPIED'
    } else if (action === 'cancel') {
      const shouldRelease = ['IN_USE', 'TEMP_LEAVE'].includes(record.status)
      record.status = 'CANCELLED'
      if (seat && shouldRelease) seat.status = 'FREE'
    } else {
      record.status = 'COMPLETED'
      if (seat) seat.status = 'FREE'
    }
    return ok(config, null)
  }

  if (method === 'get' && url === '/reservations/my') {
    return ok(config, reservations)
  }

  if (method === 'get' && url === '/borrows/my') {
    return ok(config, borrowRecords)
  }

  if (method === 'get' && url === '/borrows') {
    return ok(config, page(borrowRecords, Number(params.page || 1), Number(params.size || 10)))
  }

  const borrowActionMatch = url.match(/^\/borrows\/(\d+)\/(return|renew)$/)
  if ((method === 'put' || method === 'post') && borrowActionMatch) {
    const record = borrowRecords.find((item) => item.id === Number(borrowActionMatch[1]))
    const action = borrowActionMatch[2]
    if (!record || record.status === 'RETURNED') return fail(config, '借阅记录不存在或已归还')
    if (action === 'return') {
      record.status = 'RETURNED'
      record.returnDate = new Date().toISOString().slice(0, 10)
      const book = books.find((item) => item.id === record.bookId)
      if (book) {
        book.borrowNum = Math.max(0, book.borrowNum - 1)
        book.availableCount += 1
        book.inLibrary = 1
      }
    } else {
      if (record.userId !== 1) return fail(config, '只能续借自己借阅的图书')
      if (record.renewCount > 0) return fail(config, '每本书只能续借一次')
      record.renewCount = 1
    }
    return ok(config, null)
  }

  if (method === 'post' && url === '/borrows') {
    const body = getBody(config)
    const book = books.find((item) => item.id === Number(body.bookId))
    if (!book || book.availableCount <= 0) return fail(config, '图书库存不足')
    const activeBorrows = borrowRecords.filter((item) => item.userId === 1 && item.status === 'BORROWED')
    if (activeBorrows.some((item) => item.bookId === book.id)) return fail(config, '同一账户同一本书只能借阅一本，请先归还后再借')
    if (activeBorrows.length >= 5) return fail(config, '每个账户最多同时借阅5本不同图书')
    book.borrowNum += 1
    book.availableCount -= 1
    borrowRecords.unshift({
      id: borrowRecords.length + 1,
      userId: 1,
      bookId: book.id,
      bookTitle: book.title,
      userName: users[0].name,
      studentNo: users[0].studentNo,
      borrowDate: new Date().toISOString().slice(0, 10),
      dueDate: new Date(Date.now() + 30 * 86400000).toISOString().slice(0, 10),
      status: 'BORROWED',
      renewCount: 0
    })
    return ok(config, null)
  }

  if (method === 'get' && url === '/book-lists') {
    return ok(config, bookLists)
  }

  const bookListMatch = url.match(/^\/book-lists\/(\d+)$/)
  if (method === 'get' && bookListMatch) {
    return ok(config, bookLists.find((item) => item.id === Number(bookListMatch[1])) || null)
  }

  const bookListItemsMatch = url.match(/^\/book-lists\/(\d+)\/items$/)
  if (method === 'get' && bookListItemsMatch) {
    const list = bookLists.find((item) => item.id === Number(bookListItemsMatch[1]))
    const ids = list?.bookIds.split(',').map(Number) || []
    return ok(config, ids.map((bookId, index) => {
      const book = books.find((item) => item.id === bookId)
      return { id: index + 1, listId: list?.id, bookId, sortOrder: index + 1, bookTitle: book?.title, bookAuthor: book?.author, bookCover: book?.coverImage }
    }))
  }

  if (method === 'get' && url === '/dashboard/stats') {
    return ok(config, {
      totalBooks: books.length,
      totalUsers: users.length,
      todayBorrows: borrowRecords.length,
      overdueCount: 0,
      freeSeats: seats.filter((seat) => seat.status === 'FREE').length,
      occupiedSeats: seats.filter((seat) => seat.status === 'OCCUPIED').length,
      todayReservations: reservations.length,
      categoryStats: [...new Set(books.map((book) => book.category))].map((category) => ({ category, count: books.filter((book) => book.category === category).length })),
      borrowTrend: Array.from({ length: 7 }, (_, index) => ({ date: `06-${String(index + 4).padStart(2, '0')}`, count: index + 1 })),
      recentBorrows: borrowRecords
    })
  }

  if (url.startsWith('/reading-') || url.startsWith('/favorites')) {
    return ok(config, method === 'get' ? [] : null)
  }

  return ok(config, null)
}
