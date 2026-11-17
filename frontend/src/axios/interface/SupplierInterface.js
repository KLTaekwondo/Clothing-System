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

    // 分页查询供应商（keyword 可为空）
    const searchPage = async (page = 0, size = 10, keyword = '') => {
        const data = await supplierAPI.searchSupplierPage(page, size, keyword);
        return data || {content: [], totalElements: 0, totalPages: 0, page: page, size};
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
        searchPage,
    }
}

export default supplierInterface();
