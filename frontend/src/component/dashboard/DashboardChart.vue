<template>
    <div ref="chartRef" class="chart-box"></div>
</template>

<script setup>
import {nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
    option: {
        type: Object,
        required: true
    },
    // 容器是否可见（v-show 隐藏时先不初始化，避免 0 宽度导致图表被压扁）
    active: {
        type: Boolean,
        default: true
    }
})

const chartRef = ref(null)
let chart = null

// active 为 true 时初始化图表；从隐藏切回可见时恢复尺寸
watch(() => props.active, (visible) => {
    if (!visible) return
    nextTick(() => {
        if (!chartRef.value) return
        if (chart) {
            chart.resize()
            return
        }
        chart = echarts.init(chartRef.value)
        chart.setOption(props.option)
    })
}, {immediate: true})

onMounted(() => {
    window.addEventListener('resize', resizeChart)
})

watch(() => props.option, () => {
    chart?.setOption(props.option, true)
}, {deep: true})

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