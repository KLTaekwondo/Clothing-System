import backendService from '../backendService.js'

const memberAPI = {
    createMemberByCheckout: data => backendService.post('/member/register/checkout', data),
    createMemberByManage: data => backendService.post('/member/register/manage', data),
    updateMember: (id, data) => backendService.put(`/member/update/${id}`, data),
    searchMember: phone => backendService.get(`/member/search/${phone}`),
    searchMemberList: () => backendService.get('/member/search/list')
}

export default memberAPI
