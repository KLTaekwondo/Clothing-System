import StockRecordAPI from "../api/StockRecordAPI.js";
import stockRecordAPI from "../api/StockRecordAPI.js";

function StockRecordInterface(){
    const searchPage =async (page,size) =>{
        const data = await StockRecordAPI.searchStockRecordPage(page,size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            number: page,
            size,
        };
    }

    return {
        searchPage,
    }
}

export default StockRecordInterface();
