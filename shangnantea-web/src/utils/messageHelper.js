/**
 * 消息工具助手
 * 提供统一的消息处理方法，避免重复提示问题
 */
import { message } from '@/components/common'
import messageManager from '@/utils/messageManager'

/**
 * 标准消息类型
 * @type {Object}
 */
export const MESSAGE_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  INFO: 'info',
  WARNING: 'warning'
}

/**
 * 按操作类型的标准消息
 * @type {Object}
 */
export const STANDARD_MESSAGES = {
  // 认证相关
  LOGIN_SUCCESS: '登录成功',
  LOGIN_FAILED: '登录失败',
  LOGOUT_SUCCESS: '退出登录成功',
  REGISTER_SUCCESS: '注册成功，请使用您的账号密码登录！',
  
  // 数据操作
  SAVE_SUCCESS: '保存成功',
  SAVE_FAILED: '保存失败',
  UPDATE_SUCCESS: '更新成功',
  UPDATE_FAILED: '更新失败',
  DELETE_SUCCESS: '删除成功',
  DELETE_FAILED: '删除失败',
  UPLOAD_SUCCESS: '上传成功',
  UPLOAD_FAILED: '上传失败',
  
  // 购物相关
  ADD_TO_CART_SUCCESS: '已加入购物车',
  REMOVE_FROM_CART_SUCCESS: '商品已从购物车中移除',
  CLEAR_CART_SUCCESS: '购物车已清空',
  
  // 表单相关
  FORM_VALIDATE_FAILED: '表单验证失败，请检查输入',
  
  // 支付相关
  PAYMENT_SUCCESS: '支付成功',
  PAYMENT_FAILED: '支付失败'
}

/**
 * 显示标准消息
 * @param {string} messageKey 消息键
 * @param {string} type 消息类型
 * @param {string} customMessage 自定义消息内容(可选)
 */
export function showStandardMessage(messageKey, type = MESSAGE_TYPES.INFO, customMessage = null) {
  const content = customMessage || STANDARD_MESSAGES[messageKey]
  if (!content) {
    console.warn(`未找到消息键: ${messageKey}`);
    return;
  }
  
  switch (type) {
    case MESSAGE_TYPES.SUCCESS:
      message.success(content);
      break;
    case MESSAGE_TYPES.ERROR:
      message.error(content);
      break;
    case MESSAGE_TYPES.WARNING:
      message.warning(content);
      break;
    case MESSAGE_TYPES.INFO:
    default:
      message.info(content);
      break;
  }
}

/**
 * 处理异步操作并显示相应消息
 * @param {Promise} promise 要执行的异步操作
 * @param {Object} options 配置选项
 * @returns {Promise} 处理后的Promise
 */
export async function handleAsyncOperation(promise, options = {}) {
  const {
    successMessage,
    errorMessage,
    showSuccessMessage = true,
    showErrorMessage = true,
    successCallback = null,
    errorCallback = null
  } = options;
  
  try {
    const result = await promise;
    
    if (showSuccessMessage && successMessage) {
      message.success(successMessage);
    }
    
    if (successCallback) {
      successCallback(result);
    }
    
    return result;
  } catch (error) {
    console.error('操作失败:', error);
    
    const errorContent = errorMessage || error.message || '操作失败';
    
    if (showErrorMessage) {
      message.error(errorContent);
    }
    
    if (errorCallback) {
      errorCallback(error);
    }
    
    return Promise.reject(error);
  }
}

export default {
  show: message,
  showStandard: showStandardMessage,
  handleAsync: handleAsyncOperation,
  types: MESSAGE_TYPES,
  messages: STANDARD_MESSAGES
} 