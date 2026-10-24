import { onMounted, onUnmounted } from 'vue'

type RefreshTask = () => void | Promise<void>

export const useRealtimeRefresh = (task: RefreshTask, interval = 5000) => {
  let timer: number | undefined
  let running = false

  const run = async () => {
    if (document.hidden || running) return
    running = true
    try {
      await task()
    } catch (e) {
      // 实时刷新失败时静默处理，避免影响用户当前操作
    } finally {
      running = false
    }
  }

  const handleVisibilityChange = () => {
    if (!document.hidden) run()
  }

  onMounted(() => {
    document.addEventListener('visibilitychange', handleVisibilityChange)
    timer = window.setInterval(run, interval)
  })

  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    if (timer) window.clearInterval(timer)
  })
}
