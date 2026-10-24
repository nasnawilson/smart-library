<template>
  <div class="number-wheel" :class="{ disabled }">
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
        v-for="item in numbers"
        :key="item"
        class="wheel-item"
        :class="{ active: item === modelValue }"
        @click="setValue(item)"
      >
        {{ formatValue(item) }}
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
  unit?: string
  pad?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  min: 0,
  max: 30,
  unit: '',
  pad: false,
  disabled: false
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
}>()

const ITEM_HEIGHT = 40
const listRef = ref<HTMLDivElement | null>(null)
const numbers = ref<number[]>([])
let scrollTimer: number | undefined
let isProgrammaticScroll = false

const buildNumbers = () => {
  const min = Math.floor(props.min)
  const max = Math.max(min, Math.floor(props.max))
  const arr: number[] = []
  for (let i = min; i <= max; i++) arr.push(i)
  numbers.value = arr
}

const formatValue = (value: number) => {
  const text = props.pad ? String(value).padStart(2, '0') : String(value)
  return `${text}${props.unit}`
}

const scrollToValue = (value: number, smooth = true) => {
  if (!listRef.value) return
  const idx = numbers.value.indexOf(value)
  if (idx < 0) return
  isProgrammaticScroll = true
  listRef.value.scrollTo({ top: idx * ITEM_HEIGHT, behavior: smooth ? 'smooth' : 'auto' })
  window.setTimeout(() => { isProgrammaticScroll = false }, smooth ? 350 : 50)
}

const setValue = (value: number) => {
  if (props.disabled) return
  if (value !== props.modelValue) emit('update:modelValue', value)
  scrollToValue(value)
}

const handleScroll = () => {
  if (props.disabled || isProgrammaticScroll) return
  if (scrollTimer) window.clearTimeout(scrollTimer)
  scrollTimer = window.setTimeout(() => {
    if (!listRef.value) return
    const idx = Math.round(listRef.value.scrollTop / ITEM_HEIGHT)
    const safeIdx = Math.max(0, Math.min(numbers.value.length - 1, idx))
    const value = numbers.value[safeIdx]
    if (value !== undefined && value !== props.modelValue) {
      emit('update:modelValue', value)
    }
    scrollToValue(value, true)
  }, 120)
}

const handleWheel = (e: WheelEvent) => {
  if (props.disabled || !listRef.value) return
  const dir = e.deltaY > 0 ? 1 : -1
  const idx = numbers.value.indexOf(props.modelValue)
  const nextIdx = Math.max(0, Math.min(numbers.value.length - 1, idx + dir))
  const nextValue = numbers.value[nextIdx]
  if (nextValue !== undefined && nextValue !== props.modelValue) {
    emit('update:modelValue', nextValue)
  }
}

watch(() => [props.min, props.max], () => {
  buildNumbers()
  let value = props.modelValue
  if (value < props.min) value = props.min
  if (value > props.max) value = props.max
  if (value !== props.modelValue) emit('update:modelValue', value)
  window.setTimeout(() => scrollToValue(value, false), 0)
})

watch(() => props.modelValue, (value) => {
  scrollToValue(value, true)
})

onMounted(() => {
  buildNumbers()
  window.setTimeout(() => scrollToValue(props.modelValue, false), 0)
})
</script>

<style scoped>
.number-wheel {
  position: relative;
  width: 96px;
  height: 200px;
  overflow: hidden;
  user-select: none;
  border: 1px solid var(--tblr-border-color);
  border-radius: var(--tblr-border-radius);
  background: linear-gradient(
    180deg,
    var(--tblr-bg-surface-secondary) 0%,
    var(--tblr-bg-surface) 50%,
    var(--tblr-bg-surface-secondary) 100%
  );
}

.number-wheel.disabled {
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
  color: #94a3b8;
  cursor: pointer;
  font-size: 18px;
  scroll-snap-align: center;
  transition: color 0.2s, font-weight 0.2s, transform 0.2s;
}

.wheel-item.active {
  color: #2563eb;
  font-size: 20px;
  font-weight: 700;
  transform: scale(1.05);
}

.wheel-mask {
  position: absolute;
  z-index: 2;
  left: 0;
  right: 0;
  height: 80px;
  pointer-events: none;
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
  z-index: 1;
  left: 8px;
  right: 8px;
  top: 80px;
  height: 40px;
  pointer-events: none;
  border-top: 1px solid #cbd5e1;
  border-bottom: 1px solid #cbd5e1;
  background: rgba(37, 99, 235, 0.05);
}
</style>
