import http from '../axios.js'

export const adminapi = {

    Login:(data)=>{
        return http.post('/admin/login',data)
    }

}

export default adminapi