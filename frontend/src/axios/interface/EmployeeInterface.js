import employeeAPI from '../api/EmployeeAPI.js';

function employeeInterface() {
    // 创建员工
    const create = async (employeeInfo) => {
        await employeeAPI.createEmployee(employeeInfo);
        return true;
    }

    // 更新员工
    const update = async (id, employeeInfo) => {
        await employeeAPI.updateEmployee(id, employeeInfo);
        return true;
    }

    // 软删除员工
    const softDelete = async (id) => {
        await employeeAPI.deleteEmployee(id);
        return true;
    }

    // 根据编号查询具体员工
    const search = async (id) => {
        const data = await employeeAPI.searchEmployee(id);
        return data || {};
    }

    // 查找所有员工
    const searchList = async () => {
        const data = await employeeAPI.searchEmployeeList();
        return Array.isArray(data) ? data : [];
    }

    // 分页查询员工（keyword 可为空）
    const searchPage = async (page, size, keyword) => {
        const data = await employeeAPI.searchEmployeePage(page, size, keyword || '');
        return data || {content: [], totalElements: 0, totalPages: 0, page: 0, size};
    }

    // 查验员工是否可用
    const verify = async (id) => {
        const data = await employeeAPI.verifyEmployee(id);
        return data || {};
    }

    // 查找当前仓库可用的所有员工
    const verifyList = async () => {
        const data = await employeeAPI.verifyEmployeeList();
        return Array.isArray(data) ? data : [];
    }

    return {
        create,
        update,
        softDelete,
        search,
        searchList,
        searchPage,
        verify,
        verifyList,
    }
}

export default employeeInterface();