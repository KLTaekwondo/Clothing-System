import filterDashAPI from "../api/FilterDashAPI.js";

function filterDashInterface(){
    const filterDaily = async (start,end,wareHouseId,payMethod,employeeId)=>{
        const data = await filterDashAPI.filterDaily(start,end,wareHouseId,payMethod,employeeId);
        return data || {};
    }

    const filterWeekly = async (start,end,wareHouseId,payMethod,employeeId)=>{
        const data = await filterDashAPI.filterWeekly(start,end,wareHouseId,payMethod,employeeId);
        return data || {};
    }

    const filterMonthly = async (start,end,wareHouseId,payMethod,employeeId)=>{
        const data = await filterDashAPI.filterMonthly(start,end,wareHouseId,payMethod,employeeId);
        return data || {};
    }

    const filterYearly = async (start,end,wareHouseId,payMethod,employeeId)=>{
        const data = await filterDashAPI.filterYearly(start,end,wareHouseId,payMethod,employeeId);
        return data || {};
    }

    const filterCustom = async (start,end,wareHouseId,payMethod,employeeId)=>{
        const data = await filterDashAPI.filterCustom(start,end,wareHouseId,payMethod,employeeId);
        return data || {};
    }

    return{
        filterDaily,
        filterWeekly,
        filterMonthly,
        filterYearly,
        filterCustom
    }
}

export default filterDashInterface();
