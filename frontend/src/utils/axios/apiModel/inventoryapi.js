import http from '../axios.js'

export const inventoryapi = {
  Search: (wareHouseId, productId) => http.get(`/api/stock/search/${wareHouseId}/${productId}`),
  BatchUpdate: (data) => http.put('/api/stock/batch-update', data),
  Transfer: (data) => http.put('/api/stock/transfer', data),
}

export default inventoryapi
