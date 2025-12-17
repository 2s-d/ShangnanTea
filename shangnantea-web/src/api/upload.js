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
    url: '/upload',
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

/**
 * 上传多个文件
 * @param {Array} files 文件数组
 * @param {string} type 文件类型
 * @returns {Promise} 上传结果
 */
export function uploadMultipleFiles(files, type = 'common') {
  const formData = new FormData()
  
  files.forEach((file, index) => {
    formData.append(`files[${index}]`, file)
  })
  
  formData.append('type', type)
  
  return request({
    url: '/upload/multiple',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 