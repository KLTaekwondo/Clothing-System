import backendService from '../backendService.js';

const employeeAPI = {
    createEmployee: (data) => backendService.post("/employee/create", data),
    updateEmployee: (id, data) => backendService.put(`/employee/update/${id}`, data),
    deleteEmployee: (id) => backendService.delete(`/employee/delete/${id}`),
    searchEmployee: (id) => backendService.get(`/employee/search/${id}`),
    searchEmployeeList: () => backendService.get("/employee/search/list"),
    verifyEmployee: (id) => backendService.get(`/employee/verify/${id}`)
}

export default employeeAPI;