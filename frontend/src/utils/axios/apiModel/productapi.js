import { getActivePinia } from 'pinia'
import http from '../axios.js'

export const productapi = {
    //1.查询模块
    //1.1根据产品Id查找商品
    getProductById: async (productId) =>{
        return http.get(`/products/getById/${productId}`)
    },
    
    //1.2根据产品代码查找产品
    getProductByProductCode: async (productCode) =>{
        return http.get(`/products/getByCode/${productCode}`)
    },

    //1.3根据产品名称查找产品
    getProductByProductName: async (productName) =>{
        return http.get(`/products/getByName/${productName}`)
    },

    //1.4根据产品类别查找产品
    getProductByProductCategory: async (productCategory) =>{
        return http.get(`/products/getByCategory/${productCategory}`)
    },

    //1.5根据产品具体类别查找产品
    getProductByProductSubCategory: async (productSubCategory) =>{
        return http.get(`/products/getBySubCategory/${productSubCategory}`)
    },

    //1.6根据产品季节查找产品
    getProductByProductSeason: async (productSeason) =>{
        return http.get(`/products/getBySeason/${productSeason}`)
    },

    //1.7根据产品年份查找产品
    getProductByProductYear: async (productYear) =>{
        return http.get(`/products/getByYear/${productYear}`)
    },

    //1.8根据产品状态查找产品
    getProductByProductStatus: async (productStatus) =>{
        return http.get(`/products/getByStatus/${productStatus}`)
    },

    //1.9根据是否特价查找产品
    getProductByIsSpecialProduct: async (isSpecialProduct) =>{
        return http.get(`/products/getByIsSpecial/${isSpecialProduct}`)
    },

    //1.10获取所有产品
    getAllProducts: async () =>{
        return http.get(`/products/getAll`)
    },

    //2.添加模块
    //2.1添加产品
    addProduct: async (data) =>{
        return http.post(`/products/addProduct`,data)
    },

    //3.删除模块
    //3.1根据产品Id删除产品
    deleteProduct: async (productId) =>{
        return http.delete(`/products/deleteProduct/${productId}`)
    },

    //4.更新模块
    //4.1根据产品Id更新产品
    updateProduct: async (productId,data) =>{
        return http.put(`/products/updateProduct/${productId}`,data)
    },
}

export default productapi