import { ref, watch } from 'vue'

// 主题列表 - 每个主题都是完整的视觉风格
export const THEMES = {
  PARTICLE: 'particle',  // 主题1：粒子主题
  CODERAIN: 'coderain',  // 主题2：代码雨主题
}

export function useThemeSystem() {
  // 深色/浅色模式（独立，不属于主题）
  const isDark = ref(localStorage.getItem('darkMode') === 'true')
  
  // 当前主题
  const currentTheme = ref(localStorage.getItem('theme') || THEMES.PARTICLE)

  // 切换深色/浅色模式（导航栏月亮/太阳图标）
  const toggleDarkMode = () => {
    isDark.value = !isDark.value
  }

  // 切换主题（红灯笼）
  const toggleTheme = () => {
    currentTheme.value = currentTheme.value === THEMES.PARTICLE ? THEMES.CODERAIN : THEMES.PARTICLE
    
    // 切换到代码雨主题时自动开启黑夜模式
    if (currentTheme.value === THEMES.CODERAIN) {
      isDark.value = true
    }
    // 切换到粒子主题时自动开启白天模式
    else if (currentTheme.value === THEMES.PARTICLE) {
      isDark.value = false
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
    toggleTheme,
    THEMES
  }
}
