import stockCheckAPI from "../api/StockCheckAPI.js";

function stockCheckInterface() {
    const create = async (data) => {
        await stockCheckAPI.createStockCheck(data);
        return true;
    }

    const check = async (id) => {
        await stockCheckAPI.checkStockCheck(id);
        return true;
    }

    const approve = async (id) => {
        await stockCheckAPI.approveStockCheck(id);
        return true;
    }

    const reject = async (id) => {
        await stockCheckAPI.rejectStockCheck(id);
        return true;
    }

    const update = async (id, data) => {
        await stockCheckAPI.updateStockCheck(id, data);
        return true;
    }

    const hardDelete = async (id) => {
        await stockCheckAPI.deleteStockCheck(id);
        return true;
    }

    const search = async (id) => {
        const data = await stockCheckAPI.searchStockCheck(id);
        return data || {};
    }

    const searchPage = async (page = 0, size = 10) => {
        const data = await stockCheckAPI.searchStockCheckPage(page, size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page,
            size
        };
    }

    return {
        create,
        check,
        approve,
        reject,
        update,
        hardDelete,
        search,
        searchPage
    }
}

export default stockCheckInterface();
