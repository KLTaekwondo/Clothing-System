import http from '../axios.js'

export const employeeapi = {
  Create: (data) => http.post('/api/employee/create', data),
  Update: (id, data) => http.put(`/api/employee/update/${id}`, data),
  Delete: (id) => http.delete(`/api/employee/delete/${id}`),
  IdFind: (id) => http.get(`/api/employee/search/${id}`),
  FindAll: () => http.get('/api/employee/search/list'),
}

export default employeeapi
