import transferOrderAPI from "../api/TransferOrderAPI.js";

function transferOrderInterface(){
    // 创建转移订单
    const create = async (data) => {
        await transferOrderAPI.createTransferOrder(data);
        return true;
    }

    // 更新转移订单
    const update = async (id, data) => {
        await transferOrderAPI.updateTransferOrder(id, data);
        return true;
    }

    // 检查转移订单
    const check = async (id) => {
        await transferOrderAPI.checkTransferOrder(id);
        return true;
    }

    // 审批转移订单
    const approve = async (id) => {
        await transferOrderAPI.approveTransferOrder(id);
        return true;
    }

    // 拒绝转移订单
    const reject = async (id) => {
        await transferOrderAPI.rejectTransferOrder(id);
        return true;
    }

    // 删除转移订单
    const hardDelete = async (id) => {
        await transferOrderAPI.deleteTransferOrder(id);
        return true;
    }

    // 搜索转移订单
    const search = async (id) => {
        const data = await transferOrderAPI.searchTransferOrder(id);
        return data || {};
    }

    // 搜索转移订单列表
    const searchList = async () => {
        const data = await transferOrderAPI.searchTransferOrderList();
        return Array.isArray(data) ? data : [];
    }

    return {
        create,
        update,
        check,
        approve,
        reject,
        hardDelete,
        search,
        searchList,
    }
}

export default transferOrderInterface();
