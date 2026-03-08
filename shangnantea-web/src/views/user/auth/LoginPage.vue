<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 登录页左侧测试账号提示面板（用于展示三个测试账号） -->
      <div class="login-demo-panel">
        <div class="demo-header">测试账号</div>
        <div class="demo-body">
          <div class="demo-line demo-pass">统一密码：<span class="highlight">user1234</span></div>
          <div class="demo-line">
            <span>普通用户：<span class="highlight">111111</span></span>
            <el-button
              size="small"
              text
              @mouseenter="handleDemoHoverIn"
              @mouseleave="handleDemoHoverOut"
              @click="fillDemoAccount('user')"
            >
              一键填充
            </el-button>
          </div>
          <div class="demo-line">
            <span>商　　户：<span class="highlight">222222</span></span>
            <el-button
              size="small"
              text
              @mouseenter="handleDemoHoverIn"
              @mouseleave="handleDemoHoverOut"
              @click="fillDemoAccount('merchant')"
            >
              一键填充
            </el-button>
          </div>
          <div class="demo-line">
            <span>管理员：<span class="highlight">333333</span></span>
            <el-button
              size="small"
              text
              @mouseenter="handleDemoHoverIn"
              @mouseleave="handleDemoHoverOut"
              @click="fillDemoAccount('admin')"
            >
              一键填充
            </el-button>
          </div>
        </div>
      </div>

      <!-- 临时猫耳动画预览区（右侧空白区域预览效果，后面再挂到文本框上） -->
      <div
        class="ear-anim-demo"
        :class="[{ 'ear-boost': isEarBoost }, { 'ear-frame2-on': isEarFrame2 }]"
        :style="earAvatarStyle"
        @mousedown.prevent="onAvatarMouseDown"
        @wheel.prevent="onAvatarWheel"
      >
        <div class="ear-head"></div>
        <div class="ear-ears-wrap">
          <div class="ear-frame ear-frame-1"></div>
          <div class="ear-frame ear-frame-2"></div>
        </div>
      </div>

      <div class="login-box">
        <div class="login-header">
          <h2>用户登录</h2>
          <p>欢迎回到商南茶文化平台</p>
        </div>
        
        <el-form 
          ref="loginFormRef" 
          :model="loginForm" 
          :rules="loginRules" 
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="用户名" prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              prefix-icon="el-icon-user"
            ></el-input>
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              prefix-icon="el-icon-lock"
              show-password
            ></el-input>
          </el-form-item>
          
          <el-form-item label="角色" prop="role">
            <el-radio-group v-model="loginForm.role">
              <el-radio :value="2">普通用户</el-radio>
              <el-radio :value="3">商家</el-radio>
              <el-radio :value="1">管理员</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <div class="login-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <el-button type="text" @click="forgotPassword">忘记密码？</el-button>
          </div>
          
          <el-form-item>
            <el-button 
              type="primary" 
              class="login-button" 
              :loading="loading" 
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <p>还没有账号？ <router-link to="/register">立即注册</router-link></p>
        </div>
        
        <!-- 开发者选项 -->
        <div class="dev-options">
          <el-divider>开发者选项</el-divider>
          <div class="dev-buttons">
            <el-button type="warning" size="small" @click="resetLocalStorage">重置本地存储</el-button>
            <el-button type="info" size="small" @click="migrateData">触发数据迁移</el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'

import { checkAndMigrateData } from '@/utils/versionManager'
import { showByCode, isSuccess } from '@/utils/apiMessages'
import { userPromptMessages as userMessages } from '@/utils/promptMessages'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  role: 2 // 默认为普通用户
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

const rememberMe = ref(false)
const loading = ref(false)
const loginFormRef = ref(null)
const redirect = computed(() => route.query.redirect || '/tea-culture')

// 猫耳动画：是否触发一次“增强抖动”（呼吸感）效果
const isEarBoost = ref(false)
// 猫耳当前是否显示第二帧（用于平时闪烁与对比 demo）
const isEarFrame2 = ref(false)
// 一键填充按钮当前是否处于悬停状态
const isDemoHovering = ref(false)

