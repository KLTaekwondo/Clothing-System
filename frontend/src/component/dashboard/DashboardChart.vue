<template>
    <div ref="chartRef" class="chart-box"></div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, ref} from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
    option: {
        type: Object,
        required: true
    }
})

const chartRef = ref(null)
let chart = null

onMounted(() => {
    chart = echarts.init(chartRef.value)
    chart.setOption(props.option)
    window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart)
    chart?.dispose()
    chart = null
})

function resizeChart() {
    chart?.resize()
}
</script>

<style scoped>
.chart-box {
    width: 100%;
    height: 260px;
}
</style>
