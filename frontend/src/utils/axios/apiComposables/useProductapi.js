import productapi from '../apiModel/productapi.js'

export function useProductapi(){
    //1.查询模块
    //1.1根据产品Id查找产品
    getById: async (productId) =>{
        return productapi.getProductById(productId)
    };
    //1.2根据产品代码查找产品
    getByCode: async (productCode) =>{
        return productapi.getProductByProductCode(productCode)
    };
    //1.3根据产品名称查找产品
    getByName: async (productName) =>{
        return productapi.getProductByProductName(productName)
    };
    //1.4根据产品类别查找产品
    getByCategory: async (productCategory) =>{
        return productapi.getProductByProductCategory(productCategory)
    };
    //1.5根据产品具体类别查找产品
    getBySubCategory: async (productSubCategory) =>{
        return productapi.getProductByProductSubCategory(productSubCategory)
    };
    //1.6根据产品季节查找产品
    getBySeason: async (productSeason) =>{
        return productapi.getProductByProductSeason(productSeason)
    };
    //1.7根据产品年份查找产品
    getByYear: async (productYear) =>{
        return productapi.getProductByProductYear(productYear)
    };
    //1.8根据产品状态查找产品
    getByStatus: async (productStatus) =>{
        return productapi.getProductByProductStatus(productStatus)
    };
    //1.9根据是否特价查找产品
    getByIsSpecialProduct: async (isSpecialProduct) =>{
        return productapi.getProductByIsSpecialProduct(isSpecialProduct)
    };
    //1.10获取所有产品
    getAllProducts: async () =>{
        return productapi.getAllProducts()
    };
    //2.添加模块
    //2.1添加产品
    addProduct: async (data) =>{
        return productapi.addProduct(data)
    };
    //3.删除模块
    //3.1根据产品Id删除产品
    deleteProduct: async (productId) =>{
        return productapi.deleteProduct(productId)
    };
    //4.更新模块
    //4.1根据产品Id更新产品
    updateProduct: async (productId,data) =>{
        return productapi.updateProduct(productId,data)
    };

    return{
        getById,
        getByCode,
        getByName,
        getByCategory,
        getBySubCategory,
        getBySeason,
        getByYear,
        getByStatus,
        getByIsSpecialProduct,
        getAllProducts,
        addProduct,
        deleteProduct,
        updateProduct
    }
}
