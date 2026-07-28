<template>
    <div class="dashboard">
        <div class="welcome-section">
            <div class="welcome-text">
                <h2 class="page-title">控制台</h2>
                <p class="welcome-desc">欢迎回来，今天也是元气满满的一天！</p>
            </div>
        </div>

        <div class="section-title">工作台</div>
        <div class="workspace-layout">
            <div class="overview-card">
                <div class="workspace-heading">
                    <div class="workspace-title">数据总览</div>
                    <span class="workspace-hint">实时数据</span>
                </div>
                <div class="stat-grid">
                    <div class="stat-card">
                        <div class="stat-icon stat-teal"><IconGraphic name="product"/></div>
                        <div class="stat-body">
                            <span class="stat-value">{{ stats.products }}</span>
                            <span class="stat-label">商品总数</span>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon stat-green"><IconGraphic name="user"/></div>
                        <div class="stat-body">
                            <span class="stat-value">{{ stats.employees }}</span>
                            <span class="stat-label">员工总数</span>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon stat-orange"><IconGraphic name="warehouse"/></div>
                        <div class="stat-body">
                            <span class="stat-value">{{ stats.warehouses }}</span>
                            <span class="stat-label">仓库总数</span>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon stat-purple"><IconGraphic name="order"/></div>
                        <div class="stat-body">
                            <span class="stat-value">{{ stats.orders }}</span>
                            <span class="stat-label">订单总数</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="shortcut-card">
                <div class="workspace-heading">
                    <div class="workspace-title">快捷入口</div>
                    <span class="workspace-hint">常用功能</span>
                </div>
                <div class="quick-grid">
                    <router-link class="quick-card" to="/manage/product">
                        <span class="quick-icon-box" style="background:#dcfce7"><IconGraphic name="product"/></span>
                        <span class="quick-name">商品管理</span>
                        <span class="quick-desc">添加、编辑和管理商品</span>
                    </router-link>
                    <router-link class="quick-card" to="/manage/employee">
                        <span class="quick-icon-box" style="background:#e8f0fe"><IconGraphic name="user"/></span>
                        <span class="quick-name">员工管理</span>
                        <span class="quick-desc">管理员工信息与核验</span>
                    </router-link>
                    <router-link class="quick-card" to="/manage/warehouse">
                        <span class="quick-icon-box" style="background:#fef3c7"><IconGraphic name="warehouse"/></span>
                        <span class="quick-name">仓库管理</span>
                        <span class="quick-desc">仓库与库存调拨</span>
                    </router-link>
                    <router-link class="quick-card" to="/manage/order">
                        <span class="quick-icon-box" style="background:#fce7f3"><IconGraphic name="order"/></span>
                        <span class="quick-name">查看订单</span>
                        <span class="quick-desc">查看订单状态与金额</span>
                    </router-link>
                </div>
            </div>
        </div>

        <div class="section-title">财务概览</div>
        <div class="stat-grid">
            <div class="stat-card">
                <div class="stat-icon stat-teal"><IconGraphic name="money"/></div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.todaySales }}</span>
                    <span class="stat-label">今日销售额</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-blue"><IconGraphic name="trend"/></div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.monthlySales }}</span>
                    <span class="stat-label">本月销售额</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-green"><IconGraphic name="money"/></div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.todayProfit }}</span>
                    <span class="stat-label">今日毛利</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-purple"><IconGraphic name="trend"/></div>
                <div class="stat-body">
                    <span class="stat-value">{{ finance.grossMargin }}</span>
                    <span class="stat-label">毛利率</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-orange"><IconGraphic name="receipt"/></div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.todayCost }}</span>
                    <span class="stat-label">今日成本</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-red"><IconGraphic name="cart"/></div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.avgOrder }}</span>
                    <span class="stat-label">客单价</span>
                </div>
            </div>
        </div>

        <div class="section-title">数据图表</div>
        <div class="chart-row">
            <div class="chart-card">
                <div class="chart-header">近7日销售额趋势</div>
                <div ref="salesChartRef" class="chart-box"></div>
            </div>
            <div class="chart-card">
                <div class="chart-header">商品类目分布</div>
                <div ref="categoryChartRef" class="chart-box"></div>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
            <span style="margin-left:10px;color:var(--text-muted)">加载中…</span>
        </div>
    </div>
</template>

