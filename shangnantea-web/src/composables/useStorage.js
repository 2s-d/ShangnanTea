/**
 * 本地存储操作相关的组合式函数
 */
import { ref, watch } from 'vue'

// Token相关常量
export const TOKEN_EXPIRE_TIME = 24 * 60 * 60 * 1000 // Token过期时间 (24小时)
const SECRET_KEY = 'shangnantea-secret-key' // 服务器加密密钥 (实际应用中不应暴露，此处仅用于模拟)

/**
 * 存储类型
 * @enum {string}
 */
export const StorageType = {
  LOCAL: 'localStorage',
  SESSION: 'sessionStorage'
}

/**
 * 使用本地存储
 * @param {string} key 存储键名
 * @param {*} defaultValue 默认值
 * @param {Object} options 配置选项
 * @returns {Object} 本地存储相关方法和状态
 */
export function useStorage(key, defaultValue = null, options = {}) {
  // 默认配置
  const defaultOptions = {
    type: StorageType.LOCAL, // 存储类型，localStorage 或 sessionStorage
    prefix: 'shangnantea_', // 键名前缀
    serializer: JSON.stringify, // 序列化方法
    deserializer: JSON.parse, // 反序列化方法
    onError: (error) => console.error(error) // 错误处理
  }
  
  // 合并配置
  const config = { ...defaultOptions, ...options }
  
  // 获取存储对象
  const storage = window[config.type]
  const prefixedKey = `${config.prefix}${key}`
  
  // 从存储中读取值
  const readValue = () => {
    try {
      const item = storage.getItem(prefixedKey)
      return item === null ? defaultValue : config.deserializer(item)
    } catch (error) {
      config.onError(error)
      return defaultValue
    }
  }
  
  // 创建响应式状态
  const storedValue = ref(readValue())
  
  // 更新存储
  const updateStorage = (newValue) => {
    if (newValue === null || newValue === undefined) {
      storage.removeItem(prefixedKey)
    } else {
      try {
        storage.setItem(prefixedKey, config.serializer(newValue))
      } catch (error) {
        config.onError(error)
      }
    }
  }
  
  // 更新值
  const setValue = (newValue) => {
    try {
      // 如果是函数，则执行函数获取新值
      const valueToStore = newValue instanceof Function ? newValue(storedValue.value) : newValue
      
      // 更新响应式状态
      storedValue.value = valueToStore
      
      // 更新存储
      updateStorage(valueToStore)
      
      // 触发存储事件，确保跨标签页同步
      window.dispatchEvent(new StorageEvent('storage', {
        key: prefixedKey,
        newValue: config.serializer(valueToStore),
        storageArea: storage
      }))
      
      return true
    } catch (error) {
      config.onError(error)
      return false
    }
  }
  
  // 移除值
  const removeValue = () => {
    try {
      storedValue.value = defaultValue
      storage.removeItem(prefixedKey)
      return true
    } catch (error) {
      config.onError(error)
      return false
    }
  }
  
  // 监听存储事件，实现跨标签页同步
  const handleStorageChange = (event) => {
    if (event.key === prefixedKey && event.storageArea === storage) {
      try {
        const newValue = event.newValue ? config.deserializer(event.newValue) : defaultValue
        if (storedValue.value !== newValue) {
          storedValue.value = newValue
        }
      } catch (error) {
        config.onError(error)
      }
    }
  }
  
  // 添加存储事件监听器
  window.addEventListener('storage', handleStorageChange)
  
  // 监听 storedValue 变化，实时同步到存储
  watch(storedValue, (newValue) => {
    updateStorage(newValue)
  })
  
  /**
   * 获取存储中的所有项
   * @param {boolean} includePrefix 是否包含前缀
   * @returns {Object} 存储中的所有项
   */
  const getAll = (includePrefix = false) => {
    const result = {}
    const prefix = config.prefix
    
    try {
      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i)
        if (key.startsWith(prefix)) {
          const pureKey = includePrefix ? key : key.slice(prefix.length)
          result[pureKey] = config.deserializer(storage.getItem(key))
        }
      }
    } catch (error) {
      config.onError(error)
    }
    
    return result
  }
  
  /**
   * 清除存储中所有带前缀的项
   */
  const clearAll = () => {
    try {
      const prefix = config.prefix
      const keysToRemove = []
      
      for (let i = 0; i < storage.length; i++) {
        const key = storage.key(i)
        if (key.startsWith(prefix)) {
          keysToRemove.push(key)
        }
      }
      
      keysToRemove.forEach(key => storage.removeItem(key))
      
      if (prefixedKey.startsWith(prefix)) {
        storedValue.value = defaultValue
      }
      
      return true
    } catch (error) {
      config.onError(error)
      return false
    }
  }
  
  /**
   * 获取带前缀的键名
   * @returns {string} 带前缀的键名
   */
  const getPrefixedKey = () => prefixedKey
  
  return {
    value: storedValue,
    setValue,
    removeValue,
    getAll,
    clearAll,
    getPrefixedKey
  }
}

/**
 * 使用对象存储
 * @param {string} key 存储键名
 * @param {Object} defaultValue 默认值
 * @param {Object} options 配置选项
 * @returns {Object} 本地存储相关方法和状态
 */
