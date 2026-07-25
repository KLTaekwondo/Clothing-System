import backendService from "../backendService.js";

const TransferOrderAPI = {
    createTransferOrder:(data) => backendService.post("/transfer-order/create", data),
    updateTransferOrder:(id,data) => backendService.put(`/transfer-order/update/${id}`, data),
    checkTransferOrder:(id) => backendService.put(`/transfer-order/check/${id}`),
    approveTransferOrder:(id) => backendService.put(`/transfer-order/approve/${id}`),
    rejectTransferOrder:(id) => backendService.put(`/transfer-order/reject/${id}`),
    deleteTransferOrder:(id) => backendService.delete(`/transfer-order/delete/${id}`),
    searchTransferOrder:(id) => backendService.get(`/transfer-order/search/${id}`),
    searchTransferOrderList:() => backendService.get("/transfer-order/search/list"),
}

export default TransferOrderAPI;