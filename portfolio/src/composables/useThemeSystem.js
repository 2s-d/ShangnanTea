import { ref, watch } from 'vue'

// 主题模板列表
export const THEMES = {
  MINIMAL: 'minimal',    // 简约模式
  TECH: 'tech',          // 科技模式
  // 未来可扩展更多主题
}

export function useThemeSystem() {
  // 深色/浅色模式（独立于主题）
  const isDark = ref(localStorage.getItem('darkMode') === 'true')
  
  // 当前主题模板
  const currentTheme = ref(localStorage.getItem('theme') || THEMES.TECH)

  // 切换深色/浅色模式
  const toggleDarkMode = () => {
    isDark.value = !isDark.value
  }

  // 切换主题模板
  const setTheme = (theme) => {
    if (Object.values(THEMES).includes(theme)) {
      currentTheme.value = theme
    }
  }

  // 监听深色模式变化
  watch(isDark, (newValue) => {
    localStorage.setItem('darkMode', newValue)
    if (newValue) {
      document.body.classList.add('dark-mode')
    } else {
      document.body.classList.remove('dark-mode')
    }
  }, { immediate: true })

  // 监听主题变化
  watch(currentTheme, (newValue) => {
    localStorage.setItem('theme', newValue)
    // 移除所有主题类
    Object.values(THEMES).forEach(theme => {
      document.body.classList.remove(`theme-${theme}`)
    })
    // 添加当前主题类
    document.body.classList.add(`theme-${newValue}`)
  }, { immediate: true })

  return {
    isDark,
    currentTheme,
    toggleDarkMode,
    setTheme,
    THEMES
  }
}
