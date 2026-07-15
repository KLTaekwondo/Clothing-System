import axios from "axios";
import { useToastStore } from "../stores/toastStore.js";

const backendService = axios.create({
    baseURL: "http://localhost:8080/api",
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

        if(res.code === 200) {
            if(res.msg === "success") {
                return res.data;
            }

            if(res.msg){
                const toast = useToastStore();
                toast.success(res.msg);
            }
            return res.data
        }

        if(res.msg){
            const toast = useToastStore();
            toast.error(res.msg);
        }
        return Promise.reject()
    },

    (error) => {
        const status = error.response?.status;
        const msg = error.response?.data?.msg || error.message;

        const toast = useToastStore();
        if (status === 403) {
            toast.error("登录已过期，请重新登录");
        } else if (status === 500) {
            toast.error("网络异常，请稍后重试");
        } else if (msg) {
            toast.error(msg);
        }
        return Promise.reject(error);
    }
)

export default backendService;
