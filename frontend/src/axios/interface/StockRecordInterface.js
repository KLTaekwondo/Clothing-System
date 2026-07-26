import StockRecordAPI from "../api/StockRecordAPI.js";

function StockRecordInterface(){
    const searchPage =async (page = 0,size = 10) =>{
        const data = await StockRecordAPI.searchStockRecordPage(page,size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page: page,
            size,
        };
    }

    return {
        searchPage,
    }
}

export default StockRecordInterface();
