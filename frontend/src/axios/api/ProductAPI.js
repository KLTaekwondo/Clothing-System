import backendService from "../backendService.js";

const productAPI = {
    createProduct: (data) => backendService.post("/product/create", data),
    updateProduct: (id, data) => backendService.put(`/product/update/${id}`, data),
    deleteProduct: (id) => backendService.delete(`/product/delete/${id}`),
    searchProduct: (id) => backendService.get(`/product/search/${id}`),
    searchProductPage: (page,size) => backendService.get("/product/page", {params:{page,size}}),
}

export default productAPI;