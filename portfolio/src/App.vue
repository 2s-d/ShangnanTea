<template>
  <div class="portfolio" :class="{ 'dark-mode': isDark }">
    <!-- 测试组件 - 临时添加，改成true查看测试 -->
    <TestShadow v-if="false" />
    
    <!-- 背景特效 - 根据主题显示不同背景 -->
    <template v-if="currentTheme === THEMES.PARTICLE">
      <ParticlesBackground />
      <HexagonGrid />
    </template>
    <CodeRain v-if="currentTheme === THEMES.CODERAIN" :show="true" />
    <StarryBackground v-if="currentTheme === THEMES.STARRY" :show="true" />
    
    <!-- 通用特效层 -->
    <!-- <CursorGlow /> -->
    <ScrollProgress />
    <ClickEffect />
    
    <!-- 红灯笼：切换主题 -->
    <LanternSwitch 
      :current-theme="currentTheme" 
      :on-toggle="toggleTheme" 
    />

    <!-- 顶部导航 -->
    <el-affix :offset="0">
      <div class="nav-wrapper">
        <el-menu
          mode="horizontal"
          :ellipsis="false"
          :background-color="isDark ? '#1a1a2e' : 'rgba(255,255,255,0.95)'"
          :text-color="isDark ? '#fff' : '#2c3e50'"
          active-text-color="#667eea"
          class="nav-menu glass-effect"
        >
          <!-- 平滑追随高亮背景 -->
          <div 
            class="nav-highlight" 
            :style="highlightStyle"
            v-show="highlightVisible"
          ></div>
          
          <el-menu-item index="0" class="logo">
            <h2>{{ profile.name }}</h2>
          </el-menu-item>
          <div class="flex-grow" />
          <el-menu-item 
            index="1" 
            @click="scrollTo('hero')"
            @mouseenter="updateHighlight"
            @mouseleave="hideHighlight"
          >
            <el-icon><HomeFilled /></el-icon>
            首页
          </el-menu-item>
          <el-menu-item 
            index="2" 
            @click="scrollTo('about')"
            @mouseenter="updateHighlight"
            @mouseleave="hideHighlight"
          >
            <el-icon><User /></el-icon>
            关于
          </el-menu-item>
          <el-menu-item 
            index="3" 
            @click="scrollTo('skills')"
            @mouseenter="updateHighlight"
            @mouseleave="hideHighlight"
          >
            <el-icon><TrophyBase /></el-icon>
            技能
          </el-menu-item>
          <el-menu-item 
            index="4" 
            @click="scrollTo('projects')"
            @mouseenter="updateHighlight"
            @mouseleave="hideHighlight"
          >
            <el-icon><Briefcase /></el-icon>
            项目
          </el-menu-item>
          <el-menu-item 
            index="5" 
            @click="scrollTo('contact')"
            @mouseenter="updateHighlight"
            @mouseleave="hideHighlight"
          >
            <el-icon><Message /></el-icon>
            联系
          </el-menu-item>
          <el-menu-item 
            index="6" 
            @click="toggleDarkMode()"
            @mouseenter="updateHighlight"
            @mouseleave="hideHighlight"
          >
            <el-icon v-if="isDark"><Sunny /></el-icon>
            <el-icon v-else><Moon /></el-icon>
          </el-menu-item>
        </el-menu>
      </div>
    </el-affix>

    <!-- Hero 区域 -->
    <section id="hero" class="hero-section">
      <el-row justify="center" align="middle" style="height: 100%">
        <el-col :xs="22" :sm="20" :md="16" :lg="12">
          <div class="hero-content">
            <el-avatar :size="150" :src="profile.avatar" class="hero-avatar glow-effect" />
            <h1 class="hero-title">
              <TypeWriter :text="profile.name" :speed="150" />
            </h1>
            <p class="hero-subtitle">
              <TypeWriter :text="profile.title" :speed="80" :delay="800" />
            </p>
            <p class="hero-description">
              <TypeWriter :text="profile.description" :speed="50" :delay="1500" />
            </p>
            <div class="hero-buttons">
              <el-button type="primary" size="large" @click="scrollTo('projects')" class="hero-btn">
                <el-icon><Document /></el-icon>
                查看项目
              </el-button>
              <el-button size="large" @click="scrollTo('contact')" class="hero-btn hero-btn-outline">
                <el-icon><Message /></el-icon>
                联系我
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- 关于我 -->
    <section id="about" class="section">
      <el-container>
        <el-main>
          <h2 class="section-title">关于我</h2>
          <el-row :gutter="40" justify="center">
            <el-col :xs="24" :sm="20" :md="16" class="animate-fade-in">
              <el-card shadow="hover" class="glass-effect">
                <p class="about-text">{{ profile.about }}</p>
                <el-divider />
                <el-row :gutter="20">
                  <el-col :span="12">
                    <div class="info-item">
                      <el-icon><Location /></el-icon>
                      <span>{{ profile.location }}</span>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div class="info-item">
                      <el-icon><Message /></el-icon>
                      <span>{{ profile.email }}</span>
                    </div>
                  </el-col>
                </el-row>
              </el-card>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </section>

    <!-- 技能 -->
    <section id="skills" class="section section-gray">
      <el-container>
        <el-main>
          <h2 class="section-title">技能专长</h2>
          <el-row :gutter="20" justify="center">
            <el-col
              v-for="(skill, index) in skills"
              :key="skill.name"
              class="animate-scale-in"
              :class="`delay-${(index % 6) * 100}`"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="4"
            >
              <TiltCard :max-tilt="8">
                <BorderGlow :speed="1.0" :glow-size="50">
                  <el-card shadow="hover" class="skill-card glass-effect">
                    <div class="skill-icon">
                      <Icon :icon="skill.icon" :width="48" :height="48" />
                    </div>
                    <h3>{{ skill.name }}</h3>
                    <SkillProgress 
                      :name="skill.name"
                      :level="skill.level"
                      :color="skill.color"
                    />
                  </el-card>
                </BorderGlow>
              </TiltCard>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </section>

    <!-- 项目展示 -->
    <section id="projects" class="section">
      <el-container>
        <el-main>
          <h2 class="section-title">项目作品</h2>
          <el-row :gutter="30" justify="center">
            <el-col
              v-for="(project, index) in projects"
              :key="project.id"
              class="animate-slide-in-up"
              :class="`delay-${index * 200}`"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <TiltCard :max-tilt="5">
                <el-card shadow="hover" class="project-card glass-effect">
                  <template #header>
                    <div class="project-header">
                      <span class="project-name">{{ project.name }}</span>
                      <el-tag :type="project.status === 'online' ? 'success' : 'info'">
                        {{ project.status === 'online' ? '已上线' : '开发中' }}
                      </el-tag>
                    </div>
                  </template>
                  <div class="project-image">
                    <img :src="project.image" :alt="project.name" />
                  </div>
                  <p class="project-desc">{{ project.description }}</p>
                  <div class="project-tags">
                    <el-tag
                      v-for="tech in project.tech"
                      :key="tech"
                      size="small"
                      class="tech-tag"
                    >
                      {{ tech }}
                    </el-tag>
                  </div>
                  <div class="project-actions">
                    <el-button type="primary" link @click="openProject(project.url)">
                      <el-icon><View /></el-icon>
                      查看项目
                    </el-button>
                    <el-button link v-if="project.github" @click="openProject(project.github)">
                      <el-icon><Link /></el-icon>
                      GitHub
                    </el-button>
                  </div>
                </el-card>
              </TiltCard>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </section>

    <!-- 联系方式 -->
    <section id="contact" class="section section-gray">
      <el-container>
        <el-main>
          <h2 class="section-title">联系我</h2>
          <el-row justify="center">
            <el-col :xs="24" :sm="20" :md="16" :lg="12" class="animate-fade-in">
              <el-card shadow="hover" class="glass-effect">
                <el-row :gutter="20" justify="center">
                  <el-col :span="8" class="contact-item">
                    <el-icon :size="40" color="#409EFF"><Message /></el-icon>
                    <h3>邮箱</h3>
                    <p>{{ profile.email }}</p>
                  </el-col>
                  <el-col :span="8" class="contact-item">
                    <el-icon :size="40" color="#67C23A"><Link /></el-icon>
                    <h3>GitHub</h3>
                    <p>{{ profile.github }}</p>
                  </el-col>
                  <el-col :span="8" class="contact-item">
                    <el-icon :size="40" color="#E6A23C"><Phone /></el-icon>
                    <h3>微信</h3>
                    <p>{{ profile.wechat }}</p>
                  </el-col>
                </el-row>
              </el-card>
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </section>

    <!-- 页脚 -->
    <footer class="footer">
      <p>&copy; 2026 {{ profile.name }}. All rights reserved.</p>
    </footer>

    <!-- 返回顶部 -->
    <el-backtop :right="50" :bottom="50" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import ParticlesBackground from './components/effects/ParticlesBackground.vue'
