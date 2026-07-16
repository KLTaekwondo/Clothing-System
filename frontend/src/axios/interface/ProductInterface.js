import productAPI from "../api/ProductAPI.js";

function productInterface() {
    // 创建商品
    const create = async (data) => {
        await productAPI.createProduct(data);
        return true;
    }

    // 更新商品
    const update = async (id, data) => {
        await productAPI.updateProduct(id, data);
        return true;
    }

    // 软删除商品
    const softDelete = async (id) => {
        await productAPI.deleteProduct(id);
        return true;
    }

    // 搜索商品
    const search = async (id) => {
        const data = await productAPI.searchProduct(id);
        return data || {};
    }

    // 搜索商品列表
    const searchList = async () => {
        const data = await productAPI.searchProductList();
        return Array.isArray(data) ? data : [];
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
    }
}

export default productInterface();
