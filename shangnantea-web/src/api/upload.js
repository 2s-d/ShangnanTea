import request from './index'
import { API } from './apiConstants'

/**
 * 上传文件
 * @param {File} file 要上传的文件
 * @param {string} type 文件类型('avatar', 'product', 'shop')
 * @returns {Promise} 上传结果，包含文件URL
 */
export function uploadFile(file, type = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  
  return request({
    url: API.SYSTEM.UPLOAD,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传图片
 * @param {File} file 图片文件
 * @param {string} type 图片类型
 * @returns {Promise} 上传结果
 */
export function uploadImage(file, type = 'common') {
  return uploadFile(file, type)
} 