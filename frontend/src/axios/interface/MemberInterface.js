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

    const searchMemberPage = async (page = 0, size = 10, keyword = '', memberLevel = '') => {
        const data = await memberAPI.searchMemberPage(page, size, keyword, memberLevel || undefined)
        return data || {content: [], totalElements: 0, totalPages: 0, page: page, size}
    }

    // 按会员等级统计人数：[{memberLevel, mount}]
    const countByMemberLevel = async () => {
        const data = await memberAPI.countByMemberLevel()
        return Array.isArray(data) ? data : []
    }

    return {
        createMemberByCheckout,
        createMemberByManage,
        updateMember,
        searchMember,
        searchMemberPage,
        countByMemberLevel
    }
}

export default memberInterface()
