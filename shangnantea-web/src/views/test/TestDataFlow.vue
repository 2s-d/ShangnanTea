<template>
  <div class="test-dataflow">
    <el-card class="test-header">
      <h2>数据流通测试</h2>
      <p>测试前后端API连通性</p>
    </el-card>

    <!-- 快速测试 -->
    <el-card class="test-section">
      <template #header>
        <span>快速测试</span>
      </template>
      
      <div class="test-grid">
        <el-button type="primary" @click="testLogin" :loading="loading.login">测试登录</el-button>
        <el-button @click="testTeaList" :loading="loading.tea">获取茶叶列表</el-button>
        <el-button @click="testShopList" :loading="loading.shop">获取店铺列表</el-button>
        <el-button @click="testForumPosts" :loading="loading.forum">获取论坛帖子</el-button>
        <el-button type="success" @click="runAllTests" :loading="loading.all">运行全部测试</el-button>
      </div>
    </el-card>

    <!-- 测试结果 -->
    <el-card class="test-results">
      <template #header>
        <div class="card-header">
          <span>测试结果 ({{ testResults.length }})</span>
          <el-button @click="clearResults" size="small">清空</el-button>
        </div>
      </template>
      
      <div class="results-container">
        <div v-for="(result, index) in testResults" :key="index" 
             :class="['result-item', result.success ? 'success' : 'error']">
          <div class="result-header">
            <span class="result-icon">{{ result.success ? '✅' : '❌' }}</span>
            <span class="result-title">{{ result.title }}</span>
            <span class="result-time">{{ result.time }}</span>
          </div>
          <div class="result-content">
            <pre>{{ result.content }}</pre>
          </div>
        </div>
        <div v-if="testResults.length === 0" class="no-results">
          暂无测试结果，点击上方按钮开始测试
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as userApi from '@/api/user'
import * as teaApi from '@/api/tea'
import * as shopApi from '@/api/shop'
import * as forumApi from '@/api/forum'

export default {
  name: 'TestDataFlow',
  data() {
    return {
      loading: {
        login: false,
        tea: false,
        shop: false,
        forum: false,
        all: false
      },
      testResults: []
    }
  },
  methods: {
    addResult(title, success, content) {
      this.testResults.unshift({
        title,
        success,
        content: typeof content === 'object' ? JSON.stringify(content, null, 2) : String(content),
        time: new Date().toLocaleTimeString()
      })
    },

    // 测试登录API
    async testLogin() {
      this.loading.login = true
      try {
        const res = await userApi.login({ username: 'admin', password: '123456' })
        this.addResult('用户登录', true, res)
      } catch (error) {
        this.addResult('用户登录', false, error.message || '请求失败')
      } finally {
        this.loading.login = false
      }
    },

    // 测试茶叶列表
    async testTeaList() {
      this.loading.tea = true
      try {
        const res = await teaApi.getTeas({ page: 1, size: 5 })
        this.addResult('茶叶列表', true, `获取到 ${res?.data?.length || 0} 条数据`)
      } catch (error) {
        this.addResult('茶叶列表', false, error.message || '请求失败')
      } finally {
        this.loading.tea = false
      }
    },

    // 测试店铺列表
    async testShopList() {
      this.loading.shop = true
      try {
        const res = await shopApi.getShops({ page: 1, size: 5 })
        this.addResult('店铺列表', true, `获取到 ${res?.data?.length || 0} 条数据`)
      } catch (error) {
        this.addResult('店铺列表', false, error.message || '请求失败')
      } finally {
        this.loading.shop = false
      }
    },

    // 测试论坛帖子
    async testForumPosts() {
      this.loading.forum = true
      try {
        const res = await forumApi.getForumPosts({ page: 1, size: 5 })
        this.addResult('论坛帖子', true, `获取到 ${res?.data?.length || 0} 条数据`)
      } catch (error) {
        this.addResult('论坛帖子', false, error.message || '请求失败')
      } finally {
        this.loading.forum = false
      }
    },

    // 运行全部测试
    async runAllTests() {
      this.loading.all = true
      this.addResult('开始测试', true, '正在测试所有API...')
      
      await this.testLogin()
      await this.testTeaList()
      await this.testShopList()
      await this.testForumPosts()
      
      this.addResult('测试完成', true, '全部API测试完成')
      this.loading.all = false
    },

    clearResults() {
      this.testResults = []
    }
  }
}
</script>

<style scoped>
.test-dataflow {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.test-section, .test-results, .test-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.test-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.results-container {
  max-height: 500px;
  overflow-y: auto;
}

.result-item {
  margin-bottom: 12px;
  padding: 12px;
  border-radius: 6px;
  border-left: 4px solid;
}

.result-item.success {
  background-color: #f0f9ff;
  border-left-color: #67c23a;
}

.result-item.error {
  background-color: #fef0f0;
  border-left-color: #f56c6c;
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.result-icon {
  margin-right: 8px;
}

.result-title {
  font-weight: bold;
  margin-right: auto;
}

.result-time {
  font-size: 12px;
  color: #909399;
}

.result-content pre {
  margin: 0;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  color: #606266;
}

.no-results {
  text-align: center;
  color: #909399;
  padding: 40px;
}
</style>
