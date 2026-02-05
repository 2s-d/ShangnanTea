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
  { name: 'Java', level: 90, icon: 'devicon:java', color: '#f56c6c' },
  { name: 'Spring Boot', level: 85, icon: 'devicon:spring', color: '#67c23a' },
  { name: 'Vue.js', level: 88, icon: 'devicon:vuejs', color: '#409eff' },
  { name: 'MySQL', level: 80, icon: 'devicon:mysql', color: '#e6a23c' },
  { name: 'Redis', level: 75, icon: 'devicon:redis', color: '#f56c6c' },
  { name: 'Git', level: 85, icon: 'devicon:git', color: '#909399' },
  { name: 'Docker', level: 70, icon: 'devicon:docker', color: '#409eff' },
  { name: 'Nginx', level: 75, icon: 'devicon:nginx', color: '#67c23a' },
  { name: 'Linux', level: 80, icon: 'devicon:linux', color: '#909399' },
  { name: 'JavaScript', level: 85, icon: 'devicon:javascript', color: '#e6a23c' },
  { name: 'TypeScript', level: 78, icon: 'devicon:typescript', color: '#409eff' },
  { name: 'Node.js', level: 75, icon: 'devicon:nodejs', color: '#67c23a' }
]

// 项目列表
export const projects = [
  {
    id: 1,
    name: '商南茶电商系统',
    description: '基于 Spring Boot + Vue.js 的全栈电商平台，包含用户管理、商品管理、订单系统、支付集成等完整功能',
    image: 'https://picsum.photos/400/250?random=1',
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
    image: 'https://picsum.photos/400/250?random=2',
    tech: ['Java', 'Vue', 'MySQL'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  },
  {
    id: 3,
    name: '项目3',
    description: '这里是项目3的简介，展示你的其他作品',
    image: 'https://picsum.photos/400/250?random=3',
    tech: ['React', 'Node.js', 'MongoDB'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  },
  {
    id: 4,
    name: '项目4',
    description: '这里是项目4的简介，展示更多技术能力',
    image: 'https://picsum.photos/400/250?random=4',
    tech: ['Python', 'Django', 'PostgreSQL'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  },
  {
    id: 6,
    name: '项目6',
    description: '这里是项目6的简介，展示更多技术能力',
    image: 'https://picsum.photos/400/250?random=6',
    tech: ['Angular', 'NestJS', 'GraphQL'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  },
  {
    id: 7,
    name: '项目7',
    description: '这里是项目7的简介，完善你的作品集',
    image: 'https://picsum.photos/400/250?random=7',
    tech: ['Flutter', 'Firebase', 'Dart'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  },
  {
    id: 8,
    name: '项目8',
    description: '这里是项目8的简介，展示全栈能力',
    image: 'https://picsum.photos/400/250?random=8',
    tech: ['Go', 'gRPC', 'Kubernetes'],
    status: 'dev',
    url: '#',
    github: '',
    features: []
  }
]
