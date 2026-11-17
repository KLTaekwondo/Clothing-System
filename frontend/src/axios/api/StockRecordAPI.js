import backendService from "../backendService.js";

const stockRecordAPI = {
    searchStockRecordPage: (page, size, keyword) => backendService.get("/stock-record/page", {params: {page, size, keyword}}),
}

export default stockRecordAPI;