export function useObjectStorage(key, defaultValue = {}, options = {}) {
  const { value, setValue, removeValue, getPrefixedKey } = useStorage(key, defaultValue, options)
  
  /**
   * 更新对象中的特定字段
   * @param {string|Object} field 字段名或字段对象
   * @param {*} fieldValue 字段值
   */
  const updateField = (field, fieldValue) => {
    if (typeof field === 'object') {
      // 如果 field 是对象，则批量更新多个字段
      setValue({
        ...value.value,
        ...field
      })
    } else {
      // 更新单个字段
      setValue({
        ...value.value,
        [field]: fieldValue
      })
    }
  }
  
  /**
   * 移除对象中的特定字段
   * @param {string|Array} fields 字段名或字段名数组
   */
  const removeField = (fields) => {
    const newValue = { ...value.value }
    
    if (Array.isArray(fields)) {
      fields.forEach(field => {
        delete newValue[field]
      })
    } else {
      delete newValue[fields]
    }
    
    setValue(newValue)
  }
  
  /**
   * 检查对象中是否存在特定字段
   * @param {string} field 字段名
   * @returns {boolean} 是否存在
   */
  const hasField = (field) => {
    return value.value && Object.prototype.hasOwnProperty.call(value.value, field)
  }
  
  /**
   * 获取对象中的特定字段值
   * @param {string} field 字段名
   * @param {*} defaultFieldValue 默认字段值
   * @returns {*} 字段值
   */
  const getField = (field, defaultFieldValue = null) => {
    return hasField(field) ? value.value[field] : defaultFieldValue
  }
  
  return {
    data: value,
    updateData: setValue,
    removeData: removeValue,
    updateField,
    removeField,
    hasField,
    getField,
    getPrefixedKey
  }
}

/**
 * 专门用于Token存储和操作的hooks
 * @returns {Object} Token相关方法和状态
 */
export function useTokenStorage() {
  // 使用通用存储函数创建token存储
  const { value: token, setValue: setTokenValue, removeValue: removeTokenValue } = useStorage('token')
  
  /**
   * 获取本地存储的token
   * @returns {string|null} token或null
   */
  const getToken = () => token.value
  
  /**
   * 设置token到本地存储
   * @param {string} newToken token字符串
   */
  const setToken = (newToken) => setTokenValue(newToken)
  
  /**
   * 移除本地存储的token
   */
  const removeToken = () => removeTokenValue()
  
  /**
   * 从token中解析用户信息，统一JWT解析结构
   * @param {string} tokenStr token字符串
   * @returns {Object|null} 用户信息
   */
  const decodeToken = (tokenStr) => {
    if (!tokenStr) return null;
    
    try {
      // 获取JWT的payload部分
      const tokenParts = tokenStr.split('.');
      if (tokenParts.length < 2) return null;
      
      // 解码payload - 处理base64url格式
      const payload = tokenParts[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('解析token失败:', error);
      return null;
    }
  }
  
  /**
   * 从token中获取用户信息
   * @returns {Object|null} 用户信息或null
   */
  const getUserFromToken = () => {
    const currentToken = token.value;
    return currentToken ? decodeToken(currentToken) : null;
  }
  
  /**
   * 验证token并获取用户信息，确保与后端匹配
   * @returns {Object|null} 有效则返回标准化用户信息，否则返回null
   */
  const verifyToken = () => {
    const currentToken = token.value;
    if (!currentToken) return null;
    
    try {
      // 解析JWT token
      const payload = decodeToken(currentToken);
      if (!payload) return null;
      
      // 检查关键字段是否存在 - 必须有sub作为用户ID
      if (!payload.sub) {
        console.warn("Token缺少用户ID(sub)字段");
        return null;
      }
      
      // 检查角色是否有效 - 只有1,2,3三种角色
      if (![1, 2, 3].includes(Number(payload.role))) {
        console.warn("Token包含无效的角色值");
        return null;
      }
      
      // 检查token是否过期 - 根据exp字段（Unix时间戳，秒）
      const now = Math.floor(Date.now() / 1000); // 转换为秒
      if (payload.exp && payload.exp < now) {
        console.warn("Token已过期");
        return null;
      }
      
      // 构建标准用户信息对象，确保与后端格式一致
      return {
        id: payload.sub,        // JWT中subject作为用户ID
        role: Number(payload.role),   // 用户角色：1-管理员，2-普通用户，3-商家
        username: payload.username || payload.preferred_username,
        exp: payload.exp ? payload.exp * 1000 : undefined // 转回毫秒便于前端处理
      };
    } catch (error) {
      console.error('验证token失败:', error);
      return null;
    }
  }
  
  /**
   * 检查并处理token状态
   * 如果token无效或过期，清除token
   * @returns {boolean} token是否有效
   */
  const isTokenValid = () => {
    if (!token.value) {
      return false;
    }
    
    const userInfo = verifyToken();
    if (!userInfo) {
      // token无效，清除
      removeToken();
      return false;
    }
    
    return true;
  }
  
  return {
    token,
    getToken,
    setToken,
    removeToken,
    isTokenValid,
    decodeToken,
    getUserFromToken,
    verifyToken
  }
} 