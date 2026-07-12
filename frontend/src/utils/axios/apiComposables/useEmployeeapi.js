import employeeapi from '../apiModel/employeeapi.js'

export function useEmployeeapi() {
  const create = async (data) => (await employeeapi.Create(data)).data
  const update = async (id, data) => (await employeeapi.Update(id, data)).data
  const datadelete = async (id) => (await employeeapi.Delete(id)).data
  const idfind = async (id) => (await employeeapi.IdFind(id)).data
  const findAll = async () => (await employeeapi.FindAll()).data

  return { create, update, datadelete, idfind, findAll }
}

export default useEmployeeapi
