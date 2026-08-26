import backendService from "../backendService.js";

const wareHouseStockAPI = {
    searchStock: (warehouseId, productId) => backendService.get(`/stock/search/${warehouseId}/${productId}`),
    searchStockPage: (warehouseId, page, size) => backendService.get('/stock/page', {params: {warehouseId: warehouseId, page: page, size: size}}),
    batchUpdateStock: (data) => backendService.put("/stock/batch-update", data),
}
export default wareHouseStockAPI;
