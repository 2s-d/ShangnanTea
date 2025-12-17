/**
 * 数据版本管理工具
 * 用于检查和迁移本地存储数据，确保不同版本间的数据兼容性
 */
import { useTokenStorage } from '@/composables/useStorage'

// 创建token存储实例
const tokenStorage = useTokenStorage()
const { getToken, setToken, removeToken } = tokenStorage

// 当前应用数据版本，每次对数据结构进行不兼容修改时需要更新此版本号
export const APP_DATA_VERSION = '1.2.0';

// 版本号键名
const VERSION_KEY = 'app_data_version';

// 防止重复错误消息的标志
let errorShown = false;

/**
 * 检查并迁移本地存储数据
 * 在应用启动时调用
 * @param {boolean} silent 是否静默执行，不显示错误消息
 */
export function checkAndMigrateData(silent = true) {
  // 重置错误标志
  errorShown = false;
  
  try {
    // 获取存储的版本号
    const storedVersion = localStorage.getItem(VERSION_KEY);
    
    // 如果没有版本号或版本不匹配，则需要迁移
    if (storedVersion !== APP_DATA_VERSION) {
      console.log(`数据版本不匹配: 存储版本=${storedVersion || '无'}, 当前版本=${APP_DATA_VERSION}`);
      
      // 执行数据迁移
      const migrated = migrateData(storedVersion, APP_DATA_VERSION, silent);
      
      // 更新版本号
      localStorage.setItem(VERSION_KEY, APP_DATA_VERSION);
      console.log(`数据版本已更新为 ${APP_DATA_VERSION}`);
      
      return migrated;
    } else {
      console.log(`数据版本检查通过: ${APP_DATA_VERSION}`);
    }
  } catch (error) {
    console.error('版本检查失败:', error);
    if (!silent && !errorShown) {
      errorShown = true;
      // 这里不再主动显示错误消息，避免与其他错误消息重叠
    }
  }
  
  return false;
}

/**
 * 数据迁移函数
 * @param {string|null} fromVersion 原版本号
 * @param {string} toVersion 目标版本号
 * @param {boolean} silent 是否静默执行
 */
function migrateData(fromVersion, toVersion, silent = true) {
  let migrated = false;
  
  try {
    // 如果没有fromVersion（首次安装或之前没有版本管理）
    if (!fromVersion) {
      console.log(`首次安装或之前无版本记录，初始化数据到 ${toVersion}`);
      // 清理可能存在的旧数据
      clearAllAuthData(silent);
      migrated = true;
    }
    
    console.log(`准备执行数据迁移: ${fromVersion || '无版本'} -> ${toVersion}`);
    
    // 版本迁移路径
    const migrationPaths = [
      { version: '1.0.5', migrator: migrateToV105 },
      { version: '1.1.0', migrator: migrateToV110 },
      { version: '1.2.0', migrator: migrateToV120 }
      // 在此处添加未来的迁移版本和对应的迁移函数
    ];
    
    // 按版本顺序执行迁移
    for (const path of migrationPaths) {
      if (compareVersions(fromVersion, path.version) < 0 && 
          compareVersions(path.version, toVersion) <= 0) {
        console.log(`执行迁移: ${path.version}`);
        migrated = path.migrator(silent) || migrated;
      }
    }
  } catch (error) {
    console.error('数据迁移失败:', error);
    if (!silent && !errorShown) {
      errorShown = true;
      // 这里不再主动显示错误消息
    }
  }
  
  return migrated;
}

/**
 * 将数据迁移到v1.0.5版本
 * 处理早期版本的数据结构变化
 */
function migrateToV105(silent = true) {
  console.log('迁移数据到v1.0.5');
  // 在此版本中，我们可能进行一些基础数据结构的调整
  // 此处为示例，具体逻辑可以根据实际需求调整
  return false;
}

/**
 * 将数据迁移到v1.1.0版本
 * 处理权限系统升级带来的数据变化
 */
function migrateToV110(silent = true) {
  console.log('迁移数据到v1.1.0');
  
  // 使用tokenStorage检查token有效性，而不是直接访问localStorage
  const token = getToken();
  if (token) {
    // 验证token格式
    try {
      const tokenParts = token.split('.');
      if (tokenParts.length < 2) {
        // 不是有效的token格式
        console.log('token格式无效，清除认证数据');
        clearAllAuthData(silent);
      }
    } catch (e) {
      console.error('验证token失败', e);
      clearAllAuthData(silent);
    }
  }
  return false;
}

/**
 * 将数据迁移到v1.2.0版本
 * 处理新认证系统的变化
 */
function migrateToV120(silent = true) {
  console.log('迁移数据到v1.2.0（当前版本）');
  
  // 适配旧的token存储方式
  const oldToken = localStorage.getItem('token');
  if (oldToken) {
    // 使用tokenStorage设置token
    setToken(oldToken);
    // 删除旧的token存储
    localStorage.removeItem('token');
    console.log('旧token已迁移到useTokenStorage管理');
  }
  
  // 移除旧的认证数据
  localStorage.removeItem('userId');
  localStorage.removeItem('userRole');
  
  // 移除已弃用的购物车和订单本地存储
  localStorage.removeItem('cart_items');
  localStorage.removeItem('recent_orders');
  return true;
}

/**
 * 清除所有认证相关数据
 * @param {boolean} silent 是否静默执行
 */
function clearAllAuthData(silent = true) {
  console.log('清除所有认证数据');
  try {
    // 使用tokenStorage移除token
    removeToken();
    
    // 移除其他旧版认证数据
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
    localStorage.removeItem('token');
    
    return true;
  } catch (error) {
    console.error('清除认证数据失败:', error);
    if (!silent && !errorShown) {
      errorShown = true;
      // 这里不再主动显示错误消息
    }
    return false;
  }
}

/**
 * 比较两个版本号
 * @param {string} v1 第一个版本号
 * @param {string} v2 第二个版本号
 * @returns {number} 如果v1小于v2返回-1，相等返回0，大于返回1
 */
function compareVersions(v1, v2) {
  if (!v1) return -1; // 如果v1不存在，认为它小于任何版本
  
  const parts1 = v1.split('.').map(Number);
  const parts2 = v2.split('.').map(Number);
  
  for (let i = 0; i < Math.max(parts1.length, parts2.length); i++) {
    const p1 = parts1[i] || 0;
    const p2 = parts2[i] || 0;
    
    if (p1 < p2) return -1;
    if (p1 > p2) return 1;
  }
  
  return 0; // 版本相等
}

/**
 * 重置所有应用数据
 * 用于开发环境或问题排查
 */
export function resetAppData() {
  // 清除token - 使用tokenStorage
  removeToken();
  
  // 清除所有带前缀的数据
  const keysToRemove = [];
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    if (key.startsWith('shangnantea_')) {
      keysToRemove.push(key);
    }
  }
  
  // 移除收集的键
  keysToRemove.forEach(key => {
    localStorage.removeItem(key);
  });
  
  // 移除版本号，这样下次会触发迁移
  localStorage.removeItem('app_data_version');
  
  console.log('应用数据已重置');
  
  return keysToRemove.length + 1; // +1 是版本号
} 