<template>
    <div class="warehouse-add">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">添加仓库</h2>
                <p class="page-label-desc">创建仓库并自动绑定当前管理员</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            type="button"
                            @click="goBack"
                        >← 返回仓库</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="warehouse-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-content">
                    <div class="form-group">
                        <label>仓库编码</label>
                        <input
                            v-model="form.code"
                            maxlength="10"
                            minlength="2"
                            placeholder="请输入仓库编码"
                            required
                            type="text"
                        />
                        <span class="field-hint">用于仓库登录与业务记录，长度为 2-10 个字符</span>
                    </div>

                    <div class="form-group">
                        <label>仓库名称</label>
                        <input
                            v-model="form.name"
                            maxlength="10"
                            minlength="2"
                            placeholder="请输入仓库名称"
                            required
                            type="text"
                        />
                    </div>

                    <div class="form-group">
                        <label>仓库密码</label>
                        <input
                            v-model="form.password"
                            maxlength="12"
                            minlength="6"
                            pattern="[a-zA-Z0-9_]{6,12}"
                            placeholder="请输入 6-12 位字母、数字或下划线"
                            required
                            type="password"
                        />
                        <span class="field-hint">用于仓库账号登录，仅支持字母、数字和下划线</span>
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
                    >{{ submitting ? '保存中...' : '保存仓库' }}</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import usePageDraft from '../../../composables/usePageDraft.js'

const router = useRouter()
const submitting = ref(false)
const form = ref({
    code: '',
    name: '',
    password: ''
})

const warehouseDraft = usePageDraft(
    'clothing_manage_warehouse_add',
    form,
    saved => {
        form.value = {...form.value, ...saved}
    },
    {
        saved: () => submitting.value,
        onRestored: () => {}
    }
)

const canSubmit = computed(() => {
    const passwordPattern = /^[a-zA-Z0-9_]{6,12}$/
    return form.value.code.trim().length >= 2
        && form.value.name.trim().length >= 2
        && passwordPattern.test(form.value.password)
})

async function handleSubmit() {
    if (submitting.value || !canSubmit.value) return
    submitting.value = true
    try {
        await wareHouseInterface.create({
            code: form.value.code.trim(),
            name: form.value.name.trim(),
            password: form.value.password
        })
        warehouseDraft.clear()
        await router.push('/manage/warehouse')
    } catch {
        // 请求错误由 Axios 拦截器统一提示
    } finally {
        submitting.value = false
    }
}

function goBack() {
    router.push('/manage/warehouse')
}
</script>

<style scoped>
.warehouse-add {
    width: 100%;
    min-width: 0;
}

.warehouse-form-card {
    width: 100%;
    max-width: 620px;
    box-sizing: border-box;
    padding: 28px;
    margin-right: auto;
    margin-left: auto;
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
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

.form-group input {
    width: 100%;
}

@media (max-width: 640px) {

    .warehouse-form-card {
        padding: 18px;
    }

    .form-actions button {
        width: 100%;
    }
}
</style>