import HexagonGrid from './components/effects/HexagonGrid.vue'
import ScrollProgress from './components/effects/ScrollProgress.vue'
import TypeWriter from './components/effects/TypeWriter.vue'
import TiltCard from './components/effects/TiltCard.vue'
import LanternSwitch from './components/effects/LanternSwitch.vue'
import CodeRain from './components/effects/CodeRain.vue'
import SkillProgress from './components/effects/SkillProgress.vue'
import ClickEffect from './components/effects/ClickEffect.vue'
import StarryBackground from './components/effects/StarryBackground.vue'
import BorderGlow from './components/effects/BorderGlow.vue'
import TestShadow from './components/TestShadow.vue'
import { useThemeSystem } from './composables/useThemeSystem'
import { useScroll } from './composables/useScroll'
import { profile, skills, projects } from './data/portfolio'

// 主题系统
const { isDark, currentTheme, toggleDarkMode, toggleTheme, THEMES } = useThemeSystem()

// 滚动功能
const { scrollTo } = useScroll()

// 导航高亮追随
const highlightStyle = ref({})
const highlightVisible = ref(false)

const updateHighlight = (e) => {
  const target = e.currentTarget
  const rect = target.getBoundingClientRect()
  const parent = target.parentElement.getBoundingClientRect()
  
  requestAnimationFrame(() => {
    highlightStyle.value = {
      left: `${rect.left - parent.left}px`,
      width: `${rect.width}px`,
      opacity: 1,
      transform: 'scaleX(1)'
    }
    highlightVisible.value = true
  })
}

