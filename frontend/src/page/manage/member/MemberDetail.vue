<template>
    <div class="member-detail">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    type="button"
                    @click="goBack"
                >← 返回会员</button>
                <div>
                    <h2 class="page-title">会员详情</h2>
                    <p class="page-desc">查看和维护会员资料、等级与积分</p>
                </div>
            </div>
            <div
                v-if="member"
                class="heading-actions"
            >
                <template v-if="!editing">
                    <button
                        class="btn-primary"
                        type="button"
                        @click="startEdit"
                    >编辑会员</button>
                </template>
                <template v-else>
                    <button
                        :disabled="saving"
                        class="btn-outline"
                        type="button"
                        @click="cancelEdit"
                    >取消</button>
                    <button
                        :disabled="saving || !canSave"
                        class="btn-primary"
                        type="button"
                        @click="saveEdit"
                    >{{ saving ? '保存中...' : '保存修改' }}</button>
                </template>
            </div>
        </div>

        <div
            v-if="loading"
            class="loading-overlay"
        >
            <div class="loading-spinner"></div>
        </div>
        <div
            v-else-if="!member"
            class="empty-state"
        >
            <div class="empty-icon"><IconGraphic name="user"/></div>
            <div class="empty-text">会员不存在</div>
        </div>
        <div
            v-else
            class="detail-list"
        >
            <div class="detail-card">
                <span>会员姓名</span>
                <input
                    v-if="editing"
                    v-model="form.name"
                    maxlength="30"
                    type="text"
                />
                <strong v-else>{{ member.memberName || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span>手机号</span>
                <strong class="phone-value">{{ member.memberPhone || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span>会员等级</span>
                <select
                    v-if="editing"
                    v-model="form.level"
                    @change="syncDiscount"
                >
                    <option
                        v-for="option in levelOptions"
                        :key="option.value"
                        :value="option.value"
                    >{{ option.label }}</option>
                </select>
                <strong v-else>{{ levelLabels[member.memberLevel] || member.memberLevel || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span>会员积分</span>
                <input
                    v-if="editing"
                    v-model.number="form.points"
                    min="0"
                    step="1"
                    type="number"
                />
                <strong v-else>{{ member.memberPoints ?? 0 }}</strong>
            </div>
            <div class="detail-card">
                <span>会员等级点</span>
                <input
                    v-if="editing"
                    v-model.number="form.levelPoints"
                    min="0"
                    step="1"
                    type="number"
                />
                <strong v-else>{{ member.memberLevelPoints ?? 0 }}</strong>
            </div>
            <div class="detail-card">
                <span>会员折扣</span>
                <input
                    v-if="editing"
                    v-model.number="form.discount"
                    :disabled="form.level !== MEMBER_LEVEL.CUSTOMER"
                    max="1"
                    min="0"
                    step="0.01"
                    type="number"
                />
                <strong v-else>{{ formatDiscount(member.memberDiscount) }}</strong>
            </div>
            <div class="detail-card">
                <span>生日</span>
                <input
                    v-if="editing"
                    v-model="form.birthday"
                    type="date"
                />
                <strong v-else>{{ member.memberBirthday || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span>创建时间</span>
                <strong>{{ member.createTime || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span>更新时间</span>
                <strong>{{ member.updateTime || '-' }}</strong>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import memberInterface from '../../../axios/interface/MemberInterface.js'
import {
    MEMBER_LEVEL,
    MEMBER_LEVEL_DISCOUNTS,
    MEMBER_LEVEL_LABELS,
    MEMBER_LEVEL_OPTIONS
} from '../../../constants/memberLevel.js'

const route = useRoute()
const router = useRouter()
const toast = useToastStore()
const levelLabels = MEMBER_LEVEL_LABELS
const levelOptions = MEMBER_LEVEL_OPTIONS
const member = ref(null)
const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const form = ref({})
const formBackup = ref(null)

const canSave = computed(() => {
    return Boolean(String(form.value.name || '').trim())
        && Boolean(form.value.birthday)
        && Number.isInteger(Number(form.value.points))
        && Number(form.value.points) >= 0
        && Number.isInteger(Number(form.value.levelPoints))
        && Number(form.value.levelPoints) >= 0
        && Number(form.value.discount) >= 0
        && Number(form.value.discount) <= 1
})

onMounted(fetchMember)

async function fetchMember() {
    loading.value = true
    try {
        member.value = await memberInterface.searchMember(route.params.phone)
    } catch {
        member.value = null
    } finally {
        loading.value = false
    }
}

function startEdit() {
    form.value = {
        name: member.value.memberName || '',
        level: member.value.memberLevel || MEMBER_LEVEL.COMMON,
        points: Number(member.value.memberPoints || 0),
        levelPoints: Number(member.value.memberLevelPoints || 0),
        birthday: member.value.memberBirthday || '',
        discount: Number(member.value.memberDiscount || MEMBER_LEVEL_DISCOUNTS[MEMBER_LEVEL.COMMON])
    }
    formBackup.value = {...form.value}
    editing.value = true
}

function cancelEdit() {
    form.value = {...formBackup.value}
    formBackup.value = null
    editing.value = false
}

function syncDiscount() {
    if (form.value.level === MEMBER_LEVEL.CUSTOMER) return
    form.value.discount = MEMBER_LEVEL_DISCOUNTS[form.value.level]
}

async function saveEdit() {
    if (saving.value || !canSave.value) {
        toast.warning('请检查会员资料是否填写完整')
        return
    }
    saving.value = true
    try {
        await memberInterface.updateMember(member.value.id, {
            name: form.value.name.trim(),
            level: form.value.level,
            points: Number(form.value.points),
            levelPoints: Number(form.value.levelPoints),
            birthday: form.value.birthday,
            discount: Number(form.value.discount)
        })
        member.value = await memberInterface.searchMember(route.params.phone)
        editing.value = false
        formBackup.value = null
    } catch {
        // 请求错误由 Axios 拦截器统一提示
    } finally {
        saving.value = false
    }
}

function formatDiscount(value) {
    const discount = Number(value)
    if (!Number.isFinite(discount)) return '-'
    return `${(discount * 10).toFixed(1)} 折`
}

function goBack() {
    router.push('/manage/member')
}
</script>

<style scoped>
.member-detail {
    width: 100%;
    min-width: 0;
}



.detail-card {
    width: calc(33.33% - 10px);
    min-width: 220px;
    min-height: 112px;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: 12px;
    padding: 20px;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 14px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.detail-card > span {
    color: var(--text-muted);
    font-size: 13px;
}

.detail-card input,
.detail-card select {
    width: 100%;
    height: 38px;
}

.phone-value {
    color: var(--primary);
    font-family: ui-monospace, 'SF Mono', Consolas, monospace;
}

@media (max-width: 760px) {

    .heading-actions button {
        width: calc(50% - 6px);
    }

    .detail-card {
        width: 100%;
    }
}
</style>
