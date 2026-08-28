<template>
    <div class="statistics-page">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">数据统计</h2>
                <p class="page-label-desc">按时间段查询销售总额、利润与成本构成</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="view-tabs">
                <button
                    :class="activeView === 'sales' ? 'view-tab-active' : 'view-tab'"
                    type="button"
                    @click="activeView = 'sales'"
                >销售统计</button>
                <button
                    :class="activeView === 'filter' ? 'view-tab-active' : 'view-tab'"
                    type="button"
                    @click="activeView = 'filter'"
                >筛选统计</button>
                <button
                    :class="activeView === 'import' ? 'view-tab-active' : 'view-tab'"
                    type="button"
                    @click="activeView = 'import'"
                >进货统计</button>
            </div>
        </div>

        <div
            v-show="activeView === 'sales'"
            class="view-panel"
        >
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
                        <button
                            :disabled="exporting"
                            class="btn-outline search-button"
                            type="button"
                            @click="handleExport"
                        >{{ exporting ? '导出中...' : '导出报告' }}</button>
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
            <DashboardChart
                :active="activeView === 'sales'"
                :option="trendOption"
            />
            <div v-if="trendEmpty" class="chart-empty">暂无趋势数据</div>
            </div>
        </div>

        <div
            v-show="activeView === 'filter'"
            class="view-panel"
        >
        <div class="filter-toolbar">
            <div class="toolbox-stack">
                <div class="search-label">快捷区间</div>
                <div class="search-shell filter-shell">
                    <div class="search-controls">
                        <button
                            v-for="quick in filterRanges"
                            :key="quick.key"
                            :class="filterRange === quick.key ? 'filter-tab-active' : 'filter-tab'"
                            type="button"
                            @click="applyFilterRange(quick)"
                        >{{ quick.label }}</button>
                    </div>
                </div>
            </div>
            <div class="toolbox-stack">
                <div class="search-label">日期与范围</div>
                <div class="search-shell filter-shell">
                    <div class="search-controls">
                        <template v-if="filterRange === 'custom'">
                            <input
                                v-model="filterStartDate"
                                class="date-input"
                                type="date"
                            />
                            <span class="date-sep">至</span>
                            <input
                                v-model="filterEndDate"
                                class="date-input"
                                type="date"
                            />
                        </template>
                        <select
                            v-model="filterWareHouseId"
                            class="filter-select"
                            :disabled="filterLoading"
                            @change="onWareHouseChange"
                        >
                            <option value="">全部仓库</option>
                            <option
                                v-for="warehouse in warehouseOptions"
                                :key="warehouse.id"
                                :value="warehouse.id"
                            >{{ warehouse.name }}</option>
                        </select>
                        <select
                            v-model="filterEmployeeId"
                            class="filter-select"
                            :disabled="filterLoading"
                            @change="fetchFilterSummary"
                        >
                            <option value="">全部员工</option>
                            <option
                                v-for="employee in employeeOptions"
                                :key="employee.id"
                                :value="employee.id"
                            >{{ employee.name }}</option>
                        </select>
                    </div>
                </div>
            </div>
            <div class="toolbox-stack">
                <div class="search-label">支付方式</div>
                <div class="search-shell filter-shell">
                    <div class="search-controls">
                        <button
                            v-for="option in payFilterOptions"
                            :key="option.value"
                            :class="filterPayMethod === option.value ? 'filter-tab-active' : 'filter-tab'"
                            type="button"
                            @click="applyFilterPay(option.value)"
                        >{{ option.label }}</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="filter-scope">当前统计 · {{ filterScopeLabel }}</div>

        <div class="stats-summary filter-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="money"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(filterSummary.saleAmount) }}</strong>
                    <span>销售总额</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-success"><IconGraphic name="trend"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(filterSummary.profit) }}</strong>
                    <span>利润</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-warning"><IconGraphic name="cart"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(filterSummary.importAmount) }}</strong>
                    <span>采购成本</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-error"><IconGraphic name="receipt"/></span>
                <span class="stats-body">
                    <strong>{{ formatMargin(filterSummary.marginRate) }}</strong>
                    <span>毛利率</span>
                </span>
            </div>
            </div>
        </div>

        <div
            v-show="activeView === 'import'"
            class="view-panel"
        >
        <div class="import-toolbox">
            <div class="search-label">
                <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                    <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                    <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
                </svg>
                <span>进货检索</span>
            </div>
            <div class="search-shell">
                <div class="search-controls">
                    <div class="supplier-select">
                        <OptionValuePicker
                            v-model="supplierPickerValue"
                            :disabled="supplierLoading"
                            :options="supplierOptions"
                            error-message="请从供应商列表中选择有效供应商"
                            placeholder="输入供应商名称或编码筛选"
                            select-placeholder="全部供应商"
                            @update:model-value="onPickSupplier"
                        />
                    </div>
                    <button
                        :class="importMode === 'season' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="switchImportMode('season')"
                    >单季</button>
                    <button
                        :class="importMode === 'year' ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="switchImportMode('year')"
                    >年度</button>
                    <input
                        v-model="importDate"
                        class="date-input"
                        type="date"
                    />
                    <button
                        :disabled="importLoading"
                        class="btn-primary search-button"
                        type="button"
                        @click="fetchImportDash"
                    >{{ importLoading ? '查询中...' : '查询' }}</button>
                </div>
            </div>
        </div>

        <div class="stats-summary import-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="clothing"/></span>
                <span class="stats-body">
                    <strong>{{ importSeasonName }}</strong>
                    <span>季节</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="money"/></span>
                <span class="stats-body">
                    <strong>{{ formatMoney(importAmountTotal) }}</strong>
                    <span>总金额</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon stats-icon-warning"><IconGraphic name="cart"/></span>
                <span class="stats-body">
                    <strong>{{ formatQuantity(importQuantityTotal) }}</strong>
                    <span>总数量</span>
                </span>
            </div>
        </div>

        <div class="chart-card">
            <div class="chart-head">
                <div>
                    <strong class="chart-title">进货统计</strong>
                    <span class="chart-range">{{ importScopeLabel }} · {{ importDate }}</span>
                </div>
            </div>
            <DashboardChart
                :active="activeView === 'import'"
                :option="importChartOption"
            />
            <div v-if="importEmpty" class="chart-empty">{{ importMode === 'season' ? '该季节暂无进货数据' : '该年度暂无进货数据' }}</div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import orderDashInterface from '../../../axios/interface/OrderDashInterface.js'
