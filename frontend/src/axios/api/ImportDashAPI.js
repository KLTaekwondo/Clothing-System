import backendService from '../backendService.js'


const importDashAPI = {
    seasonAll: (date) => backendService.get('dash/import-order/season', {params:{someday:date}}),
    YearAll: (date) => backendService.get('dash/import-order/year', {params:{someday:date}}),
    seasonBySupplier: (id,date) => backendService.get(`dash/import-order/season/${id}`, {params:{someday:date}}),
    YearBySupplier: (id,date) => backendService.get(`dash/import-order/year/${id}`, {params:{someday:date}})
}

export default importDashAPI
