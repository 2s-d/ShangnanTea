import { ref, watch } from 'vue'

export function useTheme() {
  // 从 localStorage 读取主题设置，默认为浅色
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  // 切换主题
  const toggleTheme = () => {
    isDark.value = !isDark.value
  }

  // 监听主题变化，保存到 localStorage 并更新 body class
  watch(isDark, (newValue) => {
    localStorage.setItem('theme', newValue ? 'dark' : 'light')
    if (newValue) {
      document.body.classList.add('dark-mode')
    } else {
      document.body.classList.remove('dark-mode')
    }
  }, { immediate: true })

  return {
    isDark,
    toggleTheme
  }
}
