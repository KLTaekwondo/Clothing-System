import backendService from "../backendService.js";

const orderAPI = {
    completeOrder:(data) => backendService.post("/order/complete", data),
    draftOrder:(data) => backendService.post("/order/draft", data),
    refundOrder:(data) => backendService.post("/order/refund", data),
    searchOrder:(id) => backendService.get(`/order/search/${id}`),
    searchOrderList: () => backendService.get("/order/search/list"),
}
