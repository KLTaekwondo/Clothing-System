import productoptionapi from '../apiModel/productoptionapi.js'

export function useProductOptionapi() {
  const create = async (data) => (await productoptionapi.Create(data)).data
  const update = async (id, data) => (await productoptionapi.Update(id, data)).data
  const datadelete = async (id) => (await productoptionapi.Delete(id)).data
  const idfind = async (id) => (await productoptionapi.IdFind(id)).data
  const typeFind = async (type, onlyEnabled = false) => (await productoptionapi.TypeFind(type, onlyEnabled)).data
  const findAll = async () => (await productoptionapi.FindAll()).data

  return { create, update, datadelete, idfind, typeFind, findAll }
}

export default useProductOptionapi
