import backendService from '../backendService.js'

const orderDashApI = {
    dailyDash:()=>backendService.get('/dash/order/daily'),
    weeklyDash:()=>backendService.get('/dash/order/weekly'),
    monthlyDash:()=>backendService.get('/dash/order/monthly'),
    yearlyDash:()=>backendService.get('/dash/order/yearly'),
    customDash:(startDate,endDate)=>backendService.get('/dash/order/custom',{params:{startDate:startDate,endDate:endDate}}),
    sevenDaysDash:()=>backendService.get('/dash/order/sevenDays'),
    everyDayDash:(startDate,endDate)=>backendService.get('/dash/order/everyDay',{params:{startDate:startDate,endDate:endDate}}),
    checkoutCustomDash:(startDate,endDate)=>backendService.get('/dash/order/checkout/custom',{params:{startDate:startDate,endDate:endDate}}),
}

export default orderDashApI;