// 整个耳朵+脑袋组件的拖拽和缩放（内部对齐关系固定）
const avatarOffsetX = ref(0)
const avatarOffsetY = ref(0)
// 初始大小：按当前截图确定为原始尺寸的 0.5 倍（380x262 -> 190x131）
const avatarScale = ref(0.5)

const earAvatarStyle = computed(() => {
  return {
    transform: `translate(${avatarOffsetX.value}px, ${avatarOffsetY.value}px) scale(${avatarScale.value})`
  }
})

// ===== 耳朵对齐脑袋的常量矩阵（基于 4 个点重新计算出的缩放 + 平移） =====
// 只保留结果常量，不在前端做额外计算
// 注意：当前使用的是非等比缩放矩阵（sx, sy 不完全相等），以保证 4 个锚点在视觉上尽量重合
const EAR_SCALE = 1.25 // 仅作为参考展示，真实数值请看样式中的 matrix
const EAR_TX = -12.15
const EAR_TY = -166.04

// 自动填充记住的用户信息
onMounted(() => {
  // 如果存在记住的用户信息，自动填充
  const rememberedUser = localStorage.getItem('rememberedUser')
  if (rememberedUser) {
    const userInfo = JSON.parse(rememberedUser)
    loginForm.username = userInfo.username
    loginForm.role = userInfo.role
    rememberMe.value = true
  }
})

/**
 * 一键填充三个测试账号
 * - user:     111111 / 普通用户(role=2)
 * - merchant: 222222 / 商家(role=3)
 * - admin:    333333 / 管理员(role=1)
 * 密码统一为 user1234
 */
const fillDemoAccount = (type) => {
  if (!type) return
  if (type === 'user') {
    loginForm.username = '111111'
    loginForm.role = 2
  } else if (type === 'merchant') {
    loginForm.username = '222222'
    loginForm.role = 3
  } else if (type === 'admin') {
    loginForm.username = '333333'
    loginForm.role = 1
  }
  loginForm.password = 'user1234'
  // 一键填充属于演示账号，不自动记住
  rememberMe.value = false
  // 清除可能已有的表单校验错误提示
  if (loginFormRef.value) {
    loginFormRef.value.clearValidate()
  }

  // 点击填充：再次触发一次剧烈抖动，结束后恢复正常抖动
  triggerEarBoost('blink')
}

// ===== 猫耳平时闪烁节奏对比：方案 A/B/C 依次播放，每个方案结束后停顿 3 秒 =====
const earPatternA = [0.50, 0.55, 0.60, 0.55, 0.50, 0.45, 0.40, 0.45, 0.50]
const earPatternB = [0.50, 0.60, 0.70, 0.60, 0.50, 0.40, 0.30, 0.40, 0.50]
const earPatternC = [0.50, 0.52, 0.48, 0.51, 0.49, 0.53, 0.47, 0.50]

let earDemoStopped = false

/**
 * 触发一次猫耳增强抖动动画
 * @param {'freeze'|'blink'} nextState - 动画结束后的状态：
 *   - 'freeze': 强抖结束后进入静止（用于鼠标悬停）
 *   - 'blink':  强抖结束后恢复正常抖动（用于点击填充）
 */
const triggerEarBoost = (nextState) => {
  isEarBoost.value = false
  // 用 requestAnimationFrame 确保浏览器先应用上一次状态
  requestAnimationFrame(() => {
    isEarBoost.value = true
    setTimeout(() => {
      isEarBoost.value = false
      if (nextState === 'freeze') {
        // 只有当前仍处于悬停状态时才进入静止
        if (isDemoHovering.value) {
          earFrozen = true
        }
      } else if (nextState === 'blink') {
        earFrozen = false
      }
    }, 1200) // 略大于增强动画总时长
  })
}

// 当前是否处于“静止”状态（不进行平时抖动）
let earFrozen = false
// 等待在“当前这一轮正常抖动结束后”再切入的增强抖动模式
let earPendingBoostMode = null

// ===== 整个耳朵+脑袋组件：拖拽 + 缩放 =====

let isDraggingAvatar = false
let dragStartX = 0
let dragStartY = 0
let dragAvatarOriginOffsetX = 0
let dragAvatarOriginOffsetY = 0

