import {onMounted, ref} from 'vue'
import optionValueInterface from '../../../../axios/interface/OptionValueInterface.js'
import {OPTION_TYPE} from '../../../../constants/optionType.js'

export function useProductOptions(includeSkuOptions = false) {
    const loading = ref(true)
    const typeOptions = ref([])
    const categoryOptions = ref([])
    const unitOptions = ref([])
    const compositionOptions = ref([])
    const yearOptions = ref([])
    const colorOptions = ref([])
    const sizeOptions = ref([])

    async function loadOptions() {
        loading.value = true
        const optionTypes = [
            OPTION_TYPE.TYPE,
            OPTION_TYPE.CATEGORY,
            OPTION_TYPE.UNIT,
            OPTION_TYPE.COMPOSITION,
            OPTION_TYPE.YEAR
        ]
        if (includeSkuOptions) {
            optionTypes.push(OPTION_TYPE.COLOR, OPTION_TYPE.SIZE)
        }
        try {
            const results = await Promise.all(optionTypes.map(type => {
                return optionValueInterface.searchListByType(type).catch(() => [])
            }))
            typeOptions.value = normalize(results[0])
            categoryOptions.value = normalize(results[1])
            unitOptions.value = normalize(results[2])
            compositionOptions.value = normalize(results[3])
            yearOptions.value = normalize(results[4])
            colorOptions.value = normalize(results[5])
            sizeOptions.value = normalize(results[6])
        } finally {
            loading.value = false
        }
    }

    onMounted(loadOptions)

    return {
        loading,
        typeOptions,
        categoryOptions,
        unitOptions,
        compositionOptions,
        yearOptions,
        colorOptions,
        sizeOptions,
        loadOptions
    }
}

function normalize(value) {
    return Array.isArray(value) ? value : []
}
