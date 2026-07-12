import salesmanapi from '../apiModel/salesmanapi.js'

export function useSalesManapi(){
    const getall = async () => {
        try{
            const warehouseId = sessionStorage.getItem('SelectedWareHouseId')
            const response = await salesmanapi.GetAll(warehouseId)
            return response
        }catch(error){
            console.error('获取所有销售员失败:', error)
            throw error
        }
    }

    const create = async (data) => {
        try{
            const response = await salesmanapi.CreateSalesMan(data)
            return response
        }catch(error){
            console.error('创建销售员失败:', error)
            throw error
        }
    }

    const datadelete = async (id) => {
        try{
            const response = await salesmanapi.DeleteSalesMan(id)
            return response
        }catch(error){
            console.error('删除销售员失败:', error)
            throw error
        }
    }

    const idfind = async (id) => {
        try{
            const response = await salesmanapi.IdFind(id)
            return response
        }catch(error){
            console.error('根据ID查找销售员失败:', error)
            throw error
        }
    }

    const edit = async (data) => {
    try {
        console.log('🔍 编辑销售员 - 前端发送的数据:')
        console.log('📦 完整数据对象:', JSON.stringify(data, null, 2))
        console.log('🔑 销售员ID:', data.salesManId)
        console.log('👤 销售员姓名:', data.salesManName)
        
        const response = await salesmanapi.EditSalesMan(data)
        console.log('✅ 编辑销售员成功:', response)
        return response
    } catch (error) {
        console.error('❌ 更新销售员失败:')
        console.error('📡 HTTP状态码:', error.response?.status)
        console.error('📝 错误响应:', error.response?.data)
        console.error('🔗 请求URL:', error.config?.url)
        console.error('📤 请求数据:', error.config?.data)
        throw error
    }
}

    return{
        getall,
        create,
        datadelete,
        idfind,
        edit
    }
}

export default useSalesManapi