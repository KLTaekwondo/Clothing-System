import orderapi from '../apiModel/orderapi.js'

export function useOrderapi() {
  const create = async (data) => (await orderapi.Create(data)).data
  const idfind = async (id) => (await orderapi.IdFind(id)).data
  const nofind = async (orderNo) => (await orderapi.NoFind(orderNo)).data
  const findByDate = async (date) => (await orderapi.FindByDate(date)).data
  const findByDateAndWarehouse = async (date, wareHouseId) => (await orderapi.FindByDateAndWarehouse(date, wareHouseId)).data
  const cancel = async (id) => (await orderapi.Cancel(id)).data
  const returnOrder = async (id) => (await orderapi.Return(id)).data

  return { create, idfind, nofind, findByDate, findByDateAndWarehouse, cancel, returnOrder }
}

export default useOrderapi
