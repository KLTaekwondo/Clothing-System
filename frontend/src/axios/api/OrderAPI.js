import backendService from "../backendService.js";

const orderAPI = {
    completeOrder: (data) => backendService.post("/order/complete", data),
    draftOrder: (data) => backendService.post("/order/draft", data),
    updateOrder: (data) => backendService.post("/order/draft/update", data),
    deleteOrder: (id) => backendService.delete(`/order/delete/${id}`),
    searchOrder: (id) => backendService.get(`/order/search/${id}`),
    searchOrderPage: (page,size) => backendService.get("/order/page", {params:{page,size}}),
    searchCurrentDraftOrderPage: (page, size) => backendService.get("/order/search/wareHouse/draft", {params: {page, size}}),
    searchCurrentCompleteOrderPage: (page, size,startTime,endTime) => backendService.get("/order/search/wareHouse/complete", {params: {page, size,startTime,endTime}}),
}

export default orderAPI;