import filterDashInterface from '../../../axios/interface/FilterDashInterface.js'
import importDashInterface from '../../../axios/interface/ImportDashInterface.js'
import supplierInterface from '../../../axios/interface/SupplierInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import employeeInterface from '../../../axios/interface/EmployeeInterface.js'
import exportInterface from '../../../axios/interface/ExportInterface.js'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'
import IconGraphic from '../../../component/IconGraphic.vue'
import DashboardChart from '../../../component/dashboard/DashboardChart.vue'
import {PAY_METHOD_OPTIONS} from '../../../constants/payMethod.js'
import {STATUS} from '../../../constants/status.js'
import {useToastStore} from '../../../stores/toastStore.js'

const toast = useToastStore()
const loading = ref(false)
const importLoading = ref(false)
const exporting = ref(false)
const quickRange = ref('month')
// 页面视图：sales 销售统计 / filter 筛选统计 / import 进货统计
const activeView = ref('sales')

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

// ── 条件筛选统计（/dash/filter/*，独立于上方全量区间查询） ──
const filterRanges = [
    {key: 'daily', label: '今日'},
    {key: 'weekly', label: '本周'},
    {key: 'monthly', label: '本月'},
    {key: 'yearly', label: '今年'},
    {key: 'custom', label: '自定义'}
]

// 对应 FilterDashInterface 的方法名（daily/weekly/monthly/yearly 的 start/end 后端会覆盖）
const FILTER_METHODS = {
    daily: 'filterDaily',
    weekly: 'filterWeekly',
    monthly: 'filterMonthly',
    yearly: 'filterYearly',
    custom: 'filterCustom'
}

