import backendService from "../backendService.js";

const importOrderAPI = {
    createImportOrder: (data) => backendService.post("/import-order/create", data),
    updateImportOrder: (id,data) => backendService.put(`/import-order/update/${id}`, data),
    checkImportOrder: (id) => backendService.put(`/import-order/check/${id}`),
    approveImportOrder: (id) => backendService.put(`/import-order/approve/${id}`),
    rejectImportOrder: (id) => backendService.put(`/import-order/reject/${id}`),
    searchImportOrder: (id) => backendService.get(`/import-order/search/${id}`),
    searchImportOrderList: () => backendService.get("/import-order/search/list"),
    deleteImportOrder: (id) => backendService.delete(`/import-order/delete/${id}`),
}

export default importOrderAPI;
