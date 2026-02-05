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

  const width = 80
  const height = 80

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
  scene.background = null

  // 相机
  camera = new THREE.PerspectiveCamera(30, width / height, 0.1, 100)
  camera.position.set(0, 1.6, 4.0)
  camera.lookAt(0, 0.5, 0)

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

      // 自动根据包围盒缩放并居中模型，让它刚好塞进这个小画布
      const box = new THREE.Box3().setFromObject(model)
      const size = box.getSize(new THREE.Vector3())
      const center = box.getCenter(new THREE.Vector3())
      const maxAxis = Math.max(size.x, size.y, size.z) || 1

      // 稍微缩小一点，让底部托盘也能完整显示出来
      const scale = 1.8 / maxAxis
      model.scale.setScalar(scale)

      // 居中到原点附近，并稍微上抬一点
      box.setFromObject(model)
      box.getCenter(center)
      model.position.sub(center)
      // 轻微上移，但保留足够空间给底部托盘
      model.position.y += 0.58

      model.rotation.set(0, Math.PI / 6, 0)

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
    model.rotation.y += 0.01
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
  position: relative;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  transform: translateY(-25px);
}

.cloud-tea-wrapper::before {
  content: '';
  position: absolute;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.2), rgba(59, 130, 246, 0));
  filter: blur(6px);
  z-index: -1;
}

.cloud-tea-canvas {
  width: 80px;
  height: 80px;
  pointer-events: none;
}
</style>

