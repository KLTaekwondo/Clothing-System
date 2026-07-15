import backendService from "../backendService.js";

const optionValueAPI = {
    createOptionValue:(data) => backendService.post("/option-value/create", data),
    updateOptionValue:(id,data) => backendService.put(`/option-value/update/${id}`, data),
    deleteOptionValue:(id) => backendService.delete(`/option-value/delete/${id}`),
    searchOptionValue:(id) => backendService.get(`/option-value/search/${id}`),
    searchOptionValueList: (id) => backendService.get("/option-value/search/list"),
    searchOptionValueByType:(type) => backendService.get(`/option-value/search/list/${type}`)
}

export default optionValueAPI;