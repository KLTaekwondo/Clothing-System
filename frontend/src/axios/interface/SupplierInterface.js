import supplierAPI from "../api/SupplierAPI.js";

function supplierInterface() {
    const create = async (data) => {
        await supplierAPI.createSupplier(data);
        return true;
    }

    const update = async (id, data) => {
        await supplierAPI.updateSupplier(id, data);
        return true;
    }

    const softDelete = async (id) => {
        await supplierAPI.deleteSupplier(id);
        return true;
    }

    const search = async (code) => {
        const data = await supplierAPI.searchSupplier(code);
        return data || {};
    }

    const searchList = async () => {
        const data = await supplierAPI.searchSupplierList();
        return Array.isArray(data) ? data : [];
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
    }
}

export default supplierInterface();
