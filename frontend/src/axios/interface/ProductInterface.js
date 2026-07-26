import productAPI from "../api/ProductAPI.js";

function productInterface() {
    const create = async (data) => {
        await productAPI.createProduct(data);
        return true;
    }

    const update = async (id, data) => {
        await productAPI.updateProduct(id, data);
        return true;
    }

    const softDelete = async (id) => {
        await productAPI.deleteProduct(id);
        return true;
    }

    const search = async (id) => {
        const data = await productAPI.searchProduct(id);
        return data || {};
    }

    const searchPage = async (page = 0, size = 10) => {
        const data = await productAPI.searchProductPage(page, size);
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
        softDelete,
        search,
        searchPage,
    }
}

export default productInterface();
