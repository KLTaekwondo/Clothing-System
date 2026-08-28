import fileService from "../fileService.js";

const exportAPI = {
    // 期间订单总计 CSV：from/to 为 yyyy-MM-dd
    exportBetween: (from, to) => fileService.get("/export/export-between", {params: {from: from, to: to}}),
    // 商品列表 CSV
    exportProducts: () => fileService.get("/export/products"),
}

export default exportAPI;