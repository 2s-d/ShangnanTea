/**
 * API模块入口，按照六大模块导出所有API
 */

// 导入六大模块API
import * as userApi from './user'
import * as teaApi from './tea'
import * as shopApi from './shop'
import * as orderApi from './order'
import * as forumApi from './forum'
import * as messageApi from './message'

// 导入通用API
import * as uploadApi from './upload'

// 导出所有API模块
export {
  userApi,
  teaApi,
  shopApi,
  orderApi,
  forumApi,
  messageApi,
  uploadApi
}

export default {
  user: userApi,
  tea: teaApi,
  shop: shopApi,
  order: orderApi,
  forum: forumApi,
  message: messageApi,
  upload: uploadApi
} 

// 如果需要分别单独引入各 API 模块也可用按需导出：
// export { userApi, teaApi, shopApi, orderApi, forumApi, messageApi, uploadApi }
