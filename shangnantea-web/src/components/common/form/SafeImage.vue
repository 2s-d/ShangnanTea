<template>
  <img
    :src="imgSrc"
    :alt="alt"
    @error="handleError"
    v-bind="$attrs"
  />
</template>

<script setup>
import { ref, watch, computed } from 'vue'
const props = defineProps({
  src: String,
  alt: String,
  type: {
    type: String,
    default: 'tea' // 默认茶叶
  },
  defaultSrc: String
})

// 内置多种默认图片（已放在 public/images/ 目录下）
const defaultMap = {
  avatar: '/images/avatars/default.jpg',
  tea: '/images/teas/default.jpg',
  banner: '/images/banners/default.jpg',
  post: '/images/posts/default.jpg'
}

// 计算最终兜底图片
const fallbackSrc = computed(() => {
  // 打印日志，帮助调试
  console.log('类型:', props.type, '默认图片:', defaultMap[props.type])
  return props.defaultSrc || defaultMap[props.type] || defaultMap.tea
})

const imgSrc = ref(props.src || fallbackSrc.value)

watch(() => props.src, val => {
  console.log('SafeImage src变化:', val, '类型:', props.type)
  imgSrc.value = val || fallbackSrc.value
})

function handleError() {
  console.log('图片加载错误，使用默认图片:', fallbackSrc.value)
  // 检查如果当前已经是默认图片，则不再重新赋值，避免无限循环
  if (imgSrc.value !== fallbackSrc.value) {
    imgSrc.value = fallbackSrc.value
  } else {
    console.error('默认图片也加载失败:', fallbackSrc.value)
    // 如果默认图片也加载失败，直接使用内联的base64头像
    imgSrc.value = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0iI2UwZTBlMCIgZD0iTTEyIDJDNi40OCAyIDIgNi40OCAyIDEyczQuNDggMTAgMTAgMTAgMTAtNC40OCAxMC0xMFMxNy41MiAyIDEyIDJ6TTEyIDVjMS42NiAwIDMgMS4zNCAzIDNzLTEuMzQgMy0zIDMtMy0xLjM0LTMtMyAxLjM0LTMgMy0zem0wIDE0LjJjLTIuNSAwLTQuNzEtMS4yOC02LTMuMjIuMDMtMS45OSA0LTMuMDggNi0zLjA4IDEuOTkgMCA1Ljk3IDEuMDkgNiAzLjA4LTEuMjkgMS45NC0zLjUgMy4yMi02IDMuMjJ6Ii8+PC9zdmc+'
  }
}
</script> 