const hideHighlight = () => {
  highlightStyle.value = {
    ...highlightStyle.value,
    opacity: 0,
    transform: 'scaleX(0.8)'
  }
}

// 打开项目链接
const openProject = (url) => {
  if (url && url !== '#') {
    window.open(url, '_blank')
  }
}
</script>

<style scoped>
.portfolio {
  min-height: 100vh;
  background: var(--color-bg);
  position: relative;
  overflow-x: hidden;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

/* 导航栏 */
.nav-wrapper {
  backdrop-filter: blur(12px) saturate(180%);
  -webkit-backdrop-filter: blur(12px) saturate(180%);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.nav-menu {
  border-bottom: none !important;
  padding: 0 var(--spacing-lg);
  position: relative;
}

.nav-highlight {
  position: absolute;
  bottom: 0;
  height: 3px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px 2px 0 0;
  transition: all 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  pointer-events: none;
  z-index: 0;
  box-shadow: 0 -2px 8px rgba(102, 126, 234, 0.4);
  will-change: left, width;
}

.nav-menu .el-menu-item {
  font-weight: var(--font-medium);
  font-size: var(--text-base);
  transition: color var(--transition-fast);
  position: relative;
  z-index: 1;
}

.nav-menu .el-menu-item:hover {
  background-color: transparent;
  color: #667eea !important;
}

.nav-menu .el-menu-item .el-icon {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-menu .el-menu-item:hover .el-icon {
  transform: translateY(-3px) scale(1.15);
  filter: drop-shadow(0 4px 8px rgba(102, 126, 234, 0.4));
  color: #667eea;
}

.nav-menu .el-menu-item:hover h2 {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  transform: scale(1.05);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.logo h2 {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.02em;
}

.flex-grow {
  flex-grow: 1;
}

/* Hero 区域 */
.hero-section {
  height: 100vh;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #d946ef 100%);
  background-size: 200% 200%;
  animation: gradientShift 20s ease infinite;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 40%),
    radial-gradient(circle at 40% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 40%);
  pointer-events: none;
  animation: float 15s ease-in-out infinite;
}

.hero-content {
  text-align: center;
  position: relative;
  z-index: 2;
}

.hero-avatar {
  margin-bottom: var(--spacing-2xl);
  border: 6px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
  animation: float 3s ease-in-out infinite;
  backdrop-filter: blur(10px);
}

.hero-title {
  font-size: var(--text-5xl);
  margin-bottom: var(--spacing-md);
  font-weight: var(--font-extrabold);
  min-height: 60px;
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.hero-subtitle {
  font-size: var(--text-2xl);
  margin-bottom: var(--spacing-lg);
  opacity: 0.95;
  min-height: 32px;
  font-weight: var(--font-medium);
  letter-spacing: -0.01em;
}

.hero-description {
  font-size: var(--text-lg);
  margin-bottom: var(--spacing-2xl);
  opacity: 0.9;
  max-width: 650px;
  margin-left: auto;
  margin-right: auto;
  min-height: 50px;
  line-height: 1.7;
  font-weight: var(--font-normal);
}

@media (min-width: 768px) {
  .hero-title {
    font-size: var(--text-6xl);
  }
  
  .hero-subtitle {
    font-size: var(--text-3xl);
  }
}

.hero-buttons {
  display: flex;
  gap: var(--spacing-lg);
  justify-content: center;
  animation: slideInUp 0.8s ease-out 2s both;
  flex-wrap: wrap;
}

.hero-btn {
  padding: var(--spacing-md) var(--spacing-2xl);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  border-radius: var(--radius-full);
  transition: all var(--transition-base);
  letter-spacing: -0.01em;
  box-shadow: 0 8px 20px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(0);
}

.hero-btn:hover {
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  transform: translateY(2px) scale(0.98);
}

.hero-btn:active {
  transform: translateY(4px) scale(0.96);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.2);
}

.hero-btn-outline {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 2px solid rgba(255, 255, 255, 0.8) !important;
  color: white !important;
  backdrop-filter: blur(10px);
}

.hero-btn-outline:hover {
  background: rgba(255, 255, 255, 0.95) !important;
  color: #6366f1 !important;
  border-color: white !important;
}

/* 通用区域样式 */
.section {
  padding: var(--spacing-5xl) var(--spacing-lg);
  max-width: 1400px;
  margin: 0 auto;
}

.section-gray {
  background: var(--color-bg-tertiary);
}

@media (min-width: 768px) {
  .section {
    padding: 120px var(--spacing-2xl);
  }
}

.section-title {
  text-align: center;
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--spacing-3xl);
  color: var(--color-text);
  position: relative;
  animation: slideInDown 0.6s ease-out;
  letter-spacing: -0.02em;
}

.section-title::after {
  content: '';
  display: block;
  width: 80px;
  height: 5px;
  background: var(--gradient-primary);
  margin: var(--spacing-lg) auto 0;
  border-radius: var(--radius-full);
  animation: scaleIn 0.6s ease-out 0.3s both;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

@media (min-width: 768px) {
  .section-title {
    font-size: var(--text-5xl);
  }
}

/* 关于我 */
.about-text {
  font-size: var(--text-lg);
  line-height: 1.8;
  color: var(--color-text-secondary);
  text-align: left;
  margin-bottom: var(--spacing-lg);
  font-weight: var(--font-normal);
}

.info-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  font-weight: var(--font-medium);
}

.info-item .el-icon {
  color: var(--color-primary);
  font-size: var(--text-lg);
}

/* 技能卡片 */
.skill-card {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  transition: all var(--transition-base);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-xl);
  padding: var(--spacing-xl) var(--spacing-lg);
}

.skill-card:hover {
  border-color: var(--color-primary);
  /* box-shadow已经移到BorderGlow的wrapper上了 */
}

.skill-icon {
  font-size: 48px;
  margin-bottom: 15px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
  transition: all var(--transition-base);
}

.skill-card:hover .skill-icon {
  filter: drop-shadow(0 8px 16px rgba(102, 126, 234, 0.3)) brightness(1.2);
}

.skill-card h3 {
  margin-bottom: var(--spacing-md);
  color: var(--color-text);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  letter-spacing: -0.01em;
}

/* 项目卡片 */
.project-card {
  margin-bottom: var(--spacing-xl);
  transition: all var(--transition-base);
  border: 1px solid var(--color-border);
  height: 100%;
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.project-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-colored);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--color-bg-tertiary);
  margin: calc(-1 * var(--spacing-lg));
  margin-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.project-name {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-text);
  letter-spacing: -0.01em;
}

