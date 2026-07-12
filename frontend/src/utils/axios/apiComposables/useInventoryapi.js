import inventoryapi from '../apiModel/inventoryapi.js'

export function useInventoryapi() {
  const search = async (wareHouseId, productId) => (await inventoryapi.Search(wareHouseId, productId)).data
  const batchUpdate = async (data) => (await inventoryapi.BatchUpdate(data)).data
  const transfer = async (data) => (await inventoryapi.Transfer(data)).data

  return { search, batchUpdate, transfer }
}

export default useInventoryapi
