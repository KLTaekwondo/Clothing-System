import http from '../axios.js'

export const productskuapi = {
  Create: (data) => http.post('/api/productSku/create', data),
  IdFind: (id) => http.get(`/api/productSku/search/${id}`),
  ProductFind: (productId) => http.get(`/api/productSku/search/byProductId/${productId}`),
  Update: (id, data) => http.put(`/api/productSku/update/${id}`, data),
  Delete: (id) => http.delete(`/api/productSku/delete/${id}`),
}

export default productskuapi
