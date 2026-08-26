<template>
    <div class="statistics-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">数据统计</h2>
                <p class="page-label-desc">按时间段查询销售总额、利润与成本构成</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-label">
                    <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                        <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                        <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                    </svg>
                    <span>区间查询</span>
                </div>
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            v-for="quick in quickRanges"
                            :key="quick.key"
                            :class="quickRange === quick.key ? 'filter-tab-active' : 'filter-tab'"
                            type="button"
                            @click="applyQuick(quick)"
                        >{{ quick.label }}</button>
                        <input
                            v-model="startDate"
                            class="date-input"
                            type="date"
                        />
                        <span class="date-sep">至</span>
                        <input
                            v-model="endDate"
                            class="date-input"
                            type="date"
                        />
                        <button
                            :disabled="loading"
                            class="btn-primary search-button"
                            type="button"
                            @click="queryCustom"
                        >查询</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="stats-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="money"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(summary.saleAmount) }}</strong>
                    <span>销售总额</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-success"><IconGraphic name="trend"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(summary.profit) }}</strong>
                    <span>利润</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-warning"><IconGraphic name="cart"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(summary.importAmount) }}</strong>
                    <span>采购成本</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-error"><IconGraphic name="receipt"/></span>
                <span class="stats-body">
                    <strong>{{ formatMargin(summary.marginRate) }}</strong>
                    <span>毛利率</span>
                </span>
            </div>
        </div>

        <div class="chart-card">
            <div class="chart-head">
                <div>
                    <strong class="chart-title">销售额趋势</strong>
                    <span class="chart-range">{{ startDate }} 至 {{ endDate }}</span>
                </div>
                <span class="chart-note">每日销售额</span>
            </div>
            <DashboardChart :option="trendOption"/>
            <div v-if="trendEmpty" class="chart-empty">暂无趋势数据</div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import orderDashInterface from '../../../axios/interface/OrderDashInterface.js'
import IconGraphic from '../../../component/IconGraphic.vue'
import DashboardChart from '../../../component/dashboard/DashboardChart.vue'

const loading = ref(false)
const quickRange = ref('month')

const today = new Date()
const formatDate = (date) => {
    const y = date.getFullYear()
    const m = String(date.getMonth() + 1).padStart(2, '0')
    const d = String(date.getDate()).padStart(2, '0')
    return `${y}-${m}-${d}`
}

const startDate = ref(formatDate(new Date(today.getFullYear(), today.getMonth(), 1)))
const endDate = ref(formatDate(today))

const quickRanges = [
    {key: 'today', label: '今日'},
    {key: '7d', label: '近7天'},
    {key: 'month', label: '本月'},
    {key: '30d', label: '近30天'},
    {key: 'year', label: '今年'}
]

const summary = ref({
    saleAmount: 0,
    profit: 0,
    importAmount: 0,
    marginRate: null
})

const trendList = ref([])
const trendEmpty = computed(() => trendList.value.length === 0)

const trendOption = computed(() => ({
    tooltip: {
        trigger: 'axis'
    },
    grid: {
        left: '3%',
        right: '3%',
        bottom: '8%',
        top: '12%',
        containLabel: true
    },
    xAxis: {
        type: 'category',
        data: trendList.value.map(item => String(item.day || '').slice(5)),
        axisLine: {show: false},
        axisTick: {show: false},
        axisLabel: {color: '#94a3b8', fontSize: 11}
    },
    yAxis: {
        type: 'value',
        splitLine: {lineStyle: {color: '#f1f5f9'}},
        axisLabel: {color: '#94a3b8', fontSize: 11}
    },
    series: [{
        data: trendList.value.map(item => Number(item.amount || 0)),
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: {color: '#0d9488', width: 2},
        itemStyle: {color: '#0d9488'},
        areaStyle: {
            color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                    {offset: 0, color: 'rgba(13,148,136,0.25)'},
                    {offset: 1, color: 'rgba(13,148,136,0.02)'}
                ]
            }
        }
    }]
}))

function applyQuick(quick) {
    quickRange.value = quick.key
    const now = new Date()
    let start = new Date(now)
    let end = new Date(now)
    if (quick.key === 'today') {
        start = new Date(now)
    } else if (quick.key === '7d') {
        start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 6)
    } else if (quick.key === 'month') {
        start = new Date(now.getFullYear(), now.getMonth(), 1)
    } else if (quick.key === '30d') {
        start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 29)
    } else if (quick.key === 'year') {
        start = new Date(now.getFullYear(), 0, 1)
    }
    startDate.value = formatDate(start)
    endDate.value = formatDate(end)
    fetchSummary()
}

async function queryCustom() {
    if (!startDate.value || !endDate.value) return
    if (startDate.value > endDate.value) {
        alert('开始日期不能晚于结束日期')
        return
    }
    quickRange.value = 'custom'
    fetchSummary()
}

async function fetchSummary() {
    loading.value = true
    try {
        const [data, trend] = await Promise.all([
            orderDashInterface.customDash(startDate.value, endDate.value).catch(() => ({})),
            orderDashInterface.everyDayDash(startDate.value, endDate.value).catch(() => [])
        ])
        summary.value = {
            saleAmount: Number(data.saleAmount || 0),
            profit: Number(data.profit || 0),
            importAmount: Number(data.importAmount || 0),
            marginRate: data.marginRate ?? null
        }
        trendList.value = Array.isArray(trend) ? trend : []
    } finally {
        loading.value = false
    }
}

function formatMoney(value) {
    const number = Number(value || 0)
    return '¥' + number.toLocaleString('zh-CN', {minimumFractionDigits: 2, maximumFractionDigits: 2})
}

function formatMargin(value) {
    if (value === null || value === undefined || value === '') return '--'
    const number = Number(value)
    return (number * 100).toFixed(2) + '%'
}

onMounted(fetchSummary)
</script>

<style scoped>
.statistics-page {
    width: 100%;
    min-width: 0;
}

.date-input {
    width: 148px;
    height: 40px;
    padding: 0 14px;
    border: 1px solid var(--border-strong);
    border-radius: 999px;
    background: var(--bg-card);
    font-size: 13px;
    color: var(--text);
    outline: none;
    transition: var(--transition);
    cursor: pointer;
}

.date-input:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
}

.date-sep {
    color: var(--text-muted);
    font-size: 13px;
}

.chart-card {
    padding: 20px 18px 12px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: var(--shadow);
}

.chart-head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 4px;
    padding-left: 4px;
}

.chart-title {
    font-size: 14px;
    font-weight: 700;
    color: var(--text);
}

.chart-range {
    display: block;
    margin-top: 2px;
    color: var(--text-muted);
    font-size: 12px;
}

.chart-note {
    color: var(--text-muted);
    font-size: 11px;
    font-family: var(--mono);
    letter-spacing: 0.5px;
}

.chart-empty {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-muted);
    font-size: 13px;
    pointer-events: none;
}
</style>