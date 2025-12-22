<template>
  <div class="user-test-page">
    <el-card class="test-header">
      <h2>用户模块测试</h2>
      <p>通过 Vuex Store 调用 API，对比 Controller 预期返回数据</p>
    </el-card>

    <el-card class="test-section">
      <template #header>测试项</template>
      <div class="test-grid">
        <el-button type="primary" @click="testLogin" :loading="loading.login">登录</el-button>
        <el-button @click="testGetUserInfo" :loading="loading.userInfo">用户信息</el-button>
        <el-button @click="testGetAddresses" :loading="loading.addresses">地址列表</el-button>
        <el-button @click="testGetFollows" :loading="loading.follows">关注列表</el-button>
        <el-button @click="testGetFavorites" :loading="loading.favorites">收藏列表</el-button>
        <el-button @click="testGetPreferences" :loading="loading.preferences">偏好设置</el-button>
        <el-button type="success" @click="runAllTests" :loading="loading.all">全部测试</el-button>
      </div>
    </el-card>

    <el-card class="log-section">
      <template #header>
        <div class="log-header">
          <span>测试日志 ({{ logs.length }})</span>
          <div>
            <el-button size="small" @click="copyLogs" :disabled="logs.length === 0">复制日志</el-button>
            <el-button size="small" @click="clearLogs">清空</el-button>
          </div>
        </div>
      </template>
      <div class="log-container" ref="logContainer">
        <div v-for="(log, i) in logs" :key="i" :class="['log-item', log.type]">
          <span class="log-time">[{{ log.time }}]</span>
          <span class="log-msg">{{ log.msg }}</span>
        </div>
        <div v-if="logs.length === 0" class="no-logs">暂无日志</div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

// Controller 预期返回的测试数据
const EXPECTED = {
  login: {
    fields: ['id', 'username', 'nickname', 'email', 'phone', 'avatar', 'role'],
    checks: { role: 2 } // 普通用户
  },
  userInfo: {
    fields: ['id', 'username', 'nickname', 'email', 'phone', 'avatar', 'role', 'status'],
    checks: { status: 1 }
  },
  addresses: {
    minCount: 2, // Controller返回2条测试地址
    itemFields: ['id', 'name', 'phone', 'province', 'city', 'district', 'detail', 'isDefault'],
    // 前端store做了字段映射: receiverName->name, receiverPhone->phone, detailAddress->detail
    keywords: ['商南', '张三'] // 应包含的关键词
  },
  follows: {
    minCount: 2, // Controller返回2条：1个用户+1个店铺
    itemFields: ['id', 'targetId', 'targetType', 'targetName', 'createTime'],
    keywords: ['茶友小王', '商南茶庄']
  },
  favorites: {
    minCount: 2, // Controller返回2条：1个茶叶+1个帖子
    itemFields: ['id', 'targetId', 'targetType', 'targetName', 'createTime'],
    keywords: ['商南毛尖', '茶文化']
  },
  preferences: {
    fields: ['theme', 'primaryColor', 'fontSize', 'enableAnimation', 'listMode', 'pageSize'],
    checks: { theme: 'light', fontSize: 14 }
  }
}

