import backendService from '../backendService.js'

const filterDashAPI = {
    filterDaily: (start, end, wareHouseId, payMethod, employeeId) => backendService.get("/dash/filter/daily", {
        params: {
            start: start,
            end: end,
            wareHouseId: wareHouseId,
            payMethod: payMethod,
            employeeId: employeeId,
        }
    }),
    filterWeekly: (start, end, wareHouseId, payMethod, employeeId) => backendService.get("/dash/filter/weekly", {
        params: {
            start: start,
            end: end,
            wareHouseId: wareHouseId,
            payMethod: payMethod,
            employeeId: employeeId,
        }
    }),
    filterMonthly: (start, end, wareHouseId, payMethod, employeeId) => backendService.get("/dash/filter/monthly", {
        params: {
            start: start,
            end: end,
            wareHouseId: wareHouseId,
            payMethod: payMethod,
            employeeId: employeeId,
        }
    }),
    filterYearly: (start, end, wareHouseId, payMethod, employeeId) => backendService.get("/dash/filter/yearly", {
        params: {
            start: start,
            end: end,
            wareHouseId: wareHouseId,
            payMethod: payMethod,
            employeeId: employeeId,
        }
    }),
    filterCustom: (start, end, wareHouseId, payMethod, employeeId) => backendService.get("/dash/filter/custom", {
        params: {
            start: start,
            end: end,
            wareHouseId: wareHouseId,
            payMethod: payMethod,
            employeeId: employeeId,
        }
    })
}

export default filterDashAPI;
