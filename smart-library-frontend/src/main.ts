import { createApp } from 'vue'
import { createPinia } from 'pinia'
import {
  ElAlert,
  ElAside,
  ElAvatar,
  ElButton,
  ElCard,
  ElCol,
  ElCollapse,
  ElCollapseItem,
  ElContainer,
  ElDatePicker,
  ElDialog,
  ElDivider,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElHeader,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElLoading,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPageHeader,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElRow,
  ElSelect,
  ElSlider,
  ElTabPane,
  ElTable,
  ElTableColumn,
  ElTabs,
  ElTag,
  ElTimeline,
  ElTimelineItem
} from 'element-plus'
// Tabler（Bootstrap 5 基座）基础样式：reset + 按钮 / 卡片 / 表格 / 输入框
import '@tabler/core/dist/css/tabler.css'
import 'element-plus/dist/index.css'
// 桥接层：品牌色对齐 + 让 Element Plus 控件收敛到 Tabler 的度量
import '@/styles/tabler-bridge.css'
import {
  ArrowDown,
  Calendar,
  Collection,
  DataLine,
  Document,
  DocumentChecked,
  Location,
  MapLocation,
  OfficeBuilding,
  Reading,
  Search,
  Setting,
  Star,
  Timer,
  User,
  Warning
} from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'

const app = createApp(App)

const elementPlusComponents = [
  ElAlert,
  ElAside,
  ElAvatar,
  ElButton,
  ElCard,
  ElCol,
  ElCollapse,
  ElCollapseItem,
  ElContainer,
  ElDatePicker,
  ElDialog,
  ElDivider,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElEmpty,
  ElForm,
  ElFormItem,
  ElHeader,
  ElIcon,
  ElInput,
  ElInputNumber,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElOption,
  ElPageHeader,
  ElPagination,
  ElRadioButton,
  ElRadioGroup,
  ElResult,
  ElRow,
  ElSelect,
  ElSlider,
  ElTabPane,
  ElTable,
  ElTableColumn,
  ElTabs,
  ElTag,
  ElTimeline,
  ElTimelineItem
]

const icons = {
  ArrowDown,
  Calendar,
  Collection,
  DataLine,
  Document,
  DocumentChecked,
  Location,
  MapLocation,
  OfficeBuilding,
  Reading,
  Search,
  Setting,
  Star,
  Timer,
  User,
  Warning
}

elementPlusComponents.forEach(component => {
  app.use(component)
})

app.use(ElLoading)

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

app.mount('#app')
