import {adminapi} from '../apiModel/adminapi'

export function useAdminapi(){
    const login = async (data) => {
        try{
            const response = await adminapi.Login(data)
            sessionStorage.setItem('currentAdminId', response.currentUserId)
            sessionStorage.setItem('currentAdmin', response.currentUsername)
            return response
        }catch(error){
            console.error('登录失败:', error)
            throw error
        }
    }

    return {
        login
    }
}