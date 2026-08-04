<template>
    <div class="supplier-add">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    @click="requestBack"
                >← 返回供应商</button>
                <div>
                    <h2 class="page-title">添加供应商</h2>
                    <p class="page-desc">创建可用于采购订单的供应商资料</p>
                </div>
            </div>
        </div>

        <div class="card form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-row">
                    <div class="form-group">
                        <label>供应商编码</label>
                        <input
                            v-model="form.supplierCode"
                            maxlength="20"
                            minlength="1"
                            placeholder="请输入供应商编码"
                            required
                            type="text"
                        />
                    </div>
                    <div class="form-group">
                        <label>供应商名称</label>
                        <input
                            v-model="form.supplierName"
                            maxlength="20"
                            minlength="1"
                            placeholder="请输入供应商名称"
                            required
                            type="text"
                        />
                    </div>
                </div>
                <div class="form-row">
                    <div class="form-group">
                        <label>联系电话</label>
                        <input
                            v-model="form.contactPhone"
                            placeholder="请输入联系电话"
                            type="text"
                        />
                    </div>
                    <div class="form-group">
                        <label>备注</label>
                        <input
                            v-model="form.remark"
                            placeholder="请输入备注"
                            type="text"
                        />
                    </div>
                </div>
                <div class="form-actions">
                    <button
                        class="btn-outline"
                        type="button"
                        @click="requestBack"
                    >取消</button>
                    <button
                        :disabled="submitting || !canSubmit"
                        class="btn-primary"
                        type="submit"
                    >{{ submitting ? '保存中...' : '保存供应商' }}</button>
                </div>
            </form>
        </div>

        <div
            v-if="showLeaveConfirm"
            class="modal-overlay"
            @click.self="cancelLeave"
        >
            <div class="modal-content confirm-modal">
                <div class="modal-body">
                    <div class="confirm-box">
                        <div class="confirm-icon"><IconGraphic name="warning"/></div>
                        <div class="confirm-msg">当前供应商资料尚未保存，确定离开吗？</div>
                        <div class="confirm-hint">未保存的输入内容将会丢失</div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button
                        class="btn-outline"
                        @click="cancelLeave"
                    >继续编辑</button>
                    <button
                        class="btn-danger"
                        @click="confirmLeave"
                    >确定离开</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {computed, ref} from 'vue'
import {onBeforeRouteLeave, useRouter} from 'vue-router'
import {useToastStore} from '../../../stores/toastStore.js'
import {useUserStore} from '../../../stores/userStore.js'
import supplierInterface from '../../../axios/interface/SupplierInterface.js'

const router = useRouter()
const toast = useToastStore()
const userStore = useUserStore()
const submitting = ref(false)
const saved = ref(false)
const showLeaveConfirm = ref(false)
let pendingNavigation = null

const form = ref({
    supplierCode: '',
    supplierName: '',
    contactPhone: '',
    remark: ''
})

const canSubmit = computed(() => {
    return form.value.supplierCode.trim() && form.value.supplierName.trim()
})

const isDirty = computed(() => {
    return Object.values(form.value).some(value => String(value || '').trim())
})

onBeforeRouteLeave((to, from, next) => {
    if (saved.value || !isDirty.value) {
        next()
        return
    }
    pendingNavigation = next
    showLeaveConfirm.value = true
})

async function handleSubmit() {
    if (submitting.value || !canSubmit.value) return
    const adminId = Number(userStore.userInfo?.id || 0)
    if (!adminId) {
        toast.warning('未获取到当前管理员信息')
        return
    }
    submitting.value = true
    try {
        await supplierInterface.create({
            supplierCode: form.value.supplierCode.trim(),
            supplierName: form.value.supplierName.trim(),
            contactPhone: form.value.contactPhone.trim(),
            remark: form.value.remark.trim(),
            adminId
        })
        saved.value = true
        await router.push('/manage/supplier')
    } catch {
        // 拦截器已处理
    } finally {
        submitting.value = false
    }
}

function requestBack() {
    router.push('/manage/supplier')
}

function cancelLeave() {
    showLeaveConfirm.value = false
    const navigation = pendingNavigation
    pendingNavigation = null
    navigation?.(false)
}

function confirmLeave() {
    showLeaveConfirm.value = false
    saved.value = true
    const navigation = pendingNavigation
    pendingNavigation = null
    navigation?.()
}
</script>

<style scoped>
.supplier-add {
    width: 100%;
    min-width: 0;
}

.page-heading {
    margin-bottom: 28px;
}

.heading-left {
    display: flex;
    align-items: center;
    gap: 12px;
}

.page-title {
    margin-bottom: 6px;
    font-size: 26px;
}

.page-desc {
    color: var(--text-secondary);
    font-size: var(--font-sm);
}

.form-card {
    max-width: 840px;
    padding: 28px;
    border-radius: 16px;
    box-shadow: 0 8px 26px rgba(22, 83, 78, 0.06);
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 18px;
    padding-top: 20px;
    border-top: 1px solid var(--border-light);
}

.confirm-modal {
    width: 380px;
}

@media (max-width: 640px) {
    .form-card {
        width: 100%;
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
