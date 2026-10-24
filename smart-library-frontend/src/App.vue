<template>
  <router-view />
</template>

<script setup lang="ts">
</script>

<style>
/* 基础重置交给 Tabler（Bootstrap reset）处理，这里只做字体平滑与主题底色兜底 */
/* 必须用 :root 才能覆盖 Tabler 在 :root 上声明的同名变量（html 选择器优先级更低） */
:root {
  --tblr-font-sans-serif: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI',
    'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Roboto,
    'Helvetica Neue', Arial, sans-serif;
}

body {
  min-height: 100vh;
  background: var(--tblr-body-bg);
  color: var(--tblr-body-color);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
}
</style>