const onAvatarMouseMove = (event) => {
  if (!isDraggingAvatar) return
  const dx = event.clientX - dragStartX
  const dy = event.clientY - dragStartY
  avatarOffsetX.value = dragAvatarOriginOffsetX + dx
  avatarOffsetY.value = dragAvatarOriginOffsetY + dy
}

const onAvatarMouseUp = () => {
  if (!isDraggingAvatar) return
  isDraggingAvatar = false
  window.removeEventListener('mousemove', onAvatarMouseMove)
  window.removeEventListener('mouseup', onAvatarMouseUp)
}

const onAvatarMouseDown = (event) => {
  isDraggingAvatar = true
  dragStartX = event.clientX
  dragStartY = event.clientY
  dragAvatarOriginOffsetX = avatarOffsetX.value
  dragAvatarOriginOffsetY = avatarOffsetY.value
  window.addEventListener('mousemove', onAvatarMouseMove)
  window.addEventListener('mouseup', onAvatarMouseUp)
}

const onAvatarWheel = (event) => {
  const delta = event.deltaY
  const factor = delta > 0 ? 0.9 : 1.1
  let nextScale = avatarScale.value * factor
  // 限制缩放范围
  if (nextScale < 0.5) nextScale = 0.5
  if (nextScale > 2.0) nextScale = 2.0
  avatarScale.value = nextScale
}

// 悬停进入：标记悬停，并预约一次增强抖动（当前这一轮正常抖动结束后触发）
const handleDemoHoverIn = () => {
  isDemoHovering.value = true
  // 已经静止或正在增强抖动时，不重复预约
  if (earFrozen || isEarBoost.value) return
  earPendingBoostMode = 'freeze'
}

// 悬停离开：如果当前是静止状态且没有正在增强抖动，则恢复正常抖动；
// 如果只是尚未开始的预约增强抖动，则直接取消预约
const handleDemoHoverOut = () => {
  isDemoHovering.value = false
  if (!earFrozen && !isEarBoost.value) {
    // 还没进入静止，只是预约了增强抖动，离开时直接取消
    earPendingBoostMode = null
  }

  if (earFrozen && !isEarBoost.value) {
    earFrozen = false
  }
}

onMounted(() => {
  const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms))

  ;(async () => {
    const patterns = [earPatternA, earPatternB, earPatternC]

    while (!earDemoStopped) {
      for (const pattern of patterns) {
        // 按当前方案的一串间隔依次闪烁
        for (const interval of pattern) {
          if (earDemoStopped) return

          // 如果正在执行增强抖动动画或处于静止状态，则等待
          while ((isEarBoost.value || earFrozen) && !earDemoStopped) {
            await sleep(50)
          }

          // 如果有预约的增强抖动（例如悬停事件），在当前这一轮正常抖动结束后执行
          if (earPendingBoostMode && !earDemoStopped) {
            const mode = earPendingBoostMode
            earPendingBoostMode = null
            triggerEarBoost(mode)
            // 等待增强抖动流程占用时间，下一个循环再继续正常抖动
            continue
          }

          isEarFrame2.value = !isEarFrame2.value
          await sleep(interval * 1000)
        }
      }
    }
  })()
})

