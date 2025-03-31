import axios from "axios";
import {Message} from 'element-ui';
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置NProgress
NProgress.configure({ showSpinner: false })
let loadingInstance = null
let requestCount = 0
function showLoading() {
    if (requestCount === 0) {
        NProgress.start()
    }
    requestCount++
}
function hideLoading() {
    requestCount--
    if (requestCount <= 0) {
        loadingInstance && loadingInstance.close()
        NProgress.done()
        requestCount = 0
    }
}

const http = axios.create({
    baseURL: process.env.VUE_APP_MODE === 'toBackend' ? '' : '/api',
    timeout: 60000
});

http.interceptors.request.use(
    (config) => {
        showLoading()
        return config;
    },
    (error) => {
        hideLoading()
        return Promise.reject(error)
    }
)
http.interceptors.response.use(
    (response) => {
        hideLoading()
        // 如果后端有自定义响应体, 则返回内层的业务数据
        const res = response.data
        if (res.code != "0") {
            Message.error(res.msg)
            return Promise.reject(res)
        }
        return Promise.resolve(res)
    },
    (error) => {
        hideLoading()
        console.error(error)
        Message.error(error.msg)
        // return Promise.reject(error)
    }
)

export default http;