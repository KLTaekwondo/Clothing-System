import axios from 'axios'

const http =axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 10000,
        headers:{
            'Content-Type': 'application/json',
        }
})

//请求拦截器
http.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

//响应拦截器
http.interceptors.response.use(
    (response) => {
        return response.data
    },
    (error) => {
        console.log('api响应错误', error)
        return Promise.reject(error)
    }
)

export default http