.project-image {
  width: 100%;
  height: 220px;
  overflow: hidden;
  border-radius: var(--radius-lg);
  margin-bottom: var(--spacing-md);
  background: var(--color-bg-tertiary);
}

.project-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.5s ease;
  filter: brightness(0.95);
}

.project-card:hover .project-image img {
  transform: scale(1.1);
  filter: brightness(1.05);
}

.project-desc {
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  line-height: 1.7;
  margin-bottom: var(--spacing-md);
  min-height: 70px;
  font-weight: var(--font-normal);
}

.project-tags {
  margin-bottom: var(--spacing-md);
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.tech-tag {
  margin: 0;
  transition: all var(--transition-fast);
  cursor: default;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  padding: 4px 12px;
  border-radius: var(--radius-full);
}

.tech-tag:hover {
  transform: translateY(-2px) scale(1.05);
  box-shadow: var(--shadow-md);
}

.project-actions {
  display: flex;
  justify-content: space-between;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border);
}

.project-actions .el-button {
  font-weight: var(--font-medium);
  transition: all var(--transition-fast);
}

.project-actions .el-button:hover {
  transform: translateX(4px);
}

/* 联系方式 */
.contact-item {
  text-align: center;
  padding: var(--spacing-xl);
  transition: all var(--transition-base);
  cursor: pointer;
  border-radius: var(--radius-lg);
  border: 2px solid transparent;
}

