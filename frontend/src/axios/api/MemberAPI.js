import backendService from '../backendService.js'

const memberAPI = {
    createMemberByCheckout: data => backendService.post('/member/register/checkout', data),
    createMemberByManage: data => backendService.post('/member/register/manage', data),
    updateMember: (id, data) => backendService.put(`/member/update/${id}`, data),
    searchMember: phone => backendService.get(`/member/search/${phone}`),
    searchMemberPage: (page, size, keyword, memberLevel) => backendService.get('/member/page', {params: {page, size, keyword, memberLevel}}),
    countByMemberLevel: () => backendService.get('/member/count')
}

export default memberAPI
