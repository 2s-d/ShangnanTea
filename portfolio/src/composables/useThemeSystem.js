import { ref, watch } from 'vue'

// 主题列表 - 每个主题都是完整的视觉风格
export const THEMES = {
  STARRY: 'starry',      // 主题1：星空主题（原主题3）
  PARTICLE: 'particle',  // 主题2：粒子主题（原主题1）
  CODERAIN: 'coderain',  // 主题3：代码雨主题（原主题2）
}

export function useThemeSystem() {
  // 深色/浅色模式（独立，不属于主题）
  const isDark = ref(localStorage.getItem('darkMode') === 'true')
  
  // 当前主题
  const currentTheme = ref(localStorage.getItem('theme') || THEMES.STARRY)

  // 切换深色/浅色模式（导航栏月亮/太阳图标）
  const toggleDarkMode = () => {
    isDark.value = !isDark.value
  }

  // 切换主题（红灯笼）
  const toggleTheme = () => {
    const themes = Object.values(THEMES)
    const currentIndex = themes.indexOf(currentTheme.value)
    const nextIndex = (currentIndex + 1) % themes.length
    currentTheme.value = themes[nextIndex]
    
    // 切换到星空主题时自动开启黑夜模式
    if (currentTheme.value === THEMES.STARRY) {
      isDark.value = true
    }
    // 切换到粒子主题时自动开启白天模式
    else if (currentTheme.value === THEMES.PARTICLE) {
      isDark.value = false
    }
    // 切换到代码雨主题时自动开启黑夜模式
    else if (currentTheme.value === THEMES.CODERAIN) {
      isDark.value = true
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
