<template>
  <div class="page-container">
    <div class="page-header-modern">
      <div class="page-heading-block">
        <div class="page-eyebrow">概览</div>
        <h1 class="page-title-modern">数据看板</h1>
        <p class="page-subtitle-modern">聚合商品、库存、销售与团队状态，快速掌握今日经营节奏。</p>
      </div>
      <div class="action-row">
        <router-link class="btn-modern-primary" to="/dashboard/checkout">快速开单</router-link>
        <router-link class="btn-modern-soft" to="/dashboard/products/create">新增商品</router-link>
      </div>
    </div>

    <section class="metric-grid">
      <article v-for="metric in metrics" :key="metric.label" :class="metric.cardClass">
        <div class="metric-label">{{ metric.label }}</div>
        <div class="metric-value">{{ metric.value }}</div>
        <p class="metric-hint">{{ metric.hint }}</p>
      </article>
    </section>

    <section class="dashboard-row">
      <article class="dashboard-card-wide">
        <div class="card-title-row">
          <h2 class="card-title-text">常用操作</h2>
          <span class="card-note-text">高频业务入口</span>
        </div>
        <div class="quick-actions">
          <router-link v-for="action in actions" :key="action.title" class="quick-action" :to="action.to">
            <span class="quick-icon">{{ action.icon }}</span>
            <span class="quick-title">{{ action.title }}</span>
            <span class="quick-desc">{{ action.desc }}</span>
          </router-link>
        </div>
      </article>

      <article class="dashboard-card-side">
        <div class="card-title-row">
          <h2 class="card-title-text">待关注</h2>
          <span class="card-note-text">经营提醒</span>
        </div>
        <div class="attention-list">
          <div v-for="item in attention" :key="item.title" class="attention-item">
            <span :class="item.dotClass"></span>
            <div class="attention-copy">
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="dashboard-row">
      <article class="dashboard-card-wide">
        <div class="card-title-row">
          <h2 class="card-title-text">近期动态</h2>
          <span class="card-note-text">示例占位</span>
        </div>
        <div class="timeline-list">
          <div v-for="item in timeline" :key="item.title" class="timeline-item">
            <span class="timeline-time">{{ item.time }}</span>
            <div class="timeline-copy">
              <strong>{{ item.title }}</strong>
              <span>{{ item.desc }}</span>
            </div>
          </div>
        </div>
      </article>

      <article class="dashboard-card-side">
        <div class="card-title-row">
          <h2 class="card-title-text">系统状态</h2>
          <span class="status-pill-active">运行中</span>
        </div>
        <div class="system-panel">
          <p>前端已连接管理控制台，后续可接入真实报表接口展示销售趋势、库存预警与员工绩效。</p>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup>
const metrics = [
  { label: '商品总数', value: '--', hint: '等待商品接口数据接入', cardClass: 'metric-card' },
  { label: '本月订单', value: '--', hint: '订单统计将在报表模块展示', cardClass: 'metric-card-accent' },
  { label: '本月销售额', value: '--', hint: '支持后续接入销售流水', cardClass: 'metric-card' },
  { label: '库存预警', value: '--', hint: '关注低库存 SKU 与仓库补货', cardClass: 'metric-card-accent' }
]

const actions = [
  { title: '销售开单', desc: '快速创建销售订单', icon: '¥', to: '/dashboard/checkout' },
  { title: '商品管理', desc: '维护商品基础信息', icon: '衣', to: '/dashboard/products' },
  { title: '库存管理', desc: '查看仓库库存状态', icon: '仓', to: '/dashboard/inventory' },
  { title: '销售报表', desc: '分析销售表现', icon: '报', to: '/dashboard/reports' }
]

const attention = [
  { title: '库存数据待接入', desc: '可在库存模块完善实时库存接口', dotClass: 'attention-dot-warning' },
  { title: '报表模块建设中', desc: '销售趋势图与汇总统计后续扩展', dotClass: 'attention-dot-info' },
  { title: '商品资料可优化', desc: '建议补充图片、规格和 SKU 信息', dotClass: 'attention-dot-success' }
]

const timeline = [
  { time: '今天', title: '控制台视觉升级', desc: '统一侧边栏、标签页、卡片和页面结构。' },
  { time: '本周', title: '基础资料管理', desc: '仓库与销售员模块已具备列表和表单交互。' },
  { time: '后续', title: '经营数据可视化', desc: '可接入销售、库存与订单统计接口。' }
]
</script>

<style scoped>
.dashboard-row {
  display: flex;
  align-items: stretch;
  gap: 16px;
  margin-top: 16px;
}

.dashboard-card-wide,
.dashboard-card-side {
  padding: 18px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.dashboard-card-wide {
  width: calc(66.666% - 8px);
}

.dashboard-card-side {
  width: calc(33.333% - 8px);
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-action {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: calc(25% - 9px);
  min-width: 140px;
  padding: 14px;
  color: var(--text);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface-soft);
  transition: border-color var(--transition-fast), background var(--transition-fast), transform var(--transition-fast), box-shadow var(--transition-fast);
}

.quick-action:hover {
  border-color: var(--primary-lighter);
  background: var(--primary-50);
  box-shadow: var(--shadow-sm);
  transform: translateY(-2px);
}

.quick-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  color: var(--primary);
  background: var(--surface);
  border-radius: 12px;
  font-weight: 820;
}

.quick-title {
  font-size: 14px;
  font-weight: 780;
}

.quick-desc {
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.5;
}

.attention-list,
.timeline-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attention-item,
.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--surface-soft);
}

.attention-dot-warning,
.attention-dot-info,
.attention-dot-success {
  width: 9px;
  height: 9px;
  margin-top: 5px;
  border-radius: 999px;
  flex-shrink: 0;
}

.attention-dot-warning {
  background: var(--warning);
}

.attention-dot-info {
  background: var(--info);
}

.attention-dot-success {
  background: var(--success);
}

.attention-copy,
.timeline-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.attention-copy strong,
.timeline-copy strong {
  color: var(--text);
  font-size: 13px;
}

.attention-copy span,
.timeline-copy span,
.system-panel p {
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.65;
}

.timeline-time {
  width: 44px;
  color: var(--primary);
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
}

.system-panel {
  padding: 14px;
  border-radius: var(--radius-md);
  background: var(--primary-50);
  border: 1px solid var(--primary-lighter);
}

@media (max-width: 1100px) {
  .dashboard-row {
    flex-direction: column;
  }

  .dashboard-card-wide,
  .dashboard-card-side {
    width: 100%;
  }
}

@media (max-width: 720px) {
  .quick-action {
    width: 100%;
  }
}
</style>
