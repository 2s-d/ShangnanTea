// 个人信息配置
export const profile = {
  name: '张三',
  title: '全栈开发工程师',
  description: '热爱编程，专注于 Web 全栈开发，擅长 Java、Vue.js 等技术栈',
  about: '我是一名充满热情的全栈开发工程师，拥有丰富的项目经验。擅长使用 Spring Boot、Vue.js 等现代技术栈构建高质量的 Web 应用。注重代码质量和用户体验，持续学习新技术，追求技术卓越。',
  avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  location: '中国·北京',
  email: 'your.email@example.com',
  github: 'github.com/yourname',
  wechat: 'your_wechat'
}

// 技能列表
export const skills = [
  { name: 'Java', level: 90, icon: '☕', color: '#f56c6c' },
  { name: 'Spring Boot', level: 85, icon: '🍃', color: '#67c23a' },
  { name: 'Vue.js', level: 88, icon: '💚', color: '#409eff' },
  { name: 'MySQL', level: 80, icon: '🗄️', color: '#e6a23c' },
  { name: 'Redis', level: 75, icon: '🔴', color: '#f56c6c' },
  { name: 'Git', level: 85, icon: '📦', color: '#909399' }
]

// 项目列表
export const projects = [
  {
    id: 1,
    name: '商南茶电商系统',
    description: '基于 Spring Boot + Vue.js 的全栈电商平台，包含用户管理、商品管理、订单系统、支付集成等完整功能',
    image: 'https://via.placeholder.com/400x250/409EFF/FFFFFF?text=Shangnantea',
    tech: ['Spring Boot', 'Vue 3', 'MySQL', 'Redis', 'Alipay'],
    status: 'online',
    url: 'http://96.30.204.197/shangnantea/',
    github: '',
    features: [
      '用户注册登录、权限管理',
      '商品浏览、购物车、订单管理',
      '支付宝沙箱支付集成',
      '社区论坛、消息系统',
      'Redis 缓存优化'
    ]
  },
  {
    id: 2,
    name: '项目2',
    description: '这里是项目2的简介，可以描述项目的主要功能和技术亮点',
    image: 'https://via.placeholder.com/400x250/67C23A/FFFFFF?text=Project+2',
    tech: ['Java', 'Vue', 'MySQL'],
    status: 'dev',
    url: 'http://96.30.204.197/project2/',
    github: '',
    features: []
  },
  {
    id: 3,
    name: '项目3',
    description: '这里是项目3的简介，展示你的其他作品',
    image: 'https://via.placeholder.com/400x250/E6A23C/FFFFFF?text=Project+3',
    tech: ['React', 'Node.js', 'MongoDB'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  }
]
