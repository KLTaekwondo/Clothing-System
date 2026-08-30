import axios from "axios";
import {useToastStore} from "../stores/toastStore.js";
import {useUserStore} from "../stores/userStore.js";
import router from "../router/router.js";

const backendService = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 10000,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json",
    }
})

backendService.interceptors.request.use(
    (config) => config,
    (error) => Promise.reject(error)
)


backendService.interceptors.response.use(
    (response) => {
        const res = response.data;

        if (res.code === 200) {
            if (res.msg === "success") {
                return res.data;
            }

            if (res.msg) {
                const toast = useToastStore();
                toast.success(res.msg);
            }
            return res.data
        }

        if (res.msg) {
            const toast = useToastStore();
            toast.error(res.msg);
        }
        return Promise.reject()
    },

    (error) => {
        const status = error.response?.status;
        // 后端新异常处理：错误响应 = 真实 HTTP 状态码 + body 纯字符串（中文错误消息）
        const data = error.response?.data;
        const bodyMsg = typeof data === "string" && data.trim() ? data : (data && typeof data === "object" ? data.msg : "");
        const toast = useToastStore();

        // 401：未登录 / token 过期 / 账号被顶下线 → 清除登录态并回登录页
        if (status === 401) {
            const userStore = useUserStore();
            userStore.logout();
            router.push("/");
            toast.error(bodyMsg || "未登录或登录已失效，请重新登录");
        } else if (status === 403) {
            toast.error(bodyMsg || "权限不足，请联系管理员");
        } else if (status === 500) {
            toast.error(bodyMsg || "网络异常，请稍后重试");
        } else if (bodyMsg) {
            toast.error(bodyMsg);
        } else if (error.message) {
            toast.error(error.message);
        }
        return Promise.reject(error);
    }
)

export default backendService;
