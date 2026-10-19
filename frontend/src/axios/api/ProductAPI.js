import backendService from "../backendService.js";
import fileService from "../fileService.js";

const productAPI = {
    createProduct: (data) => backendService.post("/product/create", data),
    updateProduct: (id, data) => backendService.put(`/product/update/${id}`, data),
    deleteProduct: (id) => backendService.delete(`/product/delete/${id}`),
    searchProduct: (id) => backendService.get(`/product/search/${id}`),
    searchProductPage: (page,size) => backendService.get("/product/page", {params:{page,size}}),
    // 上传商品图片：multipart/form-data，字段名 file
    // 走 fileService（multipart + 文件流），backendService 的 JSON 头会把 FormData 序列化成 JSON
    uploadProductImage: (id, file) => {
        const formData = new FormData();
        formData.append("file", file);
        return fileService.post(`/product/upload/${id}`, formData);
    },
}

export default productAPI;