const filterRange = ref('monthly')
const filterStartDate = ref(formatDate(new Date(today.getFullYear(), today.getMonth(), 1)))
const filterEndDate = ref(formatDate(today))
const filterWareHouseId = ref('')
const filterEmployeeId = ref('')
const filterPayMethod = ref('')
const filterLoading = ref(false)
const filterSummary = ref({
    saleAmount: 0,
    profit: 0,
    importAmount: 0,
    marginRate: null
})

const warehouses = ref([])
const employees = ref([])
const payFilterOptions = [{value: '', label: '全部支付方式'}, ...PAY_METHOD_OPTIONS]

const warehouseOptions = computed(() => warehouses.value.filter(item => item.status === STATUS.ENABLE))

// 员工与所选仓库联动（按仓库编码匹配）
const employeeOptions = computed(() => {
    const warehouse = warehouses.value.find(item => item.id === filterWareHouseId.value)
    if (!warehouse) {
        return employees.value.filter(item => item.status === STATUS.ENABLE)
    }
    return employees.value.filter(item => item.wareHouseCode === warehouse.code && item.status === STATUS.ENABLE)
})

const filterScopeLabel = computed(() => {
    const range = filterRanges.find(item => item.key === filterRange.value)
    const warehouse = warehouses.value.find(item => item.id === filterWareHouseId.value)
    const employee = employees.value.find(item => item.id === filterEmployeeId.value)
    const pay = payFilterOptions.find(item => item.value === filterPayMethod.value)
    return [
        range ? range.label : '',
        warehouse ? warehouse.name : '全部仓库',
        employee ? employee.name : '全部员工',
        pay ? pay.label : '全部支付方式'
    ].join(' · ')
})

function applyFilterRange(quick) {
    filterRange.value = quick.key
    fetchFilterSummary()
}

function applyFilterPay(value) {
    filterPayMethod.value = value
    fetchFilterSummary()
}

// 切换仓库后重置员工选择（联动选项可能已变化）
function onWareHouseChange() {
    filterEmployeeId.value = ''
    fetchFilterSummary()
}

async function fetchFilterSummary() {
    filterLoading.value = true
    try {
        const data = await filterDashInterface[FILTER_METHODS[filterRange.value]](
            filterStartDate.value,
            filterEndDate.value,
            filterWareHouseId.value || null,
            filterPayMethod.value || null,
            filterEmployeeId.value || null
        )
        filterSummary.value = {
            saleAmount: Number(data.saleAmount || 0),
            profit: Number(data.profit || 0),
            importAmount: Number(data.importAmount || 0),
            marginRate: data.marginRate ?? null
        }
    } catch {
        filterSummary.value = {saleAmount: 0, profit: 0, importAmount: 0, marginRate: null}
    } finally {
        filterLoading.value = false
    }
}

async function loadWarehouses() {
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    }
}

async function loadEmployees() {
    try {
        employees.value = await employeeInterface.searchList()
    } catch {
        employees.value = []
    }
}

// ── 进货统计（单季柱状 / 年度饼图） ──
const SEASON_LABEL_MAP = {
    SPRING: '春季',
    SUMMER: '夏季',
    AUTUMN: '秋季',
    WINTER: '冬季'
}

const importMode = ref('season')
const importDate = ref(formatDate(today))
const seasonInfo = ref(null)
const yearList = ref([])
const suppliers = ref([])
const supplierLoading = ref(false)
const supplierPickerValue = ref('')
const supplierId = ref('')

const supplierOptions = computed(() => suppliers.value.map(item => ({
    id: item.id,
    optionValue: formatSupplierOption(item)
})))

const importScopeLabel = computed(() => {
    const scope = supplierId.value ? supplierPickerValue.value : '全部供应商'
    if (importMode.value === 'season') {
        const season = SEASON_LABEL_MAP[seasonInfo.value?.season] || '单季'
        return `${scope} · ${season}`
    }
    return `${scope} · 全年`
})

const importEmpty = computed(() => {
    if (importMode.value === 'season') return !seasonInfo.value
    return yearList.value.length === 0
})

