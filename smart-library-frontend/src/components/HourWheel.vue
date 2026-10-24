<template>
  <div class="hour-wheel" :class="{ disabled }">
    <div class="wheel-mask top"></div>
    <div class="wheel-mask bottom"></div>
    <div class="wheel-indicator"></div>
    <div
      ref="listRef"
      class="wheel-list"
      @scroll="handleScroll"
      @wheel.prevent="handleWheel"
    >
      <div class="wheel-pad"></div>
      <div
        v-for="hour in hours"
        :key="hour"
        class="wheel-item"
        :class="{ active: hour === modelValue }"
        @click="setValue(hour)"
      >
        {{ formatHour(hour) }}
      </div>
      <div class="wheel-pad"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

interface Props {
  modelValue: number
  min?: number
  max?: number
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  min: 0,
  max: 23,
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
}>()

const ITEM_HEIGHT = 40
const listRef = ref<HTMLDivElement | null>(null)
const hours = ref<number[]>([])
let scrollTimer: number | undefined
let isProgrammaticScroll = false

const buildHours = () => {
  const min = Math.max(0, Math.min(23, props.min))
  const max = Math.max(min, Math.min(23, props.max))
  const arr: number[] = []
  for (let i = min; i <= max; i++) arr.push(i)
  hours.value = arr
}

const formatHour = (hour: number) => {
  return String(hour).padStart(2, '0') + ':00'
}

const scrollToValue = (value: number, smooth = true) => {
  if (!listRef.value) return
  const idx = hours.value.indexOf(value)
  if (idx < 0) return
  isProgrammaticScroll = true
  listRef.value.scrollTo({ top: idx * ITEM_HEIGHT, behavior: smooth ? 'smooth' : 'auto' })
  window.setTimeout(() => { isProgrammaticScroll = false }, smooth ? 350 : 50)
}

const setValue = (value: number) => {
  if (props.disabled) return
  if (value === props.modelValue) {
    scrollToValue(value)
    return
  }
  emit('update:modelValue', value)
}

const handleScroll = () => {
  if (props.disabled) return
  if (isProgrammaticScroll) return
  if (scrollTimer) window.clearTimeout(scrollTimer)
  scrollTimer = window.setTimeout(() => {
    if (!listRef.value) return
    const idx = Math.round(listRef.value.scrollTop / ITEM_HEIGHT)
    const safeIdx = Math.max(0, Math.min(hours.value.length - 1, idx))
    const value = hours.value[safeIdx]
    if (value !== undefined && value !== props.modelValue) {
      emit('update:modelValue', value)
    }
    scrollToValue(value, true)
  }, 120)
}

const handleWheel = (e: WheelEvent) => {
  if (props.disabled || !listRef.value) return
  const dir = e.deltaY > 0 ? 1 : -1
  const idx = hours.value.indexOf(props.modelValue)
  const nextIdx = Math.max(0, Math.min(hours.value.length - 1, idx + dir))
  const nextValue = hours.value[nextIdx]
  if (nextValue !== undefined && nextValue !== props.modelValue) {
    emit('update:modelValue', nextValue)
  }
}

watch(() => [props.min, props.max], () => {
  buildHours()
  // 边界裁剪
  let v = props.modelValue
  if (v < props.min) v = props.min
  if (v > props.max) v = props.max
  if (v !== props.modelValue) emit('update:modelValue', v)
  // 等待 DOM 渲染
  window.setTimeout(() => scrollToValue(v, false), 0)
})

watch(() => props.modelValue, (v) => {
  scrollToValue(v, true)
})

onMounted(() => {
  buildHours()
  window.setTimeout(() => scrollToValue(props.modelValue, false), 0)
})
</script>

<style scoped>
.hour-wheel {
  position: relative;
  width: 96px;
  height: 200px;
  background: linear-gradient(
    180deg,
    var(--tblr-bg-surface-secondary) 0%,
    var(--tblr-bg-surface) 50%,
    var(--tblr-bg-surface-secondary) 100%
  );
  border-radius: var(--tblr-border-radius);
  border: 1px solid var(--tblr-border-color);
  overflow: hidden;
  user-select: none;
}

.hour-wheel.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.wheel-list {
  position: absolute;
  inset: 0;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
  scrollbar-width: none;
}

.wheel-list::-webkit-scrollbar {
  display: none;
}

.wheel-pad {
  height: 80px;
}

.wheel-item {
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 18px;
  color: #94a3b8;
  scroll-snap-align: center;
  cursor: pointer;
  transition: color 0.2s, font-weight 0.2s, transform 0.2s;
}

.wheel-item.active {
  color: #2563eb;
  font-weight: 700;
  font-size: 20px;
  transform: scale(1.05);
}

.wheel-mask {
  position: absolute;
  left: 0;
  right: 0;
  height: 80px;
  pointer-events: none;
  z-index: 2;
}

.wheel-mask.top {
  top: 0;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--tblr-bg-surface-secondary) 95%, transparent) 0%,
    transparent 100%
  );
}

.wheel-mask.bottom {
  bottom: 0;
  background: linear-gradient(
    0deg,
    color-mix(in srgb, var(--tblr-bg-surface-secondary) 95%, transparent) 0%,
    transparent 100%
  );
}

.wheel-indicator {
  position: absolute;
  left: 8px;
  right: 8px;
  top: 80px;
  height: 40px;
  border-top: 1px solid #cbd5e1;
  border-bottom: 1px solid #cbd5e1;
  background: rgba(37, 99, 235, 0.05);
  pointer-events: none;
  z-index: 1;
}
</style>
