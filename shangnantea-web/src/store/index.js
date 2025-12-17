import { createStore } from 'vuex'
import user from './modules/user'
import tea from './modules/tea'
import shop from './modules/shop'
import order from './modules/order'
import forum from './modules/forum'
import message from './modules/message'

export default createStore({
  modules: {
    user,
    tea,
    shop,
    order,
    forum,
    message
  }
}) 