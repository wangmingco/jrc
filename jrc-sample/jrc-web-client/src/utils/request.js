import axios from 'axios'
import { Message, MessageBox } from 'element-ui'
import store from '../store'
// import _this from '../../main.js'

// import { getToken } from '@/utils/auth'

// 创建axios实例
const service = axios.create({
  timeout: 600000 // 请求超时时间
})

if (process.env.NODE_ENV === 'development') {
  service.defaults.baseURL = 'http://localhost:8070/'
  service.defaults.withCredentials = true
}

// request拦截器
service.interceptors.request.use(
  config => {
    // if (store.getters.token) {
    //   config.headers['NB-Token'] = getToken() // 让每个请求携带自定义token 请根据实际情况自行修改
    // }
    return config
  },
  error => {
    // Do something with request error
    console.log('网络请求发生异常: ' + error) // for debug
    Promise.reject(error)
  }
)

// response 拦截器
service.interceptors.response.use(
  response => {
    /**
     * code为非20000是抛错 可结合自己业务进行修改
     */
    const res = response.data
    if (!res) {
      Message({
        message: '后台应答为空',
        type: 'warning',
        duration: 3 * 1000
      })
      return
    }
    if (res.code === 1) {
      return response.data
    }
    
    Message({
      message: '后台发生错误, 应答信息 ->' + res.code + ' : ' + res.msg,
      type: 'warning',
      duration: 5 * 1000
    })

    // return Promise.reject('error')
    return response.data
  },
  error => {
    console.log('bad, 网络请求发生异常 -----> ' + error) // for debug
    if (error.message === 'Error: Network Error') {
      Message({
        message: '网络异常, 跳转到首页',
        type: 'warning',
        duration: 5 * 1000
      })
      console.log('网络异常, 开始重新登录1')
      this.$store.dispatch('LogOut').then(() => {
        location.reload() // 为了重新实例化vue-router对象 避免bug
      })
    } else {
      Message({
        message: error.message,
        type: 'warning',
        duration: 5 * 1000
      })
      console.log('网络异常, 开始重新登录2')
      this.$store.dispatch('LogOut').then(() => {
        location.reload() // 为了重新实例化vue-router对象 避免bug
      })
    }
    return Promise.reject(error)
  }
)

export default service
