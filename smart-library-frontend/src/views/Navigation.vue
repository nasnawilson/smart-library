<template>
  <div>
    <div class="row row-cards">
      <!-- 楼层平面图 -->
      <div class="col-12 col-lg-8">
        <div class="card h-100">
          <div class="card-header">
            <h3 class="card-title">楼层平面图</h3>
            <div class="card-actions">
              <el-radio-group v-model="activeFloor" size="small">
                <el-radio-button label="1">一楼</el-radio-button>
                <el-radio-button label="2">二楼</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div class="card-body">
            <div class="map-scroll">
              <FloorMap :floor="Number(activeFloor)" />
            </div>
          </div>
        </div>
      </div>

      <!-- 快速导航 -->
      <div class="col-12 col-lg-4">
        <div class="card h-100">
          <div class="card-header">
            <h3 class="card-title">快速导航</h3>
            <div class="card-actions">
              <span class="badge bg-primary-lt">点击定位</span>
            </div>
          </div>
          <div class="card-body">
            <div class="subheader mb-2">图书区域</div>
            <div class="row g-3 mb-3">
              <div v-for="area in bookAreas" :key="area.code" class="col-12 col-md-6">
                <div class="card card-link" @click="highlightArea(area.code)">
                  <div class="card-body d-flex align-items-center gap-2">
                    <span class="avatar avatar-sm rounded bg-primary-lt text-primary">
                      <el-icon :size="16"><Reading /></el-icon>
                    </span>
                    <div class="entry-text">
                      <div class="fw-bold text-truncate">{{ area.name }}</div>
                      <div class="text-secondary small">书架区域</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="subheader mb-2">座位区域</div>
            <div class="row g-3 mb-3">
              <div v-for="area in seatAreas" :key="area.code" class="col-12 col-md-6">
                <div class="card card-link" @click="highlightArea(area.code)">
                  <div class="card-body d-flex align-items-center gap-2">
                    <span class="avatar avatar-sm rounded bg-success-lt text-success">
                      <el-icon :size="16"><OfficeBuilding /></el-icon>
                    </span>
                    <div class="entry-text">
                      <div class="fw-bold text-truncate">{{ area.name }}</div>
                      <div class="text-secondary small">座位区域</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="subheader mb-2">服务设施</div>
            <div class="row g-3">
              <div v-for="facility in facilities" :key="facility.code" class="col-12 col-md-6">
                <div class="card card-link" @click="highlightArea(facility.code)">
                  <div class="card-body d-flex align-items-center gap-2">
                    <span class="avatar avatar-sm rounded bg-warning-lt text-warning">
                      <el-icon :size="16"><Location /></el-icon>
                    </span>
                    <div class="entry-text">
                      <div class="fw-bold text-truncate">{{ facility.name }}</div>
                      <div class="text-secondary small">服务设施</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 导航提示 -->
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">导航提示</h3>
            <div class="card-actions">
              <span class="badge bg-secondary-lt">
                {{ activeFloor === '1' ? '一楼' : '二楼' }}
              </span>
            </div>
          </div>
          <div class="list-group list-group-flush">
            <div v-for="(tip, index) in currentTips" :key="index" class="list-group-item">
              <div class="d-flex align-items-start gap-2">
                <span class="badge" :class="`bg-${tip.type}-lt`">{{ index + 1 }}</span>
                <span>{{ tip.content }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import FloorMap from '@/components/FloorMap.vue'

const activeFloor = ref('1')

const bookAreas = [
  { code: 'A', name: 'A区 - 计算机类' },
  { code: 'B', name: 'B区 - 文学类' },
  { code: 'C', name: 'C区 - 数学类' },
  { code: 'D', name: 'D区 - 英语类' },
  { code: 'E', name: 'E区 - 历史类' }
]

const seatAreas = [
  { code: 'SA', name: '自习区' },
  { code: 'SB', name: '阅览区' },
  { code: 'SC', name: '电脑区' },
  { code: 'SF', name: '讨论区' }
]

const facilities = [
  { code: 'ENTRANCE', name: '入口' },
  { code: 'EXIT', name: '出口' },
  { code: 'DESK', name: '服务台' },
  { code: 'REST', name: '休息区' },
  { code: 'WC', name: '洗手间' }
]

const floor1Tips = [
  { type: 'primary', content: '从入口进入后，左侧为A区计算机类图书' },
  { type: 'success', content: '右侧为B区文学类图书' },
  { type: 'warning', content: '中央区域为自习区和阅览区' },
  { type: 'info', content: '服务台位于大厅中央，可办理借阅和咨询' }
]

const floor2Tips = [
  { type: 'primary', content: '二楼左侧为C区数学类图书' },
  { type: 'success', content: '右侧为D区英语类和E区历史类图书' },
  { type: 'warning', content: '中央区域为自习区和讨论区' },
  { type: 'info', content: '电脑区位于二楼东南角' }
]

const currentTips = computed(() => {
  return activeFloor.value === '1' ? floor1Tips : floor2Tips
})

const highlightArea = (code: string) => {
  // 预留区域高亮入口
  void code
}
</script>

<style scoped>
.map-scroll {
  overflow-x: auto;
}

.entry-text {
  min-width: 0;
}
</style>
