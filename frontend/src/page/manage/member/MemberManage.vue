<template>
    <div class="member-manage">
        <div class="page-heading">
            <div>
                <h2 class="page-title">会员管理</h2>
                <p class="page-desc">管理会员资料、等级、积分与折扣</p>
            </div>
            <router-link
                class="btn-primary"
                to="/manage/member/add"
            >+ 添加会员</router-link>
        </div>

        <div class="member-summary">
            <div class="summary-item">
                <span>会员总数</span>
                <strong>{{ memberList.length }}</strong>
            </div>
            <div class="summary-item">
                <span>VIP 会员</span>
                <strong>{{ vipCount }}</strong>
            </div>
            <div class="summary-item">
                <span>高级会员</span>
                <strong>{{ mvpCount }}</strong>
            </div>
            <div class="summary-item">
                <span>累计积分</span>
                <strong>{{ totalPoints }}</strong>
            </div>
        </div>

        <div class="member-card">
            <div class="member-toolbar">
                <div class="level-tabs">
                    <button
                        v-for="option in filterOptions"
                        :key="option.value"
                        :class="levelFilter === option.value ? 'level-tab-active' : 'level-tab'"
                        type="button"
                        @click="levelFilter = option.value"
                    >{{ option.label }}</button>
                </div>
                <input
                    v-model="searchQuery"
                    class="member-search"
                    placeholder="搜索会员姓名或手机号"
                    type="text"
                />
            </div>

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
const totalPoints = computed(() => memberList.value.reduce((sum, item) => sum + Number(item.memberPoints || 0), 0))

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

.page-heading {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    margin-bottom: 22px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.member-summary {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
    margin-bottom: 18px;
}

.summary-item {
    width: calc(25% - 11px);
    min-width: 160px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    padding: 17px;
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 12px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.summary-item span {
    color: var(--text-muted);
    font-size: 13px;
}

.summary-item strong {
    color: var(--primary);
    font-size: 21px;
}

.member-card {
    padding: 8px 20px 20px;
    overflow-x: auto;
    background: #fff;
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
}

.member-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 10px 0 16px;
    border-bottom: 1px solid var(--border-light);
}

.level-tabs {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
}

.level-tab,
.level-tab-active {
    padding: 7px 13px;
    color: var(--text-secondary);
    background: transparent;
    border-radius: 8px;
    font-size: 13px;
}

.level-tab:hover,
.level-tab-active {
    color: var(--primary);
    background: var(--primary-light);
}

.level-tab-active {
    font-weight: 700;
}

.member-search {
    width: 280px;
    height: 36px;
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
    color: #475569;
    background: #f1f5f9;
}

.member-level-vip {
    color: #0f766e;
    background: #e8f7f3;
}

.member-level-mvp {
    color: #b45309;
    background: #fef3c7;
}

.member-level-customer {
    color: #7e22ce;
    background: #f3e8ff;
}

@media (max-width: 900px) {
    .page-heading,
    .member-toolbar {
        align-items: flex-start;
        flex-direction: column;
    }

    .summary-item {
        width: calc(50% - 7px);
    }

    .member-search {
        width: 100%;
    }
}

@media (max-width: 560px) {
    .summary-item {
        width: 100%;
    }
}
</style>
