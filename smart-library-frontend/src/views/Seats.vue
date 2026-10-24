<template>
  <div class="row row-cards">
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">{{ isAdmin ? '座位管理' : '座位预约' }}</h3>
            <div class="text-secondary small mt-1">
              {{ isAdmin ? '查看与维护座位实时状态' : '选择座位、预约时段与签到使用' }}
            </div>
          </div>
          <div class="card-actions">
            <div class="legend">
              <span class="legend-item"><span class="legend-dot bg-success"></span>空闲</span>
              <span class="legend-item"><span class="legend-dot bg-primary"></span>被预定</span>
              <span class="legend-item"><span class="legend-dot bg-warning"></span>暂离</span>
              <span class="legend-item"><span class="legend-dot bg-danger"></span>有人</span>
            </div>
          </div>
        </div>

        <div class="card-body">
          <div class="row g-3 stat-grid mb-3">
            <div v-for="item in seatStatsCards" :key="item.label" class="col-6 col-md-4 col-xl-2">
              <div class="stat-tile">
                <div class="subheader">{{ item.label }}</div>
                <div class="stat-tile-value">{{ item.value }}</div>
              </div>
            </div>
          </div>

          <el-tabs v-model="activeFloor" @tab-change="handleFloorChange">
            <el-tab-pane label="一楼" name="1">
              <SeatMap :seats="floor1Seats" @select="handleSeatSelect" />
            </el-tab-pane>
            <el-tab-pane label="二楼" name="2">
              <SeatMap :seats="floor2Seats" @select="handleSeatSelect" />
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>

    <!-- 座位操作对话框 -->
    <el-dialog v-model="dialogVisible" :title="`座位 ${selectedSeat?.seatNo}`" width="400px">
      <div v-if="selectedSeat" class="seat-detail">
        <div class="row g-3">
          <div class="col-12 col-md-6">
            <div class="text-secondary small">楼层</div>
            <div class="fw-bold">{{ selectedSeat.floor }} 楼</div>
          </div>
          <div class="col-12 col-md-6">
            <div class="text-secondary small">区域</div>
            <div class="fw-bold">{{ selectedSeat.area }}</div>
          </div>
          <div class="col-12 col-md-6">
            <div class="text-secondary small">状态</div>
            <span class="badge" :class="`bg-${getStatusType(selectedSeat.status)}-lt`">
              {{ getStatusLabel(selectedSeat.status) }}
            </span>
          </div>
          <div
            v-if="isAdmin && (selectedSeat.status === 'OCCUPIED' || selectedSeat.status === 'TEMP_LEAVE')"
            class="col-12 col-md-6"
          >
            <div class="text-secondary small">占用用户</div>
            <div class="fw-bold">{{ selectedSeat.currentUserName || '未知用户' }}</div>
          </div>
          <div
            v-if="selectedSeat.status === 'OCCUPIED' || selectedSeat.status === 'TEMP_LEAVE'"
            class="col-12 col-md-6"
          >
            <div class="text-secondary small">已入座</div>
            <span class="badge bg-success-lt">{{ formatDuration(selectedSeat.occupyTime) }}</span>
          </div>
          <div v-if="selectedSeat.status === 'TEMP_LEAVE'" class="col-12 col-md-6">
            <div class="text-secondary small">暂离倒计时</div>
            <span class="badge bg-warning-lt">{{ formatRemaining(selectedSeat.tempLeaveExpireTime) }}</span>
          </div>
        </div>

        <div v-if="selectedSeat.reservedTimeSlots?.length" class="reserved-slots">
          <div class="text-secondary small mb-2">已预约时段</div>
          <div class="d-flex flex-wrap gap-1">
            <span v-for="slot in selectedSeat.reservedTimeSlots" :key="slot" class="badge bg-primary-lt">
              {{ slot }}
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="d-flex flex-wrap justify-content-end gap-2">
          <button type="button" class="btn" @click="dialogVisible = false">关闭</button>
          <button
            v-if="!isAdmin && selectedSeat?.status === 'FREE'"
            type="button"
            class="btn btn-primary"
            @click="handleOccupy"
          >
            立即入座
          </button>
          <button
            v-if="!isAdmin && (selectedSeat?.status === 'FREE' || selectedSeat?.status === 'RESERVED')"
            type="button"
            class="btn btn-success"
            @click="handleReserve"
          >
            预约座位
          </button>
          <button
            v-if="selectedSeat?.status === 'OCCUPIED' && canManageSelectedSeat"
            type="button"
            class="btn btn-warning"
            @click="handleTempLeave"
          >
            暂离
          </button>
          <button
            v-if="selectedSeat?.status === 'TEMP_LEAVE' && canManageSelectedSeat"
            type="button"
            class="btn btn-primary"
            @click="handleReturn"
          >
            归来
          </button>
          <button
            v-if="(selectedSeat?.status === 'OCCUPIED' || selectedSeat?.status === 'TEMP_LEAVE') && canManageSelectedSeat"
            type="button"
            class="btn btn-outline-danger"
            @click="handleRelease"
          >
            离开座位
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 预约对话框 -->
    <el-dialog v-if="!isAdmin" v-model="reserveDialogVisible" title="预约座位" width="560px">
      <el-form :model="reserveForm" label-width="100px">
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="reserveForm.reservationDate"
            type="date"
            placeholder="选择日期"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="时间段">
          <div class="wheel-picker">
            <div class="wheel-col">
              <div class="wheel-label">开始</div>
              <HourWheel v-model="reserveForm.startHour" :min="0" :max="22" />
            </div>
            <div class="wheel-sep">至</div>
            <div class="wheel-col">
              <div class="wheel-label">结束</div>
              <HourWheel v-model="reserveForm.endHour" :min="reserveForm.startHour + 1" :max="23" />
            </div>
            <div class="wheel-summary">
              <div class="wheel-summary-tag">已选时段</div>
              <div class="wheel-summary-value">
                {{ formatHourLabel(reserveForm.startHour) }} - {{ formatHourLabel(reserveForm.endHour) }}
              </div>
              <div class="wheel-summary-hint">共 {{ reserveForm.endHour - reserveForm.startHour }} 小时</div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn" @click="reserveDialogVisible = false">取消</button>
          <button type="button" class="btn btn-primary" @click="submitReserve">确认预约</button>
        </div>
      </template>
    </el-dialog>

    <!-- 我的预约 -->
    <div v-if="!isAdmin" class="col-12">
      <div class="card">
        <div class="card-header">
          <h3 class="card-title">我的预约</h3>
          <div class="card-actions">
            <span class="badge bg-primary-lt">{{ myReservations.length }} 条记录</span>
          </div>
        </div>
        <div class="table-responsive">
          <el-table :data="myReservations" style="width: 100%">
            <el-table-column prop="seatNo" label="座位号" width="100" />
            <el-table-column prop="floor" label="楼层" width="80" />
            <el-table-column prop="area" label="区域" width="100" />
            <el-table-column prop="reservationDate" label="预约日期" width="120" />
            <el-table-column prop="timeSlot" label="时间段" width="160">
              <template #default="{ row }">
                {{ getTimeSlotLabel(row.timeSlot) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <span class="badge" :class="`bg-${getReservationStatusType(row.status)}-lt`">
                  {{ getReservationStatusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="使用时长" width="120">
              <template #default="{ row }">
                <span v-if="row.status === 'IN_USE' || row.status === 'TEMP_LEAVE'" class="badge bg-success-lt">
                  {{ formatDuration(row.checkInTime) }}
                </span>
                <span v-else class="text-secondary">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <div class="d-flex flex-wrap gap-1">
                  <button
                    v-if="row.status === 'PENDING'"
                    type="button"
                    class="btn btn-sm btn-primary"
                    @click="handleCheckIn(row.id)"
                  >
                    签到
                  </button>
                  <button
                    v-if="row.status === 'PENDING'"
                    type="button"
                    class="btn btn-sm"
                    @click="handleCancel(row.id)"
                  >
                    取消
                  </button>
                  <button
                    v-if="row.status === 'IN_USE'"
                    type="button"
                    class="btn btn-sm"
                    @click="handleComplete(row.id)"
                  >
                    离开座位
                  </button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SeatMap from '@/components/SeatMap.vue'
import HourWheel from '@/components/HourWheel.vue'
import { getSeatsByFloor, getSeatVersion, occupySeat, releaseSeat, tempLeave, returnFromTempLeave } from '@/api/seat'
import { reserveSeat, checkIn, cancelReservation, completeReservation, getMyReservations } from '@/api/reservation'
import { useUserStore } from '@/store/user'
import { isAdminUser } from '@/constants/auth'

const userStore = useUserStore()
const activeFloor = ref('1')
const floor1Seats = ref<any[]>([])
const floor2Seats = ref<any[]>([])
const dialogVisible = ref(false)
const reserveDialogVisible = ref(false)
const selectedSeat = ref<any>(null)
const myReservations = ref<any[]>([])
const now = ref(Date.now())
const seatVersion = ref<number | null>(null)
let timer: number | undefined
let refreshTimer: number | undefined
const isPageVisible = ref(!document.hidden)
const seatRefreshing = ref(false)

const reserveForm = ref({
  reservationDate: new Date(),
  startHour: 8,
  endHour: 10
})

const formatHourLabel = (hour: number) => `${String(hour).padStart(2, '0')}:00`

const isMySeat = computed(() => {
  return selectedSeat.value?.currentUserId === userStore.userInfo?.id
})

const isAdmin = computed(() => isAdminUser(userStore.userInfo))

const canManageSelectedSeat = computed(() => isMySeat.value || isAdmin.value)

const allSeats = computed(() => [...floor1Seats.value, ...floor2Seats.value])

const seatStatsCards = computed(() => {
  const free = allSeats.value.filter((seat) => seat.status === 'FREE').length
  const reserved = allSeats.value.filter((seat) => seat.status === 'RESERVED').length
  const occupied = allSeats.value.filter((seat) => seat.status === 'OCCUPIED').length
  const tempLeave = allSeats.value.filter((seat) => seat.status === 'TEMP_LEAVE').length
  const activeReservations = myReservations.value.filter((item) => ['PENDING', 'IN_USE', 'TEMP_LEAVE'].includes(item.status)).length
  const base = [
    { label: '总座位', value: allSeats.value.length, color: '#0f172a' },
    { label: '空闲', value: free, color: '#16a34a' },
    { label: '被预定', value: reserved, color: '#2563eb' },
    { label: '暂离', value: tempLeave, color: '#d97706' },
    { label: '有人', value: occupied, color: '#dc2626' }
  ]
  if (!isAdmin.value) {
    base.push({ label: '我的预约', value: activeReservations, color: '#2563eb' })
  }
  return base
})

const formatDuration = (startTime: string | Date | null | undefined) => {
  if (!startTime) return '0分钟'
  const start = new Date(startTime).getTime()
  if (Number.isNaN(start)) return '0分钟'
  const diff = Math.max(0, now.value - start)
  const totalMinutes = Math.floor(diff / 60000)
  const hours = Math.floor(totalMinutes / 60)
  const minutes = totalMinutes % 60
  if (hours <= 0) return `${minutes}分钟`
  return `${hours}小时${minutes}分钟`
}

const formatRemaining = (endTime: string | Date | null | undefined) => {
  if (!endTime) return '15分钟'
  const end = new Date(endTime).getTime()
  if (Number.isNaN(end)) return '15分钟'
  const diff = end - now.value
  if (diff <= 0) return '即将自动释放'
  const totalSeconds = Math.ceil(diff / 1000)
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}分${String(seconds).padStart(2, '0')}秒`
}

const fetchSeats = async (silent = true) => {
  try {
    const res1 = await getSeatsByFloor(1)
    floor1Seats.value = res1.data || []
    const res2 = await getSeatsByFloor(2)
    floor2Seats.value = res2.data || []
    syncSelectedSeat()
  } catch (e) {
    if (!silent) ElMessage.error('获取座位信息失败')
    throw e
  }
}

const syncSelectedSeat = () => {
  if (!selectedSeat.value) return
  const seats = [...floor1Seats.value, ...floor2Seats.value]
  const latest = seats.find((seat) => seat.id === selectedSeat.value.id)
  if (latest) selectedSeat.value = latest
}

const refreshSeatData = async (silent = true) => {
  if (!isPageVisible.value) return
  if (seatRefreshing.value) return
  seatRefreshing.value = true
  try {
    await fetchSeats(silent)
    await fetchMyReservations()
  } catch (e) {
    if (!silent) ElMessage.error('刷新座位信息失败')
  } finally {
    seatRefreshing.value = false
  }
}

const updateSeatVersion = async () => {
  const res = await getSeatVersion()
  seatVersion.value = res.data
}

const refreshSeatDataIfChanged = async () => {
  if (!isPageVisible.value) return
  if (dialogVisible.value || reserveDialogVisible.value) return
  try {
    const res = await getSeatVersion()
    if (seatVersion.value === null || seatVersion.value !== res.data) {
      seatVersion.value = res.data
      await refreshSeatData()
    } else {
      await fetchMyReservations()
    }
  } catch (e) {
    // 静默等待下一轮检查
  }
}

const fetchMyReservations = async () => {
  try {
    const res = await getMyReservations()
    myReservations.value = res.data || []
  } catch (e) {
    // 忽略错误
  }
}

const handleFloorChange = () => {
  refreshSeatData(false)
}

const handleVisibilityChange = () => {
  isPageVisible.value = !document.hidden
  if (isPageVisible.value) {
    refreshSeatData()
  }
}

const handleSeatSelect = (seat: any) => {
  selectedSeat.value = seat
  dialogVisible.value = true
}

const handleOccupy = async () => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生入座功能')
    return
  }
  try {
    await occupySeat(selectedSeat.value.id)
    ElMessage.success('入座成功')
    dialogVisible.value = false
    refreshSeatData()
    updateSeatVersion()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '入座失败')
  }
}

const handleReserve = () => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生预约座位功能')
    return
  }
  const nowHour = new Date().getHours()
  const start = Math.min(22, Math.max(0, nowHour))
  reserveForm.value = {
    reservationDate: new Date(),
    startHour: start,
    endHour: Math.min(23, start + 2)
  }
  reserveDialogVisible.value = true
}

const submitReserve = async () => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生预约座位功能')
    return
  }
  if (reserveForm.value.endHour <= reserveForm.value.startHour) {
    ElMessage.warning('结束时间必须晚于开始时间')
    return
  }
  try {
    await reserveSeat({
      seatId: selectedSeat.value.id,
      reservationDate: reserveForm.value.reservationDate.toISOString().split('T')[0],
      startHour: reserveForm.value.startHour,
      endHour: reserveForm.value.endHour
    })
    ElMessage.success('预约成功')
    reserveDialogVisible.value = false
    dialogVisible.value = false
    fetchMyReservations()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '预约失败')
  }
}

const handleTempLeave = async () => {
  ElMessageBox.confirm(`确认将座位 ${selectedSeat.value?.seatNo} 标记为暂离吗？系统会开始15分钟倒计时。`, '暂离确认', {
    confirmButtonText: '确认暂离',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await tempLeave(selectedSeat.value.id)
      ElMessage.warning('已开始15分钟暂离计时，请及时返回座位')
      dialogVisible.value = false
      refreshSeatData()
      updateSeatVersion()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  })
}

const handleReturn = async () => {
  try {
    await returnFromTempLeave(selectedSeat.value.id)
    ElMessage.success('欢迎归来')
    dialogVisible.value = false
    refreshSeatData()
    updateSeatVersion()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

const handleRelease = async () => {
  const userText = isAdmin.value ? `占用用户：${selectedSeat.value?.currentUserName || '未知用户'}。` : ''
  ElMessageBox.confirm(`${userText}确认释放座位 ${selectedSeat.value?.seatNo} 吗？`, '释放座位确认', {
    confirmButtonText: '确认释放',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await releaseSeat(selectedSeat.value.id)
      ElMessage.success('已离开座位')
      dialogVisible.value = false
      refreshSeatData()
      updateSeatVersion()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  })
}

const handleCheckIn = async (id: number) => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生签到功能')
    return
  }
  try {
    await checkIn(id)
    ElMessage.success('签到成功')
    refreshSeatData()
    updateSeatVersion()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '签到失败')
  }
}

const handleCancel = async (id: number) => {
  if (isAdmin.value) {
    ElMessage.warning('管理员不具备学生取消预约功能')
    return
  }
  ElMessageBox.confirm('确认取消该座位预约吗？', '取消预约确认', {
    confirmButtonText: '确认取消',
    cancelButtonText: '返回',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelReservation(id)
      ElMessage.success('预约已取消')
      fetchMyReservations()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '取消失败')
    }
  })
}

const handleComplete = async (id: number) => {
  if (isAdmin.value) {
    ElMessage.warning('管理员请在座位管理中维护座位状态')
    return
  }
  ElMessageBox.confirm('确认结束本次座位使用吗？', '离开座位确认', {
    confirmButtonText: '确认离开',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await completeReservation(id)
      ElMessage.success('已离开座位')
      refreshSeatData()
      updateSeatVersion()
    } catch (e: any) {
      ElMessage.error(e.response?.data?.message || '操作失败')
    }
  })
}

const disabledDate = (time: Date) => {
  return time.getTime() < Date.now() - 86400000
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    FREE: 'success',
    RESERVED: 'primary',
    OCCUPIED: 'danger',
    TEMP_LEAVE: 'warning'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    FREE: '空闲',
    RESERVED: '被预定',
    OCCUPIED: '有人',
    TEMP_LEAVE: '暂离'
  }
  return map[status] || status
}

const getTimeSlotLabel = (slot: string) => {
  if (!slot) return '-'
  // 兼容旧数据
  const legacy: Record<string, string> = {
    MORNING: '上午 8:00-12:00',
    AFTERNOON: '下午 14:00-18:00',
    EVENING: '晚上 19:00-22:00',
    上午: '上午 8:00-12:00',
    下午: '下午 14:00-18:00',
    晚上: '晚上 19:00-22:00'
  }
  if (legacy[slot]) return legacy[slot]
  // 新格式 HH:00-HH:00 直接返回
  return slot
}

const getReservationStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    IN_USE: 'success',
    TEMP_LEAVE: 'warning',
    COMPLETED: 'info',
    CANCELLED: 'info',
    VIOLATION: 'danger'
  }
  return map[status] || 'info'
}

const getReservationStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待使用',
    IN_USE: '使用中',
    TEMP_LEAVE: '暂离',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    VIOLATION: '已违规'
  }
  return map[status] || status
}

onMounted(() => {
  refreshSeatData(false).then(updateSeatVersion)
  document.addEventListener('visibilitychange', handleVisibilityChange)
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
  refreshTimer = window.setInterval(() => {
    refreshSeatDataIfChanged()
  }, 5000)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  if (timer) window.clearInterval(timer)
  if (refreshTimer) window.clearInterval(refreshTimer)
})
</script>

<style scoped>
.card-header {
  flex-wrap: wrap;
  gap: 0.75rem;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem 1rem;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.8125rem;
  color: var(--tblr-secondary);
}

.legend-dot {
  display: inline-block;
  width: 0.625rem;
  height: 0.625rem;
  border-radius: 50%;
}

.stat-tile {
  height: 100%;
  padding: 0.75rem 0.875rem;
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);
  background-color: var(--tblr-bg-surface-secondary);
}

.stat-tile-value {
  margin-top: 0.25rem;
  font-size: 1.5rem;
  font-weight: 600;
  line-height: 1.1;
  color: var(--tblr-body-color);
  font-variant-numeric: tabular-nums;
}

/* 统计着色顺序：总数 / 空闲 / 被预定 / 暂离 / 有人 / 我的预约 */
.stat-grid > div:nth-child(2) .stat-tile-value {
  color: var(--tblr-success);
}

.stat-grid > div:nth-child(3) .stat-tile-value {
  color: var(--tblr-primary);
}

.stat-grid > div:nth-child(4) .stat-tile-value {
  color: var(--tblr-warning);
}

.stat-grid > div:nth-child(5) .stat-tile-value {
  color: var(--tblr-danger);
}

.stat-grid > div:nth-child(6) .stat-tile-value {
  color: var(--tblr-primary);
}

.reserved-slots {
  margin-top: 0.75rem;
}

.wheel-picker {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
}

.wheel-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.wheel-label {
  font-size: 0.75rem;
  color: var(--tblr-secondary);
}

.wheel-sep {
  font-size: 0.875rem;
  color: var(--tblr-secondary);
  padding-top: 1.5rem;
}

.wheel-summary {
  margin-left: 0.75rem;
  padding: 0.75rem 1rem;
  min-width: 9.375rem;
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);
  background-color: var(--tblr-bg-surface-secondary);
}

.wheel-summary-tag {
  margin-bottom: 0.25rem;
  font-size: 0.75rem;
  color: var(--tblr-secondary);
}

.wheel-summary-value {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--tblr-body-color);
  font-variant-numeric: tabular-nums;
}

.wheel-summary-hint {
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--tblr-success);
}
</style>
