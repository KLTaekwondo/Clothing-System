import importOrderAPI from "../api/ImportOrderAPI.js";

function importOrderInterface() {
    const create = async (data) => {
        await importOrderAPI.createImportOrder(data);
        return true;
    }

    const update = async (id, data) => {
        await importOrderAPI.updateImportOrder(id, data);
        return true;
    }

    const check = async (id) => {
        await importOrderAPI.checkImportOrder(id);
        return true;
    }

    const approve = async (id) => {
        await importOrderAPI.approveImportOrder(id);
        return true;
    }

    const reject = async (id) => {
        await importOrderAPI.rejectImportOrder(id);
        return true;
    }

    const hardDelete = async (id) => {
        await importOrderAPI.deleteImportOrder(id);
        return true;
    }

    const search = async (id) => {
        const data = await importOrderAPI.searchImportOrder(id);
        return data || {}
    }

    const searchPage = async (page = 0, size = 10) => {
        const data = await importOrderAPI.searchImportOrderPage(page, size);
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
        searchPage,
        search,
    }
}

export default importOrderInterface();
