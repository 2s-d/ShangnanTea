/**
 * 消息工具索引文件
 * 统一导出所有模块的消息工具，方便开发中一次性导入
 */
import messageManager from './messageManager'
import userMessages from './userMessages'
import teaMessages from './teaMessages'
import orderMessages from './orderMessages'
import forumMessages from './forumMessages'
import shopMessages from './shopMessages'
import messageMessages from './messageMessages'

// 统一导出所有消息常量
export const MESSAGE_CONSTANTS = {
  USER: userMessages.USER_MESSAGES,
  TEA: teaMessages.TEA_MESSAGES,
  ORDER: orderMessages.ORDER_MESSAGES,
  FORUM: forumMessages.FORUM_MESSAGES,
  SHOP: shopMessages.SHOP_MESSAGES,
  MESSAGE: messageMessages.MESSAGE_MESSAGES
}

// 创建开发辅助函数
const devHelper = {
  /**
   * 显示所有可用的消息常量
   * @returns {Object} 所有消息常量的集合
   */
  showAllConstants() {
    console.table({
      'USER.API': Object.keys(MESSAGE_CONSTANTS.USER.API).length,
      'USER.BUSINESS': Object.keys(MESSAGE_CONSTANTS.USER.BUSINESS).length,
      'USER.UI': Object.keys(MESSAGE_CONSTANTS.USER.UI).length,
      'TEA.API': Object.keys(MESSAGE_CONSTANTS.TEA.API).length,
      'TEA.BUSINESS': Object.keys(MESSAGE_CONSTANTS.TEA.BUSINESS).length,
      'TEA.UI': Object.keys(MESSAGE_CONSTANTS.TEA.UI).length,
      'ORDER.API': Object.keys(MESSAGE_CONSTANTS.ORDER.API).length,
      'ORDER.BUSINESS': Object.keys(MESSAGE_CONSTANTS.ORDER.BUSINESS).length,
      'ORDER.UI': Object.keys(MESSAGE_CONSTANTS.ORDER.UI).length,
      'FORUM.API': Object.keys(MESSAGE_CONSTANTS.FORUM.API).length,
      'FORUM.BUSINESS': Object.keys(MESSAGE_CONSTANTS.FORUM.BUSINESS).length,
      'FORUM.UI': Object.keys(MESSAGE_CONSTANTS.FORUM.UI).length,
      'SHOP.API': Object.keys(MESSAGE_CONSTANTS.SHOP.API).length,
      'SHOP.BUSINESS': Object.keys(MESSAGE_CONSTANTS.SHOP.BUSINESS).length,
      'SHOP.UI': Object.keys(MESSAGE_CONSTANTS.SHOP.UI).length,
      'MESSAGE.API': Object.keys(MESSAGE_CONSTANTS.MESSAGE.API).length,
      'MESSAGE.BUSINESS': Object.keys(MESSAGE_CONSTANTS.MESSAGE.BUSINESS).length,
      'MESSAGE.UI': Object.keys(MESSAGE_CONSTANTS.MESSAGE.UI).length
    })
    return MESSAGE_CONSTANTS
  },
  
  /**
   * 查找指定消息文本对应的常量键
   * @param {string} text 要查找的消息文本
   * @returns {Array} 匹配的常量键数组
   */
  findMessageByText(text) {
    if (!text) return []
    
    const results = []
    
    // 在所有模块中查找
    Object.entries(MESSAGE_CONSTANTS).forEach(([moduleName, moduleMessages]) => {
      // 在每个层中查找
      Object.entries(moduleMessages).forEach(([layerName, layerMessages]) => {
        // 在每个消息中查找
        Object.entries(layerMessages).forEach(([msgKey, msgValue]) => {
          if (msgValue.includes(text)) {
            results.push({
              module: moduleName,
              layer: layerName,
              key: msgKey,
              value: msgValue
            })
          }
        })
      })
    })
    
    return results
  },
  
  /**
   * 打开消息监控面板
   */
  openMonitor() {
    messageManager.openMessageMonitor()
  },
  
  /**
   * 关闭消息监控面板
   */
  closeMonitor() {
    messageManager.closeMessageMonitor()
  },
  
  /**
   * 清除所有消息状态
   */
  clearAllStates() {
    messageManager.clearAllMessageStates()
  }
}

// 导出所有消息模块
export default {
  manager: messageManager,
  user: userMessages,
  tea: teaMessages,
  order: orderMessages,
  forum: forumMessages,
  shop: shopMessages,
  message: messageMessages,
  devHelper
} 