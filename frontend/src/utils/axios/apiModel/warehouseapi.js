import http from '../axios.js'

export const warehouseapi = {
    GetAll: async (adminid) => {
        return http.get(`/warehouse/findAll/${adminid}`)
    },
    Create: async (data) => {
        return http.post('/warehouse/create', data)
    },
    Update: async (data) => {
        return http.put('/warehouse/update', data)
    },
    Delete: async (id) => {
        return http.delete(`/warehouse/delete/${id}`)
    },
    IdFind: async (id) => {
        return http.get(`/warehouse/idFind/${id}`)
    }
}

export default warehouseapi