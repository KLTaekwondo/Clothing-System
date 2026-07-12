import http from '../axios.js'

export const productoptionapi = {
  Create: (data) => http.post('/api/option-value/create', data),
  Update: (id, data) => http.put(`/api/option-value/update/${id}`, data),
  Delete: (id) => http.delete(`/api/option-value/delete/${id}`),
  IdFind: (id) => http.get(`/api/option-value/search/${id}`),
  TypeFind: (type, onlyEnabled = false) => http.get(`/api/option-value/search/list/${type}`, { params: { onlyEnabled } }),
  FindAll: () => http.get('/api/option-value/search/list'),
}

export default productoptionapi
