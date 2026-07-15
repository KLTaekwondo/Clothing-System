import productSkuAPI from "../api/ProductSkuAPI.js";

function productSkuInterface(){
    // 创建商品SKU
    const create = async (data) =>{
        await productSkuAPI.createProductSku(data);
        return true;
    }

    // 更新商品SKU
    const update = async (id, data) =>{
        await productSkuAPI.updateProductSku(id, data);
        return true;
    }

    // 软删除商品SKU
    const softDelete = async (id) =>{
        await productSkuAPI.deleteProductSku(id);
        return true;
    }

    // 搜索商品SKU
    const search = async (id) =>{
        const {data} = await productSkuAPI.searchProductSku(id);
        return data || {};
    }

    // 根据商品ID搜索商品SKU列表
    const searchListByProductId = async (productId) =>{
        const {data} = await productSkuAPI.searchSkuListByProductId(productId);
        return Array.isArray(data) ? data : [];
    }

    // 扫描商品SKU
    const scan = async (code) =>{
        const {data} = await productSkuAPI.scanProductSku(code);
        return data || {};
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchListByProductId,
        scan,
    }
}

export default productSkuInterface();
