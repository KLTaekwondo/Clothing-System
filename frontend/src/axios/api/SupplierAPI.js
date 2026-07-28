import backendService from "../backendService.js";

const supplierAPI = {
    createSupplier: (data) => backendService.post("/supplier/create", data),
    updateSupplier: (id, data) => backendService.put(`/supplier/update/${id}`, data),
    deleteSupplier: (id) => backendService.delete(`/supplier/delete/${id}`),
    searchSupplier: (code) => backendService.get(`/supplier/search/${code}`),
    searchSupplierList: () => backendService.get("/supplier/search/list"),
}

export default supplierAPI;
