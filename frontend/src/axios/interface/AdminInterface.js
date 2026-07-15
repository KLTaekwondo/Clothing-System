import adminAPI from "../api/AdminAPI.js";

function adminInterface(){
    // 登录
    const login = async (body) =>{
        const {data} = await adminAPI.login(body);
        return data || {};
    }

    // 登出
    const logout = async () =>{
        await adminAPI.logout();
        return true;
    }

    // 重置密码
    const resetPassword = async (data) =>{
        await adminAPI.resetPassword(data);
        return true;
    }

    return {
        login,
        logout,
        resetPassword
    }
}

export default adminInterface();