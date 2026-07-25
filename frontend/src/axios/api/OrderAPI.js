import backendService from "../backendService.js";

const orderAPI = {
    completeOrder: (data) => backendService.post("/order/complete", data),
    draftOrder: (data) => backendService.post("/order/draft", data),
    updateOrder: (data) => backendService.post("/order/draft/update", data),
    deleteOrder: (id) => backendService.delete(`/order/delete/${id}`),
    searchOrder: (id) => backendService.get(`/order/search/${id}`),
    searchOrderList: () => backendService.get("/order/search/list"),
}

export default orderAPI;
