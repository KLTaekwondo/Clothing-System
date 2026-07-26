import backendService from "../backendService.js";

const stockRecordAPI = {
    searchStockRecordPage:(page,size) => backendService.get("/stock-record/page", {params:{page,size}}),
}

export default stockRecordAPI;