const importAmountTotal = computed(() => {
    if (importMode.value === 'season') {
        return Number(seasonInfo.value?.amount || 0)
    }
    return yearList.value.reduce((sum, item) => sum + Number(item.amount || 0), 0)
})

const importQuantityTotal = computed(() => {
    if (importMode.value === 'season') {
        return Number(seasonInfo.value?.quantity || 0)
    }
    return yearList.value.reduce((sum, item) => sum + Number(item.quantity || 0), 0)
})

const importSeasonName = computed(() => {
    if (importMode.value === 'season') {
        return SEASON_LABEL_MAP[seasonInfo.value?.season] || '--'
    }
    return '全年'
})

const importChartOption = computed(() => {
    if (importMode.value === 'season') {
        const info = seasonInfo.value || {}
        return {
            tooltip: {trigger: 'axis'},
            legend: {data: ['金额', '数量'], top: 0, right: 0, textStyle: {color: '#94a3b8', fontSize: 11}},
            grid: {left: '3%', right: '3%', bottom: '8%', top: '12%', containLabel: true},
            xAxis: {
                type: 'category',
                data: [SEASON_LABEL_MAP[info.season] || '本季'],
                axisLine: {show: false},
                axisTick: {show: false},
                axisLabel: {color: '#94a3b8', fontSize: 11}
            },
            yAxis: [
                {
                    type: 'value',
                    name: '金额',
                    nameLocation: 'middle',
                    nameGap: 42,
                    splitLine: {lineStyle: {color: '#f1f5f9'}},
                    axisLabel: {color: '#94a3b8', fontSize: 11}
                },
                {
                    type: 'value',
                    name: '数量',
                    nameLocation: 'middle',
                    nameGap: 42,
                    splitLine: {show: false},
                    axisLabel: {color: '#94a3b8', fontSize: 11}
                }
            ],
            series: [
                {
                    name: '金额',
                    type: 'bar',
                    barWidth: 28,
                    data: [Number(info.amount || 0)],
                    itemStyle: {color: '#0d9488', borderRadius: [6, 6, 0, 0]}
                },
                {
                    name: '数量',
                    type: 'bar',
                    yAxisIndex: 1,
                    barWidth: 28,
                    data: [Number(info.quantity || 0)],
                    itemStyle: {color: '#3b82f6', borderRadius: [6, 6, 0, 0]}
                }
            ]
        }
    }

    const pieColors = {SPRING: '#0d9488', SUMMER: '#3b82f6', AUTUMN: '#f59e0b', WINTER: '#8b5cf6'}
    const data = yearList.value
        .filter(item => item && item.season)
        .map(item => ({
            name: SEASON_LABEL_MAP[item.season] || item.season,
            value: Number(item.amount || 0),
            quantity: Number(item.quantity || 0),
            itemStyle: {color: pieColors[item.season] || '#94a3b8'}
        }))
    return {
        tooltip: {
            trigger: 'item',
            formatter: (params) => {
                const qty = params.data?.quantity ?? 0
                return `${params.name}<br/>金额：¥${Number(params.value || 0).toLocaleString('zh-CN', {minimumFractionDigits: 2})}<br/>数量：${qty} 件`
            }
        },
        legend: {bottom: 0, textStyle: {color: '#94a3b8', fontSize: 11}},
        series: [{
            type: 'pie',
            radius: ['45%', '70%'],
            center: ['50%', '45%'],
            avoidLabelOverlap: true,
            itemStyle: {borderRadius: 4, borderColor: '#ffffff', borderWidth: 2},
            label: {show: true, color: '#94a3b8', fontSize: 12, formatter: '{b}'},
            labelLine: {lineStyle: {color: '#d1d5db'}},
            data
        }]
    }
})

function switchImportMode(mode) {
    if (importMode.value === mode) return
    importMode.value = mode
    seasonInfo.value = null
    yearList.value = []
    fetchImportDash()
}

function formatSupplierOption(supplier) {
    if (supplier.supplierCode) return `${supplier.supplierName}（${supplier.supplierCode}）`
    if (supplier.code) return `${supplier.supplierName}（${supplier.code}）`
    return supplier.supplierName
}

