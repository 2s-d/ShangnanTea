<template>
  <div class="portfolio">
    <!-- 顶部导航 -->
    <el-affix :offset="0">
      <el-menu
        mode="horizontal"
        :ellipsis="false"
        background-color="#2c3e50"
        text-color="#fff"
        active-text-color="#409EFF"
        class="nav-menu"
      >
        <el-menu-item index="0" class="logo">
          <h2>{{ profile.name }}</h2>
        </el-menu-item>
        <div class="flex-grow" />
        <el-menu-item index="1" @click="scrollTo('hero')">首页</el-menu-item>
        <el-menu-item index="2" @click="scrollTo('about')">关于</el-menu-item>
        <el-menu-item index="3" @click="scrollTo('skills')">技能</el-menu-item>
        <el-menu-item index="4" @click="scrollTo('projects')">项目</el-menu-item>
        <el-menu-item index="5" @click="scrollTo('contact')">联系</el-menu-item>
      </el-menu>
    </el-affix>

    <!-- Hero 区域 -->
    <section id="hero" class="hero-section">
      <el-row justify="center" align="middle" style="height: 100%">
        <el-col :xs="22" :sm="20" :md="16" :lg="12">
          <div class="hero-content">
            <el-avatar :size="150" :src="profile.avatar" class="hero-avatar" />
            <h1 class="hero-title">{{ profile.name }}</h1>
            <p class="hero-subtitle">{{ profile.title }}</p>
            <p class="hero-description">{{ profile.description }}</p>
            <div class="hero-buttons">
              <el-button type="primary" size="large" @click="scrollTo('projects')">
                <el-icon><Document /></el-icon>
                查看项目
              </el-button>
              <el-button size="large" @click="scrollTo('contact')">
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
            <el-col :xs="24" :sm="20" :md="16">
              <el-card shadow="hover">
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
              v-for="skill in skills"
              :key="skill.name"
              :xs="12"
              :sm="8"
              :md="6"
              :lg="4"
            >
              <el-card shadow="hover" class="skill-card">
                <div class="skill-icon">{{ skill.icon }}</div>
                <h3>{{ skill.name }}</h3>
                <el-progress
                  :percentage="skill.level"
                  :color="skill.color"
                  :stroke-width="8"
                />
              </el-card>
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
              v-for="project in projects"
              :key="project.id"
              :xs="24"
              :sm="12"
              :md="8"
            >
              <el-card shadow="hover" class="project-card">
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
            <el-col :xs="24" :sm="20" :md="16" :lg="12">
              <el-card shadow="hover">
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

// 个人信息配置
const profile = ref({
  name: '张三',
  title: '全栈开发工程师',
  description: '热爱编程，专注于 Web 全栈开发，擅长 Java、Vue.js 等技术栈',
  about: '我是一名充满热情的全栈开发工程师，拥有丰富的项目经验。擅长使用 Spring Boot、Vue.js 等现代技术栈构建高质量的 Web 应用。注重代码质量和用户体验，持续学习新技术，追求技术卓越。',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  location: '中国·北京',
  email: 'your.email@example.com',
  github: 'github.com/yourname',
  wechat: 'your_wechat'
})

// 技能列表
const skills = ref([
  { name: 'Java', level: 90, icon: '☕', color: '#f56c6c' },
  { name: 'Spring Boot', level: 85, icon: '🍃', color: '#67c23a' },
  { name: 'Vue.js', level: 88, icon: '💚', color: '#409eff' },
  { name: 'MySQL', level: 80, icon: '🗄️', color: '#e6a23c' },
  { name: 'Redis', level: 75, icon: '🔴', color: '#f56c6c' },
  { name: 'Git', level: 85, icon: '📦', color: '#909399' }
])

// 项目列表
const projects = ref([
  {
    id: 1,
    name: '商南茶电商系统',
    description: '基于 Spring Boot + Vue.js 的全栈电商平台，包含用户管理、商品管理、订单系统、支付集成等完整功能',
    image: 'https://via.placeholder.com/400x250/409EFF/FFFFFF?text=Shangnantea',
    tech: ['Spring Boot', 'Vue 3', 'MySQL', 'Redis', 'Alipay'],
    status: 'online',
    url: 'http://96.30.204.197/shangnantea/',
    github: ''
  },
  {
    id: 2,
    name: '项目2',
    description: '这里是项目2的简介，可以描述项目的主要功能和技术亮点',
    image: 'https://via.placeholder.com/400x250/67C23A/FFFFFF?text=Project+2',
    tech: ['Java', 'Vue', 'MySQL'],
    status: 'dev',
    url: 'http://96.30.204.197/project2/',
    github: ''
  },
  {
    id: 3,
    name: '项目3',
    description: '这里是项目3的简介，展示你的其他作品',
    image: 'https://via.placeholder.com/400x250/E6A23C/FFFFFF?text=Project+3',
    tech: ['React', 'Node.js', 'MongoDB'],
    status: 'dev',
    url: '#',
    github: ''
  }
])

// 滚动到指定区域
const scrollTo = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.portfolio {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 导航栏 */
.nav-menu {
  border-bottom: none !important;
}

.logo h2 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.flex-grow {
  flex-grow: 1;
}

/* Hero 区域 */
.hero-section {
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-content {
  text-align: center;
}

.hero-avatar {
  margin-bottom: 30px;
  border: 5px solid white;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.hero-title {
  font-size: 48px;
  margin-bottom: 10px;
  font-weight: bold;
}

.hero-subtitle {
  font-size: 24px;
  margin-bottom: 20px;
  opacity: 0.9;
}

.hero-description {
  font-size: 18px;
  margin-bottom: 40px;
  opacity: 0.8;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

/* 通用区域样式 */
.section {
  padding: 80px 20px;
}

.section-gray {
  background: #f5f7fa;
}

.section-title {
  text-align: center;
  font-size: 36px;
  margin-bottom: 50px;
  color: #2c3e50;
  position: relative;
}

.section-title::after {
  content: '';
  display: block;
  width: 60px;
  height: 4px;
  background: #409eff;
  margin: 20px auto 0;
  border-radius: 2px;
}

/* 关于我 */
.about-text {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  text-align: justify;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

/* 技能卡片 */
.skill-card {
  text-align: center;
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.skill-card:hover {
  transform: translateY(-10px);
}

.skill-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.skill-card h3 {
  margin-bottom: 15px;
  color: #2c3e50;
}

/* 项目卡片 */
.project-card {
  margin-bottom: 30px;
  transition: transform 0.3s;
}

.project-card:hover {
  transform: translateY(-10px);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-name {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
}

.project-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 15px;
}

.project-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.project-card:hover .project-image img {
  transform: scale(1.1);
}

.project-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 15px;
  min-height: 60px;
}

.project-tags {
  margin-bottom: 15px;
}

.tech-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.project-actions {
  display: flex;
  justify-content: space-between;
}

/* 联系方式 */
.contact-item {
  text-align: center;
  padding: 20px;
}

.contact-item h3 {
  margin: 15px 0 10px;
  color: #2c3e50;
}

.contact-item p {
  color: #606266;
  font-size: 14px;
}

/* 页脚 */
.footer {
  background: #2c3e50;
  color: white;
  text-align: center;
  padding: 30px;
}

.footer p {
  margin: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .section-title {
    font-size: 28px;
  }
}
</style>
