<template>
    <div class="member-manage">
        

        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">会员管理</h2>
                <p class="page-label-desc">管理会员资料、等级、积分与折扣</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
            <div class="search-label">
            <svg class="search-icon" viewBox="0 0 20 20" fill="none" width="14" height="14">
                <circle cx="9" cy="9" r="6" stroke="currentColor" stroke-width="1.5"/>
                <path d="M14 14l4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>会员检索</span>
        </div><div class="search-shell">
            <div class="search-controls">
                <div class="filter-tabs">
                    <button
                        v-for="option in filterOptions"
                        :key="option.value"
                        :class="levelFilter === option.value ? 'filter-tab-active' : 'filter-tab'"
                        type="button"
                        @click="levelFilter = option.value"
                    >{{ option.label }}</button>
                </div>
                <input
                    v-model="searchQuery"
                    class="search-code-input"
                    placeholder="搜索会员姓名或手机号"
                    type="text"
                />
                <router-link
                    class="btn-primary search-button"
                    to="/manage/member/add"
                >+ 添加会员</router-link>
            </div>
        </div>
            </div>
        </div>

        <div class="stats-summary">
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="user"/></span>
                <span class="stats-body">
                    <strong>{{ memberList.length }}</strong>
                    <span>会员总数</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="user"/></span>
                <span class="stats-body">
                    <strong>{{ vipCount }}</strong>
                    <span>VIP 会员</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="user"/></span>
                <span class="stats-body">
                    <strong>{{ mvpCount }}</strong>
                    <span>高级会员</span>
                </span>
            </div>
            <div class="stats-item">
                <span class="stats-icon"><IconGraphic name="tag"/></span>
                <span class="stats-body">
                    <strong>{{ customCount }}</strong>
                    <span>自定义会员</span>
                </span>
            </div>
        </div>

        <div class="member-card">
            <div
                v-if="loading"
                class="loading-overlay"
            >
                <div class="loading-spinner"></div>
            </div>
            <div
                v-else-if="filteredList.length === 0"
                class="empty-state"
            >
                <div class="empty-icon"><IconGraphic name="user"/></div>
                <div class="empty-text">暂无会员数据</div>
            </div>
            <table
                v-else
                class="data-table"
            >
                <thead>
                <tr>
                    <th>会员姓名</th>
                    <th>手机号</th>
                    <th>会员等级</th>
                    <th>积分</th>
                    <th>等级点</th>
                    <th>折扣</th>
                    <th>生日</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr
                    v-for="item in filteredList"
                    :key="item.id"
                    @dblclick="goDetail(item)"
                >
                    <td><strong>{{ item.memberName || '-' }}</strong></td>
                    <td><code>{{ item.memberPhone || '-' }}</code></td>
                    <td>
                        <span :class="levelClass(item.memberLevel)">
                            {{ levelLabels[item.memberLevel] || item.memberLevel || '-' }}
                        </span>
                    </td>
                    <td>{{ item.memberPoints ?? 0 }}</td>
                    <td>{{ item.memberLevelPoints ?? 0 }}</td>
                    <td>{{ formatDiscount(item.memberDiscount) }}</td>
                    <td>{{ item.memberBirthday || '-' }}</td>
                    <td>
                        <router-link
                            :to="`/manage/member/${item.memberPhone}`"
                            class="btn-outline"
                        >详情/编辑</router-link>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import memberInterface from '../../../axios/interface/MemberInterface.js'
import {
    MEMBER_LEVEL,
    MEMBER_LEVEL_LABELS,
    MEMBER_LEVEL_OPTIONS
} from '../../../constants/memberLevel.js'

const router = useRouter()
const levelLabels = MEMBER_LEVEL_LABELS
const filterOptions = [
    {value: '', label: '全部'},
    ...MEMBER_LEVEL_OPTIONS
]
const memberList = ref([])
const searchQuery = ref('')
const levelFilter = ref('')
const loading = ref(true)

const filteredList = computed(() => {
    const query = searchQuery.value.trim().toLowerCase()
    return memberList.value.filter(item => {
        const matchesLevel = !levelFilter.value || item.memberLevel === levelFilter.value
        const matchesQuery = !query || [item.memberName, item.memberPhone]
            .some(value => String(value || '').toLowerCase().includes(query))
        return matchesLevel && matchesQuery
    })
})

const vipCount = computed(() => memberList.value.filter(item => item.memberLevel === MEMBER_LEVEL.VIP).length)
const mvpCount = computed(() => memberList.value.filter(item => item.memberLevel === MEMBER_LEVEL.MVP).length)
const customCount = computed(() => memberList.value.filter(item => item.memberLevel === MEMBER_LEVEL.CUSTOMER).length)

onMounted(fetchList)

async function fetchList() {
    loading.value = true
    try {
        memberList.value = await memberInterface.searchMemberList()
    } catch {
        memberList.value = []
    } finally {
        loading.value = false
    }
}

function goDetail(item) {
    router.push(`/manage/member/${item.memberPhone}`)
}

function levelClass(level) {
    if (level === MEMBER_LEVEL.MVP) return 'member-level-mvp'
    if (level === MEMBER_LEVEL.VIP) return 'member-level-vip'
    if (level === MEMBER_LEVEL.CUSTOMER) return 'member-level-customer'
    return 'member-level-common'
}

function formatDiscount(value) {
    const discount = Number(value)
    if (!Number.isFinite(discount)) return '-'
    return `${(discount * 10).toFixed(1)} 折`
}
</script>

<style scoped>
.member-manage {
    width: 100%;
    min-width: 0;
}

/* ── 会员总览条（使用全局 stats-summary） ── */

.member-card {
    padding: 8px 20px 20px;
    overflow-x: auto;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: var(--shadow);
}

.data-table {
    min-width: 920px;
}

.data-table th,
.data-table td {
    text-align: center;
}

.data-table tbody tr {
    cursor: pointer;
}

.member-level-common,
.member-level-vip,
.member-level-mvp,
.member-level-customer {
    display: inline-block;
    min-width: 72px;
    padding: 4px 9px;
    border-radius: 10px;
    font-size: 12px;
    font-weight: 700;
}

.member-level-common {
    color: var(--text-secondary);
    background: #f1f5f9;
}

.member-level-vip {
    color: var(--primary-dark);
    background: var(--bg-hover);
}

.member-level-mvp {
    color: var(--warning-dark);
    background: var(--warning-light);
}

.member-level-customer {
    color: #7e22ce;
    background: color-mix(in srgb, var(--info) 12%, transparent);
}

@media (max-width: 900px) {
    .page-heading {
        align-items: flex-start;
        flex-direction: column;
        gap: 14px;
    }
}

@media (max-width: 560px) {
}
</style>
