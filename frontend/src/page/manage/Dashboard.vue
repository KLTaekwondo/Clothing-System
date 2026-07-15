<template>
    <div class="dashboard">
        <div class="welcome-section">
            <div class="welcome-text">
                <h2 class="page-title">控制台</h2>
                <p class="welcome-desc">欢迎回来，今天也是元气满满的一天！</p>
            </div>
        </div>

        <div class="section-title">数据总览</div>
        <div class="stat-grid">
            <div class="stat-card">
                <div class="stat-icon stat-teal">📦</div>
                <div class="stat-body">
                    <span class="stat-value">{{ stats.products }}</span>
                    <span class="stat-label">商品总数</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-blue">🏷️</div>
                <div class="stat-body">
                    <span class="stat-value">{{ stats.skus }}</span>
                    <span class="stat-label">SKU 总数</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-green">👤</div>
                <div class="stat-body">
                    <span class="stat-value">{{ stats.employees }}</span>
                    <span class="stat-label">员工总数</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-orange">🏭</div>
                <div class="stat-body">
                    <span class="stat-value">{{ stats.warehouses }}</span>
                    <span class="stat-label">仓库总数</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-purple">📋</div>
                <div class="stat-body">
                    <span class="stat-value">{{ stats.orders }}</span>
                    <span class="stat-label">订单总数</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-red">⚠️</div>
                <div class="stat-body">
                    <span class="stat-value">{{ stats.lowStock }}</span>
                    <span class="stat-label">库存预警</span>
                </div>
            </div>
        </div>

        <div class="section-title">财务概览</div>
        <div class="stat-grid">
            <div class="stat-card">
                <div class="stat-icon stat-teal">💰</div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.todaySales }}</span>
                    <span class="stat-label">今日销售额</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-blue">📈</div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.monthlySales }}</span>
                    <span class="stat-label">本月销售额</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-green">💵</div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.todayProfit }}</span>
                    <span class="stat-label">今日毛利</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-purple">📊</div>
                <div class="stat-body">
                    <span class="stat-value">{{ finance.grossMargin }}</span>
                    <span class="stat-label">毛利率</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-orange">🧾</div>
                <div class="stat-body">
                    <span class="stat-value stat-amount">{{ finance.todayCost }}</span>
                    <span class="stat-label">今日成本</span>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon stat-red">🛒</div>
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

        <div class="section-title">快捷入口</div>
        <div class="quick-grid">
            <router-link to="/manage/product" class="quick-card">
                <span class="quick-icon-box" style="background:#dcfce7">📦</span>
                <span class="quick-name">商品管理</span>
                <span class="quick-desc">添加、编辑和管理商品</span>
            </router-link>
            <router-link to="/manage/employee" class="quick-card">
                <span class="quick-icon-box" style="background:#e8f0fe">👤</span>
                <span class="quick-name">员工管理</span>
                <span class="quick-desc">管理员工信息与核验</span>
            </router-link>
            <router-link to="/manage/warehouse" class="quick-card">
                <span class="quick-icon-box" style="background:#fef3c7">🏭</span>
                <span class="quick-name">仓库管理</span>
                <span class="quick-desc">仓库与库存调拨</span>
            </router-link>
            <router-link to="/manage/option" class="quick-card">
                <span class="quick-icon-box" style="background:#fce7f3">🏷️</span>
                <span class="quick-name">选项管理</span>
                <span class="quick-desc">颜色、尺寸等选项值</span>
            </router-link>
            <router-link to="/checkout" class="quick-card">
                <span class="quick-icon-box" style="background:#fef3c7">🛒</span>
                <span class="quick-name">收银结账</span>
                <span class="quick-desc">创建订单与结算</span>
            </router-link>
            <div class="quick-card quick-disabled">
                <span class="quick-icon-box" style="background:#f1f5f9">📊</span>
                <span class="quick-name">数据报表</span>
                <span class="quick-desc">销售统计与分析（即将上线）</span>
            </div>
        </div>

        <div v-if="loading" class="loading-overlay">
            <div class="loading-spinner"></div>
            <span style="margin-left:10px;color:var(--text-muted)">加载中…</span>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import productInterface from '../../axios/interface/ProductInterface.js'
import productSkuInterface from '../../axios/interface/ProductSkuInterface.js'
import employeeInterface from '../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../axios/interface/WareHouseInterface.js'
import orderInterface from '../../axios/interface/OrderInterface.js'
import { STATUS } from '../../constants/status.js'

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
            productInterface.searchList().catch(() => []),
            employeeInterface.searchList().catch(() => []),
            wareHouseInterface.searchList().catch(() => []),
            orderInterface.searchList().catch(() => [])
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
            tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
            grid: { left: '3%', right: '3%', bottom: '8%', top: '8%', containLabel: true },
            xAxis: {
                type: 'category',
                data: salesDays,
                axisLine: { show: false },
                axisTick: { show: false },
                axisLabel: { color: '#94a3b8', fontSize: 11 }
            },
            yAxis: {
                type: 'value',
                splitLine: { lineStyle: { color: '#f1f5f9' } },
                axisLabel: { color: '#94a3b8', fontSize: 11 }
            },
            series: [{
                data: salesData,
                type: 'line',
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { color: '#0d9488', width: 2 },
                itemStyle: { color: '#0d9488' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(13,148,136,0.25)' },
                        { offset: 1, color: 'rgba(13,148,136,0.02)' }
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
            tooltip: { trigger: 'item' },
            series: [{
                type: 'pie',
                radius: ['45%', '70%'],
                center: ['50%', '50%'],
                avoidLabelOverlap: true,
                itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
                label: { show: true, color: '#636e72', fontSize: 12, formatter: '{b}' },
                labelLine: { lineStyle: { color: '#e8e8e8' } },
                data: [
                    { value: 38, name: '上衣', itemStyle: { color: '#0d9488' } },
                    { value: 22, name: '裤装', itemStyle: { color: '#3b82f6' } },
                    { value: 15, name: '裙装', itemStyle: { color: '#f59e0b' } },
                    { value: 10, name: '配饰', itemStyle: { color: '#8b5cf6' } },
                    { value: 6,  name: '鞋履', itemStyle: { color: '#ec4899' } }
                ]
            }]
        })
        window.addEventListener('resize', () => categoryChart.resize())
    }
}
</script>

<style scoped>
.dashboard {
    max-width: 1100px;
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
    background: #fff;
    border-radius: 12px;
    padding: 18px 20px;
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow);
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

.stat-teal { background: rgb(204 251 241 / 0.8); }
.stat-blue { background: #e8f0fe; }
.stat-green { background: #dcfce7; }
.stat-orange { background: #fef3c7; }
.stat-purple { background: #f3e8ff; }
.stat-red { background: #fee2e2; }

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
    flex: 1;
    background: #fff;
    border-radius: 12px;
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow);
    padding: 18px 16px 10px;
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
    padding: 18px 20px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow);
    text-decoration: none;
    transition: var(--transition);
    cursor: pointer;
}

.quick-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-hover);
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
</style>
