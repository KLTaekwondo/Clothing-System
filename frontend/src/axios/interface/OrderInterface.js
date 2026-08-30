import orderAPI from "../api/OrderAPI.js";

// 生成 LocalDateTime 可解析的 ISO 字符串（yyyy-MM-ddTHH:mm:ss）
function formatDateTime(date, endOfDay) {
    const pad = n => String(n).padStart(2, '0')
    const datePart = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
    return `${datePart}T${endOfDay ? '23:59:59' : '00:00:00'}`
}

// 默认查询范围：今天
const Now = formatDateTime(new Date(), false)
const NowEnd = formatDateTime(new Date(), true)
function orderInterface() {
    const complete = async (data) => {
        const result = await orderAPI.completeOrder(data);
        return result;
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

    const searchCurrentCompletePage = async (page = 0, size = 10, startTime = Now, endTime = NowEnd, employeeId = null) => {
        const data = await orderAPI.searchCurrentCompleteOrderPage(page, size, startTime, endTime, employeeId);
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
