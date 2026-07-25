import wareHouseStockAPI from "../api/WareHouseStockAPI.js";

function wareHouseStockInterface() {
    // 搜索库存
    const searchStock = async (warehouseId, productId) => {
        const data = await wareHouseStockAPI.searchStock(warehouseId, productId);
        return Array.isArray(data) ? data : [];
    }

    // 批量更新库存
    const batchUpdateStock = async (data) => {
        await wareHouseStockAPI.batchUpdateStock(data);
        return true;
    }
    
    return {
        searchStock,
        batchUpdateStock,
    }
}

export default wareHouseStockInterface();