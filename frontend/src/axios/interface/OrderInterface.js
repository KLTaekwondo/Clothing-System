import orderAPI from "../api/OrderAPI.js";

function orderInterface(){
    // 完成订单
    const complete = async (data) =>{
        await orderAPI.completeOrder(data);
        return true;
    }

    // 草稿订单
    const draft = async (data) =>{
        await orderAPI.draftOrder(data);
        return true;
    }

    // 退款订单
    const refund = async (data) =>{
        await orderAPI.refundOrder(data);
        return true;
    }

    // 搜索订单
    const search = async (id) =>{
        const {data} = await orderAPI.searchOrder(id);
        return data || {};
    }


    // 搜索订单列表
    const searchList = async () =>{
        const {data} = await orderAPI.searchOrderList();
        return Array.isArray(data) ? data : [];
    }

    return {
        complete,
        draft,
        refund,
        search,
        searchList,
    }
}

export default orderInterface();