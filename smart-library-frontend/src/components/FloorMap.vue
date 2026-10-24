<template>
  <div class="floor-map">
    <svg :viewBox="`0 0 ${svgWidth} ${svgHeight}`" class="floor-svg">
      <!-- 楼层背景 -->
      <rect x="10" y="10" :width="svgWidth - 20" :height="svgHeight - 20" fill="#fafafa" rx="8" stroke="#dcdfe6" stroke-width="2" />

      <!-- 入口 -->
      <g transform="translate(20, 280)">
        <rect width="60" height="30" fill="#67c23a" rx="4" />
        <text x="30" y="20" class="label-text">入口</text>
      </g>

      <!-- 出口 -->
      <g transform="translate(720, 280)">
        <rect width="60" height="30" fill="#f56c6c" rx="4" />
        <text x="30" y="20" class="label-text">出口</text>
      </g>

      <!-- 服务台 -->
      <g :transform="`translate(${svgWidth/2 - 40}, 20)`">
        <rect width="80" height="30" fill="#409eff" rx="4" />
        <text x="40" y="20" class="label-text">服务台</text>
      </g>

      <!-- 图书区域 -->
      <template v-if="floor === 1">
        <!-- A区 - 计算机类 -->
        <g transform="translate(100, 70)">
          <rect width="200" height="120" fill="#e6f7ff" rx="4" stroke="#1890ff" stroke-width="1" stroke-dasharray="4" />
          <text x="100" y="20" class="area-title">A区 - 计算机类</text>
          <text x="100" y="40" class="area-subtitle">1排-4排</text>
          <!-- 书架示意 -->
          <rect x="20" y="50" width="160" height="8" fill="#1890ff" rx="2" />
          <rect x="20" y="65" width="160" height="8" fill="#1890ff" rx="2" />
          <rect x="20" y="80" width="160" height="8" fill="#1890ff" rx="2" />
        </g>

        <!-- B区 - 文学类 -->
        <g transform="translate(100, 210)">
          <rect width="200" height="120" fill="#f6ffed" rx="4" stroke="#52c41a" stroke-width="1" stroke-dasharray="4" />
          <text x="100" y="20" class="area-title">B区 - 文学类</text>
          <text x="100" y="40" class="area-subtitle">1排-4排</text>
          <rect x="20" y="50" width="160" height="8" fill="#52c41a" rx="2" />
          <rect x="20" y="65" width="160" height="8" fill="#52c41a" rx="2" />
          <rect x="20" y="80" width="160" height="8" fill="#52c41a" rx="2" />
        </g>
      </template>

      <template v-if="floor === 2">
        <!-- C区 - 数学类 -->
        <g transform="translate(100, 70)">
          <rect width="200" height="120" fill="#fff7e6" rx="4" stroke="#fa8c16" stroke-width="1" stroke-dasharray="4" />
          <text x="100" y="20" class="area-title">C区 - 数学类</text>
          <text x="100" y="40" class="area-subtitle">1排-4排</text>
          <rect x="20" y="50" width="160" height="8" fill="#fa8c16" rx="2" />
          <rect x="20" y="65" width="160" height="8" fill="#fa8c16" rx="2" />
          <rect x="20" y="80" width="160" height="8" fill="#fa8c16" rx="2" />
        </g>

        <!-- D区 - 英语类 -->
        <g transform="translate(100, 210)">
          <rect width="100" height="120" fill="#f9f0ff" rx="4" stroke="#722ed1" stroke-width="1" stroke-dasharray="4" />
          <text x="50" y="20" class="area-title">D区 - 英语</text>
          <text x="50" y="40" class="area-subtitle">1排-4排</text>
          <rect x="10" y="50" width="80" height="8" fill="#722ed1" rx="2" />
          <rect x="10" y="65" width="80" height="8" fill="#722ed1" rx="2" />
        </g>

        <!-- E区 - 历史类 -->
        <g transform="translate(210, 210)">
          <rect width="90" height="120" fill="#fff1f0" rx="4" stroke="#f5222d" stroke-width="1" stroke-dasharray="4" />
          <text x="45" y="20" class="area-title">E区 - 历史</text>
          <text x="45" y="40" class="area-subtitle">1排-4排</text>
          <rect x="10" y="50" width="70" height="8" fill="#f5222d" rx="2" />
          <rect x="10" y="65" width="70" height="8" fill="#f5222d" rx="2" />
        </g>
      </template>

      <!-- 座位区域 -->
      <template v-if="floor === 1">
        <!-- 自习区 A01-A20 -->
        <g transform="translate(340, 70)">
          <rect width="200" height="100" fill="#e6fffb" rx="4" stroke="#13c2c2" stroke-width="1" />
          <text x="100" y="20" class="area-title">自习区 (A01-A20)</text>
          <circle cx="40" cy="50" r="8" fill="#13c2c2" />
          <circle cx="70" cy="50" r="8" fill="#13c2c2" />
          <circle cx="100" cy="50" r="8" fill="#13c2c2" />
          <circle cx="130" cy="50" r="8" fill="#13c2c2" />
          <circle cx="160" cy="50" r="8" fill="#13c2c2" />
          <circle cx="40" cy="75" r="8" fill="#13c2c2" />
          <circle cx="70" cy="75" r="8" fill="#13c2c2" />
          <circle cx="100" cy="75" r="8" fill="#13c2c2" />
          <circle cx="130" cy="75" r="8" fill="#13c2c2" />
          <circle cx="160" cy="75" r="8" fill="#13c2c2" />
        </g>

        <!-- 阅览区 B01-B20 -->
        <g transform="translate(340, 190)">
          <rect width="200" height="100" fill="#e6fffb" rx="4" stroke="#13c2c2" stroke-width="1" />
          <text x="100" y="20" class="area-title">阅览区 (B01-B20)</text>
          <circle cx="40" cy="50" r="8" fill="#13c2c2" />
          <circle cx="70" cy="50" r="8" fill="#13c2c2" />
          <circle cx="100" cy="50" r="8" fill="#13c2c2" />
          <circle cx="130" cy="50" r="8" fill="#13c2c2" />
          <circle cx="160" cy="50" r="8" fill="#13c2c2" />
          <circle cx="40" cy="75" r="8" fill="#13c2c2" />
          <circle cx="70" cy="75" r="8" fill="#13c2c2" />
          <circle cx="100" cy="75" r="8" fill="#13c2c2" />
          <circle cx="130" cy="75" r="8" fill="#13c2c2" />
          <circle cx="160" cy="75" r="8" fill="#13c2c2" />
        </g>

        <!-- 电脑区 C01-C10 -->
        <g transform="translate(560, 70)">
          <rect width="160" height="220" fill="#f0f5ff" rx="4" stroke="#2f54eb" stroke-width="1" />
          <text x="80" y="20" class="area-title">电脑区 (C01-C10)</text>
          <rect x="20" y="40" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="70" y="40" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="20" y="80" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="70" y="80" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="20" y="120" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="70" y="120" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="20" y="160" width="40" height="30" fill="#2f54eb" rx="2" />
          <rect x="70" y="160" width="40" height="30" fill="#2f54eb" rx="2" />
        </g>
      </template>

      <template v-if="floor === 2">
        <!-- 自习区 D01-D20 -->
        <g transform="translate(340, 70)">
          <rect width="200" height="100" fill="#e6fffb" rx="4" stroke="#13c2c2" stroke-width="1" />
          <text x="100" y="20" class="area-title">自习区 (D01-D20)</text>
          <circle cx="40" cy="50" r="8" fill="#13c2c2" />
          <circle cx="70" cy="50" r="8" fill="#13c2c2" />
          <circle cx="100" cy="50" r="8" fill="#13c2c2" />
          <circle cx="130" cy="50" r="8" fill="#13c2c2" />
          <circle cx="160" cy="50" r="8" fill="#13c2c2" />
          <circle cx="40" cy="75" r="8" fill="#13c2c2" />
          <circle cx="70" cy="75" r="8" fill="#13c2c2" />
          <circle cx="100" cy="75" r="8" fill="#13c2c2" />
          <circle cx="130" cy="75" r="8" fill="#13c2c2" />
          <circle cx="160" cy="75" r="8" fill="#13c2c2" />
        </g>

        <!-- 阅览区 E01-E20 -->
        <g transform="translate(340, 190)">
          <rect width="200" height="100" fill="#e6fffb" rx="4" stroke="#13c2c2" stroke-width="1" />
          <text x="100" y="20" class="area-title">阅览区 (E01-E20)</text>
          <circle cx="40" cy="50" r="8" fill="#13c2c2" />
          <circle cx="70" cy="50" r="8" fill="#13c2c2" />
          <circle cx="100" cy="50" r="8" fill="#13c2c2" />
          <circle cx="130" cy="50" r="8" fill="#13c2c2" />
          <circle cx="160" cy="50" r="8" fill="#13c2c2" />
          <circle cx="40" cy="75" r="8" fill="#13c2c2" />
          <circle cx="70" cy="75" r="8" fill="#13c2c2" />
          <circle cx="100" cy="75" r="8" fill="#13c2c2" />
          <circle cx="130" cy="75" r="8" fill="#13c2c2" />
          <circle cx="160" cy="75" r="8" fill="#13c2c2" />
        </g>

        <!-- 讨论区 F01-F10 -->
        <g transform="translate(560, 70)">
          <rect width="160" height="220" fill="#fff2e8" rx="4" stroke="#fa541c" stroke-width="1" />
          <text x="80" y="20" class="area-title">讨论区 (F01-F10)</text>
          <rect x="20" y="40" width="50" height="40" fill="#fa541c" rx="2" />
          <rect x="80" y="40" width="50" height="40" fill="#fa541c" rx="2" />
          <rect x="20" y="90" width="50" height="40" fill="#fa541c" rx="2" />
          <rect x="80" y="90" width="50" height="40" fill="#fa541c" rx="2" />
          <rect x="20" y="140" width="50" height="40" fill="#fa541c" rx="2" />
          <rect x="80" y="140" width="50" height="40" fill="#fa541c" rx="2" />
        </g>
      </template>

      <!-- 洗手间 -->
      <g transform="translate(20, 120)">
        <rect width="50" height="40" fill="#bfbfbf" rx="4" />
        <text x="25" y="25" class="label-text">WC</text>
      </g>

      <!-- 休息区 -->
      <g transform="translate(20, 180)">
        <rect width="50" height="40" fill="#ffd666" rx="4" />
        <text x="25" y="25" class="label-text">休息</text>
      </g>

      <!-- 楼梯 -->
      <g :transform="`translate(${svgWidth - 80}, 120)`">
        <rect width="50" height="60" fill="#d9d9d9" rx="4" />
        <text x="25" y="20" class="label-text">楼梯</text>
        <line x1="10" y1="30" x2="40" y2="30" stroke="#8c8c8c" />
        <line x1="10" y1="40" x2="40" y2="40" stroke="#8c8c8c" />
        <line x1="10" y1="50" x2="40" y2="50" stroke="#8c8c8c" />
      </g>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { defineProps } from 'vue'

const props = defineProps<{
  floor: number
}>()

const svgWidth = 800
const svgHeight = 350
</script>

<style scoped>
.floor-map {
  flex: 1;
  min-width: 500px;
}

.floor-svg {
  width: 100%;
  height: auto;
  max-height: 500px;
}

.label-text {
  font-size: 12px;
  fill: #fff;
  text-anchor: middle;
  font-weight: bold;
}

.area-title {
  font-size: 13px;
  fill: #333;
  text-anchor: middle;
  font-weight: bold;
}

.area-subtitle {
  font-size: 11px;
  fill: #666;
  text-anchor: middle;
}
</style>
