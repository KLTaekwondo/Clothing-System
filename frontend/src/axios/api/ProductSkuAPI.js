import backendService from "../backendService.js";

const productSkuAPI = {
    createProductSku:(data) => backendService.post("/product/sku/create", data),
    updateProductSku:(id, data) => backendService.put(`/product/sku/update/${id}`, data),
    deleteProductSku:(id) => backendService.delete(`/product/sku/delete/${id}`),
    searchProductSku:(id) => backendService.get(`/product/sku/search/${id}`),
    searchSkuListByProductId: (productId) => backendService.get(`/product/sku/search/list/${productId}`),
    scanProductSku:(code) => backendService.get(`/product/sku/scan/${code}`),
}