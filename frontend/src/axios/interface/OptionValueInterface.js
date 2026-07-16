import optionValueAPI from "../api/OptionValueAPI.js";

function optionValueInterface() {
    // 创建选项值
    const create = async (body) => {
        await optionValueAPI.createOptionValue(body);
        return true;
    }

    // 更新选项值
    const update = async (id, body) => {
        await optionValueAPI.updateOptionValue(id, body);
        return true;
    }

    // 软删除选项值
    const softDelete = async (id) => {
        await optionValueAPI.deleteOptionValue(id);
        return true;
    }

    // 搜索选项值
    const search = async (id) => {
        const data = await optionValueAPI.searchOptionValue(id);
        return data || {}
    }

    // 搜索选项值列表
    const searchList = async () => {
        const data = await optionValueAPI.searchOptionValueList();
        return Array.isArray(data) ? data : [];
    }

    // 搜索选项值列表根据类型
    const searchListByType = async (type) => {
        const data = await optionValueAPI.searchOptionValueByType(type);
        return Array.isArray(data) ? data : [];
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
        searchListByType,
    }
}

export default optionValueInterface();