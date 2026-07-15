import axios from "axios";
import { useToastStore } from "../stores/toastStore.js";

// 启动http携带Cookie
axios.default.withCredentials = true;

const backendService = axios.create({
    baseURL: "http://localhost:8080/api",
    timeout: 10000,
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
        const msg = error?.data?.msg || error.message;

        const toast = useToastStore();
        if(status === 500){
            toast.error("网络异常，请稍后重试");
        }
        return Promise.reject(error);
    }
)

export default backendService;
