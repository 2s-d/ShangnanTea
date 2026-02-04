import { ref, onMounted } from 'vue'

export function useAnimations() {
  // 使用 Intersection Observer 实现滚动触发动画
  const observeElements = (selector, animationClass) => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add(animationClass)
            observer.unobserve(entry.target) // 动画只触发一次
          }
        })
      },
      {
        threshold: 0.1, // 元素 10% 可见时触发
        rootMargin: '0px 0px -50px 0px'
      }
    )

    onMounted(() => {
      const elements = document.querySelectorAll(selector)
      elements.forEach((el) => observer.observe(el))
    })

    return observer
  }

  // 数字递增动画
  const animateNumber = (target, start, end, duration) => {
    const startTime = performance.now()
    const range = end - start

    const updateNumber = (currentTime) => {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      
      // 使用 easeOutQuad 缓动函数
      const easeProgress = 1 - Math.pow(1 - progress, 3)
      const current = Math.floor(start + range * easeProgress)
      
      target.value = current

      if (progress < 1) {
        requestAnimationFrame(updateNumber)
      } else {
        target.value = end
      }
    }

    requestAnimationFrame(updateNumber)
  }

  // 打字机效果
  const typeWriter = (text, callback, speed = 100) => {
    let index = 0
    const result = ref('')

    const type = () => {
      if (index < text.length) {
        result.value += text.charAt(index)
        index++
        setTimeout(type, speed)
      } else if (callback) {
        callback()
      }
    }

    type()
    return result
  }

  return {
    observeElements,
    animateNumber,
    typeWriter
  }
}
