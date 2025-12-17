import permissionDirective from './permission'

/**
 * 集中注册自定义指令
 */
export default {
  install(app) {
    // 注册权限指令 v-permission
    app.directive('permission', permissionDirective)
    
    // 未来可以在这里注册更多指令
    // app.directive('focus', focusDirective)
    // app.directive('click-outside', clickOutsideDirective)
  }
} 