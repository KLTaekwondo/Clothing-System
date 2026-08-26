import wareHouseStockAPI from "../api/WareHouseStockAPI.js";

function wareHouseStockInterface() {
    // 搜索库存
    const searchStock = async (warehouseId, productId) => {
        const data = await wareHouseStockAPI.searchStock(warehouseId, productId);
        return Array.isArray(data) ? data : [];
    }

    // 分页查询某仓库的全部库存
    const searchStockPage = async (warehouseId, page, size) => {
        const data = await wareHouseStockAPI.searchStockPage(warehouseId, page, size);
        return data || null;
    }

    // 批量更新库存
    const batchUpdateStock = async (data) => {
        await wareHouseStockAPI.batchUpdateStock(data);
        return true;
    }
    
    return {
        searchStock,
        searchStockPage,
        batchUpdateStock,
    }
}

export default wareHouseStockInterface();