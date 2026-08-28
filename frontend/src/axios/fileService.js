import axios from "axios";

// 文件流服务：独立于 backendService（JSON 接口），专门处理文件下载/上传
const fileService = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 60000,
    withCredentials: true,
    responseType: "blob",
    headers: {
        "Content-Type": "multipart/form-data",
    }
})

fileService.interceptors.request.use(
    (config) => config,
    (error) => Promise.reject(error)
)

fileService.interceptors.response.use(
    (response) => response,
    (error) => Promise.reject(error)
)

export default fileService;