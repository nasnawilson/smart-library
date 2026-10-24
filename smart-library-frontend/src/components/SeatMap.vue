<template>
  <div class="seat-map">
    <svg viewBox="0 0 800 400" class="seat-svg">
      <!-- 背景区域 -->
      <rect x="10" y="10" width="780" height="380" fill="#f5f7fa" rx="8" />

      <!-- 区域标签 -->
      <text x="150" y="35" class="area-label">座位区 01-20</text>
      <text x="400" y="35" class="area-label">座位区 21-40</text>
      <text x="650" y="35" class="area-label">座位区 41-50</text>

      <!-- 座位渲染 -->
      <g v-for="seat in seats" :key="seat.id">
        <rect
          :x="getSeatX(seat)"
          :y="getSeatY(seat)"
          width="50"
          height="40"
          rx="4"
          :class="['seat-rect', getSeatClass(seat)]"
          @click="handleClick(seat)"
        />
        <text
          :x="getSeatX(seat) + 25"
          :y="getSeatY(seat) + 24"
          class="seat-text"
          @click="handleClick(seat)"
        >
          {{ seat.seatNo }}
        </text>
      </g>

      <!-- 图例 -->
      <g transform="translate(20, 360)">
        <rect x="0" y="0" width="16" height="12" rx="2" class="seat-rect free" />
        <text x="22" y="10" class="legend-text">空闲</text>

        <rect x="80" y="0" width="16" height="12" rx="2" class="seat-rect occupied" />
        <text x="102" y="10" class="legend-text">有人</text>

        <rect x="160" y="0" width="16" height="12" rx="2" class="seat-rect reserved" />
        <text x="182" y="10" class="legend-text">被预定</text>

        <rect x="260" y="0" width="16" height="12" rx="2" class="seat-rect temp-leave" />
        <text x="282" y="10" class="legend-text">暂离</text>
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { defineProps, defineEmits } from 'vue'

const props = defineProps<{
  seats: any[]
}>()

const emit = defineEmits<{
  (e: 'select', seat: any): void
}>()

/**
 * 根据座位号计算坐标。
 * 兼容旧格式 A01/B01/C01，也兼容新 PRD 格式 1-01/2-01。
 */
const getSeatNumber = (seat: any) => {
  const seatNo = String(seat.seatNo || '')
  const matched = seatNo.match(/(\d+)$/)
  return matched ? Number(matched[1]) : 1
}

const getSeatX = (seat: any) => {
  const seatNo = String(seat.seatNo || '')
  const prefix = seatNo.charAt(0)
  const num = getSeatNumber(seat)

  if (/^\d-\d{2}$/.test(seatNo)) {
    if (num <= 20) {
      const col = (num - 1) % 10
      return 50 + col * 55
    }
    if (num <= 40) {
      const col = (num - 21) % 10
      return 50 + col * 55
    }
    const col = (num - 41) % 3
    return 600 + col * 55
  }

  if (prefix === 'A' || prefix === 'D') {
    const col = (num - 1) % 10
    return 50 + col * 55
  } else if (prefix === 'B' || prefix === 'E') {
    const col = (num - 1) % 10
    return 50 + col * 55
  } else if (prefix === 'C' || prefix === 'F') {
    const col = (num - 1) % 3
    return 600 + col * 55
  }
  return 50
}

const getSeatY = (seat: any) => {
  const seatNo = String(seat.seatNo || '')
  const prefix = seatNo.charAt(0)
  const num = getSeatNumber(seat)

  if (/^\d-\d{2}$/.test(seatNo)) {
    if (num <= 20) {
      const row = Math.floor((num - 1) / 10)
      return 60 + row * 55
    }
    if (num <= 40) {
      const row = Math.floor((num - 21) / 10)
      return 200 + row * 55
    }
    const row = Math.floor((num - 41) / 3)
    return 60 + row * 55
  }

  if (prefix === 'A' || prefix === 'D') {
    const row = Math.floor((num - 1) / 10)
    return 60 + row * 55
  } else if (prefix === 'B' || prefix === 'E') {
    const row = Math.floor((num - 1) / 10)
    return 200 + row * 55
  } else if (prefix === 'C' || prefix === 'F') {
    const row = Math.floor((num - 1) / 3)
    return 60 + row * 55
  }
  return 60
}

const getSeatClass = (seat: any) => {
  const map: Record<string, string> = {
    FREE: 'free',
    RESERVED: 'reserved',
    OCCUPIED: 'occupied',
    TEMP_LEAVE: 'temp-leave'
  }
  return map[seat.status] || 'free'
}

const handleClick = (seat: any) => {
  emit('select', seat)
}
</script>

<style scoped>
.seat-map {
  padding: 10px;
}

.seat-svg {
  width: 100%;
  height: auto;
  max-height: 500px;
}

.area-label {
  font-size: 14px;
  font-weight: bold;
  fill: #606266;
}

.seat-rect {
  cursor: pointer;
  stroke: #fff;
  stroke-width: 1;
  transition: all 0.2s;
}

.seat-rect:hover {
  stroke: #409eff;
  stroke-width: 2;
  filter: brightness(1.1);
}

.seat-rect.free {
  fill: #67c23a;
}

.seat-rect.occupied {
  fill: #f56c6c;
}

.seat-rect.reserved {
  fill: #409eff;
}

.seat-rect.temp-leave {
  fill: #e6a23c;
}

.seat-text {
  font-size: 12px;
  fill: #fff;
  text-anchor: middle;
  cursor: pointer;
  pointer-events: none;
}

.legend-text {
  font-size: 12px;
  fill: #606266;
}
</style>
