import backendService from "../backendService.js";

const stockRecordAPI = {
    searchStockRecordPage:(page,size) => backendService.get("/stock-record/page", {pramas:{page,size}}),
}

export default stockRecordAPI;