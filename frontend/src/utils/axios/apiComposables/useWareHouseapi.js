import {warehouseapi} from '../apiModel/warehouseapi'

export function useWareHouseapi(){
    const getall = async () => {
        try{
            const currentAdminId = sessionStorage.getItem('currentAdminId')
            const response = await warehouseapi.GetAll(currentAdminId)
            return response
        }catch(error){
            console.error('获取所有仓库失败:', error)
            throw error
        }
    }

    const create = async (data) => {
        try{
            const response = await warehouseapi.Create(data)
            return response
        }catch(error){
            console.error('创建仓库失败:', error)
            throw error
        }
    }

    const datadelete = async (id) => {
        try{
            const response = await warehouseapi.Delete(id)
            return response
        }catch(error){
            console.error('删除仓库失败:', error)
            throw error
        }
    }

    const idfind = async (id) => {
        try{
            const response = await warehouseapi.IdFind(id)
            return response
        }catch(error){
            console.error('根据ID查找仓库失败:', error)
            throw error
        }
    }

    const update = async (data) => {
        try{
            const response = await warehouseapi.Update(data)
            return response
        }catch(error){
            console.error('更新仓库失败:', error)
            throw error
        }
    }

    return{
        getall,
        create,
        datadelete,
        idfind,
        update,
    }
}