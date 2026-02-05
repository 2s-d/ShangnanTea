<template>
  <div class="cloud-tea-wrapper">
    <canvas ref="canvasRef" class="cloud-tea-canvas"></canvas>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'

const canvasRef = ref(null)

let renderer = null
let scene = null
let camera = null
let animationId = null
let model = null

onMounted(() => {
  const canvas = canvasRef.value
  if (!canvas) return

  const width = 70
  const height = 70

  // 渲染器
  renderer = new THREE.WebGLRenderer({
    canvas,
    alpha: true,
    antialias: true
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(window.devicePixelRatio || 1)

  // 场景
  scene = new THREE.Scene()

  // 相机
  camera = new THREE.PerspectiveCamera(35, width / height, 0.1, 100)
  camera.position.set(0, 1.2, 3)

  // 灯光
  const ambient = new THREE.AmbientLight(0xffffff, 0.9)
  scene.add(ambient)
  const dir = new THREE.DirectionalLight(0xffffff, 0.8)
  dir.position.set(2, 4, 3)
  scene.add(dir)

  // 加载模型
  const loader = new GLTFLoader()
  loader.load(
    '/models/cloud_tea.glb',
    gltf => {
      model = gltf.scene
      // 轻微缩放并居中
      model.scale.set(1.2, 1.2, 1.2)
      model.rotation.set(0, Math.PI / 4, 0)
      scene.add(model)
      animate()
    },
    undefined,
    error => {
      // eslint-disable-next-line no-console
      console.error('加载 cloud_tea.glb 失败:', error)
    }
  )
})

const animate = () => {
  animationId = requestAnimationFrame(animate)
  if (model) {
    model.rotation.y += 0.003
  }
  renderer.render(scene, camera)
}

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  if (renderer) {
    renderer.dispose()
    renderer = null
  }
  scene = null
  camera = null
  model = null
})
</script>

<style scoped>
.cloud-tea-wrapper {
  width: 70px;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}

.cloud-tea-canvas {
  width: 70px;
  height: 70px;
  pointer-events: none;
}
</style>

