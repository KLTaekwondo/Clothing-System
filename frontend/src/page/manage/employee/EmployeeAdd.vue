<template>
    <div class="employee-add">
        <div class="page-heading">
            <div class="heading-left">
                <button
                    class="btn-outline"
                    type="button"
                    @click="goBack"
                >← 返回员工</button>
                <div class="heading-content">
                    <h2 class="page-title">添加员工</h2>
                    <p class="page-desc">创建员工账号并分配所属仓库</p>
                </div>
            </div>
        </div>

        <div class="employee-form-card">
            <form @submit.prevent="handleSubmit">
                <div class="form-content">
                    <div class="form-group">
                        <label>员工编码</label>
                        <input
                            v-model="form.code"
                            maxlength="10"
                            minlength="2"
                            placeholder="请输入员工编码"
                            required
                            type="text"
                        />
                        <span class="field-hint">用于员工登录与业务记录，长度为 2-10 个字符</span>
                    </div>

                    <div class="form-group">
                        <label>员工姓名</label>
                        <input
                            v-model="form.name"
                            maxlength="10"
                            minlength="2"
                            placeholder="请输入员工姓名"
                            required
                            type="text"
                        />
                    </div>

                    <div class="form-group">
                        <label>所属仓库</label>
                        <OptionValuePicker
                            v-model="warehousePickerValue"
                            :disabled="warehouseLoading"
                            :options="warehouseOptions"
                            error-message="请从仓库列表中选择有效仓库"
                            placeholder="输入仓库名称或编码筛选"
                            select-placeholder="选择仓库"
                            @update:model-value="syncWarehouseId"
                        />
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
                    >{{ submitting ? '保存中...' : '保存员工' }}</button>
                </div>
            </form>
        </div>
    </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import employeeInterface from '../../../axios/interface/EmployeeInterface.js'
import wareHouseInterface from '../../../axios/interface/WareHouseInterface.js'
import OptionValuePicker from '../../../component/product/OptionValuePicker.vue'
import usePageDraft from '../../../composables/usePageDraft.js'

const router = useRouter()
const submitting = ref(false)
const warehouseLoading = ref(false)
const warehouses = ref([])
const warehousePickerValue = ref('')
const form = ref({
    code: '',
    name: '',
    wareHouseId: ''
})

const employeeDraft = usePageDraft(
    'clothing_manage_employee_add',
    () => ({form: form.value, warehousePickerValue: warehousePickerValue.value}),
    saved => {
        form.value = {...form.value, ...(saved.form || {})}
        warehousePickerValue.value = saved.warehousePickerValue || ''
    },
    {
        saved: () => submitting.value
    }
)

const warehouseOptions = computed(() => warehouses.value.map(item => ({
    id: item.id,
    optionValue: formatWarehouseOption(item)
})))

const canSubmit = computed(() => {
    return form.value.code.trim().length >= 2
        && form.value.name.trim().length >= 2
        && Boolean(form.value.wareHouseId)
})

onMounted(async () => {
    warehouseLoading.value = true
    try {
        warehouses.value = await wareHouseInterface.searchList()
    } catch {
        warehouses.value = []
    } finally {
        warehouseLoading.value = false
    }
})

async function handleSubmit() {
    if (submitting.value || !canSubmit.value) return
    submitting.value = true
    try {
        await employeeInterface.create({
            code: form.value.code.trim(),
            name: form.value.name.trim(),
            wareHouseId: Number(form.value.wareHouseId)
        })
        employeeDraft.clear()
        await router.push('/manage/employee')
    } catch {
        // 请求错误由 Axios 拦截器统一提示
    } finally {
        submitting.value = false
    }
}

function syncWarehouseId(value) {
    const selected = warehouses.value.find(item => formatWarehouseOption(item) === value)
    form.value.wareHouseId = selected?.id || ''
}

function formatWarehouseOption(warehouse) {
    if (warehouse.code) return `${warehouse.name}（${warehouse.code}）`
    return warehouse.name
}

function goBack() {
    router.push('/manage/employee')
}
</script>

<style scoped>
.employee-add {
    width: 100%;
    min-width: 0;
}

.employee-form-card {
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

.form-group > input {
    width: 100%;
}

@media (max-width: 640px) {

    .employee-form-card {
        padding: 18px;
    }

    .form-actions button {
        width: 100%;
    }
}
</style>
