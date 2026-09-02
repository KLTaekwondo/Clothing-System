<template>
    <div class="supplier-add">
        <div class="page-toolbar">
            <div class="page-label">
                <h2 class="page-label-title">添加供应商</h2>
                <p class="page-label-desc">创建可用于采购订单的供应商资料</p>
                <hr class="label-hr"/>
            </div>
            <i class="toolbar-divider"></i>
            <div class="toolbox-stack">
                <div class="search-shell">
                    <div class="search-controls">
                        <button
                            class="btn-outline search-button"
                            @click="requestBack"
                        >← 返回供应商</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-content">
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
                        <span class="field-hint">用于采购订单和供应商资料识别</span>
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
                        <textarea
                            v-model="form.remark"
                            maxlength="200"
                            placeholder="请输入备注"
                            rows="4"
                        ></textarea>
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
import supplierInterface from '../../../axios/interface/SupplierInterface.js'
import usePageDraft from '../../../composables/usePageDraft.js'

const router = useRouter()
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

const supplierDraft = usePageDraft(
    'clothing_manage_supplier_add',
    form,
    saved => {
        form.value = {...form.value, ...saved}
    },
    {
        saved: () => submitting.value || saved.value
    }
)

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
    submitting.value = true
    try {
        await supplierInterface.create({
            supplierCode: form.value.supplierCode.trim(),
            supplierName: form.value.supplierName.trim(),
            contactPhone: form.value.contactPhone.trim(),
            remark: form.value.remark.trim()
        })
        saved.value = true
        supplierDraft.clear()
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

.form-card {
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

.form-group input,
.form-group textarea {
    width: 100%;
}

.form-group textarea {
    min-height: 104px;
    resize: vertical;
}

.confirm-modal {
    width: 380px;
}

@media (max-width: 640px) {
    .form-card {
        width: 100%;
        padding: 18px;
    }

    .form-actions button {
        width: 100%;
    }
}
</style>
