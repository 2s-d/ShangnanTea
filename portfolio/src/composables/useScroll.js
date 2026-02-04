import { ref, onMounted, onUnmounted } from 'vue'

export function useScroll() {
  const scrollY = ref(0)
  const scrollProgress = ref(0)

  const updateScroll = () => {
    scrollY.value = window.scrollY
    
    // 计算滚动进度（0-100）
    const windowHeight = document.documentElement.scrollHeight - window.innerHeight
    scrollProgress.value = (scrollY.value / windowHeight) * 100
  }

  onMounted(() => {
    window.addEventListener('scroll', updateScroll)
    updateScroll()
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', updateScroll)
  })

  // 平滑滚动到指定元素
  const scrollTo = (id) => {
    const element = document.getElementById(id)
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' })
    }
  }

  return {
    scrollY,
    scrollProgress,
    scrollTo
  }
}