onBeforeUnmount(() => {
  earDemoStopped = true
  // 清理可能遗留的拖拽监听
  window.removeEventListener('mousemove', onAvatarMouseMove)
  window.removeEventListener('mouseup', onAvatarMouseUp)
})

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    // 先做一次表单校验，确保用户名/密码/角色都已填写
    await loginFormRef.value.validate()
    
    loading.value = true

    const res = await userStore.login({
      username: loginForm.username,
      password: loginForm.password,
      role: loginForm.role
    })
    
    // 只有在后端业务码表示成功时，才认为登录成功并跳转
    if (isSuccess(res?.code)) {
      // 显示“登录成功”提示（2000）
      showByCode(res.code)
      
      // 处理重定向逻辑：
      // - 如果有 redirect 且不是 /login，则跳转到 redirect
      // - 否则统一跳转到茶文化首页 /tea-culture
      const rawRedirect = route.query.redirect
      let redirectPath = '/tea-culture'
      if (rawRedirect && rawRedirect !== '/login') {
        redirectPath = rawRedirect
      }
      
      // 处理“记住我”功能：只记用户名和角色，不记密码
      if (rememberMe.value) {
        localStorage.setItem('rememberedUser', JSON.stringify({
          username: loginForm.username,
          role: loginForm.role
        }))
      } else {
        localStorage.removeItem('rememberedUser')
      }
      
      router.push(redirectPath)
    }
  } catch (error) {
    // store中已处理消息提示，这里只记录日志
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 忘记密码 - 跳转到密码找回页面
const forgotPassword = () => {
  router.push('/reset-password')
}

// 开发者选项：重置本地存储
const resetLocalStorage = () => {
  if (confirm('确定要重置所有本地存储数据吗？这将清除所有登录状态和用户数据。')) {
    localStorage.clear()
    userMessages.success.showStorageReset()
    setTimeout(() => {
      window.location.reload()
    }, 1000)
  }
}

// 开发者选项：触发数据迁移
const migrateData = () => {
  try {
    // 将版本号重置，触发迁移
    localStorage.removeItem('app_data_version')
    
    // 使用非静默模式(false)执行迁移，允许显示消息
    checkAndMigrateData(false)
    
    userMessages.success.showDataMigrationTriggered()
  } catch (e) {
    console.error('数据迁移失败:', e)
    userMessages.error.showDataMigrationFailed(e.message)
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 40px 0;
  position: relative;
  /* 默认整页文字不可选，避免误拖选中 */
  user-select: none;
}

.login-container {
  width: 100%;
  max-width: 1920px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.login-box {
  width: 450px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

/* 保留表单输入类控件的文字选择能力 */
.login-page input,
.login-page textarea {
  user-select: text;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    font-size: 28px;
    color: #409EFF;
    margin-bottom: 10px;
    font-weight: 600;
  }
  
  p {
    font-size: 16px;
    color: #606266;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  padding: 12px 0;
  font-size: 16px;
  border-radius: 4px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #606266;
  
  a {
    color: #409EFF;
    text-decoration: none;
    font-weight: 500;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.dev-options {
  margin-top: 20px;
}

.dev-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

/* 左侧测试账号面板，大小和位置参考开发监控面板 */
.login-demo-panel {
  position: absolute;
  left: 200px;
  top: 380px;
  width: 230px;
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.6);
  border: 1px solid #007acc;
  font-size: 12px;
}

.demo-header {
  padding: 6px 10px;
  background: #252526;
  border-bottom: 1px solid #007acc;
  font-weight: 600;
}

.demo-body {
  padding: 8px 10px 10px;
}

.demo-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
  white-space: nowrap;
}

.demo-line:last-child {
  margin-bottom: 0;
}

.demo-pass {
  margin-bottom: 10px;
}

.demo-line .el-button {
  padding: 0 4px;
  font-size: 11px;
  background-color: transparent !important;
  border-radius: 4px;
  border: 1px solid transparent;
  box-shadow: none;
  transition: color 0.12s ease, border-color 0.12s ease, box-shadow 0.12s ease;
}

.demo-line .el-button:hover {
  /* 悬停时：立即变为深绿文字 + 发光边框，不再出现白底 */
  background-color: transparent !important;
  color: #0f9d58;
  border-color: rgba(0, 255, 128, 0.8);
  box-shadow: 0 0 4px rgba(0, 255, 128, 0.9);
}

.highlight {
  color: #ffd04b;
  font-weight: 600;
}

/* ===== 临时猫耳两帧动画预览 ===== */

.ear-anim-demo {
  position: absolute;
  /* 初始位置：距离左边界约 375px，底部贴合测试账号面板顶部 */
  left: 146px;
  top: 124px;
  /* 组件内部真实坐标系：与脑袋 SVG 的 viewBox 一致 */
  width: 380px;
  height: 262px;
  pointer-events: auto;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  transform-origin: center bottom;
}

/* 头部轮廓：使用 frame_60_delay-0.03s.svg 作为“脑袋”轮廓底图 */
.ear-head {
  position: relative;
  width: 100%;
  height: 100%;
  background-image: url('/images/svg/frame_60_delay-0.03s.svg');
  background-repeat: no-repeat;
  /* 头部直接按 viewBox 尺寸铺满组件，使坐标系 1:1 对应 */
  background-position: 0 0;
  background-size: 100% 100%;
  /* 让耳朵可以覆盖在头部之上 */
  z-index: 1;
  pointer-events: none;
}

.ear-ears-wrap {
  position: absolute;
  left: 0;
  top: 0;
  /* 使用耳朵 SVG 的原始尺寸作为内部坐标系（0~280） */
  width: 280px;
  height: 280px;
  transform-origin: 0 0;
  /* 耳朵相对于脑袋的缩放 + 平移，以组件(脑袋 viewBox)为坐标系 */
  transform: matrix(1.2715, 0, 0, 1.2297, -12.15, -166.04);
}

.ear-frame {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-repeat: no-repeat;
  /* 让耳朵 SVG 以其 viewBox 1:1 填满 280x280 的内部坐标系 */
  background-position: 0 0;
  background-size: 100% 100%;
  transform-origin: center bottom;
  z-index: 2;
}

.ear-frame-1 {
  background-image: url('/images/svg/0eddd592808247dbba1d8dd3ed6841b8.svg');
}

.ear-frame-2 {
  background-image: url('/images/svg/frame_1_delay-0.5s.svg');
}

/* 基本可见性控制：默认显示第一帧 */
.ear-anim-demo .ear-frame-1 {
  opacity: 1;
}

.ear-anim-demo .ear-frame-2 {
  opacity: 0;
}

/* 当容器带有 ear-frame2-on 时，切换为显示第二帧 */
.ear-anim-demo.ear-frame2-on .ear-frame-1 {
  opacity: 0;
}

.ear-anim-demo.ear-frame2-on .ear-frame-2 {
  opacity: 1;
}

/* ===== 一键填充时：执行一次“有呼吸感”的增强抖动动画 ===== */

.ear-anim-demo.ear-boost .ear-frame-1 {
  /* 总时长从 1.1s 缩短到约 0.8s（整体减少约 30%） */
  animation: ear-frame-1-breath 0.8s linear 1;
}

.ear-anim-demo.ear-boost .ear-frame-2 {
  animation: ear-frame-2-breath 0.8s linear 1;
}

/* 一轮 1.1s（用于增强抖动）：
   - 立即进入剧烈抖动，不再先静止 0.8s，避免中间“停顿感”
   - 结束时回到帧1的正常静止形态
*/
@keyframes ear-frame-1-breath {
  0% {
    opacity: 1;
    transform: translateY(0) rotate(0deg) scale(1);
  }
  10%, 90% {
    opacity: 0;
    transform: translateY(0) rotate(0deg) scale(1);
  }
  100% {
    opacity: 1;
    transform: translateY(0) rotate(0deg) scale(1);
  }
}

@keyframes ear-frame-2-breath {
  0% {
    opacity: 0;
    transform: translateY(0) rotate(0deg) scale(1);
  }
  10% {
    opacity: 1;
    /* 原来位移 -3px、旋转 -4deg、scale 1.03，将幅度缩小约 60% */
    transform: translateY(-1.2px) rotate(-1.6deg) scale(1.012);
  }
  35% {
    opacity: 1;
    /* 原来位移 -1px、旋转 3deg、scale 1.02 */
    transform: translateY(-0.4px) rotate(1.2deg) scale(1.008);
  }
  65% {
    opacity: 1;
    /* 原来旋转 -1.5deg、scale 1.01 */
    transform: translateY(0) rotate(-0.6deg) scale(1.004);
  }
  90% {
    opacity: 1;
    transform: translateY(0) rotate(0deg) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(0) rotate(0deg) scale(1);
  }
}

/* 添加Logo装饰效果 */
.login-page:before {
  content: "商南茶文化";
  position: absolute;
  top: 30px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.1);
}

@media (max-width: 576px) {
  .login-box {
    width: 90%;
    padding: 30px 20px;
  }
  
  .login-page:before {
    font-size: 24px;
  }
}
</style> 