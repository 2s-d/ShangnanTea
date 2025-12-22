/**
 * 消息工具索引文件
 * 统一导出所有模块的消息工具
 * 
 * 消息分类说明：
 * - SUCCESS: 操作成功反馈（登录成功、保存成功等）
 * - ERROR: 操作失败/错误提示（登录失败、权限不足等）
 * - PROMPT: 用户提示（表单验证+确认提示）
 * 
 * 使用示例：
 * import userMessages from '@/utils/userMessages'
 * userMessages.success.showLoginSuccess()
 * userMessages.error.showLoginFailure('密码错误')
 * userMessages.prompt.showUsernameRequired()
 */

import messageManager from './messageManager'
import userMessages from './userMessages'
import teaMessages from './teaMessages'
import orderMessages from './orderMessages'
import forumMessages from './forumMessages'
import shopMessages from './shopMessages'
import messageMessages from './messageMessages'
import commonMessages from './commonMessages'

// 统一导出所有消息常量
export const MESSAGE_CONSTANTS = {
  USER: userMessages.USER_MESSAGES,
  TEA: teaMessages.TEA_MESSAGES,
  ORDER: orderMessages.ORDER_MESSAGES,
  FORUM: forumMessages.FORUM_MESSAGES,
  SHOP: shopMessages.SHOP_MESSAGES,
  MESSAGE: messageMessages.MESSAGE_MESSAGES,
  COMMON: commonMessages.COMMON_MESSAGES
}

// 开发辅助函数
const devHelper = {
  /**
   * 显示所有可用的消息常量统计
   */
  showAllConstants() {
    console.table({
      'USER.SUCCESS': Object.keys(MESSAGE_CONSTANTS.USER.SUCCESS).length,
      'USER.ERROR': Object.keys(MESSAGE_CONSTANTS.USER.ERROR).length,
      'USER.PROMPT': Object.keys(MESSAGE_CONSTANTS.USER.PROMPT).length,
      'TEA.SUCCESS': Object.keys(MESSAGE_CONSTANTS.TEA.SUCCESS).length,
      'TEA.ERROR': Object.keys(MESSAGE_CONSTANTS.TEA.ERROR).length,
      'TEA.PROMPT': Object.keys(MESSAGE_CONSTANTS.TEA.PROMPT).length,
      'ORDER.SUCCESS': Object.keys(MESSAGE_CONSTANTS.ORDER.SUCCESS).length,
      'ORDER.ERROR': Object.keys(MESSAGE_CONSTANTS.ORDER.ERROR).length,
      'ORDER.PROMPT': Object.keys(MESSAGE_CONSTANTS.ORDER.PROMPT).length,
      'FORUM.SUCCESS': Object.keys(MESSAGE_CONSTANTS.FORUM.SUCCESS).length,
      'FORUM.ERROR': Object.keys(MESSAGE_CONSTANTS.FORUM.ERROR).length,
      'FORUM.PROMPT': Object.keys(MESSAGE_CONSTANTS.FORUM.PROMPT).length,
      'SHOP.SUCCESS': Object.keys(MESSAGE_CONSTANTS.SHOP.SUCCESS).length,
      'SHOP.ERROR': Object.keys(MESSAGE_CONSTANTS.SHOP.ERROR).length,
      'SHOP.PROMPT': Object.keys(MESSAGE_CONSTANTS.SHOP.PROMPT).length,
      'MESSAGE.SUCCESS': Object.keys(MESSAGE_CONSTANTS.MESSAGE.SUCCESS).length,
      'MESSAGE.ERROR': Object.keys(MESSAGE_CONSTANTS.MESSAGE.ERROR).length,
      'MESSAGE.PROMPT': Object.keys(MESSAGE_CONSTANTS.MESSAGE.PROMPT).length,
      'COMMON.SUCCESS': Object.keys(MESSAGE_CONSTANTS.COMMON.SUCCESS).length,
      'COMMON.ERROR': Object.keys(MESSAGE_CONSTANTS.COMMON.ERROR).length,
      'COMMON.PROMPT': Object.keys(MESSAGE_CONSTANTS.COMMON.PROMPT).length
    })
    return MESSAGE_CONSTANTS
  },
  
  /**
   * 查找指定消息文本对应的常量键
   */
  findMessageByText(text) {
    if (!text) return []
    const results = []
    Object.entries(MESSAGE_CONSTANTS).forEach(([moduleName, moduleMessages]) => {
      Object.entries(moduleMessages).forEach(([layerName, layerMessages]) => {
        Object.entries(layerMessages).forEach(([msgKey, msgValue]) => {
          if (msgValue.includes(text)) {
            results.push({ module: moduleName, layer: layerName, key: msgKey, value: msgValue })
          }
        })
      })
    })
    return results
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
  common: commonMessages,
  devHelper
}
