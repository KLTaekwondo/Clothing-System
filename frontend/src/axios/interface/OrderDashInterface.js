import orderDashApI from '../api/OrderDashAPI.js'

function orderDashInterface() {
    const dailyDash = async () => {
        const data = await orderDashApI.dailyDash()
        return data ? data : {}
    }

    const weeklyDash = async () => {
        const data = await orderDashApI.weeklyDash()
        return data ? data : {}
    }

    const monthlyDash = async () => {
        const data = await orderDashApI.monthlyDash()
        return data ? data : {}
    }

    const yearlyDash = async () => {
        const data = await orderDashApI.yearlyDash()
        return data ? data : {}
    }

    const customDash = async (startDate,endDate) => {
        const data = await orderDashApI.customDash(startDate,endDate)
        return data ? data : {}
    }

    const sevenDaysDash = async () => {
        const data = await orderDashApI.sevenDaysDash()
        return Array.isArray(data) ? data : {}
    }

    return {
        dailyDash,
        weeklyDash,
        monthlyDash,
        yearlyDash,
        customDash,
        sevenDaysDash,
    }
}

export default orderDashInterface()
