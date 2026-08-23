import backendService from '../backendService.js'

const orderDashApI = {
    dailyDash:()=>backendService.get('/dash/daily'),
    weeklyDash:()=>backendService.get('/dash/weekly'),
    monthlyDash:()=>backendService.get('/dash/monthly'),
    yearlyDash:()=>backendService.get('/dash/yearly'),
    customDash:(startDate,endDate)=>backendService.get('/dash/custom/',{params:{startDate:startDate,endDate:endDate}}),
    sevenDaysDash:()=>backendService.get('/dash/sevenDays'),
}

export default orderDashApI;