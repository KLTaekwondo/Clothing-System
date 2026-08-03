<template>
    <div class="supplier-detail">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    @click="requestBack"
                >← 返回供应商</button>
                <div>
                    <h2 class="page-title">供应商详情</h2>
                    <p
                        v-if="supplier"
                        class="page-desc"
                    >{{ supplier.supplierName }} · {{ supplier.supplierCode }}</p>
                </div>
            </div>
            <div
                v-if="supplier"
                class="heading-actions"
            >
                <button
                    v-if="!editing"
                    class="btn-primary"
                    @click="startEdit"
                >编辑供应商</button>
                <template v-else>
                    <button
                        :disabled="saving"
                        class="btn-outline"
                        @click="cancelEdit"
                    >取消</button>
                    <button
                        :disabled="saving || !canSave"
                        class="btn-primary"
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
            v-else-if="!supplier"
            class="empty-state"
        >
            <div class="empty-icon"><IconGraphic name="supplier"/></div>
            <div class="empty-text">供应商不存在</div>
        </div>
        <div
            v-else
            class="detail-list"
        >
            <div class="detail-card">
                <span class="detail-label">供应商编码</span>
                <input
                    v-if="editing"
                    v-model="form.supplierCode"
                    class="card-input"
                    maxlength="10"
                    minlength="4"
                    type="text"
                />
                <strong
                    v-else
                    class="code-value"
                >{{ supplier.supplierCode }}</strong>
            </div>
            <div class="detail-card">
                <span class="detail-label">供应商名称</span>
                <input
                    v-if="editing"
                    v-model="form.supplierName"
                    class="card-input"
                    maxlength="10"
                    minlength="2"
                    type="text"
                />
                <strong v-else>{{ supplier.supplierName }}</strong>
            </div>
            <div class="detail-card">
                <span class="detail-label">联系电话</span>
                <input
                    v-if="editing"
                    v-model="form.contactPhone"
                    class="card-input"
                    type="text"
                />
                <strong v-else>{{ supplier.contactPhone || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span class="detail-label">备注</span>
                <input
                    v-if="editing"
                    v-model="form.remark"
                    class="card-input"
                    type="text"
                />
                <strong v-else>{{ supplier.remark || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span class="detail-label">状态</span>
                <select
                    v-if="editing"
                    v-model="form.status"
                    class="card-select"
                >
                    <option
                        v-for="item in statusOptions"
                        :key="item.value"
                        :value="item.value"
                    >{{ item.label }}</option>
                </select>
                <span
                    v-else
                    :class="supplier.status === STATUS.ENABLE ? 'status-ok' : 'status-error'"
                    class="status-badge"
                >{{ statusLabels[supplier.status] || supplier.status || '-' }}</span>
            </div>
            <div class="detail-card">
                <span class="detail-label">创建时间</span>
                <strong>{{ supplier.createTime || '-' }}</strong>
            </div>
            <div class="detail-card">
                <span class="detail-label">更新时间</span>
                <strong>{{ supplier.updateTime || '-' }}</strong>
            </div>
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
                        <div class="confirm-msg">供应商资料尚未保存，确定离开吗？</div>
                        <div class="confirm-hint">未保存的修改将会丢失</div>
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
import {computed, onMounted, ref} from 'vue'
import {onBeforeRouteLeave, useRoute, useRouter} from 'vue-router'
import supplierInterface from '../../../axios/interface/SupplierInterface.js'
import {STATUS, STATUS_LABELS, STATUS_OPTIONS} from '../../../constants/status.js'

const route = useRoute()
const router = useRouter()
const statusLabels = STATUS_LABELS
const statusOptions = STATUS_OPTIONS
const supplier = ref(null)
const loading = ref(true)
const editing = ref(false)
const saving = ref(false)
const form = ref({})
const formBackup = ref(null)
const showLeaveConfirm = ref(false)
let pendingNavigation = null
let allowLeave = false

const canSave = computed(() => {
    return form.value.supplierCode?.trim().length >= 4 &&
        form.value.supplierName?.trim().length >= 2
})

const isDirty = computed(() => {
    if (!editing.value || !formBackup.value) return false
    return JSON.stringify(form.value) !== JSON.stringify(formBackup.value)
})

onMounted(fetchDetail)

onBeforeRouteLeave((to, from, next) => {
    if (allowLeave || !isDirty.value) {
        allowLeave = false
        next()
        return
    }
    pendingNavigation = next
    showLeaveConfirm.value = true
})

async function fetchDetail() {
    loading.value = true
    try {
        supplier.value = await supplierInterface.search(route.params.code)
    } catch {
        supplier.value = null
    } finally {
        loading.value = false
    }
}

function startEdit() {
    form.value = {
        supplierCode: supplier.value.supplierCode || '',
        supplierName: supplier.value.supplierName || '',
        contactPhone: supplier.value.contactPhone || '',
        remark: supplier.value.remark || '',
        status: supplier.value.status || STATUS.ENABLE
    }
    formBackup.value = {...form.value}
    editing.value = true
}

function cancelEdit() {
    form.value = {...formBackup.value}
    formBackup.value = null
    editing.value = false
}

async function saveEdit() {
    if (saving.value || !canSave.value) return
    saving.value = true
    try {
        const newCode = form.value.supplierCode.trim()
        await supplierInterface.update(supplier.value.id, {
            supplierCode: newCode,
            supplierName: form.value.supplierName.trim(),
            contactPhone: form.value.contactPhone.trim(),
            remark: form.value.remark.trim(),
            status: form.value.status
        })
        editing.value = false
        formBackup.value = null
        if (newCode !== route.params.code) {
            allowLeave = true
            await router.replace(`/manage/supplier/${encodeURIComponent(newCode)}`)
        }
        await fetchDetail()
    } catch {
        // 拦截器已处理
    } finally {
        saving.value = false
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
    allowLeave = true
    const navigation = pendingNavigation
    pendingNavigation = null
    navigation?.()
}
</script>

<style scoped>
.supplier-detail {
    width: 100%;
    min-width: 0;
}

.page-heading,
.heading-left,
.heading-actions {
    display: flex;
    align-items: center;
}

.page-heading {
    justify-content: space-between;
    margin-bottom: 28px;
}

.heading-left,
.heading-actions {
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

.detail-list {
    display: flex;
    flex-wrap: wrap;
    gap: 14px;
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
    background: #fff;
    border: 1px solid #e3efed;
    border-radius: 16px;
    box-shadow: 0 8px 24px rgba(22, 83, 78, 0.06);
}

.detail-label {
    color: var(--text-secondary);
    font-size: 13px;
}

.code-value {
    color: #0d9488;
    font-family: ui-monospace, "SF Mono", Consolas, monospace;
}

.card-input,
.card-select {
    width: 100%;
    height: 38px;
    padding: 0 10px;
    border: 1px solid #dceae7;
    border-radius: 8px;
    background: #fff;
}

.confirm-modal {
    width: 380px;
}
</style>
