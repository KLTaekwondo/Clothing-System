import transferOrderAPI from "../api/TransferOrderAPI.js";

function transferOrderInterface() {
    const create = async (data) => {
        await transferOrderAPI.createTransferOrder(data);
        return true;
    }

    const update = async (id, data) => {
        await transferOrderAPI.updateTransferOrder(id, data);
        return true;
    }

    const check = async (id) => {
        await transferOrderAPI.checkTransferOrder(id);
        return true;
    }

    const approve = async (id) => {
        await transferOrderAPI.approveTransferOrder(id);
        return true;
    }

    const reject = async (id) => {
        await transferOrderAPI.rejectTransferOrder(id);
        return true;
    }

    const hardDelete = async (id) => {
        await transferOrderAPI.deleteTransferOrder(id);
        return true;
    }

    const search = async (id) => {
        const data = await transferOrderAPI.searchTransferOrder(id);
        return data || {};
    }

    const searchPage = async (page = 0, size = 10) => {
        const data = await transferOrderAPI.searchTransferOrderPage(page, size);
        return data || {
            content: [],
            totalElements: 0,
            totalPages: 0,
            page: page,
            size,
        };
    }

    return {
        create,
        update,
        check,
        approve,
        reject,
        hardDelete,
        search,
        searchPage,
    }
}

export default transferOrderInterface();
