import backendService from "../backendService.js";

const wareHouseStockAPI = {
    searchStock: (warehouseId, productId) => backendService.get(`/stock/search/${warehouseId}/${productId}`),
    batchUpdateStock: (data) => backendService.put("/stock/batch-update", data),
    transferStock: (data) => backendService.put("/stock/transfer", data),
}
export default wareHouseStockAPI;
