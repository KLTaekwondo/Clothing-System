import backendService from "../backendService.js";

const orderAPI = {
    completeOrder: (data) => backendService.post("/order/complete", data),
    draftOrder: (data) => backendService.post("/order/draft", data),
    updateOrder: (data) => backendService.post("/order/draft/update", data),
    deleteOrder: (id) => backendService.delete(`/order/delete/${id}`),
    searchOrder: (id) => backendService.get(`/order/search/${id}`),
    countOrderByStatus: () => backendService.get("/order/count"),
    searchOrderPage: (page, size, keyword, status) => backendService.get("/order/page", {params: {page, size, keyword, status}}),
    searchCurrentDraftOrderPage: (page, size, keyword) => backendService.get("/order/search/wareHouse/draft", {params: {page, size, keyword}}),
    searchCurrentCompleteOrderPage: (page, size, startTime, endTime, employeeId, keyword) => backendService.get("/order/search/wareHouse/complete", {params: {page, size, startTime, endTime, employeeId, keyword}}),
}

export default orderAPI;
