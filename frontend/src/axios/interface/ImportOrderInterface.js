import importOrderAPI from "../api/ImportOrderAPI.js";

function importOrderInterface() {
    // 创建导入订单
    const create = async (data) => {
        await importOrderAPI.createImportOrder(data);
        return true;
    }

    // 更新导入订单
    const update = async (id,data) => {
        await importOrderAPI.updateImportOrder(id,data);
        return true;
    }

    // 检查导入订单
    const check = async (id) => {
        await importOrderAPI.checkImportOrder(id);
        return true;
    }

    // 审批导入订单
    const approve = async (id) => {
        await importOrderAPI.approveImportOrder(id);
        return true;
    }

    // 拒绝导入订单
    const reject = async (id) => {
        await importOrderAPI.rejectImportOrder(id);
        return true;
    }

    // 删除导入订单
    const hardDelete = async (id) => {
        await importOrderAPI.deleteImportOrder(id);
        return true;
    }

    // 获取特定ID的导入订单详情
    const search = async (id) => {
        const data = await importOrderAPI.searchImportOrder(id);
        return data || {}
    }

    // 搜索导入订单列表
    const searchList = async () => {
        const data = await importOrderAPI.searchImportOrderList();
        return Array.isArray(data) ? data : [];
    }

    return{
        create,
        update,
        check,
        approve,
        reject,
        hardDelete,
        searchList,
        search,
    }
}

export default importOrderInterface();
