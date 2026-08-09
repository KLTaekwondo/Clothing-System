<template>
    <div class="member-add">
        <div class="page-heading">
            <button
                class="btn-outline"
                type="button"
                @click="goBack"
            >← 返回会员</button>
            <div>
                <h2 class="page-title">添加会员</h2>
                <p class="page-desc">创建会员资料并设置初始等级、积分和折扣</p>
            </div>
        </div>

        <div class="member-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-content">
                    <div class="form-row">
                        <div class="form-group">
                            <label>会员姓名</label>
                            <input
                                v-model="form.name"
                                maxlength="30"
                                placeholder="请输入会员姓名"
                                required
                                type="text"
                            />
                        </div>
                        <div class="form-group">
                            <label>手机号</label>
                            <input
                                v-model="form.phone"
                                inputmode="numeric"
                                maxlength="11"
                                placeholder="请输入 11 位手机号"
                                required
                                type="tel"
                            />
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>生日</label>
                            <input
                                v-model="form.birthday"
                                required
                                type="date"
                            />
                        </div>
                        <div class="form-group">
                            <label>会员等级</label>
                            <select
                                v-model="form.level"
                                @change="syncDiscount"
                            >
                                <option
                                    v-for="option in levelOptions"
                                    :key="option.value"
                                    :value="option.value"
                                >{{ option.label }}</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label>初始积分</label>
                            <input
                                v-model.number="form.points"
                                min="0"
                                required
                                step="1"
                                type="number"
                            />
                        </div>
                        <div class="form-group">
                            <label>初始等级点</label>
                            <input
                                v-model.number="form.levelPoints"
                                min="0"
                                required
                                step="1"
                                type="number"
                            />
                        </div>
                    </div>

                    <div class="form-group">
                        <label>会员折扣</label>
                        <input
                            v-model.number="form.discount"
                            :disabled="form.level !== MEMBER_LEVEL.CUSTOMER"
                            max="1"
                            min="0"
                            required
                            step="0.01"
                            type="number"
                        />
                        <span class="field-hint">折扣按 0-1 填写，例如 0.85 表示 8.5 折；自定义会员可手动设置</span>
                    </div>
                </div>

                <div class="form-actions">
                    <button
                        class="btn-outline"
                        type="button"
                        @click="goBack"
                    >取消</button>
                    <button
                        :disabled="submitting || !canSubmit"
                        class="btn-primary"
                        type="submit"
                    >{{ submitting ? '保存中...' : '保存会员' }}</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import memberInterface from '../../../axios/interface/MemberInterface.js'
import usePageDraft from '../../../composables/usePageDraft.js'
import {
    MEMBER_LEVEL,
    MEMBER_LEVEL_DISCOUNTS,
    MEMBER_LEVEL_OPTIONS
} from '../../../constants/memberLevel.js'

const router = useRouter()
const toast = useToastStore()
const levelOptions = MEMBER_LEVEL_OPTIONS
const submitting = ref(false)
const form = ref({
    name: '',
    phone: '',
    birthday: '',
    level: MEMBER_LEVEL.COMMON,
    points: 0,
    levelPoints: 0,
    discount: MEMBER_LEVEL_DISCOUNTS[MEMBER_LEVEL.COMMON]
})

const memberDraft = usePageDraft(
    'clothing_manage_member_add',
    form,
    saved => {
        form.value = {...form.value, ...saved}
    },
    {
        saved: () => submitting.value
    }
)

const canSubmit = computed(() => {
    return Boolean(form.value.name.trim())
        && /^1[3456789]\d{9}$/.test(form.value.phone.trim())
        && Boolean(form.value.birthday)
        && Number.isInteger(Number(form.value.points))
        && Number(form.value.points) >= 0
        && Number.isInteger(Number(form.value.levelPoints))
        && Number(form.value.levelPoints) >= 0
        && Number(form.value.discount) >= 0
        && Number(form.value.discount) <= 1
})

function syncDiscount() {
    if (form.value.level === MEMBER_LEVEL.CUSTOMER) return
    form.value.discount = MEMBER_LEVEL_DISCOUNTS[form.value.level]
}

async function handleSubmit() {
    if (submitting.value || !canSubmit.value) {
        toast.warning('请检查会员资料是否填写完整')
        return
    }
    submitting.value = true
    try {
        await memberInterface.createMemberByManage({
            name: form.value.name.trim(),
            phone: form.value.phone.trim(),
            birthday: form.value.birthday,
            level: form.value.level,
            points: Number(form.value.points),
            levelPoints: Number(form.value.levelPoints),
            discount: Number(form.value.discount)
        })
        memberDraft.clear()
        await router.push('/manage/member')
    } catch {
        // 请求错误由 Axios 拦截器统一提示
    } finally {
        submitting.value = false
    }
}

function goBack() {
    router.push('/manage/member')
}
</script>

<style scoped>
.member-add {
    width: 100%;
    min-width: 0;
}

.page-heading {
    display: flex;
    align-items: center;
    gap: 14px;
    margin-bottom: 24px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.member-form-card {
    width: 100%;
    max-width: 760px;
    box-sizing: border-box;
    padding: 28px;
    margin-right: auto;
    margin-left: auto;
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
}

.form-content {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.form-row {
    display: flex;
    gap: 16px;
}

.form-group {
    width: 100%;
}

.form-group label {
    display: block;
    margin-bottom: 7px;
    color: var(--text);
    font-size: 13px;
    font-weight: 600;
}

.form-group input,
.form-group select {
    width: 100%;
}

.field-hint {
    display: block;
    margin-top: 6px;
    color: var(--text-muted);
    font-size: 12px;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 24px;
    padding-top: 20px;
    border-top: 1px solid var(--border-light);
}

@media (max-width: 640px) {
    .page-heading,
    .form-row {
        align-items: flex-start;
        flex-direction: column;
    }

    .member-form-card {
        padding: 18px;
    }

    .form-actions {
        flex-direction: column-reverse;
    }

    .form-actions button {
        width: 100%;
    }
}
</style>