<script setup>
import {nextTick, onMounted, ref} from 'vue'
import * as echarts from 'echarts'
import productInterface from '../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import orderInterface from '../../axios/interface/OrderInterface.js'
import {STATUS} from '../../constants/status.js'
import IconGraphic from '../../component/IconGraphic.vue'

const loading = ref(true)
const salesChartRef = ref(null)
const categoryChartRef = ref(null)

const stats = ref({
    products: 0, skus: 0, employees: 0,
    warehouses: 0, orders: 0, lowStock: 0
})

const finance = ref({
    todaySales: '¥ 0.00', monthlySales: '¥ 0.00', todayProfit: '¥ 0.00',
    grossMargin: '0.00%', todayCost: '¥ 0.00', avgOrder: '¥ 0.00'
})

const salesData = [820, 932, 901, 934, 1290, 1330, 1520]
const salesDays = ['07-09', '07-10', '07-11', '07-12', '07-13', '07-14', '07-15']

onMounted(async () => {
    try {
        const [products, employees, warehouses, orders] = await Promise.all([
            productInterface.searchPage().then(d => d.content || []).catch(() => []),
            employeeInterface.searchList().catch(() => []),
            wareHouseInterface.searchList().catch(() => []),
            orderInterface.searchPage().then(data => data.content || []).catch(() => [])
        ])
        const enabledProducts = products.filter(item => item.status === STATUS.ENABLE)
        const skuLists = await Promise.all(
            enabledProducts.map(item => productSkuInterface.searchListByProductId(item.id).catch(() => []))
        )
        stats.value = {
            products: products.length || 0,
            skus: skuLists.reduce((total, list) => total + list.length, 0),
            employees: employees.length || 0,
            warehouses: warehouses.length || 0,
            orders: orders.length || 0,
            lowStock: 0
        }
    } catch {
        // 静默
    } finally {
        loading.value = false
    }

    await nextTick()
    initCharts()
})

function initCharts() {
    // 销售额趋势图
    if (salesChartRef.value) {
        const salesChart = echarts.init(salesChartRef.value)
        salesChart.setOption({
            tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
            grid: {left: '3%', right: '3%', bottom: '8%', top: '8%', containLabel: true},
            xAxis: {
                type: 'category',
                data: salesDays,
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
                data: salesData,
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: {color: '#0d9488', width: 2},
                itemStyle: {color: '#0d9488'},
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        {offset: 0, color: 'rgba(13,148,136,0.25)'},
                        {offset: 1, color: 'rgba(13,148,136,0.02)'}
                    ])
                }
            }]
        })
        window.addEventListener('resize', () => salesChart.resize())
    }

    // 类目分布图（环形饼图）
    if (categoryChartRef.value) {
        const categoryChart = echarts.init(categoryChartRef.value)
        categoryChart.setOption({
            tooltip: {trigger: 'item'},
            series: [{
                type: 'pie',
                radius: ['45%', '70%'],
                center: ['50%', '50%'],
                avoidLabelOverlap: true,
                itemStyle: {borderRadius: 4, borderColor: '#fff', borderWidth: 2},
                label: {show: true, color: '#636e72', fontSize: 12, formatter: '{b}'},
                labelLine: {lineStyle: {color: '#e8e8e8'}},
                data: [
                    {value: 38, name: '上衣', itemStyle: {color: '#0d9488'}},
                    {value: 22, name: '裤装', itemStyle: {color: '#3b82f6'}},
                    {value: 15, name: '裙装', itemStyle: {color: '#f59e0b'}},
                    {value: 10, name: '配饰', itemStyle: {color: '#8b5cf6'}},
                    {value: 6, name: '鞋履', itemStyle: {color: '#ec4899'}}
                ]
            }]
        })
        window.addEventListener('resize', () => categoryChart.resize())
    }
}
</script>

<style scoped>
.dashboard {
    width: 100%;
    min-width: 0;
}

.welcome-section {
    margin-bottom: 28px;
}

.welcome-desc {
    font-size: 14px;
    color: var(--text-secondary);
    margin-top: 2px;
}

.section-title {
    font-size: 16px;
    font-weight: 700;
    color: var(--text);
    margin-bottom: 14px;
}

/* ── 工作台 ── */
.workspace-layout {
    display: flex;
    gap: 16px;
    margin-bottom: 30px;
}

