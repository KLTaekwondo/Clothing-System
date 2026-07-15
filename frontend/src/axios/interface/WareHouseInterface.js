import wareHouseAPI from "../api/WareHouseAPI.js";

function wareHouseInterface(){
    // 创建仓库
    const create = async (data) =>{
        await wareHouseAPI.createWareHouse(data);
        return true;
    }

    // 更新仓库
    const update = async (id,data) =>{
        await wareHouseAPI.updateWareHouse(id,data);
        return true;
    }

    // 软删除仓库
    const softDelete = async (id) =>{
        await wareHouseAPI.deleteWareHouse(id);
        return true;
    }

    // 搜索仓库
    const search = async (id) =>{
        const data = await wareHouseAPI.searchWareHouse(id);
        return data || {};
    }

    // 搜索仓库列表
    const searchList = async () =>{
        const data = await wareHouseAPI.searchWareHouseList();
        return Array.isArray(data) ? data : [];
    }

    // 登录
    const login = async (body) =>{
        const data = await wareHouseAPI.login(body);
        return data || {};
    }

    // 登出
    const logout = async () =>{
        await wareHouseAPI.logout();
        return true;
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
        login,
        logout
    }
}

export default wareHouseInterface();
