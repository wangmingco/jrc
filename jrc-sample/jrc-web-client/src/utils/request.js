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
    if (res.code === 20000) {
      return response.data
    }
    console.error('http 响应失败. ', response)
    if (res.code === 10001) {
      Message({
        message: '请重新登录',
        type: 'warning',
        duration: 3 * 1000
      })
      // return response.data
      // _this.$router.push({ path: '/#/login' })
      this.$store.dispatch('LogOut').then(() => {
        location.reload() // 为了重新实例化vue-router对象 避免bug
      })
      // return Promise.reject(res.msg)
      return
    }

    Message({
      message: res.code + ':' + res.msg,
      type: 'warning',
      duration: 5 * 1000
    })

    // 50008:非法的token; 50012:其他客户端登录了;  50014:Token 过期了;
    if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
      MessageBox.confirm(
        '你已被登出，可以取消继续留在该页面，或者重新登录',
        '确定登出',
        {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        store.dispatch('FedLogOut').then(() => {
          location.reload() // 为了重新实例化vue-router对象 避免bug
        })
      })
    }
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
