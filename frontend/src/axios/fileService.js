import axios from "axios";
import {useToastStore} from "../stores/toastStore.js";
import {useUserStore} from "../stores/userStore.js";
import router from "../router/router.js";

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

// ── 模块级工具（与拦截器分离） ──

// 解析 JSON 文本，失败返回 null
function parseResult(text) {
    try {
        return JSON.parse(text)
    } catch {
        return null
    }
}

// 错误响应体可能是 Blob（文件流默认响应类型）、字符串或对象，统一取出后端消息
async function readErrorBody(data) {
    if (!data) return ""
    if (typeof data === "string") return data.trim()
    if (typeof data.text === "function") {
        try {
            const text = (await data.text()).trim()
            const parsed = parseResult(text)
            return typeof parsed === "string" ? parsed : (parsed?.msg || text)
        } catch {
            return ""
        }
    }
    return data.msg || ""
}

fileService.interceptors.response.use(
    // 成功原样透传：下载要拿原始文件流，上传的解包由各自 interface 处理
    (response) => response,

    // 错误处理与 backendService 保持一致，避免文件链路绕过统一提示与 401
    async (error) => {
        const status = error.response?.status;
        const bodyMsg = await readErrorBody(error.response?.data);
        const toast = useToastStore();

        // 401：未登录 / token 过期 / 账号被踢下线 / 会话失效 → 清除登录态并回登录页
        if (status === 401) {
            const userStore = useUserStore();
            userStore.logout();
            router.push("/");
            toast.error(bodyMsg || "未登录或登录已失效，请重新登录");
        } else if (status === 403) {
            // 后端已语义化：403 只表示真正的权限不足
            toast.error("权限不足，请联系管理员");
        } else if (status === 500) {
            toast.error("服务器内部错误，请联系管理员");
        } else if (status === 422) {
            // 业务规则错误（RULE_* 系列，后端业务主通道）：展示后端具体消息
            toast.error(bodyMsg || "业务规则错误");
        } else if (bodyMsg) {
            // 其余错误码：展示后端中文消息
            toast.error(bodyMsg);
        } else if (error.message) {
            toast.error(error.message);
        }
        return Promise.reject(error);
    }
)

export default fileService;
