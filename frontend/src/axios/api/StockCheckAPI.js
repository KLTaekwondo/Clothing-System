import backendService from "../backendService.js";

const stockCheckAPI = {
    createStockCheck: (data) => backendService.post("/stockCheck/create", data),
    checkStockCheck: (id) => backendService.put(`/stockCheck/check/${id}`),
    approveStockCheck: (id) => backendService.put(`/stockCheck/approve/${id}`),
    rejectStockCheck: (id) => backendService.put(`/stockCheck/reject/${id}`),
    updateStockCheck: (id, data) => backendService.put(`/stockCheck/update/${id}`, data),
    searchStockCheck: (id) => backendService.get(`/stockCheck/detail/${id}`),
    searchStockCheckPage: (page, size) => backendService.get("/stockCheck/page", {
        params: {
            page,
            size
        }
    }),
    deleteStockCheck: (id) => backendService.delete(`/stockCheck/delete/${id}`),
}

export default stockCheckAPI;