function onPickSupplier(value) {
    const selected = suppliers.value.find(item => formatSupplierOption(item) === value)
    supplierId.value = selected?.id || ''
    seasonInfo.value = null
    yearList.value = []
    fetchImportDash()
}

async function loadSuppliers() {
    supplierLoading.value = true
    try {
        suppliers.value = await supplierInterface.searchList()
    } catch {
        suppliers.value = []
    } finally {
        supplierLoading.value = false
    }
}

async function fetchImportDash() {
    const date = importDate.value
    if (!date) return
    importLoading.value = true
    try {
        if (importMode.value === 'season') {
            seasonInfo.value = supplierId.value
                ? await importDashInterface.seasonBySupplier(supplierId.value, date).catch(() => null)
                : await importDashInterface.seasonAll(date).catch(() => null)
            yearList.value = []
        } else {
            yearList.value = supplierId.value
                ? await importDashInterface.yearBySupplier(supplierId.value, date).catch(() => [])
                : await importDashInterface.yearAll(date).catch(() => [])
            seasonInfo.value = null
        }
    } finally {
        importLoading.value = false
    }
}

// 导出当前所选区间的订单汇总 CSV
async function handleExport() {
    if (!startDate.value || !endDate.value) {
        return
    }
    exporting.value = true
    try {
        const ok = await exportInterface.exportBetween(startDate.value, endDate.value)
        if (ok) {
            toast.success('报告已导出')
        } else {
            toast.error('导出失败，请稍后重试')
        }
    } finally {
        exporting.value = false
    }
}

function formatQuantity(value) {
    return Number(value || 0).toLocaleString('zh-CN')
}

function formatMargin(value) {
    if (value === null || value === undefined || value === '') return '--'
    const number = Number(value)
    return (number * 100).toFixed(2) + '%'
}

onMounted(() => {
    fetchSummary()
    loadSuppliers()
    fetchImportDash()
    loadWarehouses()
    loadEmployees()
    fetchFilterSummary()
})
</script>

<style scoped>
.statistics-page {
    width: 100%;
    min-width: 0;
}

/* ── 视图切换 Tabs（页头主标题右侧，分段胶囊） ── */
.view-tabs {
    display: flex;
    gap: 4px;
    padding: 4px;
    background: var(--bg-subtle);
    border: 1px solid var(--border-light);
    border-radius: 999px;
}

.view-tab,
.view-tab-active {
    padding: 8px 18px;
    border: none;
    border-radius: 999px;
    background: transparent;
    color: var(--text-secondary);
    font-size: 13px;
    font-weight: 700;
    cursor: pointer;
    transition: color 0.2s, background 0.2s, box-shadow 0.2s;
}

.view-tab:hover {
    color: var(--primary);
}

.view-tab-active {
    background: var(--bg-card);
    color: var(--primary);
    box-shadow: 0 2px 8px rgba(13, 148, 136, 0.18);
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
    position: relative;
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

.import-toolbox {
    width: 100%;
    margin-top: 15px;
    margin-bottom: 18px;
}

.import-summary .stats-item {
    width: 33.33%;
}

.import-toolbox .search-shell {
    margin: 0 auto;
}

.import-toolbox .search-controls {
    flex-wrap: wrap;
}

.supplier-select {
    width: 240px;
}

/* ── 条件筛选：三个独立胶囊（快捷区间 / 日期与范围 / 支付方式） ── */
.filter-toolbar {
    display: flex;
    align-items: flex-start;
    justify-content: center;
    gap: 18px;
    flex-wrap: wrap;
    margin: 0 auto 18px;
}

.filter-toolbar .search-shell {
    margin: 0;
}

.filter-summary {
    margin-bottom: 18px;
}

.filter-scope {
    margin-bottom: 12px;
    color: var(--text-muted);
    font-size: 12px;
}

.filter-select {
    width: 150px;
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

.filter-select:focus {
    border-color: var(--primary);
    box-shadow: 0 0 0 3px var(--primary-focus);
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