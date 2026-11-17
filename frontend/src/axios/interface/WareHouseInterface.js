import wareHouseAPI from "../api/WareHouseAPI.js";

function wareHouseInterface() {
    // 创建仓库
    const create = async (data) => {
        await wareHouseAPI.createWareHouse(data);
        return true;
    }

    // 更新仓库
    const update = async (id, data) => {
        await wareHouseAPI.updateWareHouse(id, data);
        return true;
    }

    // 软删除仓库
    const softDelete = async (id) => {
        await wareHouseAPI.deleteWareHouse(id);
        return true;
    }

    // 搜索仓库
    const search = async (id) => {
        const data = await wareHouseAPI.searchWareHouse(id);
        return data || {};
    }

    // 搜索仓库列表
    const searchList = async () => {
        const data = await wareHouseAPI.searchWareHouseList();
        return Array.isArray(data) ? data : [];
    }

    // 分页查询仓库（下拉仍然用 searchList）
    const searchPage = async (page = 0, size = 10, keyword = '') => {
        const data = await wareHouseAPI.searchWareHousePage(page, size, keyword);
        return data || {content: [], totalElements: 0, totalPages: 0, page: page, size};
    }

    // 登录
    const login = async (body) => {
        const data = await wareHouseAPI.login(body);
        return data || {};
    }

    // 登出
    const logout = async () => {
        await wareHouseAPI.logout();
        return true;
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
        searchPage,
        login,
        logout
    }
}

export default wareHouseInterface();
