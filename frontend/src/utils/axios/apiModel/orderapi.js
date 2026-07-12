import http from '../axios.js'

export const orderapi = {
  Create: (data) => http.post('/api/order/create', data),
  IdFind: (id) => http.get(`/api/order/idFind/${id}`),
  NoFind: (orderNo) => http.get(`/api/order/noFind/${orderNo}`),
  FindByDate: (date) => http.get(`/api/order/findByDate/${date}`),
  FindByDateAndWarehouse: (date, wareHouseId) => http.get(`/api/order/findByDate/${date}/warehouse/${wareHouseId}`),
  Cancel: (id) => http.put(`/api/order/cancel/${id}`),
  Return: (id) => http.put(`/api/order/return/${id}`),
}

export default orderapi
