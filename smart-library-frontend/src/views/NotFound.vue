<template>
  <div class="page page-center">
    <div class="container-tight py-4">
      <div class="empty">
        <div class="empty-header">404</div>
        <p class="empty-title">抱歉，您访问的页面不存在</p>
        <p class="empty-subtitle text-secondary">
          页面可能已被移除、重命名，或您输入的地址有误。
        </p>
        <div class="empty-action">
          <button type="button" class="btn btn-primary" @click="goHome">返回首页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.page-center {
  min-height: 100vh;
}
</style>