.contact-item:hover {
  background: var(--color-bg-tertiary);
  box-shadow: var(--shadow-lg);
  border: 2px solid var(--color-primary);
}

.contact-item:hover .el-icon {
  animation: bounce 0.6s ease-in-out;
}

.contact-item .el-icon {
  transition: all var(--transition-base);
}

.contact-item h3 {
  margin: var(--spacing-md) 0 var(--spacing-sm);
  color: var(--color-text);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
}

.contact-item p {
  color: var(--color-text-secondary);
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

/* 页脚 */
.footer {
  background: var(--gradient-dark);
  color: white;
  text-align: center;
  padding: var(--spacing-2xl);
  position: relative;
  overflow: hidden;
  margin-top: var(--spacing-5xl);
}

.footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 3px;
  background: var(--gradient-primary);
  animation: shimmer 4s infinite;
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.6);
}

.footer p {
  margin: 0;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  opacity: 0.9;
  letter-spacing: 0.01em;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: var(--text-4xl);
  }

  .hero-subtitle {
    font-size: var(--text-xl);
  }
  
  .hero-description {
    font-size: var(--text-base);
  }

  .section-title {
    font-size: var(--text-3xl);
  }
  
  .section {
    padding: var(--spacing-4xl) var(--spacing-md);
  }
  
  .hero-buttons {
    gap: var(--spacing-md);
  }
  
  .hero-btn {
    padding: var(--spacing-sm) var(--spacing-xl);
    font-size: var(--text-base);
  }
  
  .skill-icon {
    font-size: 40px;
  }
  
  .contact-item {
    padding: var(--spacing-lg);
  }
}
</style>


/* 深色模式特殊样式 */
.dark-mode .section-title {
  color: var(--color-text);
}

.dark-mode .hero-section {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4c1d95 100%);
}

.dark-mode .section-gray {
  background: var(--color-bg-tertiary);
}

.dark-mode .about-text,
.dark-mode .project-desc,
.dark-mode .contact-item p {
  color: var(--color-text-secondary);
}

.dark-mode .skill-card h3,
.dark-mode .project-name,
.dark-mode .contact-item h3 {
  color: var(--color-text);
}

.dark-mode .project-card,
.dark-mode .skill-card {
  background: var(--color-bg-secondary);
  border-color: var(--color-border);
}

.dark-mode .project-card:hover,
.dark-mode .skill-card:hover {
  background: var(--color-bg-tertiary);
  border-color: var(--color-primary);
}

/* 加载动画 */
@keyframes fadeInScale {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.portfolio {
  animation: fadeInScale 0.5s ease-out;
}

/* 平滑滚动 */
html {
  scroll-behavior: smooth;
}

/* 选中文本样式 */
::selection {
  background: rgba(102, 126, 234, 0.3);
  color: inherit;
}

.dark-mode ::selection {
  background: rgba(102, 126, 234, 0.5);
}
