/**
 * 表单验证的组合式函数
 * 提供常用的表单验证规则和方法
 */
import { reactive } from 'vue'
import { commonPromptMessages } from '@/utils/promptMessages'
import { showByCode } from '@/utils/apiMessages'

/**
 * 使用表单验证
 * @returns {Object} 表单验证相关的方法和规则
 */
export function useFormValidation() {
  /**
   * 验证邮箱格式
   * @param {string} email 邮箱地址
   * @returns {boolean} 是否是有效的邮箱
   */
  const isValidEmail = (email) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    return emailRegex.test(email)
  }
  
  /**
   * 验证手机号格式
   * @param {string} phone 手机号
   * @returns {boolean} 是否是有效的手机号
   */
  const isValidPhone = (phone) => {
    const phoneRegex = /^1[3-9]\d{9}$/
    return phoneRegex.test(phone)
  }
  
  /**
   * 验证密码强度
   * @param {string} password 密码
   * @returns {boolean} 是否是强密码
   */
  const isStrongPassword = (password) => {
    // 至少8位，包含大小写字母和数字
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/
    return passwordRegex.test(password)
  }
  
  /**
   * 生成确认密码验证函数
   * @param {Object} form 表单对象
   * @param {string} passwordField 密码字段名
   * @returns {Function} 验证函数
   */
  const confirmPasswordValidator = (form, passwordField = 'password') => {
    return (rule, value, callback) => {
      if (value !== form[passwordField]) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
  }
  
  /**
   * 常用的表单验证规则
   */
  const rules = {
    required: (message = '该字段不能为空') => ({
      required: true,
      message,
      trigger: 'blur'
    }),
    
    email: (message = '请输入有效的邮箱地址') => ({
      validator: (rule, value, callback) => {
        if (value && !isValidEmail(value)) {
          callback(new Error(message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }),
    
    phone: (message = '请输入有效的手机号') => ({
      validator: (rule, value, callback) => {
        if (value && !isValidPhone(value)) {
          callback(new Error(message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }),
    
    password: (message = '密码至少8位，包含大小写字母和数字') => ({
      validator: (rule, value, callback) => {
        if (value && !isStrongPassword(value)) {
          callback(new Error(message))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }),
    
    minLength: (length, message) => ({
      min: length,
      message: message || `最少输入${length}个字符`,
      trigger: 'blur'
    }),
    
    maxLength: (length, message) => ({
      max: length,
      message: message || `最多输入${length}个字符`,
      trigger: 'blur'
    })
  }
  
  /**
   * 提交表单验证
   * @param {Object} formRef 表单引用
   * @param {Function} onValid 验证通过回调
   */
  const validateForm = async (formRef, onValid) => {
    if (!formRef) {
      // 表单引用不存在，使用通用错误提示
      showByCode(1100)
      return false
    }
    
    try {
      await formRef.validate()
      if (typeof onValid === 'function') {
        onValid()
      }
      return true
    } catch (error) {
      console.error('表单验证失败:', error)
      return false
    }
  }
  
  /**
   * 重置表单
   * @param {Object} formRef 表单引用
   */
  const resetForm = (formRef) => {
    if (formRef) {
      formRef.resetFields()
    }
  }
  
  return {
    isValidEmail,
    isValidPhone,
    isStrongPassword,
    confirmPasswordValidator,
    rules,
    validateForm,
    resetForm
  }
} 