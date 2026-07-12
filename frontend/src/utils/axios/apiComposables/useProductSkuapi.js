import productskuapi from '../apiModel/productskuapi.js'

export function useProductSkuapi() {
  const create = async (data) => (await productskuapi.Create(data)).data
  const idfind = async (id) => (await productskuapi.IdFind(id)).data
  const productFind = async (productId) => (await productskuapi.ProductFind(productId)).data
  const update = async (id, data) => (await productskuapi.Update(id, data)).data
  const datadelete = async (id) => (await productskuapi.Delete(id)).data

  return { create, idfind, productFind, update, datadelete }
}

export default useProductSkuapi
