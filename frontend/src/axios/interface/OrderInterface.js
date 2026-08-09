import orderAPI from "../api/OrderAPI.js";

const Now = new Date().setHours(0, 0, 0, 0);
const NowEnd = new Date().setHours(24, 59, 59, 999);
function orderInterface() {
    const complete = async (data) => {
        await orderAPI.completeOrder(data);
        return true;
    }

    const draft = async (data) => {
        await orderAPI.draftOrder(data);
        return true;
    }

    const update = async (data) => {
        await orderAPI.updateOrder(data);
        return true;
    }

    const hardDelete = async (id) => {
        await orderAPI.deleteOrder(id);
        return true;
    }

    const search = async (id) => {
        const data = await orderAPI.searchOrder(id);
        return data || {};
    }

    const searchPage = async (page = 0, size = 10) => {
        const data = await orderAPI.searchOrderPage(page, size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page: page,
            size,
        };
    }

    const searchCurrentDraftPage = async (page = 0, size = 10) => {
        const data = await orderAPI.searchCurrentDraftOrderPage(page, size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page,
            size,
        };
    }

    const searchCurrentCompletePage = async (page = 0, size = 10,startTime = Now,endTime = NowEnd) => {
        const data = await orderAPI.searchCurrentCompleteOrderPage(page, size,startTime,endTime);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page,
            size,
        };
    }

    return {
        complete,
        draft,
        update,
        hardDelete,
        search,
        searchPage,
        searchCurrentDraftPage,
        searchCurrentCompletePage,
    }
}

export default orderInterface();
