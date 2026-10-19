// 后端图片访问路径（access.path = /images/）挂在服务器根下，不在 /api 前缀内
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

// 服务器源：http://localhost:8080/api → http://localhost:8080
const SERVER_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, '')

// 后端返回的图片相对路径 → 可直接给 img src 的完整地址
export function toImageUrl(imgUrl) {
    if (!imgUrl) return ''
    if (/^https?:\/\//i.test(imgUrl)) return imgUrl
    const path = imgUrl.startsWith('/') ? imgUrl : '/' + imgUrl
    return SERVER_ORIGIN + path
}

// 单张图片大小上限，与后端 spring.servlet.multipart.max-file-size=5MB 保持一致
export const IMAGE_MAX_SIZE = 5 * 1024 * 1024

// 允许的图片格式，与后端 FileUploadUtil.ALLOWED 保持一致
export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp'