.overview-card,
.shortcut-card {
    padding: 20px;
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.overview-card {
    width: 58%;
}

.shortcut-card {
    width: 42%;
}

.workspace-heading {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
}

.workspace-title {
    color: var(--text);
    font-size: 16px;
    font-weight: 800;
}

.workspace-hint {
    padding: 4px 9px;
    border-radius: 999px;
    background: #eef9f7;
    color: var(--primary);
    font-size: 11px;
    font-weight: 600;
}

.overview-card .stat-grid {
    gap: 10px;
    margin-bottom: 0;
}

.overview-card .stat-card {
    width: calc(50% - 5px);
    min-width: 0;
    padding: 14px;
    gap: 11px;
    border-radius: 12px;
    box-shadow: none;
    background: #f9fcfb;
}

.overview-card .stat-card:hover {
    transform: none;
    background: #f1faf8;
    box-shadow: none;
}

.overview-card .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 21px;
    border-radius: 10px;
}

.overview-card .stat-value {
    font-size: 22px;
}

.shortcut-card .quick-grid {
    gap: 10px;
}

.shortcut-card .quick-card {
    width: calc(50% - 5px);
    min-width: 0;
    padding: 14px;
    border-radius: 12px;
    box-shadow: none;
}

.shortcut-card .quick-card:hover {
    box-shadow: 0 8px 18px rgba(22, 83, 78, 0.1);
}

.shortcut-card .quick-icon-box {
    width: 34px;
    height: 34px;
    font-size: 17px;
}

.shortcut-card .quick-desc {
    display: none;
}

/* ── 统计卡片 ── */
.stat-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 28px;
}

.stat-card {
    width: calc(33.33% - 10px);
    min-width: 240px;
    display: flex;
    align-items: center;
    gap: 16px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border-radius: 16px;
    padding: 20px;
    border: 1px solid #e3efed;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
    transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 14px 28px rgba(22, 83, 78, 0.1);
}

.stat-icon {
    font-size: 26px;
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    flex-shrink: 0;
}

.stat-teal {
    background: rgb(204 251 241 / 0.8);
}

.stat-blue {
    background: #e8f0fe;
}

.stat-green {
    background: #dcfce7;
}

.stat-orange {
    background: #fef3c7;
}

.stat-purple {
    background: #f3e8ff;
}

.stat-red {
    background: #fee2e2;
}

.stat-body {
    display: flex;
    flex-direction: column;
}

.stat-value {
    font-size: 26px;
    font-weight: 800;
    color: var(--text);
    line-height: 1.2;
}

.stat-amount {
    font-size: 22px;
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

.stat-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-top: 1px;
}

/* ── 图表 ── */
.chart-row {
    display: flex;
    gap: 14px;
    margin-bottom: 28px;
}

.chart-card {
    width: calc(50% - 7px);
    background: #fff;
    border-radius: 16px;
    border: 1px solid #e3efed;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
    padding: 20px 18px 12px;
}

.chart-header {
    font-size: 14px;
    font-weight: 700;
    color: var(--text);
    margin-bottom: 4px;
    padding-left: 4px;
}

.chart-box {
    width: 100%;
    height: 260px;
}

/* ── 快捷入口 ── */
.quick-grid {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
}

.quick-card {
    width: calc(33.33% - 8px);
    min-width: 240px;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 20px;
    background: linear-gradient(145deg, #fff, #fbfefd);
    border-radius: 16px;
    border: 1px solid #e3efed;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
    text-decoration: none;
    transition: var(--transition);
    cursor: pointer;
}

.quick-card:hover {
    transform: translateY(-3px);
    border-color: #bfe4df;
    box-shadow: 0 14px 28px rgba(22, 83, 78, 0.11);
}

.quick-disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.quick-disabled:hover {
    transform: none;
    box-shadow: var(--shadow);
}

.quick-icon-box {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    margin-bottom: 4px;
}

.quick-name {
    font-size: 14px;
    font-weight: 700;
    color: var(--text);
}

.quick-desc {
    font-size: 12px;
    color: var(--text-muted);
    line-height: 1.4;
}

@media (max-width: 900px) {
    .workspace-layout,
    .chart-row {
        flex-direction: column;
    }

    .overview-card,
    .shortcut-card,
    .chart-card {
        width: 100%;
    }
}

@media (max-width: 560px) {
    .overview-card .stat-card,
    .shortcut-card .quick-card {
        width: 100%;
    }
}
</style>
