import axios from "axios";
import {Message} from 'element-ui';

const http = axios.create({
    baseURL: process.env.VUE_APP_MODE === 'toBackend' ? '' : '/api',
    timeout: 10000
});

http.interceptors.request.use(
    (config) => {
        return config;
    },
    (error) => {
        return Promise.reject(error)
    }
)
http.interceptors.response.use(
    (response) => {
        // 如果后端有自定义响应体, 则返回内层的业务数据
        const res = response.data
        if (res.code != "0") {
            Message.error(res.msg)
            return Promise.reject(res)
        }
        return Promise.resolve(res)
    },
    (error) => {
        console.error(error)
        Message.error(error.msg)
        // return Promise.reject(error)
    }
)

export default http;