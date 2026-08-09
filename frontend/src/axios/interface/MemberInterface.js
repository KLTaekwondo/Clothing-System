import memberAPI from '../api/MemberAPI.js'

function memberInterface() {
    const createMemberByCheckout = async data => {
        await memberAPI.createMemberByCheckout(data)
        return true
    }

    const createMemberByManage = async data => {
        await memberAPI.createMemberByManage(data)
        return true
    }

    const updateMember = async (id, data) => {
        await memberAPI.updateMember(id, data)
        return true
    }

    const searchMember = async phone => {
        const data = await memberAPI.searchMember(phone)
        return data || {}
    }

    const searchMemberList = async () => {
        const data = await memberAPI.searchMemberList()
        return Array.isArray(data) ? data : []
    }

    return {
        createMemberByCheckout,
        createMemberByManage,
        updateMember,
        searchMember,
        searchMemberList
    }
}

export default memberInterface()
