import employeeAPI from '../api/EmployeeAPI.js';

function employeeInterface() {
    // 创建员工
    const create = async (employeeInfo) => {
        await employeeAPI.createEmployee(employeeInfo);
        return true;
    }

    // 更新员工
    const update = async (id,employeeInfo) => {
        await employeeAPI.updateEmployee(id,employeeInfo);
        return true;
    }

    // 软删除员工
    const softDelete = async (id) => {
        await employeeAPI.deleteEmployee(id);
        return true;
    }

    // 根据编号查询具体员工
    const search = async (id) =>{
        const {data} = await employeeAPI.searchEmployee(id);
        return data || {};
    }

    // 查找所有员工
    const searchList = async () =>{
        const {data} = await employeeAPI.searchEmployeeList();
        return Array.isArray(data) ? data : [];
    }

    // 查验员工是否可用
    const verify = async (id) => {
        const {data} = await employeeAPI.verifyEmployee(id);
        return data || {};
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
        verify,
    }
}

export default employeeInterface();