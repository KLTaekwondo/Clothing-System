import backendService from "../backendService.js";

const wareHouseAPI = {
    createWareHouse: (data) => backendService.post("/warehouse/create", data),
    updateWareHouse: (id, data) => backendService.put(`/warehouse/update/${id}`, data),
    deleteWareHouse: (id) => backendService.delete(`/warehouse/delete/${id}`),
    searchWareHouse: (id) => backendService.get(`/warehouse/search/${id}`),
    searchWareHouseList: () => backendService.get("/warehouse/search/list"),
    login: (data) => backendService.post("/warehouse/login", data),
    logout: () => backendService.post("/warehouse/logout")

}
export default wareHouseAPI;
