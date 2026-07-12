import http from '../axios.js'

export const salesmanapi = {
    CreateSalesMan:async (data)=>{
        return http.post('/salesman/create',data)
    },

    GetAll:async (id)=>{
        return http.get(`/salesman/getAll/${id}`)
    },

    DeleteSalesMan:async (id)=>{
        return http.delete(`/salesman/delete/${id}`)
    },

    IdFind:async (id)=>{
        return http.get(`/salesman/idFind/${id}`)
    },

    EditSalesMan:async (data)=>{
        return http.put('/salesman/update',data)
    }
}

export default salesmanapi
