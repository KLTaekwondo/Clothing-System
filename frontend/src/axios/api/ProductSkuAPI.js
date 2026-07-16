import backendService from "../backendService.js";

const productSkuAPI = {
    createProductSku: (data) => backendService.post("/productSku/create", data),
    updateProductSku: (id, data) => backendService.put(`/productSku/update/${id}`, data),
    deleteProductSku: (id) => backendService.delete(`/productSku/delete/${id}`),
    searchProductSku: (id) => backendService.get(`/productSku/search/${id}`),
    searchSkuListByProductId: (productId) => backendService.get(`/productSku/search/byProductId/${productId}`),
    scanProductSku: (code) => backendService.get(`/productSku/scan/${code}`),
}

export default productSkuAPI;