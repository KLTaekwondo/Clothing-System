import importDashAPI from '../api/ImportDashAPI.js'

function importDashInterface(){
    const seasonAll = async(date) =>{
        const data = await importDashAPI.seasonAll(date)
        return data || {}
    }

    const yearAll = async(date) =>{
        const data = await importDashAPI.YearAll(date)
        return Array.isArray(data) ? data : []
    }

    const seasonBySupplier = async(id,date) =>{
        const data = await importDashAPI.seasonBySupplier(id,date)
        return data || {}
    }

    const yearBySupplier = async(id,date) =>{
        const data = await importDashAPI.YearBySupplier(id,date)
        return Array.isArray(data) ? data : []
    }

    return {
        seasonAll,
        yearAll,
        seasonBySupplier,
        yearBySupplier,
    }
}

export default importDashInterface()
