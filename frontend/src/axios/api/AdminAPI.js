import backendService from "../backendService.js";


const adminAPI = {
    login:(data) => backendService.post("/admin/login", data),
    logout:() => backendService.post("/admin/logout"),
    resetPassword:(data) => backendService.put("/admin/reset-password", data),
}

export default adminAPI;