export default {
  name: 'UserTestPage',
  data() {
    return {
      loading: { login: false, userInfo: false, addresses: false, follows: false, favorites: false, preferences: false, all: false },
      logs: []
    }
  },
  methods: {
    ...mapActions('user', ['login', 'getUserInfo', 'fetchAddresses', 'fetchFollowList', 'fetchFavoriteList', 'fetchUserPreferences']),

    log(msg, type = 'info') {
      this.logs.push({ time: new Date().toLocaleTimeString(), msg, type })
      this.$nextTick(() => {
        const c = this.$refs.logContainer
        if (c) c.scrollTop = c.scrollHeight
      })
    },

    // 检查必需字段
    checkFields(data, fields, name) {
      if (!data || typeof data !== 'object') {
        this.log(`${name}: 数据为空或类型错误`, 'error')
        return false
      }
      const missing = fields.filter(f => !(f in data))
      if (missing.length > 0) {
        this.log(`${name}: 缺少字段 [${missing.join(', ')}]`, 'error')
        return false
      }
      return true
    },

    // 检查字段值
    checkValues(data, checks, name) {
      for (const [key, expected] of Object.entries(checks)) {
        if (data[key] !== expected) {
          this.log(`${name}: ${key} 期望 ${expected}，实际 ${data[key]}`, 'error')
          return false
        }
      }
      return true
    },

    // 检查数组（含条数和关键词）
    checkArray(data, config, name) {
      if (!Array.isArray(data)) {
        this.log(`${name}: 期望数组，实际 ${typeof data}`, 'error')
        return false
      }
      // 检查条数
      if (data.length < config.minCount) {
        this.log(`${name}: 期望至少 ${config.minCount} 条，实际 ${data.length} 条`, 'error')
        return false
      }
      // 检查第一条的字段
      if (data.length > 0 && config.itemFields) {
        const missing = config.itemFields.filter(f => !(f in data[0]))
        if (missing.length > 0) {
          this.log(`${name}: 元素缺少字段 [${missing.join(', ')}]`, 'error')
          return false
        }
      }
      // 检查关键词
      if (config.keywords && config.keywords.length > 0) {
        const allText = JSON.stringify(data)
        const missingKw = config.keywords.filter(kw => !allText.includes(kw))
        if (missingKw.length > 0) {
          this.log(`${name}: 缺少关键数据 [${missingKw.join(', ')}]`, 'error')
          return false
        }
      }
      return true
    },

    async testLogin() {
      this.loading.login = true
      try {
        const res = await this.login({ username: 'admin', password: '123456' })
        if (!res) {
          this.log('登录: 无返回数据', 'error')
        } else if (!this.checkFields(res, EXPECTED.login.fields, '登录')) {
          // 已记录
        } else if (!this.checkValues(res, EXPECTED.login.checks, '登录')) {
          // 已记录
        } else {
          this.log('登录: ✓ 通过', 'success')
        }
      } catch (e) {
        this.log(`登录: 请求失败 - ${e.message}`, 'error')
      } finally {
        this.loading.login = false
      }
    },

    async testGetUserInfo() {
      this.loading.userInfo = true
      try {
        const res = await this.getUserInfo()
        if (!res) {
          this.log('用户信息: 无返回数据', 'error')
        } else if (!this.checkFields(res, EXPECTED.userInfo.fields, '用户信息')) {
          // 字段检查失败，已在checkFields中记录日志
        } else if (!this.checkValues(res, EXPECTED.userInfo.checks, '用户信息')) {
          // 值检查失败，已在checkValues中记录日志
        } else {
          this.log('用户信息: ✓ 通过', 'success')
        }
      } catch (e) {
        this.log(`用户信息: 请求失败 - ${e.message}`, 'error')
      } finally {
        this.loading.userInfo = false
      }
    },

    async testGetAddresses() {
      this.loading.addresses = true
      try {
        const res = await this.fetchAddresses()
        if (!this.checkArray(res, EXPECTED.addresses, '地址列表')) {
          // 数组检查失败，已在checkArray中记录日志
        } else {
          this.log(`地址列表: ✓ 通过 (${res.length}条)`, 'success')
        }
      } catch (e) {
        this.log(`地址列表: 请求失败 - ${e.message}`, 'error')
      } finally {
        this.loading.addresses = false
      }
    },

    async testGetFollows() {
      this.loading.follows = true
      try {
        const res = await this.fetchFollowList()
        if (!this.checkArray(res, EXPECTED.follows, '关注列表')) {
          // 数组检查失败，已在checkArray中记录日志
        } else {
          this.log(`关注列表: ✓ 通过 (${res.length}条)`, 'success')
        }
      } catch (e) {
        this.log(`关注列表: 请求失败 - ${e.message}`, 'error')
      } finally {
        this.loading.follows = false
      }
    },

    async testGetFavorites() {
      this.loading.favorites = true
      try {
        const res = await this.fetchFavoriteList()
        if (!this.checkArray(res, EXPECTED.favorites, '收藏列表')) {
          // 数组检查失败，已在checkArray中记录日志
        } else {
          this.log(`收藏列表: ✓ 通过 (${res.length}条)`, 'success')
        }
      } catch (e) {
        this.log(`收藏列表: 请求失败 - ${e.message}`, 'error')
      } finally {
        this.loading.favorites = false
      }
    },

    async testGetPreferences() {
      this.loading.preferences = true
      try {
        const res = await this.fetchUserPreferences()
        if (!res) {
          this.log('偏好设置: 无返回数据', 'error')
        } else if (!this.checkFields(res, EXPECTED.preferences.fields, '偏好设置')) {
          // 字段检查失败，已在checkFields中记录日志
        } else if (!this.checkValues(res, EXPECTED.preferences.checks, '偏好设置')) {
          // 值检查失败，已在checkValues中记录日志
        } else {
          this.log('偏好设置: ✓ 通过', 'success')
        }
      } catch (e) {
        this.log(`偏好设置: 请求失败 - ${e.message}`, 'error')
      } finally {
        this.loading.preferences = false
      }
    },

    async runAllTests() {
      this.loading.all = true
      this.log('===== 开始全部测试 =====', 'info')
      await this.testLogin()
      await this.testGetUserInfo()
      await this.testGetAddresses()
      await this.testGetFollows()
      await this.testGetFavorites()
      await this.testGetPreferences()
      this.log('===== 测试完成 =====', 'info')
      this.loading.all = false
    },

    copyLogs() {
      const text = this.logs.map(l => `[${l.time}] ${l.msg}`).join('\n')
      navigator.clipboard.writeText(text).then(() => {
        import('@/utils/apiMessages').then(({ showByCode }) => {
          showByCode(1005) // 复制成功
        })
      })
    },
    clearLogs() { this.logs = [] }
  }
}
</script>

<style scoped>
.user-test-page { padding: 20px; max-width: 900px; margin: 0 auto; }
.test-header, .test-section, .log-section { margin-bottom: 20px; }
.test-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.log-header { display: flex; justify-content: space-between; align-items: center; }
.log-container { max-height: 400px; overflow-y: auto; font-family: monospace; font-size: 13px; }
.log-item { padding: 4px 8px; border-bottom: 1px solid #eee; }
.log-item.error { background: #fef0f0; color: #f56c6c; }
.log-item.warn { background: #fdf6ec; color: #e6a23c; }
.log-item.success { background: #f0f9eb; color: #67c23a; }
.log-item.info { background: #f4f4f5; color: #909399; }
.log-time { margin-right: 8px; color: #909399; }
.no-logs { text-align: center; color: #909399; padding: 40px; }
</style>
